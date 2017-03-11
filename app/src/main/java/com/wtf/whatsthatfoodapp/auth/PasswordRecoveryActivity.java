package com.wtf.whatsthatfoodapp.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;

public class PasswordRecoveryActivity extends BasicActivity {

    public static final String EMAIL_KEY = "email";

    private static final String TAG = PasswordRecoveryActivity.class
            .getSimpleName();

    private FirebaseAuth mAuth;
    private EditText emailField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        findViewById(R.id.recover_btn).setOnClickListener(this);

        emailField = (EditText) findViewById(R.id.emailfield);
        emailField.setText("");

        String email = getIntent().getStringExtra(EMAIL_KEY);
        if (email != null) emailField.append(email);
    }

    @Override
    public void onClick(View v) {
        String email = ((EditText) findViewById(R.id.emailfield)).getText()
                .toString();
        if (email.isEmpty()) return;

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Don't show the user anything. This may indicate
                        // that the email doesn't exist, which is exploitable
                        // knowledge.
                        Log.d(TAG, "Failed to send password reset email.");
                    }
                });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
