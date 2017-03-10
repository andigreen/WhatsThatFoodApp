package com.wtf.whatsthatfoodapp.memory;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

public class CreateMemoryActivity extends BasicActivity {

    public static final String IMAGE_URI_KEY = "imageUri";

    private static final String TAG = CreateMemoryActivity.class
            .getSimpleName();

    private Uri imageUri;
    private UploadTask imageUpload;

    private MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());
    private Memory memory;
    private MemoryFormFragment form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_memory);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(
                R.id.create_memory_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load photo view with the result's URI
        imageUri = getIntent().getParcelableExtra(IMAGE_URI_KEY);
        ImageView imageView = (ImageView) findViewById(R.id
                .create_memory_photo);
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(imageView);

        // Create memory in db and initiate image upload
        memory = new Memory();
        dao.writeMemory(memory);
        OnFailureListener imageFailure = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // We cancelled the upload, no error here
                if (imageUpload.isCanceled()) return;

                Log.e(TAG,
                        "Failed to upload photo for memory " + memory.getKey());
                Toast error = Toast.makeText(getApplicationContext(),
                        "Photo upload failed. We'll try again later.",
                        Toast.LENGTH_SHORT);
                error.show();
            }
        };
        imageUpload = dao.getPhotoRef(memory).putFile(imageUri);
        imageUpload.addOnFailureListener(imageFailure);

        // Set up form fragment
        form = MemoryFormFragment.newInstance(memory);
        getFragmentManager().beginTransaction().add(R.id.create_memory_form,
                form).commit();
    }

    private boolean saveMemory() {
        if (form.saveToMemory(memory)) {
            dao.writeMemory(memory);
            return true;
        }
        return false;
    }

    private void cancelMemory() {
        if (imageUpload.isInProgress()) imageUpload.cancel();
        dao.deleteMemory(memory);
    }

    @Override
    public void onBackPressed() {
        cancelMemory();
        super.onBackPressed();
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
            cancelMemory();
            finish();
        }

        // Save new memory
        if (item.getItemId() == R.id.create_memory_save) {
            saveMemory();
            finish();
        }

        // Other options not handled
        return super.onOptionsItemSelected(item);
    }

//    public void expandImage(View v) {
//        imageDialog = new Dialog(this);
//
//        imageDialog.setContentView(
//                getLayoutInflater().inflate(R.layout.image_popup, null));
//
//        ImageView imageView = (ImageView) imageDialog.findViewById(
//                R.id.image_popup);
//        try {
//            Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(
//                    this.getContentResolver(), imageUri);
//            imageView.setImageBitmap(bitmapImage);
//            imageDialog.show();
//        } catch (IOException e) {
//            Log.d(TAG, "IOEXCEPTION : photoUri");
//        }
//    }
//
//    public void closeImage(View v) {
//        if (imageDialog != null) {
//            imageDialog.dismiss();
//        }
//    }
}
