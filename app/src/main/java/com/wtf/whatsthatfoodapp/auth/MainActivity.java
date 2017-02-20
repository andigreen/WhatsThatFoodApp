package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.CollageActivity;

public class MainActivity extends BasicActivity implements View.OnClickListener,  GoogleApiClient.OnConnectionFailedListener{
    private final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private Button email_btn;
    private SignInButton google_btn;
    private LoginButton fb_btn;
    private TextView emailText;
    private TextView passwordText;
    private EditText emailField;
    private EditText passwordField;
    private Button register_btn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private Button back_btn;
    private Button signup_btn;
    private EditText usernameField;
    private Button password_recover;
    private Button reset_btn;
    private DatabaseReference mDatabase;
    private FirebaseDatabase database;
    private TextView recovery;
    private TextView usernameText;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;
    Intent displayHomePage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPrefs = getSharedPreferences(PREFS_NAME,0);
        email_btn = (Button)findViewById(R.id.email_button);
        google_btn = (SignInButton)findViewById(R.id.google_button);
        google_btn.setSize(SignInButton.SIZE_WIDE);
        fb_btn = (LoginButton)findViewById(R.id.fb_btn);
        emailText = (TextView)findViewById(R.id.emailtext);
        passwordText = (TextView)findViewById(R.id.passwordtext);
        usernameText = (TextView)findViewById(R.id.usernametxt);
        usernameText.setVisibility(View.INVISIBLE);
        emailField = (EditText)findViewById(R.id.emailfield);
        passwordField = (EditText)findViewById(R.id.passwordfield);
        usernameField = (EditText)findViewById(R.id.usernamefield);
        usernameField.setVisibility(View.INVISIBLE);
        register_btn = (Button)findViewById(R.id.Register_btn);
        password_recover = (Button)findViewById(R.id.password_recover);
        recovery = (TextView)findViewById(R.id.Recovery);
        recovery.setVisibility(View.INVISIBLE);
        reset_btn = (Button)findViewById(R.id.recover_btn);
        reset_btn.setVisibility(View.INVISIBLE);
        back_btn = (Button)findViewById(R.id.back_btn);
        back_btn.setVisibility(View.INVISIBLE);
        signup_btn = (Button)findViewById(R.id.signup_btn);
        signup_btn.setVisibility(View.INVISIBLE);

        email_btn.setOnClickListener(this);
        google_btn.setOnClickListener(this);
        fb_btn.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        password_recover.setOnClickListener(this);
        reset_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        signup_btn.setOnClickListener(this);

        App app = (App)getApplicationContext();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]
        app.setGso(gso);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, app.getGso())
                .build();

        app.setClient(mGoogleApiClient);
        app.getClient().connect();
        Log.d(TAG, "GoogleApiClient Connected: "+ app.getClient().isConnected());

        displayHomePage = new Intent(this,WelcomeActivity.class);


        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        user = mAuth.getCurrentUser();
        // [START auth_state_listener]
        if(user != null &&
                (BasicActivity.getProvider().equals("Facebook")
                        || BasicActivity.getProvider().equals("Google"))){
            startActivity(displayHomePage);
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // User is signed in
                    startActivity(displayHomePage);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        // [END auth_state_listener]


        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        fb_btn.setReadPermissions("email", "public_profile");
        fb_btn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
       

    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {
                            UserSettings user = new UserSettings(mAuth.getCurrentUser().getEmail()
                                    ,token.getUserId(),mAuth.getCurrentUser().getUid());
                            UserSettingsDAO dao = new UserSettingsDAO(mAuth.getCurrentUser().getUid());
                            dao.writeUser(user);
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString("signInMethod","Facebook");
                            editor.commit();
                            startActivity(displayHomePage);
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }


    private void EmailSignIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        // [START sign_in_with_email]
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("signInMethod","Email");
        editor.apply();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        user = mAuth.getCurrentUser();
                        if(task.isSuccessful()) {
                            if(user.isEmailVerified()) {
                                startActivity(displayHomePage);
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Please verify your email",
                                        Toast.LENGTH_SHORT).show();

                                // [START_EXCLUDE]
                                hideProgressDialog();
                                // [END_EXCLUDE]
                            }
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(MainActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                        }


                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = this.emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            this.emailField.setError("Required.");
            valid = false;
        } else {
            this.emailField.setError(null);
        }

        String password = this.passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            this.passwordField.setError("Required.");
            valid = false;
        } else if (password.length() < 6) {
            this.passwordField.setError("Password should be longer than 6 characters");
        } else {
            this.passwordField.setError(null);
        }

        return valid;
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.Register_btn) {
            updateUI(1);
        } else if (i == R.id.email_button) {
            EmailSignIn(emailField.getText().toString(), passwordField.getText().toString());
        }else if(i == R.id.back_btn){
            updateUI(0);
        }else if(i == R.id.signup_btn){
            createAccount(emailField.getText().toString(), passwordField.getText().toString());
        }else if(i == R.id.password_recover){
            updateUI(2);
        }else if(i == R.id.recover_btn){
            recoverPassword(emailField.getText().toString());
        }else if(i == R.id.google_button){
            GoogleSignIn();
        }
    }


    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            verificationEmail();
                            String username = usernameField.getText().toString();
                            UserSettings user = new UserSettings(mAuth.getCurrentUser().getEmail()
                                    ,username,mAuth.getCurrentUser().getUid());
                            UserSettingsDAO dao = new UserSettingsDAO(mAuth.getCurrentUser().getUid());
                            dao.writeUser(user);
                            updateUI(0);

                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Email is already taken",
                                    Toast.LENGTH_SHORT).show();
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                        }

                    }
                });
        // [END create_user_with_email]
    }

    private void verificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Signup successful. " +
                                        "Verification email sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void updateUI(int choice) {
        hideProgressDialog();
        //update to login
        if(choice == 0){
            email_btn.setVisibility(View.VISIBLE);
            register_btn.setVisibility(View.VISIBLE);
            password_recover.setVisibility(View.VISIBLE);
            usernameText.setVisibility(View.INVISIBLE);
            usernameField.setVisibility(View.INVISIBLE);
            back_btn.setVisibility(View.INVISIBLE);
            signup_btn.setVisibility(View.INVISIBLE);
            passwordField.setVisibility(View.VISIBLE);
            passwordText.setVisibility(View.VISIBLE);
            reset_btn.setVisibility(View.INVISIBLE);
            recovery.setVisibility(View.INVISIBLE);
            google_btn.setVisibility(View.VISIBLE);
            fb_btn.setVisibility(View.VISIBLE);
            emailField.setText("");
            passwordField.setText("");
            emailField.setError(null);
            passwordField.setError(null);
        }
        //update to signup
        else if(choice == 1){
            email_btn.setVisibility(View.INVISIBLE);
            register_btn.setVisibility(View.INVISIBLE);
            password_recover.setVisibility(View.INVISIBLE);
            usernameText.setVisibility(View.VISIBLE);
            usernameField.setVisibility(View.VISIBLE);
            back_btn.setVisibility(View.VISIBLE);
            signup_btn.setVisibility(View.VISIBLE);
            emailField.setText("");
            passwordField.setText("");
            emailField.setError(null);
            passwordField.setError(null);
            google_btn.setVisibility(View.INVISIBLE);
            fb_btn.setVisibility(View.INVISIBLE);
            //update to password recover
        }else if(choice == 2){
            email_btn.setVisibility(View.INVISIBLE);
            register_btn.setVisibility(View.INVISIBLE);
            password_recover.setVisibility(View.INVISIBLE);
            usernameText.setVisibility(View.VISIBLE);
            usernameField.setVisibility(View.VISIBLE);
            emailField.setText("");
            passwordField.setText("");
            passwordField.setVisibility(View.INVISIBLE);
            passwordText.setVisibility(View.INVISIBLE);
            reset_btn.setVisibility(View.VISIBLE);
            back_btn.setVisibility(View.VISIBLE);
            usernameField.setVisibility(View.INVISIBLE);
            usernameText.setVisibility(View.INVISIBLE);
            recovery.setVisibility(View.VISIBLE);
            google_btn.setVisibility(View.INVISIBLE);
            fb_btn.setVisibility(View.INVISIBLE);
            emailField.setError(null);
            passwordField.setError(null);
        }

    }


    private void recoverPassword(String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "Password recover email sent", Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"PasswordEmailSent: " + task.isSuccessful());
                            updateUI(0);
                        }else{
                            Log.d(TAG,"PasswordEmailSent: " + task.isSuccessful());
                            Toast.makeText(MainActivity.this,
                                    "Email does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    // [START signin]
    private void GoogleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("signInMethod","Google");
        editor.apply();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    // [END signin]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                            UserSettings user = new UserSettings(mAuth.getCurrentUser().getEmail()
                                    ,acct.getDisplayName(),mAuth.getCurrentUser().getUid());
                            UserSettingsDAO dao = new UserSettingsDAO(mAuth.getCurrentUser().getUid());
                            dao.writeUser(user);
                            startActivity(displayHomePage);
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        else{
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END auth_with_google]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(MainActivity.this, "Google Sign In failed.",
                        Toast.LENGTH_SHORT).show();
            }
        }else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    // [END onactivityresult]


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


}
