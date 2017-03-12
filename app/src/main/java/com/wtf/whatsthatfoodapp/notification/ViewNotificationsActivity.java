package com.wtf.whatsthatfoodapp.notification;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryAdapter;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;
import com.wtf.whatsthatfoodapp.memory.ViewMemoryActivity;

import static com.wtf.whatsthatfoodapp.memory.CreateMemoryActivity.PREFS;

public class ViewNotificationsActivity extends BasicActivity {
    private static final String TAG = ViewNotificationsActivity.class.getSimpleName();

    private MemoryDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Set up list and adapter
        dao = new MemoryDao(this);
        ListAdapter notificationsListAdapter = new MemoryAdapter(this, Memory.class,
                dao.getMemoriesRef().orderByChild(Memory.SAVED_FNT).equalTo(true),
                dao);

        final ListView notificationsList = (ListView) findViewById(R.id.notifications_list);
        notificationsList.setAdapter(notificationsListAdapter);

        notificationsList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Memory memory = (Memory) notificationsList
                                .getItemAtPosition(position);
                        Intent viewMemory = new Intent(view.getContext(),
                                ViewMemoryActivity.class);
                        viewMemory.putExtra(ViewMemoryActivity.MEMORY_KEY,
                                memory);
                        startActivity(viewMemory);
                    }
                });
    }
}
