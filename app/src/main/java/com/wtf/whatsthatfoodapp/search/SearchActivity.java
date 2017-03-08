package com.wtf.whatsthatfoodapp.search;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends BasicActivity {

    public final static String TAG = SearchActivity.class.getSimpleName();
    private Map<String, Memory> memories;
    private SearchTable searchTable;
    private MemoryDao dao;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private List<Memory> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        String sort_mode = getIntent().getStringExtra("sort_mode");

        if(sort_mode == null){
            sort_mode ="Any";
        }
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

        results = new ArrayList<>();
        final ArrayAdapter<Memory> resultsAdapter = new ResultAdapter(this,
                results);

        ListView listView = (ListView) findViewById(R.id.search_results);
        listView.setAdapter(resultsAdapter);

        // Initialize search table (pre-populate index)
        searchTable = new SearchTable(this);

        Button filter = (Button) findViewById(R.id.filter);

        filter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, FilterActivity.class));
            }
        });


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

        switch (sort_mode) {
            case "Highest rating":
                sort_by_rating(false);
                Log.d(TAG, "sort by highest rating");
                break;
            case "Lowest rating":
                sort_by_rating(true);
                Log.d(TAG, "sort by lowest rating");
                break;
            case "Highest price":
                sort_by_price(false);
                Log.d(TAG, "sort by highest price");
                break;
            case "Lowest price":
                sort_by_price(true);
                Log.d(TAG, "sort by lowest price");
                break;
            case "Newest":
                sort_by_time(true);
                Log.d(TAG, "sort by newest");
                break;
            case "Oldest":
                sort_by_time(false);
                Log.d(TAG, "sort by oldest");
                break;
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Search Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    public void sort_by_rating(final boolean at_least){
        Collections.sort(results, new Comparator<Memory>() {
            @Override
            public int compare(Memory lhs, Memory rhs) {
                // -1 - less than, 1 - greater than, 0 - equal
                if(at_least)
                    return lhs.getRate() > rhs.getRate() ? -1 : (lhs.getRate() < rhs.getRate()) ? 1 : 0;

                else
                    return lhs.getRate() > rhs.getRate() ? 1 : (lhs.getRate() < rhs.getRate() ) ? -1 : 0;
            }
        });
    }

    public void sort_by_price(final boolean at_least){
        Collections.sort(results, new Comparator<Memory>() {
            @Override
            public int compare(Memory lhs, Memory rhs) {
                // -1 - less than, 1 - greater than, 0 - equal
                if(at_least) {
                    return lhs.getRate() > rhs.getRate() ? -1 : (lhs.getRate() < rhs.getRate()) ? 1 : 0;
                }else
                    return lhs.getRate() > rhs.getRate() ? 1 : (lhs.getRate() < rhs.getRate()) ? -1 : 0;
            }
        });
    }

    public void sort_by_time(final boolean recent){
        Collections.sort(results, new Comparator<Memory>() {
            @Override
            public int compare(Memory lhs, Memory rhs) {
                // -1 - less than, 1 - greater than, 0 - equal
                if(recent) {
                    return lhs.getTsModified() > rhs.getTsModified() ? 1 : (lhs.getTsModified() > rhs.getTsModified()) ? -1 : 0;
                }else
                    return lhs.getTsModified() > rhs.getTsModified() ? -1 : (lhs.getTsModified() > rhs.getTsModified()) ? 1 : 0;
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
                                    Target<GlideDrawable> target,
                                    boolean isFromMemoryCache, boolean isFirstResource) {
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
