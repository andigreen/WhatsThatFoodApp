package com.wtf.whatsthatfoodapp.auth;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.wtf.whatsthatfoodapp.R;

public class BasicActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public final String TAG = "BasicActivity";
    @VisibleForTesting
    public ProgressDialog mProgressDialog;


    public static final String PREFS_NAME = "WTF";
    public SharedPreferences sharedPrefs;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    public static String getProvider() {
        for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("facebook.com")) {
                System.out.println("User is signed in with Facebook");
                return "Facebook";
            }
            if (user.getProviderId().equals("google.com")) {
                System.out.println("User is signed in with Google");
                return "Google";
            }
        }
        return "Firebase";
    }


    public static boolean IsEmailOccupied(String email){
        for (UserInfo profile : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            String userEmail = profile.getEmail();
            if(email.equals(userEmail))
                return true;
        }
        return false;
    }
    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();

    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();

    }
    // [END on_stop_remove_listener]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onClick(View v) {

    }
}
