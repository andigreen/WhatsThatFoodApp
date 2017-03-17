package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.CollageActivity;

public class EmailLoginActivity extends BasicActivity {
    private final String TAG = EmailLoginActivity.class.getSimpleName();
    private static final int REQ_RECOVERY = 2899;
    private static final int REQ_REGISTER = 9293;

    private EditText emailField;
    private EditText passwordField;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    Intent displayHomePage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        Button email_btn = (Button) findViewById(R.id.email_button);
        emailField = (EditText) findViewById(R.id.emailfield);
        passwordField = (EditText) findViewById(R.id.passwordfield);
        Button register_btn = (Button) findViewById(R.id.Register_btn);
        Button password_recover = (Button) findViewById(R.id.password_recover);

        email_btn.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        password_recover.setOnClickListener(this);

        displayHomePage = new Intent(this, CollageActivity.class);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        user = mAuth.getCurrentUser();
        // [START auth_state_listener]
        if (user != null &&
                (BasicActivity.getProvider().equals("Facebook")
                        || BasicActivity.getProvider().equals("Google"))) {
            startActivity(displayHomePage);
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // User is signed in
                    startActivity(displayHomePage);
                }
            }
        };
        // [END auth_state_listener]
    }

    private void EmailSignIn(String email, String password) {
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult>
                                                           task) {
                                user = mAuth.getCurrentUser();
                                if (task.isSuccessful()) {
                                    if (user.isEmailVerified()) {
                                        startActivity(displayHomePage);
                                    } else {
                                        Toast.makeText(EmailLoginActivity.this,
                                                "Please verify your email",
                                                Toast.LENGTH_SHORT).show();

                                        // [START_EXCLUDE]
                                        hideProgressDialog();
                                        // [END_EXCLUDE]
                                    }
                                }
                                // If sign in fails, display a message to the
                                // user. If sign in succeeds
                                // the auth state listener will be notified
                                // and logic to handle the
                                // signed in user can be handled in the
                                // listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(EmailLoginActivity.this,
                                            R.string.auth_failed,
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
        } else if (password.length() < 6) {
            this.passwordField.setError(
                    "Password should be longer than 6 characters");
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
            Intent toSignupPage = new Intent(EmailLoginActivity.this,
                    CreateAccountActivity.class);
            startActivityForResult(toSignupPage, REQ_REGISTER);
        } else if (i == R.id.email_button) {
            EmailSignIn(emailField.getText().toString(),
                    passwordField.getText().toString());
        } else if (i == R.id.password_recover) {
            Intent toRecoveryPage = new Intent(EmailLoginActivity.this,
                    PasswordRecoveryActivity.class);
            toRecoveryPage.putExtra(PasswordRecoveryActivity.EMAIL_KEY,
                    emailField.getText().toString());
            startActivityForResult(toRecoveryPage, REQ_RECOVERY);
        }
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_RECOVERY && resultCode == RESULT_OK) {
            Snackbar
                    .make(findViewById(android.R.id.content),
                            R.string.reset_password_sent,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction("DISMISS", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Implicit dimissal
                        }
                    }).show();
        }else if(requestCode == REQ_REGISTER && resultCode == RESULT_OK) {
            Snackbar
                    .make(findViewById(android.R.id.content),
                            R.string.email_verification_sent,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction("DISMISS", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Implicit dimissal
                        }
                    }).show();
        }
    }

}
