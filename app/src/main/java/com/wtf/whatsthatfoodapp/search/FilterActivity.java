package com.wtf.whatsthatfoodapp.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;

import com.wtf.whatsthatfoodapp.R;

/**
 * Created by andig on 3/1/2017.
 */
public class FilterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);

        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.9),(int)(height*.8));

        Spinner rating_spinner = (Spinner) findViewById(R.id.rating_spinner);
        ArrayAdapter<CharSequence> rating_adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_rating, android.R.layout.simple_spinner_item);
        rating_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rating_spinner.setAdapter(rating_adapter);

        Spinner price_spinner = (Spinner) findViewById(R.id.price_spinner);
        ArrayAdapter<CharSequence> price_adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_pricing, android.R.layout.simple_spinner_item);
        price_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        price_spinner.setAdapter(price_adapter);

        Spinner time_spinner = (Spinner) findViewById(R.id.time_spinner);
        ArrayAdapter<CharSequence> time_adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_time, android.R.layout.simple_spinner_item);
        time_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        time_spinner.setAdapter(time_adapter);

        final Spinner sort_by_spinner = (Spinner) findViewById(R.id.sort_by_spinner);
        ArrayAdapter<CharSequence> sort_by_adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_sort_by, android.R.layout.simple_spinner_item);
        sort_by_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_by_spinner.setAdapter(sort_by_adapter);
        sort_by_spinner.setSelection(1);

        RatingBar rating_ratingbar = (RatingBar) findViewById(R.id.rating_rating_bar);
        RatingBar price_ratingbar = (RatingBar)  findViewById(R.id.price_rating_bar);

        Button cancel = (Button) findViewById(R.id.filter_cancel);
        Button apply = (Button) findViewById(R.id.filter_apply);

        //TODO:THE RATING BARS & FETCH DATA FROM THE FILTER & GET CORRECT ALIGNMENT
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            finish();
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO
                String sort_mode = sort_by_spinner.getSelectedItem().toString();
                Intent toSearchActivity = new Intent(FilterActivity.this, SearchActivity.class);
                toSearchActivity.putExtra("sort_mode", sort_mode);
                startActivity(toSearchActivity);
            }
        });




    }


    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
