package com.wtf.whatsthatfoodapp.auth;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.camera.TakePhotoAPI21Activity;
import com.wtf.whatsthatfoodapp.memory.CreateMemoryActivity;

import java.io.FileNotFoundException;
import java.io.IOException;

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


    boolean email_changed;
    boolean change_photo;

    private static Bitmap Image = null;
    private static Bitmap rotateImage = null;

    private static final int GALLERY = 1;
    private StorageReference photoref;

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
        UserSettings userSettings = new UserSettings(this.user.getEmail(),
                usernameField.getText().toString(),this.user.getUid());
        photoref = new UserSettingsDAO(this.user.getUid()).getPhotoRef(userSettings);
        profile_photo = (ImageView) findViewById(R.id.user_profile_photo);

        Glide.with(this).using(new FirebaseImageLoader()).load(photoref).centerCrop().into(profile_photo);


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



        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    email_changed = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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

            if(email_changed) {
                user.updateEmail(emailField.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User email address updated.");
                                }
                                if (!task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Email already taken",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent. Please verify and login again");
                                    Intent intent = new Intent(ProfileActivity.this, LogoutActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });


            }
            createUserInDB(emailField.getText().toString(), usernameField.getText().toString(),
                    user.getUid());

            if(change_photo){
                savePhoto();
            }
            save.setVisible(false);
            emailField.setEnabled(false);
            usernameField.setEnabled(false);
            email_changed = false;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validateForm(){
        if(emailField.getText().toString().length() == 0) {
            emailField.setError("Cannot be empty");
            return false;
        }
        if(!emailField.getText().toString().contains("@")){
            emailField.setError("bad format");
            return false;
        }

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
            uploadPhoto();
            save.setVisible(true);
        }else if(id == R.id.edit_btn){
            if(getProvider().equals("Firebase"))
                emailField.setEnabled(true);
            usernameField.setEnabled(true);
            save.setVisible(true);
        }
    }



    public void createUserInDB(String email, String username, String Uid){
        UserSettings user = new UserSettings(email
                ,username,Uid);
        UserSettingsDAO dao = new UserSettingsDAO(Uid);
        dao.writeUser(user);
    }


    public void uploadPhoto(){
        //profile_photo.setImageBitmap(null);
        //if (Image != null)
          //  Image.recycle();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY);


    }

    public void savePhoto(){
        UserSettings user = new UserSettings(this.user.getEmail(),usernameField.getText().toString(),
                this.user.getUid());
        UserSettingsDAO dao = new UserSettingsDAO(this.user.getUid());
        StorageReference photoref = dao.getPhotoRef(user);
        UploadTask uploadTask = photoref.putFile(photoUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to upload profile photo"
                        + ProfileActivity.this.user.getUid());
                Toast error = Toast.makeText(getApplicationContext(),
                        "Photo upload failed. We'll try again later.",
                        Toast.LENGTH_SHORT);
                error.show();
            }
        }).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot
                                                  taskSnapshot) {
                        Toast success = Toast.makeText(
                                getApplicationContext(),
                                "Photo upload succeeded!",
                                Toast.LENGTH_SHORT
                        );
                        success.show();
                    }
                });
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse("gs://whatsthatfoodapp-d3847.appspot.com/" + photoref.toString()))
                .build();

        this.user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }

    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY && resultCode != 0) {
            photoUri = data.getData();
            try {
                Image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                if (getOrientation(getApplicationContext(), photoUri) != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(getOrientation(getApplicationContext(), photoUri));
                    if (rotateImage != null)
                        rotateImage.recycle();
                    rotateImage = Bitmap.createBitmap(Image, 0, 0, Image.getWidth(), Image.getHeight(), matrix,true);
                    profile_photo.setImageBitmap(rotateImage);
                } else
                    profile_photo.setImageBitmap(Image);
                change_photo = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
