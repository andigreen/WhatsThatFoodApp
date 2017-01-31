package com.wtf.whatsthatfoodapp;

import android.content.Intent;
import android.preference.EditTextPreference;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.wtf.whatsthatfoodapp.R.id.passwordField;
import static com.wtf.whatsthatfoodapp.R.id.start;
import static com.wtf.whatsthatfoodapp.R.id.username;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "SignupActivity";
    private Button next_btn;
    private Button back_btn;
    private TextView emailText;
    private TextView usernameText;
    private TextView passwordText;
    private EditText emailField;
    private EditText usernameField;
    private EditText passwordField;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        next_btn = (Button)findViewById(R.id.signup);
        back_btn = (Button)findViewById(R.id.back);
        emailText = (TextView)findViewById(R.id.email);
        usernameText = (TextView)findViewById(R.id.username);
        passwordText = (TextView)findViewById(R.id.password);

        emailField = (EditText)findViewById(R.id.EmailField);
        usernameField = (EditText)findViewById(R.id.usernameField);
        passwordField = (EditText)findViewById(R.id.passwordField);

        next_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
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
        }else if(password.length() < 6){
            this.passwordField.setError("Password should be longer than 6 characters");
        }
        else {
            this.passwordField.setError(null);
        }

        return valid;
    }

    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        final Intent displayHomePage = new Intent(this, WelcomeActivity.class);

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()) {
                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String username = usernameField.getText().toString();
                            mDatabase.child("users").child(user.getUid())
                                    .child("username").setValue(username);
                            mDatabase.child("users").child(user.getUid())
                                    .child("email").setValue(email);
                            mDatabase.child("users").child(user.getUid())
                                    .child("password").setValue(password);
                            startActivity(displayHomePage);
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        // [END create_user_with_email]
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signup) {
            createAccount(emailField.getText().toString(),passwordField.getText().toString());
        } else if (i == R.id.back) {
            final Intent displayLoginPage = new Intent(this, LoginActivity.class);
            startActivity(displayLoginPage);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
