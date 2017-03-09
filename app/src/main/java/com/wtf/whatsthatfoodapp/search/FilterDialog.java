package com.wtf.whatsthatfoodapp.search;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.Spinner;

import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.search.SearchActivity.FilterMode;
import com.wtf.whatsthatfoodapp.search.SearchActivity.SortMode;

public class FilterDialog extends DialogFragment {

    private static final String SORT_MODE = "sortMode";
    private static final String RATING_MODE = "ratingMode";
    private static final String RATING_VAL = "ratingVal";
    private static final String PRICE_MODE = "priceMode";
    private static final String PRICE_VAL = "priceVal";

    private Spinner sortSpinner;
    private Spinner ratingSpinner;
    private RatingBar ratingRating;
    private Spinner priceSpinner;
    private RatingBar priceRating;

    FilterDialogListener listener;

    static FilterDialog newInstance(SortMode sortMode, FilterMode ratingMode,
            int ratingVal, FilterMode priceMode, int priceVal) {
        FilterDialog f = new FilterDialog();

        Bundle args = new Bundle();
        args.putSerializable(SORT_MODE, sortMode);
        args.putSerializable(RATING_MODE, ratingMode);
        args.putInt(RATING_VAL, ratingVal);
        args.putSerializable(PRICE_MODE, priceMode);
        args.putInt(PRICE_VAL, priceVal);

        f.setArguments(args);
        return f;
    }

    interface FilterDialogListener {
        void onApply(SortMode sortMode, FilterMode ratingMode, int ratingVal,
                FilterMode priceMode, int priceVal);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate view and get references to view elements

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_filter, null);

        ratingSpinner = (Spinner) view.findViewById(R.id.rating_spinner);
        ArrayAdapter<FilterMode> ratingAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item,
                FilterMode.values());
        ratingSpinner.setAdapter(ratingAdapter);

        priceSpinner = (Spinner) view.findViewById(R.id.price_spinner);
        ArrayAdapter<FilterMode> priceAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item,
                FilterMode.values());
        priceSpinner.setAdapter(priceAdapter);

        Spinner time_spinner = (Spinner) view.findViewById(R.id.time_spinner);
        time_spinner.setAdapter(ArrayAdapter.createFromResource(getActivity(),
                R.array.filter_time,
                android.R.layout.simple_spinner_dropdown_item));

        sortSpinner = (Spinner) view.findViewById(R.id.sort_by_spinner);
        ArrayAdapter<SortMode> sortAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                SortMode.values());
        sortSpinner.setAdapter(sortAdapter);

        ratingRating = (RatingBar) view.findViewById(R.id.rating_rating_bar);
        priceRating = (RatingBar) view.findViewById(R.id.price_rating_bar);

        // Initialize values

        SortMode sortMode = (SortMode) getArguments().getSerializable(
                SORT_MODE);
        FilterMode ratingMode = (FilterMode) getArguments().getSerializable(
                RATING_MODE);
        int ratingVal = getArguments().getInt(RATING_VAL);
        FilterMode priceMode = (FilterMode) getArguments().getSerializable(
                PRICE_MODE);
        int priceVal = getArguments().getInt(PRICE_VAL);

        sortSpinner.setSelection(sortAdapter.getPosition(sortMode));
        ratingSpinner.setSelection(ratingAdapter.getPosition(ratingMode));
        ratingRating.setRating(ratingVal);
        priceSpinner.setSelection(priceAdapter.getPosition(priceMode));
        priceRating.setRating(priceVal);

        // Build dialog

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("Search filters")
                .setPositiveButton("Apply", applyClickListener)
                .setNegativeButton("Cancel", cancelClickListener);
        return builder.create();
    }

    private DialogInterface.OnClickListener applyClickListener = new
            DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SortMode sortMode = (SortMode) sortSpinner
                            .getSelectedItem();
                    FilterMode ratingMode = (FilterMode) ratingSpinner
                            .getSelectedItem();
                    int ratingVal = (int) ratingRating.getRating();
                    FilterMode priceMode = (FilterMode) priceSpinner
                            .getSelectedItem();
                    int priceVal = (int) priceRating.getRating();

                    listener.onApply(sortMode, ratingMode, ratingVal, priceMode,
                            priceVal);
                }
            };

    private DialogInterface.OnClickListener cancelClickListener = new
            DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FilterDialog.this.getDialog().cancel();
                }
            };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (FilterDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement "
                    + FilterDialogListener.class.getSimpleName());
        }
    }

}
