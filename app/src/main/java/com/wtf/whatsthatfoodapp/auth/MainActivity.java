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
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.CollageActivity;

public class MainActivity extends BasicActivity {
    private final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private Button email_btn;
    private SignInButton google_btn;
    private LoginButton fb_btn;
    private EditText emailField;
    private EditText passwordField;
    private Button register_btn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private Button password_recover;
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
        emailField = (EditText)findViewById(R.id.emailfield);
        passwordField = (EditText)findViewById(R.id.passwordfield);
        register_btn = (Button)findViewById(R.id.Register_btn);
        password_recover = (Button)findViewById(R.id.password_recover);

        email_btn.setOnClickListener(this);
        google_btn.setOnClickListener(this);
        fb_btn.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        password_recover.setOnClickListener(this);

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

        displayHomePage = new Intent(this,CollageActivity.class);

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
                            createUserInDB(AuthUtils.getUserEmail()
                                    ,AuthUtils.getUserDisplayName()
                                ,AuthUtils.getUserUid());
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
            Intent toSignupPage = new Intent(MainActivity.this, CreateAccountActivity.class);
            startActivity(toSignupPage);
        }else if (i == R.id.email_button) {
            EmailSignIn(emailField.getText().toString(), passwordField.getText().toString());
        }else if(i == R.id.password_recover){
            Intent toRecoveryPage = new Intent(MainActivity.this, PasswordRecoveryActivity.class);
            startActivity(toRecoveryPage);
        }else if(i == R.id.google_button){
            GoogleSignIn();
        }
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
                            createUserInDB(AuthUtils.getUserEmail()
                                    ,AuthUtils.getUserDisplayName()
                                    ,AuthUtils.getUserUid());
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


    public void createUserInDB(String email, String username, String Uid){
        UserSettings user = new UserSettings(email
                ,username,Uid);
        UserSettingsDAO dao = new UserSettingsDAO(Uid);
        dao.writeUser(user);
    }

}
