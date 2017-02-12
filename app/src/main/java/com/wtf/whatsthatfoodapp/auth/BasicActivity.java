package com.wtf.whatsthatfoodapp.auth;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.wtf.whatsthatfoodapp.R;

public class BasicActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;


    public static final String PREFS_NAME = "WTF";
    SharedPreferences sharedPrefs;

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

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
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
}
