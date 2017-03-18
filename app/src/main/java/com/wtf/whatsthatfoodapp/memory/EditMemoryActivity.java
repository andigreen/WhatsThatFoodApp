package com.wtf.whatsthatfoodapp.memory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.notification.AlarmReceiver;

public class EditMemoryActivity extends BasicActivity {

    public static final String MEMORY_KEY = "memory";
    public static final String NOTIFICATION = "notification";

    private static final String TAG = EditMemoryActivity.class.getSimpleName();

    private MemoryDao dao;
    private Memory memory;
    private MemoryFormFragment form;
    private boolean fromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memory);
        dao = new MemoryDao(this);
        memory = getIntent().getParcelableExtra(MEMORY_KEY);
        fromNotification = getIntent().getBooleanExtra(NOTIFICATION,false);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(
                R.id.edit_memory_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up form fragment
        form = MemoryFormFragment.newInstance(memory,
                new MemoryFormFragment.ImageListener() {
                    @Override
                    public void onImageReady(ImageView view) {
                        // Load photo view
                        dao.loadImage(memory)
                                .centerCrop()
                                .into(view);
                    }
                });
        getFragmentManager().beginTransaction().replace(R.id.edit_memory_form,
                form).commit();
    }

    private boolean saveMemory() {
        if (form.validateAndSaveInto(memory)) {
            memory.setSavedForNextTime(false);
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
    public void onBackPressed() {
        form.confirmDiscard(new MemoryFormFragment.ConfirmDiscardListener() {
            @Override
            public void onPositive() {
                setResult(RESULT_CANCELED);
                EditMemoryActivity.super.onBackPressed();
            }

            @Override
            public void onNegative() {
            }
        });
    }

    private MemoryFormFragment.ConfirmDiscardListener confirmDiscardListener
            = new MemoryFormFragment.ConfirmDiscardListener() {
        @Override
        public void onPositive() {
            setResult(RESULT_CANCELED);
            finish();
        }

        @Override
        public void onNegative() {
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Back to collage
            case android.R.id.home:
                form.confirmDiscard(confirmDiscardListener);
                return true;

            // Start location picker
            case R.id.edit_memory_getloc:
                form.createPlacePicker();
                return true;

            // Save new memory
            case R.id.edit_memory_save:
                if (fromNotification){
                    dao.memoryExists(memory, new MemoryDao.MemoryExistsListener() {
                        @Override
                        public void onResult(boolean exists) {
                            if (!exists) {
                                Toast.makeText(getApplicationContext(),"Memory does not exist", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                            // Do whatever if the memory exists
                            memory.setSavedForNextTime(false);
                            if (saveMemory()) {
                                Intent result = new Intent();
                                result.putExtra(MEMORY_KEY, memory);
                                setResult(RESULT_OK, result);
                                finish();
                            }
                        }
                    });
                } else {
                    memory.setSavedForNextTime(false);
                    if (saveMemory()) {
                        Intent result = new Intent();
                        result.putExtra(MEMORY_KEY, memory);
                        setResult(RESULT_OK, result);
                        finish();
                    }
                }
                return true;
        }

        // Other options not handled
        return super.onOptionsItemSelected(item);
    }

}
