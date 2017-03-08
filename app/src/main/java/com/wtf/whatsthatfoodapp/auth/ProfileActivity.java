package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.camera.TakePhotoAPI21Activity;
import com.wtf.whatsthatfoodapp.user.UserSettings;
import com.wtf.whatsthatfoodapp.user.UserSettingsDAO;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BasicActivity {

    private final String TAG = "ProfileActivity";

    private UserSettingsDAO dao;

    private CircleImageView photo_button;
    private EditText nameField;

    boolean editable = false;

    private static final int GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameField = (EditText) findViewById(R.id.profile_name);
        nameField.setEnabled(false);

        dao = new UserSettingsDAO(AuthUtils.getUserUid());

        photo_button = (CircleImageView) findViewById(R.id.profile_photo);
        photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"),
                        GALLERY);
            }
        });

        dao.getPhotoRef().getDownloadUrl().addOnSuccessListener(
                new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(ProfileActivity.this)
                                .load(uri)
                                .centerCrop()
                                .dontAnimate() // required by CircleImageView
                                .into(photo_button);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_toolbar, menu);

        MenuItem editItem = menu.findItem(R.id.profile_edit);
        MenuItem saveItem = menu.findItem(R.id.profile_save);
        editItem.setVisible(!editable);
        saveItem.setVisible(editable);

        return true;
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.profile_edit:
                editable = true;
                updateNameEditable();
                invalidateOptionsMenu();
                return true;
            case R.id.profile_save:
                Map<String, Object> newName = new HashMap<>();
                newName.put("username", nameField.getText().toString());
                dao.getUserInfoRef().updateChildren(newName);

                editable = false;
                updateNameEditable();
                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the editability of the Name field.
     */
    public void updateNameEditable() {
        nameField.setEnabled(editable);
        nameField.setFocusable(editable);
        nameField.setFocusableInTouchMode(editable);
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener userInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserSettings userInfo = dataSnapshot.getValue(
                        UserSettings.class);
                nameField.setText(userInfo.getUsername());
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
        dao.getUserInfoRef().addListenerForSingleValueEvent(userInfoListener);
        // [END post_value_event_listener]
    }
    // [END on_start_add_listener]

    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        if (requestCode == GALLERY && resultCode != 0) {
            Uri photoUri = data.getData();
            Glide.with(this).load(photoUri).dontAnimate().into(photo_button);

            UploadTask uploadTask = dao.getPhotoRef().putFile(photoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to upload profile photo for user "
                            + AuthUtils.getUserUid());
                    Toast error = Toast.makeText(getApplicationContext(),
                            "Photo upload failed. We'll try again later.",
                            Toast.LENGTH_SHORT);
                    error.show();
                }
            });
        }
    }

}
