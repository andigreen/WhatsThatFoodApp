package com.wtf.whatsthatfoodapp.memory;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

import static com.wtf.whatsthatfoodapp.R.layout.image_popup;

/**
 * Created by andig on 3/8/2017.
 */

public class FullImageActivity extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Memory memory = getIntent().getParcelableExtra("memory_key");
        setContentView(image_popup);


        final DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());
        dao.getPhotoRef(memory).getDownloadUrl().addOnSuccessListener(
                new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(FullImageActivity.this)
                                .load(uri)
                                .centerCrop()
                                .dontAnimate() // required by CircleImageView
                                .into((ImageView) findViewById(R.id.image_popup));
                    }
                });
    }
}
