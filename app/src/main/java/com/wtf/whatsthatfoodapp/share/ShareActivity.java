package com.wtf.whatsthatfoodapp.share;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

public class ShareActivity extends AppCompatActivity {

    public static final String MEMORY_KEY = "memory";
    public static final int RESULT_FAILED = RESULT_FIRST_USER + 33;

    private static final String TAG = ShareActivity.class.getSimpleName();

    MemoryDao dao;
    Memory memory;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        memory = getIntent().getParcelableExtra(MEMORY_KEY);
        dao = new MemoryDao(this);

        // Since loading the image is expensive, don't let the user cancel
        // the corresponding progress dialog. This will discourage the user
        // from carelessly clicking "Share".
        progress = new ProgressDialog(this);
        progress.setTitle(R.string.share_loading_title);
        progress.setMessage(getString(R.string.share_loading_message));
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
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void onFailure() {
            Log.w(TAG, "Could not obtain local image uri.");
            progress.dismiss();
            setResult(RESULT_FAILED);
            finish();
        }
    }

}
