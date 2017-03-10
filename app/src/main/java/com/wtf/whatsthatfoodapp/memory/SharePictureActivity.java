package com.wtf.whatsthatfoodapp.memory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;
import com.wtf.whatsthatfoodapp.auth.SettingsActivity;


public class SharePictureActivity extends AppCompatActivity {

    Uri pictureUri;
    ImageView thumbnail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_picture);

        Memory memory = getIntent().getParcelableExtra("memory");
        thumbnail = (ImageView) findViewById(R.id.share_picture_thumbnail);

        MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());
        dao.getPhotoRef(memory).getDownloadUrl().addOnSuccessListener(
                new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(SharePictureActivity.this)
                                .load(uri)
                                .centerCrop()
                                .dontAnimate() // required by CircleImageView
                                .into(thumbnail);
                        pictureUri = uri;
                    }
                });


        Button btn_share=(Button)findViewById(R.id.share_picture_share_button);
        btn_share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shareIt();
            }
        });
    }

    private void shareIt() {
        //sharing implementation here
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/jpeg");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
        startActivity(Intent.createChooser(sharingIntent, "Share picture with..."));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

}
