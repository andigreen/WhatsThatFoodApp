package com.wtf.whatsthatfoodapp.memory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

public class ViewMemoryActivity extends BasicActivity {

    private static final String TAG = ViewMemoryActivity.class.getSimpleName();
    private static final int REQ_EDIT = 3936;

    private MemoryDao dao;
    private Memory memory;

    private CollapsingToolbarLayout title;
    private TextView loc;
    private TextView desc;
    private RatingBar rating;
    private RatingBar price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_memory);
        dao = new MemoryDao(AuthUtils.getUserUid());
        memory = getIntent().getExtras().getParcelable(
                CollageActivity.MEMORY_KEY);

        ImageView view_memory_image = (ImageView) findViewById(
                R.id.view_memory_image);
        view_memory_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent full_image_intent = new Intent(ViewMemoryActivity.this,
                        FullImageActivity.class);
                full_image_intent.putExtra("memory_key", memory);
                startActivity(full_image_intent);
            }
        });
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(dao.getPhotoRef(memory))
                .centerCrop()
                .into(view_memory_image);

        title = (CollapsingToolbarLayout) findViewById(
                R.id.view_memory_collapsing_toolbar);
        loc = (TextView) findViewById(R.id.view_memory_location);
        desc = (TextView) findViewById(R.id.view_memory_description);
        rating = (RatingBar) findViewById(R.id.view_memory_rating_bar);
        price = (RatingBar) findViewById(R.id.view_memory_price);

        populateFields();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        if (requestCode == REQ_EDIT && resultCode == RESULT_OK) {
            memory = data.getParcelableExtra(EditMemoryActivity.MEMORY_KEY);
            populateFields();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateFields() {
        title.setTitle(memory.getTitle());
        loc.setText(memory.getLoc());

        String memoryDesc = memory.getDescription();
        if (memoryDesc == null || memoryDesc.isEmpty()) {
            findViewById(R.id.view_memory_description_card)
                    .setVisibility(View.GONE);
        } else {
            desc.setText(memoryDesc);
        }

        if (memory.getRate() == 0) {
            findViewById(R.id.view_memory_rating_card).setVisibility(View.GONE);
        } else {
            rating.setRating(memory.getRate());
        }

        if (memory.getPrice() == 0) {
            findViewById(R.id.view_memory_price_card).setVisibility(View.GONE);
        } else {
            price.setRating(memory.getPrice());
        }

        CheckBox view_memory_SFNT = (CheckBox) findViewById(
                R.id.view_memory_SFNT);
        view_memory_SFNT.setChecked(memory.getSavedForNextTime());

        CheckBox view_memory_remind = (CheckBox) findViewById(
                R.id.view_memory_remind_later);
        view_memory_remind.setChecked(memory.getReminder());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_memory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_memory_share:
                Intent shareIntent = new Intent(this,
                        SharePictureActivity.class);
                shareIntent.putExtra("memory", memory);
                startActivity(shareIntent);
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
}
