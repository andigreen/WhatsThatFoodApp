package com.wtf.whatsthatfoodapp.memory;

import android.app.Dialog;
import android.app.Fragment;
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
import android.widget.ImageView;
import android.widget.RatingBar;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.wtf.whatsthatfoodapp.R;

import static android.app.Activity.RESULT_OK;

public class MemoryFormFragment extends Fragment {
    public static final String ARG_MEMORY = "memory";

    private static final String TAG = MemoryFormFragment.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 200;

    private EditText titleText;
    private EditText locText;
    private EditText descText;
    private RatingBar ratingRating;
    private RatingBar priceRating;
    private CheckBox saveFNTCheck;

    private TextInputLayout titleWrapper;
    private TextInputLayout locWrapper;

    private Dialog imageDialog;

    public MemoryFormFragment() {
    }

    public static MemoryFormFragment newInstance(Memory memory) {
        MemoryFormFragment fragment = new MemoryFormFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMORY, memory);
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
        } else {
            Log.e(TAG, "MemoryFormFragment created without a Memory!");
            return;
        }

        titleText = (EditText) a.findViewById(R.id.create_memory_title);
        locText = (EditText) a.findViewById(R.id.create_memory_loc);
        descText = (EditText) a.findViewById(R.id.create_memory_description);
        ratingRating = (RatingBar) a.findViewById(R.id.create_rating_bar);
        priceRating = (RatingBar) a.findViewById(R.id.create_price_rating);
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
        titleText.addTextChangedListener(
                new ErrorClearTextWatcher(titleWrapper));
        locText.addTextChangedListener(new ErrorClearTextWatcher(locWrapper));

        a.findViewById(R.id.pickerButton).setOnClickListener
                (new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createPlacePicker();
                    }
                });

    }

    public boolean saveToMemory(Memory memory) {
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

    public void expandImage(View v) {
        imageDialog = new Dialog(getActivity());

        imageDialog.setContentView(
                getActivity().getLayoutInflater().inflate(R.layout
                        .image_popup, null));

        ImageView imageView = (ImageView) imageDialog.findViewById(
                R.id.image_popup);
//        try {
//            Bitmap bitmapImage = MediaStore.Images.Media.getBitmap(
//                    getActivity().getContentResolver(), imageUri);
//            imageView.setImageBitmap(bitmapImage);
//            imageDialog.show();
//        } catch (IOException e) {
//            Log.d(TAG, "IOEXCEPTION : photoUri");
//        }
    }

    public void closeImage(View v) {
        if (imageDialog != null) {
            imageDialog.dismiss();
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
