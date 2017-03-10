package com.wtf.whatsthatfoodapp.memory;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;
import com.wtf.whatsthatfoodapp.auth.SettingsActivity;
import com.wtf.whatsthatfoodapp.user.UserSettings;

/**
 * Created by andig on 3/8/2017.
 */

public class ViewMemoryActivity extends Activity{

    private String memoryKey;
    private final String TAG = ViewMemoryActivity.class.getSimpleName();
    private MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_memory);
        memoryKey = getIntent().getStringExtra(CollageActivity.MEMORY_KEY);

        ImageView view_memory_image = (ImageView) findViewById(R.id.view_memory_image) ;
        //view_memory_image.setImageURI(Nullable);
        view_memory_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent full_image = new Intent(ViewMemoryActivity.this, FullImageActivity.class);
                startActivity(full_image);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        final ValueEventListener view_memory_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Memory memory = dataSnapshot.getValue(
                        Memory.class);

                ((Toolbar) findViewById(R.id.view_memory_toolbar)).setTitle(memory.getTitle());

                if (memory.getLoc() != null)
                    ((TextView) findViewById(R.id.view_memory_location)).setText(memory.getLoc());
                else
                    ((CardView) findViewById(R.id.view_memory_loc_card)).setVisibility(View.GONE);

                if (memory.getDescription() != null)
                ((TextView) findViewById(R.id.view_memory_description)).setText(memory.getDescription());
                else
                    ((CardView) findViewById(R.id.view_memory_description_card)).setVisibility(View.GONE);

                if (memory.getRate() != 0)
                    ((RatingBar) findViewById(R.id.view_memory_rating_bar)).setRating(memory.getRate());
                else
                    ((CardView) findViewById(R.id.view_memory_rating_card)).setVisibility(View.GONE);

                if (memory.getPrice() != 0)
                    ((RatingBar) findViewById(R.id.view_memory_price)).setRating(memory.getPrice());
                else
                    ((CardView) findViewById(R.id.view_memory_price_card)).setVisibility(View.GONE);

                if (memory.getSavedForNextTime() == true)
                    ((CheckBox) findViewById(R.id.view_memory_SFNT)).setChecked(true);
                else
                    ((CardView) findViewById(R.id.view_memory_SFNT_card)).setVisibility(View.GONE);

                if (memory.getReminder() == true)
                    ((CheckBox) findViewById(R.id.view_memory_remind_later)).setChecked(true);
                else
                    ((CardView) findViewById(R.id.view_memory_remind_later_card)).setVisibility(View.GONE);


                dao.getPhotoRef(memory).getDownloadUrl().addOnSuccessListener(
                        new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(ViewMemoryActivity.this)
                                        .load(uri)
                                        .centerCrop()
                                        .dontAnimate() // required by CircleImageView
                                        .into((ImageView) findViewById(R.id.view_memory_image));
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadData:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(ViewMemoryActivity.this, "Failed to load data.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        dao.getMemoriesRef().child(memoryKey).addListenerForSingleValueEvent(view_memory_Listener);
    }
}
