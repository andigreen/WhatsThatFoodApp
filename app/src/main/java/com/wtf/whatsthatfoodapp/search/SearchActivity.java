package com.wtf.whatsthatfoodapp.search;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private Map<String, Memory> memories;
    private SearchTable searchTable;
    private MemoryDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Cache memories in a map
        memories = new HashMap<>();
        dao = new MemoryDao(AuthUtils.getUserUid());
        dao.getMemoriesRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Memory m = dataSnapshot.getValue(Memory.class);
                memories.put(m.getKey(), m);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        final List<Memory> results = new ArrayList<>();
        final ArrayAdapter<Memory> resultsAdapter = new ResultAdapter(this,
                results);

        ListView listView = (ListView) findViewById(R.id.search_results);
        listView.setAdapter(resultsAdapter);

        // Initialize search table (pre-populate index)
        searchTable = new SearchTable(this);

        final EditText searchQuery = (EditText) findViewById(R.id.search_query);
        searchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                results.clear();

                Cursor cursor = searchTable.query(s.toString());
                if (cursor == null) {
                    resultsAdapter.notifyDataSetChanged();
                    return;
                }

                int col = cursor.getColumnIndex(SearchTable.COL_KEY);
                cursor.moveToFirst();
                do {
                    Memory m = memories.get(cursor.getString(col));
                    if (m != null) results.add(m);
                } while (cursor.moveToNext());
                cursor.close();
                resultsAdapter.notifyDataSetChanged();
            }
        });
    }

    class ResultAdapter extends ArrayAdapter<Memory> {
        ResultAdapter(Context context, List<Memory> memories) {
            super(context, 0, memories);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup
                parent) {
            Memory model = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.memory_list_item, parent, false);
            }

            TextView viewTitle = (TextView) convertView.findViewById(R.id
                    .memory_list_item_title);
            TextView viewLoc = (TextView) convertView.findViewById(R.id
                    .memory_list_item_loc);
            ImageView viewImage = (ImageView) convertView.findViewById(R.id
                    .memory_list_item_image);
            final ProgressBar progress = (ProgressBar) convertView
                    .findViewById(R.id.memory_list_item_progress);

            viewTitle.setText(model.getTitle());
            viewLoc.setText(model.getLoc());

            // Load photo into view
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(dao.getPhotoRef(model))
                    .listener(new RequestListener<StorageReference,
                            GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e,
                                StorageReference model, Target<GlideDrawable>
                                target, boolean isFirstResource) {
                            progress.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable
                                resource, StorageReference model,
                                Target<GlideDrawable> target, boolean
                                isFromMemoryCache, boolean isFirstResource) {
                            progress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .centerCrop()
                    .into(viewImage);

            return convertView;
        }
    }

}
