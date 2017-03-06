package com.wtf.whatsthatfoodapp.memory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.camera.IOImage;
import com.wtf.whatsthatfoodapp.camera.TakePhotoAPI21Activity;

import java.io.File;
public class CreateMemoryActivity extends BasicActivity {

    private static final String TAG = CreateMemoryActivity.class
            .getSimpleName();
    private static final int REQUEST_PHOTO_GET = 144;

    private boolean fromCamera;

    private Uri photoUri;

    private boolean savedForNextTime;

    private boolean reminder;

    private CheckBox saveFNTCheck;

    private CheckBox remindCheck;

    private int imageViewHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initiate photo loading
        fromCamera = getIntent().getBooleanExtra("camera",true);
        if (fromCamera){
            if (Build.VERSION.SDK_INT >= 21 ){
                // We need to ask for permission in runtime for android 6.0
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}
                        ,1);
            } else {
                dispatchTakePictureIntent();
            }
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), REQUEST_PHOTO_GET);
        }
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

        if (item.getItemId() == R.id.saveFNTcheck){
            if (saveFNTCheck.isChecked()) {
                saveFNTCheck.setChecked(false);
            } else {
                saveFNTCheck.setChecked(true);
            }

        }

        if (item.getItemId() == R.id.remindCheck){
            if (remindCheck.isChecked()) {
                remindCheck.setChecked(false);
            } else {
                remindCheck.setChecked(true);
            }

        }

        // Save new memory
        if (item.getItemId() == R.id.create_memory_save && validateForm()) {
            String title = ((TextInputEditText) findViewById(
                    R.id.create_memory_title))
                    .getText().toString();
            String loc = ((TextInputEditText) findViewById(
                    R.id.create_memory_loc))
                    .getText().toString();
            String tags = ((TextInputEditText) findViewById(
                    R.id.create_memory_tags))
                    .getText().toString();
            boolean savedForNextTime = saveFNTCheck.isChecked();
            boolean reminder = this.remindCheck.isChecked();
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
            memory.setTag(tags);
            memory.setSavedForNextTime(savedForNextTime);
            memory.setReminder(reminder);
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
                finish();
                return;
            }
            // Setup CreateMemoryActivity UI once the image is taken
            setContentView(R.layout.activity_create_memory);

            // Set up toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.create_memory_toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            saveFNTCheck = (CheckBox) findViewById(R.id.saveFNTcheck);
            remindCheck = (CheckBox) findViewById(R.id.remindCheck);

            saveFNTCheck.setOnClickListener(this);
            remindCheck.setOnClickListener(this);


            // Load photo view with the result's URI
            if (fromCamera){
                Log.e("Photo Received","True");
                Bundle extras = data.getExtras();
                String path;
                if (Build.VERSION.SDK_INT >= 21){
                    path = (String) extras.get("data");
                } else {
                    Bitmap bitmapImage = (Bitmap) extras.get("data");
                    IOImage ioImage = new IOImage(this, bitmapImage);
                    path = ioImage.saveImage();
                }
                photoUri = Uri.fromFile(new File(path));
            } else {
                photoUri = data.getData();
            }

            ImageView imageView = (ImageView) findViewById(R.id
                    .create_memory_photo);

            Glide.with(this)
                    .load(photoUri)
                    .centerCrop()
                    .into(imageView);
        }
    }
    public void expandImage(View v){
        ImageView imageView = (ImageView) v;
        LinearLayout ll = (LinearLayout) imageView.getParent();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageView.getLayoutParams();

        if (imageViewHeight == 0){
            imageViewHeight = params.height;
            int translateX = (int) (((ll.getX() + ll.getWidth()) / 2) - imageView.getWidth()/2);
            int translateY = (int) (((ll.getY() + ll.getHeight()) / 2) - imageView.getHeight()/2);
            ScaleAnimation animation =  new ScaleAnimation(1,1,1,(float)2);
            animation.setDuration(100);
            //imageView.startAnimation(animation);
            params.height = ll.getHeight();
            imageView.setLayoutParams(params);

            Glide.with(this).load(photoUri).fitCenter().into(imageView);
        } else {
            ScaleAnimation animation =  new ScaleAnimation(1,2,1,1);
            animation.setDuration(100);
            //imageView.startAnimation(animation);
            params.height = imageViewHeight;
            imageView.setLayoutParams(params);
            imageViewHeight = 0;
            Glide.with(this).load(photoUri).centerCrop().into(imageView);
        }

    }
}
