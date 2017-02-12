package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.R;

public class LogoutActivity extends AppCompatActivity {

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_logout);
        if(BasicActivity.getProvider().equals("Google")){
            mGoogleApiClient = App.getInstance().getClient();
        }
        signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void signOut() {

        if(BasicActivity.getProvider().equals("Facebook")) {
            LoginManager.getInstance().logOut();
        }
        if(BasicActivity.getProvider().equals("Google")){
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // ...
                            Toast.makeText(getApplicationContext(),"Logged Out",Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        FirebaseAuth.getInstance().signOut();
    }


}
