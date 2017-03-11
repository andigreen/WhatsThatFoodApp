package com.wtf.whatsthatfoodapp.memory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.share.ShareActivity;

public class ViewMemoryActivity extends AppCompatActivity {

    public static final String MEMORY_KEY = "memory";

    private static final String TAG = ViewMemoryActivity.class.getSimpleName();
    private static final int REQ_EDIT = 3936;

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

        loc = (TextView) findViewById(R.id.view_memory_loc);
        desc = (TextView) findViewById(R.id.view_memory_desc);
        rating = (RatingBar) findViewById(R.id.view_memory_rating);
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
            findViewById(R.id.view_memory_desc_section)
                    .setVisibility(View.GONE);
        } else {
            desc.setText(memoryDesc);
        }

        int ratingVal = memory.getRate();
        int priceVal = memory.getPrice();
        if (ratingVal == 0) {
            findViewById(R.id.view_memory_rating).setVisibility(View.GONE);
        } else {
            rating.setRating(ratingVal);
        }
        if (priceVal == 0) {
            findViewById(R.id.view_memory_price).setVisibility(View.GONE);
        } else {
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
            case R.id.view_memory_share:
                Intent shareIntent = new Intent(this,
                        ShareActivity.class);
                shareIntent.putExtra(ShareActivity.MEMORY_KEY, memory);
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
