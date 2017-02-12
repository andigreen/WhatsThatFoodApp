package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.R;

public class WelcomeActivity extends BasicActivity {

    private final String TAG = "WelcomeActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String signInMethod;
    private GoogleApiClient mGoogleApiClient;
    private AppEventsLogger logger;
    private EditText searchField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        sharedPrefs = getSharedPreferences(PREFS_NAME,0);
        signInMethod = sharedPrefs.getString("signInMethod","default");
        Log.d(TAG, signInMethod);
        searchField = (EditText)findViewById(R.id.searchfield);
        searchField.clearFocus();
        hideDefaultKeyboard();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        if(signInMethod.equals("Facebook")){
            logger = AppEventsLogger.newLogger(this);
            logger.logEvent("User logged in with Facebook");

        }

        if(getProvider().equals("Google")){
            mGoogleApiClient = App.getInstance().getClient();
            mGoogleApiClient.connect();
        }
    }

    private void hideDefaultKeyboard() {
        WelcomeActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //you have got lot of methods here
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.profile:
                //viewProfile();
                return true;
            case R.id.settings:
                //viewSettings();
                return true;
            case R.id.logout:
                Intent intent = new Intent(this, LogoutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        if(signInMethod.equals("Facebook"))
            AppEventsLogger.activateApp(this);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if(signInMethod.equals("Facebook"))
            AppEventsLogger.deactivateApp(this);
    }
    // [END on_stop_remove_listener]



    // for Android, you should also log app deactivation
    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LogoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }




}
