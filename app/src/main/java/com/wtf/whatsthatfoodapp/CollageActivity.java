package com.wtf.whatsthatfoodapp;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryAdapter;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

public class CollageActivity extends BasicActivity {

    private static final String TAG = CollageActivity.class.getSimpleName();
    private static final int REQUEST_CREATE_MEMORY = 1858;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "onCreate: current user is null!");
            return;
        }

        MemoryDao dao = new MemoryDao(user.getUid());
        ListAdapter collageListAdapter = new MemoryAdapter(this, Memory.class,
                R.layout.memory_list_item, dao);

        ListView collageList = (ListView) findViewById(R.id.collage_list);
        collageList.setAdapter(collageListAdapter);

        FloatingActionButton btnCreateMemory = (FloatingActionButton)
                this.findViewById(R.id.create_memory_button);
        btnCreateMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CollageActivity.this,
                        CreateMemoryActivity.class));
            }
        });
    }
}
