package com.wtf.whatsthatfoodapp.memory;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

import java.io.IOException;

public class CreateMemoryActivity extends BasicActivity {

    public static final String IMAGE_URI_KEY = "imageUri";

    private static final String TAG = CreateMemoryActivity.class
            .getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 200;

    private Uri imageUri;
    private UploadTask imageUpload;

    private boolean savedForNextTime;
    private boolean reminder;

    private CheckBox saveFNTCheck;
    private CheckBox remindCheck;
    private Dialog imageDialog;

    private EditText titleText;
    private EditText locText;
    private EditText descText;
    private TextInputLayout titleWrapper;
    private TextInputLayout locWrapper;

    private MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());
    private Memory memory;

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

        titleText = (EditText) findViewById(R.id.create_memory_title);
        locText = (EditText) findViewById(R.id.create_memory_loc);
        descText = (EditText) findViewById(R.id.create_memory_description);
        titleWrapper = (TextInputLayout) findViewById(
                R.id.create_memory_title_wrapper);
        locWrapper = (TextInputLayout) findViewById(
                R.id.create_memory_loc_wrapper);

        // Clear errors whenever text changes
        titleText.addTextChangedListener(
                new ErrorClearTextWatcher(titleWrapper));
        locText.addTextChangedListener(new ErrorClearTextWatcher(locWrapper));

        saveFNTCheck = (CheckBox) findViewById(R.id.saveFNTcheck);
        remindCheck = (CheckBox) findViewById(R.id.remindCheck);

        saveFNTCheck.setOnClickListener(this);
        remindCheck.setOnClickListener(this);

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
    }

    private void saveMemory() {
        boolean savedForNextTime = saveFNTCheck.isChecked();
        boolean reminder = this.remindCheck.isChecked();

        int rating = (int) ((RatingBar) findViewById(
                R.id.create_rating_bar)).getRating();
        int price = (int) ((RatingBar) findViewById(
                R.id.create_price_rating)).getRating();

        // Write memory to dao
        memory.setTitle(titleText.getText().toString());
        memory.setLoc(locText.getText().toString());
        memory.setDescription(descText.getText().toString());
        memory.setTag(descText.getText().toString());
        memory.setRate(rating);
        memory.setPrice(price);
        memory.setSavedForNextTime(savedForNextTime);
        memory.setReminder(reminder);
        dao.writeMemory(memory);

    }

    private void cancelMemory() {
        if (imageUpload.isInProgress()) imageUpload.cancel();
        dao.deleteMemory(memory);
    }

    /**
     * Returns true if all of the form contents are valid, and also updates
     * the error status of form contents.
     * <p>
     * Currently, the form only requires that title and loc are both nonempty.
     */
    private boolean validateForm() {
        String title = titleText.getText().toString();
        String loc = locText.getText().toString();
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

    private class ErrorClearTextWatcher implements TextWatcher {
        private TextInputLayout layout;

        ErrorClearTextWatcher(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int
                count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            layout.setErrorEnabled(false);
        }
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

        if (item.getItemId() == R.id.saveFNTcheck) {
            if (saveFNTCheck.isChecked()) {
                saveFNTCheck.setChecked(false);
            } else {
                saveFNTCheck.setChecked(true);
            }

        }

        if (item.getItemId() == R.id.remindCheck) {
            if (remindCheck.isChecked()) {
                remindCheck.setChecked(false);
            } else {
                remindCheck.setChecked(true);
            }

        }

        // Save new memory
        if (item.getItemId() == R.id.create_memory_save && validateForm()) {
            saveMemory();
            finish();
        }

        // Other options not handled
        return super.onOptionsItemSelected(item);
    }

    public void createPlacePicker(View v) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            locText.setText(place.getName());
        }
    }

    public void expandImage(View v) {
        imageDialog = new Dialog(this);

        imageDialog.setContentView(
                getLayoutInflater().inflate(R.layout.image_popup, null));

        ImageView imageView = (ImageView) imageDialog.findViewById(
                R.id.image_popup);
        try {
            Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(
                    this.getContentResolver(), imageUri);
            imageView.setImageBitmap(bitmapImage);
            imageDialog.show();
        } catch (IOException e) {
            Log.d(TAG, "IOEXCEPTION : photoUri");
        }
    }

    public void closeImage(View v) {
        if (imageDialog != null) {
            imageDialog.dismiss();
        }
    }
}
