package com.wtf.whatsthatfoodapp.memory;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.notification.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.google.android.gms.location.places.Place.TYPE_BAKERY;
import static com.google.android.gms.location.places.Place.TYPE_BAR;
import static com.google.android.gms.location.places.Place.TYPE_CAFE;
import static com.google.android.gms.location.places.Place.TYPE_CONVENIENCE_STORE;
import static com.google.android.gms.location.places.Place.TYPE_FOOD;
import static com.google.android.gms.location.places.Place.TYPE_GROCERY_OR_SUPERMARKET;
import static com.google.android.gms.location.places.Place.TYPE_LIQUOR_STORE;
import static com.google.android.gms.location.places.Place.TYPE_MEAL_TAKEAWAY;
import static com.google.android.gms.location.places.Place.TYPE_RESTAURANT;

public class CreateMemoryActivity extends BasicActivity
        implements TimePickerDialog.OnTimeSetListener{

    public static final String IMAGE_URI_KEY = "imageUri";
    public static final String PREFS = "SHARED_PREFS";

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;
    private static final int REQUEST_CHECK_SETTINGS = 3;

    private static final String TAG = CreateMemoryActivity.class
            .getSimpleName();
    private static final String TAGL = "LocationService";

    private Uri imageUri;
    private UploadTask imageUpload;

    private MemoryDao dao;
    private Memory memory;
    private MemoryFormFragment form;

    private GoogleApiClient mGoogleApiClient;
    private CharSequence names[] = new CharSequence[5];
    private int count = 0;
    private int namesIndex = 0;
    private List<Integer> type;

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
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateMemoryActivity.this,
                        FullImageActivity.class);
                intent.putExtra(FullImageActivity.MEMORY_KEY, memory);
                startActivity(intent);
            }
        });

        // Create memory in db and initiate image upload
        dao = new MemoryDao(this);
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
        boolean showSFNT = false;
        form = MemoryFormFragment.newInstance(memory);
        getFragmentManager().beginTransaction().add(R.id.create_memory_form,
                form).commit();

        mGoogleApiClient = new GoogleApiClient.Builder(CreateMemoryActivity.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addOnConnectionFailedListener(CreateMemoryActivity.this)
                .enableAutoManage(this, 0, this)
                .build();
        mGoogleApiClient.connect();
    }

    private void cancelMemory() {
        if (imageUpload.isInProgress()) imageUpload.cancel();
        dao.deleteMemory(memory);
    }

    @Override
    public void onBackPressed() {
        form.confirmDiscard(confirmDiscardListener);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_memory, menu);
        return true;
    }

    private MemoryFormFragment.ConfirmDiscardListener confirmDiscardListener
            = new MemoryFormFragment.ConfirmDiscardListener() {
        @Override
        public void onPositive() {
            cancelMemory();
            finish();
        }

        @Override
        public void onNegative() {
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Back to collage
            case android.R.id.home:
                form.confirmDiscard(confirmDiscardListener);
                return true;

            // Start location picker
            case R.id.create_memory_getloc:
                askForLocationService();
                return true;

            // Save new memory
            case R.id.create_memory_save:
                if (form.validateAndSaveInto(memory)) {
                    memory.setSavedForNextTime(false);
                    dao.writeMemory(memory);
                    finish();
                }
                return true;

            // Save For Next Time : Set Alarm
            case R.id.saveFNT:
                // Make sure that form is valid first, and if so get its values
                if (!form.validateAndSaveInto(memory)) return true;

                // Open time picker
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(
                        this, this,
                        calendar.get(Calendar.HOUR_OF_DAY) + 2,
                        calendar.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(this)).show();
                return true;
        }

        // Other options not handled
        return super.onOptionsItemSelected(item);
    }

    /**
     * Receives the time picked, and schedules a notification.
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        memory.setSavedForNextTime(true);
        dao.writeMemory(memory);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
        calendar.set(Calendar.MINUTE,minute);

        int requestCode = (int)System.currentTimeMillis();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EditMemoryActivity.MEMORY_KEY,memory);
        intentAlarm.putExtra("bundle",bundle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,requestCode, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_LONG).show();

        // Save the alarm request code so it can be accessed and cancelled after
        SharedPreferences sp = getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit();
        editor.putInt(String.valueOf(memory.getTsCreated()),requestCode);
        SimpleDateFormat sf = new SimpleDateFormat("H:mm");
        editor.putString(String.valueOf(memory.getTsCreatedNeg()),sf.format(calendar.getTime()));
        editor.putInt(CollageActivity.REMINDERS_COUNT,sp.getInt(CollageActivity.REMINDERS_COUNT,0)+1);
        editor.apply();

        finish();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPlaceDetectionApi();
                }
                break;
        }
    }

    public void askForLocationService(){
        if (mGoogleApiClient.isConnected()) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest
                    .setPriority
                            (LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(5 * 1000);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()

                    .addLocationRequest(locationRequest);

            PendingResult<LocationSettingsResult> result =
                    LocationServices
                            .SettingsApi.checkLocationSettings(
                            mGoogleApiClient, builder.build());
            result.setResultCallback(
                    new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult
                                result) {
                            final Status status = result.getStatus();
                            //final
                            //LocationSettingsStates = result.getLocationSettingsStates();

                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied. The client can initialize location
                                    // requests here.
                                    checkAndFindCurrentPlace();
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied. But could be fixed by showing the user

                                    // a dialog.
                                    try {
                                        // Show the dialog by calling
                                        // startResolutionForResult(),
                                        // and check the result in
                                        // onActivityResult().
                                        status.startResolutionForResult(
                                                CreateMemoryActivity.this,
                                                REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException e) {
                                        //TODO Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes
                                        .SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied. However, we have no way to fix the
                                    // settings so we won't show the dialog.
                                    break;
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAGL, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }


    private void callPlaceDetectionApi() throws SecurityException {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                if (likelyPlaces.getCount() <= 0) {
                    Toast.makeText(CreateMemoryActivity.this, "Oops, no place " +
                                    "found! Try turn on location service.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    count = 0;
                    namesIndex = 0;
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        type = placeLikelihood.getPlace().getPlaceTypes();
                        if(type.contains(TYPE_RESTAURANT) || type.contains
                                (TYPE_BAKERY) || type.contains(TYPE_BAR) ||
                                type.contains(TYPE_CAFE) || type.contains
                                (TYPE_CONVENIENCE_STORE) || type.contains
                                (TYPE_FOOD) || type.contains
                                (TYPE_GROCERY_OR_SUPERMARKET) || type
                                .contains(TYPE_LIQUOR_STORE) || type.contains
                                (TYPE_MEAL_TAKEAWAY))
                            count++;
                    }
                    if (count < 5) {
                        names = new CharSequence[count];
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            type = placeLikelihood.getPlace().getPlaceTypes();
                            if(type.contains(TYPE_RESTAURANT) || type.contains
                                    (TYPE_BAKERY) || type.contains(TYPE_BAR) ||
                                    type.contains(TYPE_CAFE) || type.contains
                                    (TYPE_CONVENIENCE_STORE) || type.contains
                                    (TYPE_FOOD) || type.contains
                                    (TYPE_GROCERY_OR_SUPERMARKET) || type
                                    .contains(TYPE_LIQUOR_STORE) || type.contains
                                    (TYPE_MEAL_TAKEAWAY))
                                names[namesIndex++] = placeLikelihood
                                        .getPlace().getName();
                        }
                    } else {
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            type = placeLikelihood.getPlace().getPlaceTypes();
                            if (type.contains(TYPE_RESTAURANT) || type.contains
                                    (TYPE_BAKERY) || type.contains(TYPE_BAR) ||
                                    type.contains(TYPE_CAFE) || type.contains
                                    (TYPE_CONVENIENCE_STORE) || type.contains
                                    (TYPE_FOOD) || type.contains
                                    (TYPE_GROCERY_OR_SUPERMARKET) || type
                                    .contains(TYPE_LIQUOR_STORE) || type.contains
                                    (TYPE_MEAL_TAKEAWAY)) {
                                if (namesIndex < 5)
                                    names[namesIndex++] = placeLikelihood
                                            .getPlace().getName();
                            }
                        }
                    }
                    //No restaurants found
                    if (namesIndex == 0) {
                        new AlertDialog.Builder(CreateMemoryActivity.this)
                                .setTitle("Can't find a thing, are you in a " +
                                        "desert?")
                                .setPositiveButton("TRY MAP?",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                form.createPlacePicker();
                                            }
                                        }).show();
                    } else {
                        new AlertDialog.Builder(CreateMemoryActivity.this)
                                .setTitle("Looking for these?")
                                .setPositiveButton("GO TO MAP",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                form.createPlacePicker();
                                            }
                                        })
                                .setItems(names,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface
                                                    dialog, int which) {
                                                form.setLocation(names[which]);
                                            }
                                        })
                                .show();
                    }
                }
                likelyPlaces.release();
            }
        });
    }

    protected void checkAndFindCurrentPlace() {
        if (ContextCompat.checkSelfPermission(CreateMemoryActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat
                    .shouldShowRequestPermissionRationale(CreateMemoryActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(CreateMemoryActivity.this)
                        .setTitle("Location Access")
                        .setMessage(
                                "Need location permission to get precise " +
                                        "result")
                        .setPositiveButton("Got it!", new DialogInterface
                                .OnClickListener() {
                            public void onClick(DialogInterface dialog, int
                                    which) {
                                ActivityCompat.requestPermissions
                                        (CreateMemoryActivity.this,
                                        new String[]{android.Manifest.permission
                                                .ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(
                        CreateMemoryActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            callPlaceDetectionApi();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //states has no use for now
        final LocationSettingsStates states = LocationSettingsStates
                .fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        checkAndFindCurrentPlace();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}
