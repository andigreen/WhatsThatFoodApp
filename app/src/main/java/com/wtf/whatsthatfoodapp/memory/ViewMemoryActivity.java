package com.wtf.whatsthatfoodapp.memory;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

/**
 * Created by andig on 3/8/2017.
 */

public class ViewMemoryActivity extends Activity{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_memory);
        String memoryKey = getIntent().getStringExtra(CollageActivity.MEMORY_KEY);
        MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());

        dao.getMemoriesRef().child(memoryKey);




        ImageView view_memory_image = (ImageView) findViewById(R.id.view_memory_image) ;
        //view_memory_image.setImageURI(Nullable);
        view_memory_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent full_image = new Intent(ViewMemoryActivity.this, FullImageActivity.class);
                startActivity(full_image);
            }
        });

        Toolbar view_memory_bar = (Toolbar) findViewById(R.id.view_memory_toolbar);
        view_memory_bar.setTitle("Memory entry");

        TextView view_memory_loc = (TextView) findViewById(R.id.view_memory_location);
        view_memory_loc.setText("at home");

        TextView view_memory_description = (TextView) findViewById(R.id.view_memory_description);
        view_memory_description.setText("The picture will stretch while sliding." +
                "Need to implement some thing like the MemoryListAdapter to specific on " +
                "which entry is selected. " +
                "Hard to figure out, till now");

        RatingBar view_memory_rating = (RatingBar) findViewById(R.id.view_memory_rating_bar);
        view_memory_rating.setRating(3);

        RatingBar view_memory_price = (RatingBar) findViewById(R.id.view_memory_price);
        view_memory_price.setRating(2);

        CheckBox view_memory_SFNT = (CheckBox) findViewById(R.id.view_memory_SFNT);
        view_memory_SFNT.setChecked(true);

        CheckBox view_memory_remind = (CheckBox) findViewById(R.id.view_memory_remind_later);
        view_memory_remind.setChecked(true);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
