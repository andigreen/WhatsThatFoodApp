package com.wtf.whatsthatfoodapp;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.wtf.whatsthatfoodapp.R;

public class BasicActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public final String TAG = BasicActivity.class.getSimpleName();
    @VisibleForTesting
    public ProgressDialog mProgressDialog;

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
