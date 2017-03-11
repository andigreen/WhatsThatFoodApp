package com.wtf.whatsthatfoodapp.memory;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;
import com.wtf.whatsthatfoodapp.notification.AlarmReceiver;

import java.util.Calendar;

public class CreateMemoryActivity extends BasicActivity
        implements TimePickerDialog.OnTimeSetListener{

    public static final String IMAGE_URI_KEY = "imageUri";

    private static final String TAG = CreateMemoryActivity.class
            .getSimpleName();

    private Uri imageUri;
    private UploadTask imageUpload;

    private MemoryDao dao;
    private Memory memory;
    private MemoryFormFragment form;

    public static final int REQUEST_CODE = 160;

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
        form = MemoryFormFragment.newInstance(memory,showSFNT);
        getFragmentManager().beginTransaction().add(R.id.create_memory_form,
                form).commit();
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
                form.createPlacePicker();
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

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EditMemoryActivity.MEMORY_KEY,memory);
        intentAlarm.putExtra("bundle",bundle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,REQUEST_CODE, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_LONG).show();
        finish();
    }

}
