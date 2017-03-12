package com.wtf.whatsthatfoodapp.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.user.UserSettings;
import com.wtf.whatsthatfoodapp.user.UserSettingsDao;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends BasicActivity {

    private final String TAG = SettingsActivity.class.getSimpleName();

    private UserSettingsDao dao;


    private EditText nameField;

    private static final int GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        nameField = (EditText) findViewById(R.id.settings_name);


    }

    /**
     * Writes the value of nameField to the DB using the dao.
     */
    private void updateName() {
        Map<String, Object> newName = new HashMap<>();
        newName.put("username", nameField.getText().toString());
        dao.getUserInfoRef().updateChildren(newName);
    }

    // Update name when the toolbar back button is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            updateName();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Update name when the system back button is pressed
    @Override
    public void onBackPressed() {
        updateName();
        super.onBackPressed();
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
                Toast.makeText(SettingsActivity.this, "Failed to load data.",
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

    }

}
