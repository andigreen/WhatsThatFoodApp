package com.wtf.whatsthatfoodapp.memory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

public class EditMemoryActivity extends BasicActivity {

    public static final String MEMORY_KEY = "memory";

    private static final String TAG = EditMemoryActivity.class.getSimpleName();

    private MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());
    private Memory memory;
    private MemoryFormFragment form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memory);
        memory = getIntent().getParcelableExtra(MEMORY_KEY);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(
                R.id.edit_memory_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load photo view
        ImageView imageView = (ImageView) findViewById(R.id
                .edit_memory_photo);
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(dao.getPhotoRef(memory))
                .centerCrop()
                .into(imageView);

        // Set up form fragment
        form = MemoryFormFragment.newInstance(memory);
        getFragmentManager().beginTransaction().add(R.id.edit_memory_form,
                form).commit();
    }

    private boolean saveMemory() {
        if (form.validateAndSaveInto(memory)) {
            dao.writeMemory(memory);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_memory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Back to collage
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;

            // Start location picker
            case R.id.edit_memory_getloc:
                form.createPlacePicker();
                return true;

            // Save new memory
            case R.id.edit_memory_save:
                if (saveMemory()) {
                    Intent result = new Intent();
                    result.putExtra(MEMORY_KEY, memory);
                    setResult(RESULT_OK, result);
                    finish();
                }
                return true;
        }

        // Other options not handled
        return super.onOptionsItemSelected(item);
    }

}
