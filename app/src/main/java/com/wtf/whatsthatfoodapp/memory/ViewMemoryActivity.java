package com.wtf.whatsthatfoodapp.memory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

public class ViewMemoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_memory);
        MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());
        Memory memory = getIntent().getExtras().getParcelable(
                CollageActivity.MEMORY_KEY);

        ImageView view_memory_image = (ImageView) findViewById(
                R.id.view_memory_image);
        view_memory_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent full_image = new Intent(ViewMemoryActivity.this,
                        FullImageActivity.class);
                startActivity(full_image);
            }
        });
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(dao.getPhotoRef(memory))
                .centerCrop()
                .into(view_memory_image);

        Toolbar view_memory_bar = (Toolbar) findViewById(
                R.id.view_memory_toolbar);
        view_memory_bar.setTitle(memory.getTitle());

        TextView view_memory_loc = (TextView) findViewById(
                R.id.view_memory_location);
        view_memory_loc.setText(memory.getLoc());

        TextView view_memory_description = (TextView) findViewById(
                R.id.view_memory_description);
        view_memory_description.setText(memory.getDescription());

        RatingBar view_memory_rating = (RatingBar) findViewById(
                R.id.view_memory_rating_bar);
        view_memory_rating.setRating(memory.getRate());

        RatingBar view_memory_price = (RatingBar) findViewById(
                R.id.view_memory_price);
        view_memory_price.setRating(memory.getPrice());

        CheckBox view_memory_SFNT = (CheckBox) findViewById(
                R.id.view_memory_SFNT);
        view_memory_SFNT.setChecked(memory.getSavedForNextTime());

        CheckBox view_memory_remind = (CheckBox) findViewById(
                R.id.view_memory_remind_later);
        view_memory_remind.setChecked(memory.getReminder());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
