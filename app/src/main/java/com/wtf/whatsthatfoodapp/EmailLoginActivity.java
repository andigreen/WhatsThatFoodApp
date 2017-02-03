package com.wtf.whatsthatfoodapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
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
    private Button back_btn;
    private Button signup_btn;
    private EditText usernameField;
    private TextView usernameText;
    private Button password_recover;
    private Button reset_btn;
    private DatabaseReference mDatabase;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);
        emailText = (TextView) findViewById(R.id.email);
        passwordText = (TextView) findViewById(R.id.password);
        emailField = (EditText) findViewById(R.id.EmailField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        login_btn = (Button) findViewById(R.id.Login_btn);
        register_btn = (Button) findViewById(R.id.Register_btn);
        back_btn = (Button)findViewById(R.id.back_btn);
        back_btn.setVisibility(View.INVISIBLE);
        signup_btn = (Button)findViewById(R.id.signup_btn);
        signup_btn.setVisibility(View.INVISIBLE);
        password_recover = (Button)findViewById(R.id.password_recover);
        usernameField = (EditText)findViewById(R.id.usernamefield);
        usernameText = (TextView)findViewById(R.id.usenametxt);
        usernameField.setVisibility(View.INVISIBLE);
        usernameText.setVisibility(View.INVISIBLE);
        reset_btn = (Button)findViewById(R.id.reset_btn);
        reset_btn.setVisibility(View.INVISIBLE);
        sharedPrefs = getSharedPreferences(PREFS_NAME,0);
        // Buttons
        login_btn.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        signup_btn.setOnClickListener(this);
        password_recover.setOnClickListener(this);
        reset_btn.setOnClickListener(this);
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]


        final Intent displayHomePage = new Intent(this, WelcomeActivity.class);

        if(user != null) {
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            if (user.isEmailVerified()) {
                Log.d(TAG, "EmailVerified: " + user.isEmailVerified());
                startActivity(displayHomePage);
            }
        }
        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if(user.isEmailVerified()) {
                        Log.d(TAG, "EmailVerified: " + user.isEmailVerified());
                        startActivity(displayHomePage);
                    }
                    else {
                        Log.d(TAG, "EmailVerified: " + user.isEmailVerified());
                        Toast.makeText(EmailLoginActivity.this, "Please verify your email",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };
        // [END auth_state_listener]
        //final Intent displaySignupPage = new Intent(this, RegisterActivity.class);


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

                                // [START_EXCLUDE]
                                hideProgressDialog();
                                // [END_EXCLUDE]
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
        } else if (password.length() < 6) {
            this.passwordField.setError("Password should be longer than 6 characters");
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
            /*final Intent displaySignupPage = new Intent(this, EmailSignupActivity.class);
            startActivity(displaySignupPage);*/
            updateUI(1);
        } else if (i == R.id.Login_btn) {
            signIn(emailField.getText().toString(), passwordField.getText().toString());
        }else if(i == R.id.back_btn){
            updateUI(0);
        }else if(i == R.id.signup_btn){
            createAccount(emailField.getText().toString(), passwordField.getText().toString());
        }else if(i == R.id.password_recover){
            updateUI(2);
        }else if(i == R.id.reset_btn){
            recoverPassword(emailField.getText().toString());
        }
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
                            updateUI(0);

                        }
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(EmailLoginActivity.this, R.string.auth_failed,
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
                                Toast.makeText(EmailLoginActivity.this, "Signup successful. " +
                                        "Verification email sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void updateUI(int choice) {
        hideProgressDialog();
        //update to login
        if(choice == 0){
            login_btn.setVisibility(View.VISIBLE);
            register_btn.setVisibility(View.VISIBLE);
            password_recover.setVisibility(View.VISIBLE);
            usernameText.setVisibility(View.INVISIBLE);
            usernameField.setVisibility(View.INVISIBLE);
            back_btn.setVisibility(View.INVISIBLE);
            signup_btn.setVisibility(View.INVISIBLE);
            passwordField.setVisibility(View.VISIBLE);
            passwordText.setVisibility(View.VISIBLE);
            reset_btn.setVisibility(View.INVISIBLE);
        }
        //update to signup
        else if(choice == 1){
            login_btn.setVisibility(View.INVISIBLE);
            register_btn.setVisibility(View.INVISIBLE);
            password_recover.setVisibility(View.INVISIBLE);
            usernameText.setVisibility(View.VISIBLE);
            usernameField.setVisibility(View.VISIBLE);
            back_btn.setVisibility(View.VISIBLE);
            signup_btn.setVisibility(View.VISIBLE);
            emailField.setText("");
            passwordField.setText("");
            //update to password recover
        }else if(choice == 2){
            login_btn.setVisibility(View.INVISIBLE);
            register_btn.setVisibility(View.INVISIBLE);
            password_recover.setVisibility(View.INVISIBLE);
            usernameText.setVisibility(View.VISIBLE);
            usernameField.setVisibility(View.VISIBLE);
            emailField.setText("");
            passwordField.setText("");
            passwordField.setVisibility(View.INVISIBLE);
            passwordText.setVisibility(View.INVISIBLE);
            reset_btn.setVisibility(View.VISIBLE);
            back_btn.setVisibility(View.VISIBLE);
            usernameField.setVisibility(View.INVISIBLE);
            usernameText.setVisibility(View.INVISIBLE);
        }

    }


    private void recoverPassword(String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(EmailLoginActivity.this,
                            "Password recover email sent", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"PasswordEmailSent: " + task.isSuccessful());
                    updateUI(0);
                }else{
                    Log.d(TAG,"PasswordEmailSent: " + task.isSuccessful());
                    Toast.makeText(EmailLoginActivity.this,
                            "Email does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
