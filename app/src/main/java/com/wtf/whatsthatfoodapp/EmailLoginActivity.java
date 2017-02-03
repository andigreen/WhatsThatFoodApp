package com.wtf.whatsthatfoodapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailLoginActivity extends BasicActivity implements View.OnClickListener {

    private final String TAG = "EmailLoginActivity";
    private TextView emailText;
    private TextView passwordText;
    private EditText emailField;
    private EditText passwordField;
    private Button login_btn;
    private Button register_btn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailText = (TextView) findViewById(R.id.email);
        passwordText = (TextView) findViewById(R.id.password);
        emailField = (EditText) findViewById(R.id.EmailField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        login_btn = (Button) findViewById(R.id.Login_btn);
        register_btn = (Button) findViewById(R.id.Register_btn);
        sharedPrefs = getSharedPreferences(PREFS_NAME,0);
        // Buttons
        login_btn.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };
        // [END auth_state_listener]
        //final Intent displaySignupPage = new Intent(this, RegisterActivity.class);
        //final Intent displayLoginPage = new Intent(this, WelcomeActivity.class);

    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        final Intent displayHomePage = new Intent(this, WelcomeActivity.class);
        final Intent displayStartPage = new Intent(this, MainActivity.class);
        // [START sign_in_with_email]
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("signInMethod","Email");
        editor.commit();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        if(task.isSuccessful()) {
                            if(user.isEmailVerified()) {
                                startActivity(displayHomePage);
                            }
                            else {
                                Toast.makeText(EmailLoginActivity.this, "Please verify your email",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(EmailLoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                        }


                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = this.emailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            this.emailField.setError("Required.");
            valid = false;
        } else {
            this.emailField.setError(null);
        }

        String password = this.passwordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            this.passwordField.setError("Required.");
            valid = false;
        } else {
            this.passwordField.setError(null);
        }

        return valid;
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
        if (i == R.id.Register_btn) {
            final Intent displaySignupPage = new Intent(this, EmailSignupActivity.class);
            startActivity(displaySignupPage);
        } else if (i == R.id.Login_btn) {
            signIn(emailField.getText().toString(), passwordField.getText().toString());
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
