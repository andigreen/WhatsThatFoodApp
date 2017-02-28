package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.camera.TakePhotoAPI21Activity;

public class ProfileActivity extends BasicActivity implements GoogleApiClient.OnConnectionFailedListener{

    private final String TAG = "ProfileActivity";
    private static final int REQUEST_PHOTO_GET = 144;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;
    private FirebaseDatabase database;
    private FirebaseUser user;

    private String username;

    private Uri photoUri;

    private EditText usernameField;
    private EditText emailField;
    private GoogleApiClient mGoogleApiClient;

    private ImageView profile_photo;
    private Button upload_photo;
    private Button edit_btn;
    private MenuItem save;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usernameField = (EditText) findViewById(R.id.name_content);
        usernameField.setEnabled(false);

        emailField = (EditText) findViewById(R.id.email_content);
        emailField.setEnabled(false);
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        emailField.setText(user.getEmail());
        upload_photo = (Button) findViewById(R.id.upload_photo);
        upload_photo.setOnClickListener(this);

        edit_btn = (Button) findViewById(R.id.edit_btn);
        edit_btn.setOnClickListener(this);
        photoUri = user.getPhotoUrl();
        profile_photo = (ImageView) findViewById(R.id.user_profile_photo);
        Glide.with(this).load(photoUri).centerCrop().into(profile_photo);
        App app = (App)getApplicationContext();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, app.getGso())
                .build();

        app.setClient(mGoogleApiClient);
        app.getClient().connect();

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
                // ...
            }
        };



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_memory_toolbar, menu);
        save = menu.findItem(R.id.create_memory_save);
        save.setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create_memory_save && validateForm()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                Log.e(TAG, "onOptionsItemSelected: current user is null!");
                return false;
            }
            save.setVisible(false);
            emailField.setEnabled(false);
            usernameField.setEnabled(false);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateForm(){
        return true;
    }

    private void dispatchTakePictureIntent() {
        final int REQUEST_IMAGE_CAPTURE = 1;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_PHOTO_GET);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, take picture
                    Intent getPhoto = new Intent(this, TakePhotoAPI21Activity.class);
                    startActivityForResult(getPhoto,REQUEST_PHOTO_GET);
                } else {

                    // permission denied, return to WelcomeActivity
                    Toast.makeText(this, "Cannot Access Camera", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener userInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserSettings userInfo = dataSnapshot.getValue(UserSettings.class);
                username = userInfo.getUsername();
                usernameField.setText(username);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadData:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(ProfileActivity.this, "Failed to load data.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        UserSettingsDAO dao = new UserSettingsDAO(user.getUid());
        dao.getUserInfoRef().addListenerForSingleValueEvent(userInfoListener);
        // [END post_value_event_listener]
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v){
        int id = v.getId();
        if(id == R.id.upload_photo){
            //uploadPhoto();
        }else if(id == R.id.edit_btn){
            emailField.setEnabled(true);
            usernameField.setEnabled(true);
            save.setVisible(true);
        }
    }
}
