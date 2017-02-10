package com.wtf.whatsthatfoodapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

public class CreateMemoryActivity extends AppCompatActivity {

    private static final String TAG = CreateMemoryActivity.class
            .getSimpleName();
    private static final int REQUEST_PHOTO_GET = 144;

    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_memory);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.create_memory_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initiate photo loading
        Intent getPhoto = new Intent(Intent.ACTION_GET_CONTENT);
        getPhoto.setType("image/*");
        startActivityForResult(getPhoto, REQUEST_PHOTO_GET);
    }

    /**
     * Returns true if all of the form contents are valid, and also updates
     * the error status of form contents.
     *
     * Currently, the form only requires that title and loc are both nonempty.
     */
    private boolean validateForm() {
        String title = ((TextInputEditText) findViewById(
                R.id.create_memory_title))
                .getText().toString();
        String loc = ((TextInputEditText) findViewById(R.id.create_memory_loc))
                .getText().toString();
        TextInputLayout titleWrapper = (TextInputLayout) findViewById(R.id
                .create_memory_title_wrapper);
        TextInputLayout locWrapper = (TextInputLayout) findViewById(R.id
                .create_memory_loc_wrapper);
        boolean valid = true;

        if (title.isEmpty()) {
            titleWrapper.setError("Please enter a title.");
            valid = false;
        } else titleWrapper.setErrorEnabled(false);

        if (loc.isEmpty()) {
            locWrapper.setError("Please enter a location.");
            valid = false;
        } else locWrapper.setErrorEnabled(false);

        return valid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_memory_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Back to collage
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        // Save new memory
        if (item.getItemId() == R.id.create_memory_save && validateForm()) {
            String title = ((TextInputEditText) findViewById(
                    R.id.create_memory_title))
                    .getText().toString();
            String loc = ((TextInputEditText) findViewById(
                    R.id.create_memory_loc))
                    .getText().toString();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.e(TAG, "onOptionsItemSelected: current user is null!");
                return false;
            }

            // Write memory to dao in order to generate db key
            MemoryDao dao = new MemoryDao(user.getUid());
            final Memory memory = new Memory();
            memory.setTitle(title);
            memory.setLoc(loc);
            dao.writeMemory(memory);

            // Upload photo
            StorageReference photoRef = dao.getPhotoRef(memory);
            UploadTask uploadTask = photoRef.putFile(photoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to upload photo for memory "
                            + memory.getKey());
                    Toast error = Toast.makeText(getApplicationContext(),
                            "Photo upload failed. We'll try again later.",
                            Toast.LENGTH_SHORT);
                    error.show();
                    CreateMemoryActivity.this.finish();
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
                            CreateMemoryActivity.this.finish();
                        }
                    });
        }

        // Other options not handled
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        if (requestCode == REQUEST_PHOTO_GET) {
            if (resultCode != RESULT_OK) {
                Log.d(TAG, "onActivityResult (PHOTO_GET): Failed to get photo");
                return;
            }

            // Load photo view with the result's URI
            photoUri = data.getData();
            ImageView imageView = (ImageView) findViewById(R.id
                    .create_memory_photo);
            Glide.with(this)
                    .load(photoUri)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
