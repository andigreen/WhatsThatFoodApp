package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.CollageActivity;
import com.wtf.whatsthatfoodapp.user.UserSettings;
import com.wtf.whatsthatfoodapp.user.UserSettingsDao;

import java.util.Arrays;

public class MainActivity extends BasicActivity {

    public static final String EXIT_KEY = "exit";

    private final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;
    private static final int REQ_REGISTER = 9293;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager mCallbackManager;
    Intent displayHomePage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getBooleanExtra(EXIT_KEY, false)) {
            finish();
        }

        Button email_btn = (Button) findViewById(R.id.email_button);
        Button facebook_login_btn = (Button) findViewById(R.id.btn_fb_login);
        Button google_login_btn = (Button) findViewById(R.id.btn_google_login);
        Button register_btn = (Button) findViewById(R.id.signup_btn);

        email_btn.setOnClickListener(this);
        google_login_btn.setOnClickListener(this);
        register_btn.setOnClickListener(this);

        App app = (App) getApplicationContext();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]
        app.setGso(gso);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /*
                OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, app.getGso())
                .build();

        app.setClient(mGoogleApiClient);
        app.getClient().connect();
        Log.d(TAG,
                "GoogleApiClient Connected: " + app.getClient().isConnected());

        displayHomePage = new Intent(this, CollageActivity.class);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        user = mAuth.getCurrentUser();
        // [START auth_state_listener]
        if (user != null &&
                (BasicActivity.getProvider().equals("Facebook")
                        || BasicActivity.getProvider().equals("Google"))) {
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

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
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

        facebook_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(
                        MainActivity.this,
                        Arrays.asList("email", "public_profile"));
            }
        });


    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(
                token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult>
                                    task) {

                                if (task.isSuccessful()) {
                                    createUserIfNotExist(AuthUtils.getUserUid());
                                    Log.d(TAG,
                                            "signInWithCredential:onComplete:" + task.isSuccessful());
                                    startActivity(displayHomePage);
                                }
                                // If sign in fails, display a message to the
                                // user. If sign in succeeds
                                // the auth state listener will be notified
                                // and logic to handle the
                                // signed in user can be handled in the
                                // listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithCredential",
                                            task.getException());
                                    Toast.makeText(MainActivity.this,
                                            "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }

                                // ...
                            }
                        });
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
        if (i == R.id.email_button) {
            Intent toEmailPage = new Intent(MainActivity.this,
                    EmailLoginActivity.class);
            startActivity(toEmailPage);
        } else if (i == R.id.btn_google_login) {
            GoogleSignIn();
        } else if (i == R.id.signup_btn) {
            Intent toSignupPage = new Intent(MainActivity.this,
                    CreateAccountActivity.class);
            startActivityForResult(toSignupPage, REQ_REGISTER);
        }
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    // [START signin]
    private void GoogleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(
                mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    // [END signin]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(
                acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult>
                                    task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG,
                                            "signInWithCredential:onComplete:" + task.isSuccessful());
                                    createUserIfNotExist(AuthUtils.getUserUid());
                                    startActivity(displayHomePage);
                                }
                                // If sign in fails, display a message to the
                                // user. If sign in succeeds
                                // the auth state listener will be notified
                                // and logic to handle the
                                // signed in user can be handled in the
                                // listener.
                                else {
                                    Log.w(TAG, "signInWithCredential",
                                            task.getException());
                                    Toast.makeText(MainActivity.this,
                                            "Authentication failed.",
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

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(
                            data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
               Log.d(TAG,"Google Sign In cancelled");
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    // [END onactivityresult]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.",
                Toast.LENGTH_SHORT).show();
    }

    public void createUserInDB(String email, String username, String Uid) {
        UserSettings user = new UserSettings(email
                , username, Uid);
        UserSettingsDao dao = new UserSettingsDao(Uid);
        dao.writeUser(user);
    }

    private void createUserIfNotExist(final String Uid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ValueEventListener userInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child(Uid).exists()){
                    createUserInDB(AuthUtils.getUserEmail(),
                            AuthUtils.getUserDisplayName(),Uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadData:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(MainActivity.this, "Failed to load data.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        ref.addListenerForSingleValueEvent(userInfoListener);
    }


}
