package com.wtf.whatsthatfoodapp.search;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends BasicActivity {

    public final static String TAG = SearchActivity.class.getSimpleName();
    public final static String SORT_MODE_KEY = "sort_mode";
    public final static String RATING_MODE_KEY = "rating_mode";
    public final static String RATING_VAL_KEY = "rating_val";
    public final static String PRICE_MODE_KEY = "price_mode";
    public final static String PRICE_VAL_KEY = "price_val";
    public final static int FILTER_REQ = 343;

    private Map<String, Memory> memories;
    private SearchTable searchTable;
    private String query;
    private MemoryDao dao;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private List<Memory> results;
    private ArrayAdapter<Memory> resultsAdapter;

    // Sort/filter options
    private SortMode sortMode = SortMode.RATING_HIGH;
    private FilterMode ratingMode = FilterMode.ANY;
    private int ratingVal = 1;
    private FilterMode priceMode = FilterMode.ANY;
    private int priceVal = 1;

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

        results = new ArrayList<>();
        resultsAdapter = new ResultAdapter(this, results);

        ListView listView = (ListView) findViewById(R.id.search_results);
        listView.setAdapter(resultsAdapter);

        // Initialize search table (pre-populate index)
        searchTable = new SearchTable(this);
//        searchTable.setRating(ratingMode, ratingVal);
//        searchTable.setPrice(priceMode, priceVal);
        query = "";

        Button filter = (Button) findViewById(R.id.filter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filterIntent = new Intent(SearchActivity.this,
                        FilterActivity.class);
                filterIntent.putExtra(SORT_MODE_KEY, sortMode);
                filterIntent.putExtra(RATING_MODE_KEY, ratingMode);
                filterIntent.putExtra(RATING_VAL_KEY, ratingVal);
                filterIntent.putExtra(PRICE_MODE_KEY, priceMode);
                filterIntent.putExtra(PRICE_VAL_KEY, priceVal);
                startActivityForResult(filterIntent, FILTER_REQ);
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
                query = s.toString();
                requery();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void requery() {
        results.clear();

        searchTable.setRating(ratingMode, ratingVal);
        searchTable.setPrice(priceMode, priceVal);
        Cursor cursor = searchTable.query(query);
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

        resultsAdapter.sort(sortMode.getComparator());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        if (requestCode == FILTER_REQ && resultCode == RESULT_OK) {
            sortMode = (SortMode) data.getSerializableExtra(SORT_MODE_KEY);

            ratingMode = (FilterMode) data.getSerializableExtra(
                    RATING_MODE_KEY);
            ratingVal = data.getIntExtra(RATING_VAL_KEY, ratingVal);
            priceMode = (FilterMode) data.getSerializableExtra(PRICE_MODE_KEY);
            priceVal = data.getIntExtra(PRICE_VAL_KEY, priceVal);

            requery();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName(
                        "Search Page") // TODO: Define a title for the
                // content shown.
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

    enum FilterMode {
        ANY("Any", ""),
        AT_LEAST("At least", ">="),
        AT_MOST("At most", "<="),;

        private String label;
        private String operator;

        FilterMode(String label, String operator) {
            this.label = label;
            this.operator = operator;
        }

        @Override
        public String toString() {
            return label;
        }

        public String getOperator() {
            return operator;
        }
    }

    enum SortMode {
        RATING_HIGH("Highest rating", new Comparator<Memory>() {
            @Override
            public int compare(Memory o1, Memory o2) {
                return Float.compare(o2.getRate(), o1.getRate());
            }
        }),
        RATING_LOW("Lowest rating", new Comparator<Memory>() {
            @Override
            public int compare(Memory o1, Memory o2) {
                return Float.compare(o1.getRate(), o2.getRate());
            }
        }),
        PRICE_HIGH("Highest price", new Comparator<Memory>() {
            @Override
            public int compare(Memory o1, Memory o2) {
                return Float.compare(o2.getPrice(), o1.getPrice());
            }
        }),
        PRICE_LOW("Lowest price", new Comparator<Memory>() {
            @Override
            public int compare(Memory o1, Memory o2) {
                return Float.compare(o1.getPrice(), o2.getPrice());
            }
        }),
        NEW("Newest", new Comparator<Memory>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Memory o1, Memory o2) {
                return Long.compare(o2.getTsCreated(), o1.getTsCreated());
            }
        }),
        OLD("Oldest", new Comparator<Memory>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Memory o1, Memory o2) {
                return Long.compare(o1.getTsCreated(), o2.getTsCreated());
            }
        }),;

        private String name;
        private Comparator<Memory> comparator;

        SortMode(String name, Comparator<Memory> comparator) {
            this.name = name;
            this.comparator = comparator;
        }

        @Override
        public String toString() {
            return name;
        }

        public Comparator<Memory> getComparator() {
            return comparator;
        }
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
                                boolean isFromMemoryCache, boolean
                                isFirstResource) {
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
