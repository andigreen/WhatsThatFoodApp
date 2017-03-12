package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.user.UserSettings;
import com.wtf.whatsthatfoodapp.user.UserSettingsDAO;

public class CreateAccountActivity extends BasicActivity {

    public final String TAG = CreateAccountActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private EditText emailField;
    private EditText passwordField;
    private EditText usernameField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        emailField = (EditText)findViewById(R.id.emailfield);
        passwordField = (EditText)findViewById(R.id.passwordfield);
        usernameField = (EditText)findViewById(R.id.usernamefield);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        findViewById(R.id.signup_btn).setOnClickListener(this);
    }

    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            verificationEmail();
                            String username = usernameField.getText().toString();
                            createUserInDB(AuthUtils.getUserEmail()
                                    ,username
                                    ,AuthUtils.getUserUid());
                            hideProgressDialog();
                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(CreateAccountActivity.this, "Email is already taken",
                                    Toast.LENGTH_SHORT).show();
                            // [START_EXCLUDE]
                            hideProgressDialog();
                            // [END_EXCLUDE]
                        }

                    }
                });
        // [END create_user_with_email]
    }

    private void verificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Don't show the user anything. This may indicate
                    // that the email doesn't exist, which is exploitable
                    // knowledge.
                    Log.d(TAG, "Failed to send verification email");
                }
            });;
        }
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

    public void createUserInDB(String email, String username, String Uid){
        UserSettings user = new UserSettings(email
                ,username,Uid);
        UserSettingsDAO dao = new UserSettingsDAO(Uid);
        dao.writeUser(user);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signup_btn) {
            createAccount(emailField.getText().toString(), passwordField.getText().toString());
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
