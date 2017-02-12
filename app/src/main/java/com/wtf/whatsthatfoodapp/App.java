package com.wtf.whatsthatfoodapp;

import android.app.Application;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;


/**
 * Created by peiranli on 2/11/17.
 */

public class App extends Application {
    private GoogleApiClient mGoogleApiClient;
    private static App mInstance;
    private static GoogleSignInOptions gso;
    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]
    }


    public static GoogleSignInOptions getGso(){
        return gso;
    }

    public void setClient(GoogleApiClient client){
        mGoogleApiClient = client;
    }

    public GoogleApiClient getClient(){
        return mGoogleApiClient;
    }

    public static synchronized App getInstance() {
        return mInstance;
    }


}
