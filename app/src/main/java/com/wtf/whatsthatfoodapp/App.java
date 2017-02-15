package com.wtf.whatsthatfoodapp;

import android.app.Application;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;


/**
 * Created by peiranli on 2/11/17.
 */

public class App extends Application {
    private GoogleApiClient mGoogleApiClient;

    private GoogleSignInOptions gso;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public GoogleSignInOptions getGso(){
        return gso;
    }

    public void setGso(GoogleSignInOptions gso){
        this.gso = gso;
    }

    public void setClient(GoogleApiClient client){
        this.mGoogleApiClient = client;
    }

    public GoogleApiClient getClient(){
        return this.mGoogleApiClient;
    }


}
