package com.wtf.whatsthatfoodapp.memory;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.wtf.whatsthatfoodapp.R;

import static com.wtf.whatsthatfoodapp.R.layout.image_popup;

/**
 * Created by andig on 3/8/2017.
 */

public class FullImageActivity extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(image_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

    }
}
