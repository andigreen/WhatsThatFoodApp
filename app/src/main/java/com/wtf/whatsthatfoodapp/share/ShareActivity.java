package com.wtf.whatsthatfoodapp.share;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

public class ShareActivity extends AppCompatActivity
        implements View.OnClickListener {

    public static final String MEMORY_KEY = "memory";

    private static final String TAG = ShareActivity.class.getSimpleName();

    MemoryDao dao;
    Memory memory;
    ImageView thumbnail;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_picture);

        memory = getIntent().getParcelableExtra(MEMORY_KEY);
        dao = new MemoryDao(this);

        thumbnail = (ImageView) findViewById(R.id.share_picture_thumbnail);
        dao.loadImage(memory).into(thumbnail);

        findViewById(R.id.share_picture_share_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Since loading the image is expensive, don't let the user cancel
        // the corresponding progress dialog. This will discourage the user
        // from carelessly clicking "Share".
        progress = new ProgressDialog(ShareActivity.this);
        progress.setTitle(R.string.dialog_share_title);
        progress.setMessage(getString(R.string.dialog_share_message));
        progress.setCancelable(false);
        progress.show();

        dao.getLocalImageUri(memory, new LocalUriShare());
    }

    class LocalUriShare implements MemoryDao.LocalImageUriListener {
        @Override
        public void onSuccess(Uri uri) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");

            progress.dismiss();
            startActivity(Intent.createChooser(shareIntent,
                    "Send image"));
        }

        @Override
        public void onFailure() {
            Log.w(TAG, "Could not obtain local image uri.");
        }
    }

}
