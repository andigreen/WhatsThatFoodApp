package com.wtf.whatsthatfoodapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FacebookLoginActivity extends BasicActivity implements View.OnClickListener {

    private static final String TAG = "FacebookLoginActivity";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]


    private Button sign_in_button;
    private Button back_button;

    private DatabaseReference mDatabase;
    private FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefs = getSharedPreferences(PREFS_NAME,0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);
        sign_in_button = (Button) findViewById(R.id.signin_btn);
        back_button = (Button) findViewById(R.id.back_btn);
        sign_in_button.setOnClickListener(this);
        back_button.setOnClickListener(this);
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.signin_btn) {
            //signIn();
        }else if(i == R.id.back_btn){
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
