package com.wtf.whatsthatfoodapp.memory;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TableRow;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.wtf.whatsthatfoodapp.R;

import static android.app.Activity.RESULT_OK;

public class MemoryFormFragment extends Fragment {
    public static final String ARG_MEMORY = "memory";
    public static final String SHOW_SFNT = "saveFNT";

    private static final String TAG = MemoryFormFragment.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 200;

    private boolean changesMade = false;

    private EditText titleText;
    private EditText locText;
    private EditText descText;
    private RatingBar ratingRating;
    private RatingBar priceRating;
    private CheckBox saveFNTCheck;
    private boolean saveFNT;

    private TextInputLayout titleWrapper;
    private TextInputLayout locWrapper;

    public MemoryFormFragment() {
    }

    public static MemoryFormFragment newInstance(Memory memory, boolean saveFNT) {
        MemoryFormFragment fragment = new MemoryFormFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMORY, memory);
        args.putBoolean(SHOW_SFNT,saveFNT);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppCompatActivity a = (AppCompatActivity) getActivity();

        super.onCreate(savedInstanceState);
        Memory memory;
        if (getArguments() != null) {
            memory = getArguments().getParcelable(ARG_MEMORY);
            saveFNT = getArguments().getBoolean(SHOW_SFNT);
        } else {
            Log.e(TAG, "MemoryFormFragment created without a Memory!");
            return;
        }

        titleText = (EditText) a.findViewById(R.id.create_memory_title);
        locText = (EditText) a.findViewById(R.id.create_memory_loc);
        descText = (EditText) a.findViewById(R.id.create_memory_description);
        ratingRating = (RatingBar) a.findViewById(R.id.create_rating_bar);
        priceRating = (RatingBar) a.findViewById(R.id.create_price_rating);

        TableRow tableRow = (TableRow) a.findViewById(R.id.saveFNT_fragment);
        if (!saveFNT){
            tableRow.setVisibility(View.GONE);
        }

        saveFNTCheck = (CheckBox) a.findViewById(R.id.saveFNTcheck);

        titleText.setText(memory.getTitle());
        locText.setText(memory.getLoc());
        descText.setText(memory.getDescription());
        ratingRating.setRating(memory.getRate());
        priceRating.setRating(memory.getPrice());
        saveFNTCheck.setChecked(memory.getSavedForNextTime());

        // Clear errors whenever text changes
        titleWrapper = (TextInputLayout) a.findViewById(
                R.id.create_memory_title_wrapper);
        locWrapper = (TextInputLayout) a.findViewById(
                R.id.create_memory_loc_wrapper);
        titleWrapper.setErrorEnabled(false);
        locWrapper.setErrorEnabled(false);
        titleText.addTextChangedListener(
                new ErrorClearTextWatcher(titleWrapper));
        locText.addTextChangedListener(new ErrorClearTextWatcher(locWrapper));

        // Track whenever changes are made
        AnyChangeListener changeListener = new AnyChangeListener();
        titleText.addTextChangedListener(changeListener);
        locText.addTextChangedListener(changeListener);
        descText.addTextChangedListener(changeListener);
        ratingRating.setOnRatingBarChangeListener(changeListener);
        priceRating.setOnRatingBarChangeListener(changeListener);
    }

    public boolean validateAndSaveInto(Memory memory) {
        if (!validateForm()) return false;

        // Write memory fields
        memory.setTitle(titleText.getText().toString());
        memory.setLoc(locText.getText().toString());
        memory.setDescription(descText.getText().toString());
        memory.setTag(descText.getText().toString());
        memory.setRate((int) ratingRating.getRating());
        memory.setPrice((int) priceRating.getRating());
        memory.setSavedForNextTime(saveFNTCheck.isChecked());

        return true;
    }

    /**
     * Callback interface for the "Discard changes?" dialog (see
     * confirmDiscard).
     */
    public interface ConfirmDiscardListener {
        void onPositive();

        void onNegative();
    }

    /**
     * If changes were made to the form fields, shows the user a dialog to
     * confirm whether they would like to discard those changes. Calls the
     * corresponding callbacks of the given {@link ConfirmDiscardListener}.
     * their input. If no changes were made, calls onPositive().
     */
    public void confirmDiscard(final ConfirmDiscardListener listener) {
        if (!changesMade) {
            listener.onPositive();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.memory_form_discard);
        builder.setPositiveButton(R.string.memory_form_discard_pos,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPositive();
                    }
                });
        builder.setNegativeButton(R.string.memory_form_discard_neg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onNegative();
                    }
                });
        builder.create().show();
    }

    /**
     * A simple listener which sets changesMade to true anytime a form
     * element's value is modified.
     */
    private class AnyChangeListener
            implements TextWatcher, RatingBar.OnRatingBarChangeListener {
        @Override
        public void afterTextChanged(Editable s) {
            changesMade = true;
        }

        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating,
                boolean fromUser) {
            changesMade = true;
        }

        // Unused methods

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int
                count) {

        }

    }

    private class ErrorClearTextWatcher implements TextWatcher {
        private TextInputLayout layout;

        ErrorClearTextWatcher(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int
                count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            layout.setErrorEnabled(false);
        }
    }

    /**
     * Returns true if all of the form contents are valid, and also updates
     * the error status of form contents.
     * <p>
     * Currently, the form only requires that title and loc are both nonempty.
     */
    private boolean validateForm() {
        String title = titleText.getText().toString();
        String loc = locText.getText().toString();
        boolean valid = true;

        if (title.isEmpty()) {
            titleWrapper.setError("Please enter a title.");
            valid = false;
        } else titleWrapper.setErrorEnabled(false);

        if (loc.isEmpty()) {
            locWrapper.setError("Please enter a location.");
            valid = false;
        } else locWrapper.setErrorEnabled(false);

        return valid;
    }

    public void createPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(getActivity()),
                    PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(getActivity(), data);
            locText.setText(place.getName());
        }
    }

    /*
     * Boilerplate Fragment lifecycle hooks
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memory_form, container,
                false);
    }

}
