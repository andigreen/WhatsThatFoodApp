package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;

public class PasswordRecoveryActivity extends BasicActivity {

    public final String TAG = PasswordRecoveryActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        findViewById(R.id.recover_btn).setOnClickListener(this);
        findViewById(R.id.login_btn).setOnClickListener(this);
        findViewById(R.id.signup_btn).setOnClickListener(this);
    }

    private void recoverPassword(String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(PasswordRecoveryActivity.this,
                                    "Password recover email sent", Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"PasswordEmailSent: " + task.isSuccessful());
                        }else{
                            Log.d(TAG,"PasswordEmailSent: " + task.isSuccessful());
                            Toast.makeText(PasswordRecoveryActivity.this,
                                    "Email does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signup_btn) {
            Intent toSignupPage = new Intent(PasswordRecoveryActivity.this, CreateAccountActivity.class);
            startActivity(toSignupPage);
        }else if (i == R.id.login_btn) {
            Intent toLoginPage = new Intent(PasswordRecoveryActivity.this,MainActivity.class);
            startActivity(toLoginPage);
        }else if(i == R.id.recover_btn){
            recoverPassword(((EditText)findViewById(R.id.emailfield)).getText().toString());
        }
    }
}
