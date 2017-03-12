package com.wtf.whatsthatfoodapp;

import android.app.Application;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wtf.whatsthatfoodapp.search.SearchTable;


public class App extends Application {

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;

    private SearchTable searchTable;

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

    /**
     * Returns an Application-singleton instance of {@link SearchTable}.
     */
    public SearchTable getSearchTable() {
        if (searchTable == null) searchTable = new SearchTable(this);
        return searchTable;
    }

}
