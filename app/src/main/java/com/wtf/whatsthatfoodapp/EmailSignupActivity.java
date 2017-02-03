package com.wtf.whatsthatfoodapp;

import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class EmailSignupActivity extends BasicActivity implements View.OnClickListener {

    private final String TAG = "EmailSignupActivity";
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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        next_btn = (Button) findViewById(R.id.signup);
        back_btn = (Button) findViewById(R.id.back);
        emailText = (TextView) findViewById(R.id.email);
        usernameText = (TextView) findViewById(R.id.username);
        passwordText = (TextView) findViewById(R.id.password);

        emailField = (EditText) findViewById(R.id.EmailField);
        usernameField = (EditText) findViewById(R.id.usernameField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        next_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        mAuth = FirebaseAuth.getInstance();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
        } else if (password.length() < 6) {
            this.passwordField.setError("Password should be longer than 6 characters");
        } else {
            this.passwordField.setError(null);
        }

        return valid;
    }

    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        final Intent displayLoginPage = new Intent(this, EmailLoginActivity.class);
        //displayLoginPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            verificationEmail();
                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String username = usernameField.getText().toString();
                            mDatabase.child("users").child(user.getUid())
                                    .child("username").setValue(username);
                            startActivity(displayLoginPage);

                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(EmailSignupActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                        }

                    }
                });
        // [END create_user_with_email]
    }


    // [END on_stop_remove_listener]
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signup) {
            createAccount(emailField.getText().toString(), passwordField.getText().toString());
        } else if (i == R.id.back) {
            final Intent displayLoginPage = new Intent(this, EmailLoginActivity.class);
            //displayLoginPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(displayLoginPage);
        }
    }


    private void verificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(EmailSignupActivity.this, "Signup successful. " +
                                        "Verification email sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }



    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, EmailLoginActivity.class));
    }


}
