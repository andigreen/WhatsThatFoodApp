package com.wtf.whatsthatfoodapp.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
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

    public final static String TAG = FilterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * .9), (int) (height * .8));

        final Spinner rating_spinner = (Spinner) findViewById(
                R.id.rating_spinner);
        ArrayAdapter<SearchActivity.FilterMode> ratingModeAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        SearchActivity.FilterMode.values());
        rating_spinner.setAdapter(ratingModeAdapter);

        final Spinner price_spinner = (Spinner) findViewById(
                R.id.price_spinner);
        ArrayAdapter<SearchActivity.FilterMode> priceModeAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        SearchActivity.FilterMode.values());
        price_spinner.setAdapter(priceModeAdapter);

        Spinner time_spinner = (Spinner) findViewById(R.id.time_spinner);
        ArrayAdapter<CharSequence> time_adapter = ArrayAdapter
                .createFromResource(
                        this,
                        R.array.filter_time,
                        android.R.layout.simple_spinner_item);
        time_adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        time_spinner.setAdapter(time_adapter);

        final Spinner sort_by_spinner = (Spinner) findViewById(
                R.id.sort_by_spinner);
        ArrayAdapter<SearchActivity.SortMode> sortAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        SearchActivity.SortMode.values());
        sort_by_spinner.setAdapter(sortAdapter);

        final RatingBar rating_ratingbar = (RatingBar) findViewById(
                R.id.rating_rating_bar);
        final RatingBar price_ratingbar = (RatingBar) findViewById(
                R.id.price_rating_bar);

        // Init values from calling activity

        SearchActivity.SortMode mode = (SearchActivity.SortMode)
                getIntent().getSerializableExtra(SearchActivity.SORT_MODE_KEY);
        sort_by_spinner.setSelection(sortAdapter.getPosition(mode));

        SearchActivity.FilterMode ratingMode = (SearchActivity.FilterMode)
                getIntent().getSerializableExtra(
                        SearchActivity.RATING_MODE_KEY);
        rating_spinner.setSelection(ratingModeAdapter.getPosition(ratingMode));

        int ratingVal = getIntent().getIntExtra(SearchActivity.RATING_VAL_KEY,
                1);
        rating_ratingbar.setRating(ratingVal);

        SearchActivity.FilterMode priceMode = (SearchActivity.FilterMode)
                getIntent().getSerializableExtra(
                        SearchActivity.PRICE_MODE_KEY);
        price_spinner.setSelection(priceModeAdapter.getPosition(priceMode));

        int priceVal = getIntent().getIntExtra(SearchActivity.PRICE_VAL_KEY, 1);
        price_ratingbar.setRating(priceVal);

        Button cancel = (Button) findViewById(R.id.filter_cancel);
        Button apply = (Button) findViewById(R.id.filter_apply);

        //TODO:THE RATING BARS & FETCH DATA FROM THE FILTER & GET CORRECT
        // ALIGNMENT
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.SortMode sortMode = (SearchActivity.SortMode)
                        sort_by_spinner.getSelectedItem();
                SearchActivity.FilterMode ratingMode = (SearchActivity
                        .FilterMode) rating_spinner.getSelectedItem();
                int ratingVal = (int) rating_ratingbar.getRating();
                SearchActivity.FilterMode priceMode = (SearchActivity
                        .FilterMode) price_spinner.getSelectedItem();
                int priceVal = (int) price_ratingbar.getRating();

                Intent result = new Intent();
                result.putExtra(SearchActivity.SORT_MODE_KEY, sortMode);
                result.putExtra(SearchActivity.RATING_MODE_KEY, ratingMode);
                result.putExtra(SearchActivity.RATING_VAL_KEY, ratingVal);
                result.putExtra(SearchActivity.PRICE_MODE_KEY, priceMode);
                result.putExtra(SearchActivity.PRICE_VAL_KEY, priceVal);
                setResult(RESULT_OK, result);
                finish();
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
