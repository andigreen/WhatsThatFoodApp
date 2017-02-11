package com.wtf.whatsthatfoodapp.memory;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wtf.whatsthatfoodapp.auth.BasicActivity;
import com.wtf.whatsthatfoodapp.auth.MainActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.search.SearchTable;

public class CollageActivity extends BasicActivity {

    private static final String TAG = CollageActivity.class.getSimpleName();

    private SearchTable searchTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.collage_toolbar);
        setSupportActionBar(toolbar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "onCreate: current user is null!");
            return;
        }

        // Set up list and adapter
        MemoryDao dao = new MemoryDao(user.getUid());
        ListAdapter collageListAdapter = new MemoryAdapter(this, Memory.class,
                R.layout.memory_list_item,
                dao.getMemoriesRef().orderByChild(Memory.TS_KEY_NEWEST),
                dao);

        ListView collageList = (ListView) findViewById(R.id.collage_list);
        collageList.setAdapter(collageListAdapter);

        // Set up Create Memory button
        FloatingActionButton btnCreateMemory = (FloatingActionButton)
                this.findViewById(R.id.create_memory_button);
        btnCreateMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CollageActivity.this,
                        CreateMemoryActivity.class));
            }
        });

        // Initialize search table (pre-populate index)
        searchTable = new SearchTable(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collage_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.collage_logout:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
        }

        // Other options not handled
        return super.onOptionsItemSelected(item);
    }
}
