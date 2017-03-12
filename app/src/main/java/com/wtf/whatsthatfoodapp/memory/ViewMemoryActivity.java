package com.wtf.whatsthatfoodapp.memory;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.notification.AlarmReceiver;
import com.wtf.whatsthatfoodapp.TextUtil;
import com.wtf.whatsthatfoodapp.share.ShareActivity;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.wtf.whatsthatfoodapp.memory.CreateMemoryActivity.PREFS;

public class ViewMemoryActivity extends AppCompatActivity {

    public static final String MEMORY_KEY = "memory";

    private static final String TAG = ViewMemoryActivity.class.getSimpleName();
    private static final int REQ_EDIT = 3936;
    private static final int REQ_SHARE = 3937;

    private MemoryDao dao;
    private Memory memory;

    private ImageView image;
    private CollapsingToolbarLayout title;
    private Toolbar toolbar;
    private TextView loc;
    private TextView desc;
    private RatingBar rating;
    private RatingBar price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_memory);

        dao = new MemoryDao(this);
        memory = getIntent().getExtras().getParcelable(MEMORY_KEY);

        image = (ImageView) findViewById(R.id.view_memory_image);
        dao.loadImage(memory).centerCrop().into(image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewMemoryActivity.this,
                        FullImageActivity.class);
                intent.putExtra(FullImageActivity.MEMORY_KEY, memory);
                startActivity(intent);
            }
        });

        // Set up collapsing toolbar
        toolbar = (Toolbar) findViewById(R.id.view_memory_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = (CollapsingToolbarLayout) findViewById(
                R.id.view_memory_collapsing_toolbar);

        // Set up share FAB
        findViewById(R.id.view_memory_edit).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent shareIntent = new Intent(ViewMemoryActivity.this,
                                ShareActivity.class);
                        shareIntent.putExtra(ShareActivity.MEMORY_KEY, memory);
                        startActivityForResult(shareIntent, REQ_SHARE);
                    }
                });

        loc = (TextView) findViewById(R.id.view_memory_loc);
        desc = (TextView) findViewById(R.id.view_memory_desc);
        rating = (RatingBar) findViewById(R.id.view_memory_rating);
        price = (RatingBar) findViewById(R.id.view_memory_price);

        if (memory.getSavedForNextTime()) {
            ImageButton removeAlarmBtn = (ImageButton) findViewById(R.id.remove_alarm_button);
            ImageButton editAlarmBtn = (ImageButton) findViewById(R.id.edit_alarm_button);
            removeAlarmBtn.setVisibility(View.VISIBLE);
            editAlarmBtn.setVisibility(View.VISIBLE);

            SharedPreferences sf = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            TextView tv = (TextView) findViewById(R.id.time_tv);
            tv.setText(tv.getText() + " " + sf.getString(String.valueOf(memory.getTsCreatedNeg()), ""));
            tv.setVisibility(View.VISIBLE);
        }

        populateFields();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        if (requestCode == REQ_EDIT && resultCode == RESULT_OK) {
            memory = data.getParcelableExtra(EditMemoryActivity.MEMORY_KEY);
            populateFields();
        }

        if (requestCode == REQ_SHARE && resultCode == ShareActivity
                .RESULT_FAILED) {
            Snackbar.make(findViewById(android.R.id.content), R.string
                    .share_loading_failed, Snackbar.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateFields() {
        title.setTitle(memory.getTitle());
        loc.setText(memory.getLoc());

        String memoryDesc = memory.getDescription();
        if (memoryDesc == null || memoryDesc.isEmpty()) {
            findViewById(R.id.view_memory_desc_section)
                    .setVisibility(View.GONE);
        } else {
            findViewById(R.id.view_memory_desc_section)
                    .setVisibility(View.VISIBLE);
            desc.setText(memoryDesc);
            TextUtil.linkifyTags(desc);
        }

        int ratingVal = memory.getRate();
        int priceVal = memory.getPrice();
        if (ratingVal == 0) {
            findViewById(R.id.view_memory_rating).setVisibility(View.GONE);
        } else {
            findViewById(R.id.view_memory_rating).setVisibility(View.VISIBLE);
            rating.setRating(ratingVal);
        }
        if (priceVal == 0) {
            findViewById(R.id.view_memory_price).setVisibility(View.GONE);
        } else {
            findViewById(R.id.view_memory_price).setVisibility(View.VISIBLE);
            price.setRating(priceVal);
        }
        if (ratingVal == 0 && priceVal == 0) {
            findViewById(R.id.view_memory_rating_section)
                    .setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_memory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.view_memory_edit:
                Intent editIntent = new Intent(this, EditMemoryActivity.class);
                editIntent.putExtra(EditMemoryActivity.MEMORY_KEY, memory);
                startActivityForResult(editIntent, REQ_EDIT);
                break;
            case R.id.view_memory_delete:
                AlertDialog dialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.dialog_delete_memory_title);
                builder.setMessage(R.string.dialog_delete_memory_message);
                builder.setPositiveButton(R.string.dialog_delete_pos,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int
                                    which) {
                                dao.deleteMemory(memory);
                                finish();
                            }
                        });
                builder.setNegativeButton(R.string.dialog_delete_neg,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int
                                    which) {
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void removeAlarm(View v) {
        memory.setSavedForNextTime(false);
        dao.writeMemory(memory);
        SharedPreferences sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int requestCode = sp.getInt((String.valueOf(memory.getTsCreated())), 0);
        if (requestCode == 0) {
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(CollageActivity.REMINDERS_COUNT, sp.getInt(CollageActivity.REMINDERS_COUNT, 1) - 1);
        editor.apply();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EditMemoryActivity.MEMORY_KEY, memory);
        intentAlarm.putExtra("bundle", bundle);
        alarmManager.cancel(PendingIntent.getBroadcast(this, requestCode, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        finish();
    }

    public void editAlarm(View v) {

        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(
                this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("H:m");

                SharedPreferences sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                int requestCode = sp.getInt((String.valueOf(memory.getTsCreated())), 0);
                if (requestCode == 0) {
                    return;
                }

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intentAlarm = new Intent(getApplicationContext(), AlarmReceiver.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(EditMemoryActivity.MEMORY_KEY, memory);
                intentAlarm.putExtra("bundle", bundle);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                // Save the alarm request code so it can be accessed and cancelled after
                SharedPreferences.Editor editor = getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
                editor.putInt(String.valueOf(memory.getTsCreated()), requestCode);
                editor.putString(String.valueOf(memory.getTsCreatedNeg()), sdf.format(calendar.getTime()));
                editor.apply();

                finish();
            }
        },
                calendar.get(Calendar.HOUR_OF_DAY) + 2,
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this)).show();

    }

    public void showInMap(View v) {
        TextView tv = (TextView) v;
        CharSequence loc = tv.getText();

        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(String.valueOf(loc)));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
        }
    private float[] convertLocToCoordinates (String loc){
        float[] coordinates = new float[2];
        String[] sCoordinates = loc.split(" ");
        String degrees = "°";
        String minutes = "'";
        String[] sDegrees = sCoordinates[0].split(degrees);
        float longitude = Float.valueOf(sDegrees[0]);
        longitude += Float.valueOf(sDegrees[1].split(minutes)[0]) / 60;

        String[] s2Degrees = sCoordinates[1].split(degrees);
        float latitude = Float.valueOf(s2Degrees[0]);
        latitude += Float.valueOf(s2Degrees[1].split(minutes)[0]) / 60;

        coordinates[0] = longitude;
        coordinates[1] = latitude;
        return coordinates;
    }
}
