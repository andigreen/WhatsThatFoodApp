package com.wtf.whatsthatfoodapp.memory;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
import java.io.IOException;

public class CreateMemoryActivity extends BasicActivity {

    public static final String IMAGE_URI_KEY = "imageUri";

    private static final String TAG = CreateMemoryActivity.class
            .getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 200;

    private Uri imageUri;

    private boolean savedForNextTime;
    private boolean reminder;

    private CheckBox saveFNTCheck;
    private CheckBox remindCheck;
    private Dialog imageDialog;
    private PlaceAutocompleteFragment placeAutocompleteFragment;
    private String localString;

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

        saveFNTCheck = (CheckBox) findViewById(R.id.saveFNTcheck);
        remindCheck = (CheckBox) findViewById(R.id.remindCheck);

        saveFNTCheck.setOnClickListener(this);
        remindCheck.setOnClickListener(this);

        // Set up placeAutoComplete fragment
        placeAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.create_memory_loc);
        placeAutocompleteFragment.setHint("Your Restaurant");
        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                localString = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Load photo view with the result's URI
        imageUri = getIntent().getParcelableExtra(IMAGE_URI_KEY);
        ImageView imageView = (ImageView) findViewById(R.id
                .create_memory_photo);
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(imageView);
    }

    /**
     * Returns true if all of the form contents are valid, and also updates
     * the error status of form contents.
     * <p>
     * Currently, the form only requires that title and loc are both nonempty.
     */
    private boolean validateForm() {
        String title = ((TextInputEditText) findViewById(
                R.id.create_memory_title))
                .getText().toString();
        /*String loc = ((TextInputEditText) findViewById(R.id
        .create_memory_loc)).getText().toString();*/
        String loc = localString;

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
            String title = ((TextInputEditText) findViewById(
                    R.id.create_memory_title))
                    .getText().toString();
            /*String loc = ((TextInputEditText) findViewById(
                    R.id.create_memory_loc))
                    .getText().toString();*/
            String loc = localString;
            String description = ((TextInputEditText) findViewById(
                    R.id.create_memory_description))
                    .getText().toString();
            boolean savedForNextTime = saveFNTCheck.isChecked();
            boolean reminder = this.remindCheck.isChecked();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.e(TAG, "onOptionsItemSelected: current user is null!");
                return false;
            }

            int rating = (int) ((RatingBar) findViewById(
                    R.id.create_rating_bar)).getRating();
            int price = (int) ((RatingBar) findViewById(
                    R.id.create_price_rating)).getRating();

            // Write memory to dao in order to generate db key
            MemoryDao dao = new MemoryDao(user.getUid());
            final Memory memory = new Memory();
            memory.setTitle(title);
            memory.setLoc(loc);
            memory.setDescription(description);
            memory.setTag(description);
            memory.setRate(rating);
            memory.setPrice(price);
            memory.setSavedForNextTime(savedForNextTime);
            memory.setReminder(reminder);
            dao.writeMemory(memory);

            // Upload photo
            StorageReference photoRef = dao.getPhotoRef(memory);
            UploadTask uploadTask = photoRef.putFile(imageUri);
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
        if(requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                placeAutocompleteFragment.setText(place.getName());
                localString = place.getName().toString();
            }
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
