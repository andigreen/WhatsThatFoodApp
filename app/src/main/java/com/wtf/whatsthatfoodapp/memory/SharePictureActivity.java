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

import com.wtf.whatsthatfoodapp.R;


public class SharePictureActivity extends AppCompatActivity {

    private static int PHOTO_ID = 101;
    Bitmap picture;
    Uri pictureUri;
    ImageView thumbnail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_picture);

        thumbnail = (ImageView) findViewById(R.id.share_picture_thumbnail);
        Button cameraButton = (Button) findViewById(R.id.share_picture_camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // we want to start an intent to get a picture!
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PHOTO_ID);
            }
        });


        Button sharePicture = (Button) findViewById(R.id.share_picture_share_button);
        sharePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (picture == null) {
                    Toast.makeText(SharePictureActivity.this, "Please take a valid picture", Toast.LENGTH_LONG);
                } else {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, pictureUri);
                    startActivity(Intent.createChooser(intent, "Share picture with..."));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PHOTO_ID) {
            if (resultCode == RESULT_OK) {
                // we got a picture back
                this.showPicture(intent);
            }
        }
    }

    private void showPicture(Intent intent) {
        Bundle intentExtras = intent.getExtras();
        picture = (Bitmap)intentExtras.get("data");
        pictureUri = intent.getData();

        if (picture != null) {
            thumbnail.setImageBitmap(picture);
        }
    }
}
