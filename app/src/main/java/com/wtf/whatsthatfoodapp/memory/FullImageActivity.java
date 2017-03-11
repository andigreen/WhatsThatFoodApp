package com.wtf.whatsthatfoodapp.memory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

import uk.co.senab.photoview.PhotoViewAttacher;

public class FullImageActivity extends AppCompatActivity {

    public static final String MEMORY_KEY = "memory";

    private PhotoViewAttacher attacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        MemoryDao dao = new MemoryDao(this);
        Memory memory = getIntent().getParcelableExtra(MEMORY_KEY);

        ImageView imageView = (ImageView) findViewById(R.id.full_image_view);
        Glide.with(FullImageActivity.this)
                .using(new FirebaseImageLoader())
                .load(dao.getPhotoRef(memory))
                .into(imageView);

        attacher = new PhotoViewAttacher(imageView);
        attacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
        attacher.setOnViewTapListener(
                new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float x, float y) {
                        FullImageActivity.this.finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (attacher != null) attacher.cleanup();
    }
}
