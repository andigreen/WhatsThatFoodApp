package com.wtf.whatsthatfoodapp.search;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.FloatingSearchView.OnMenuItemClickListener;
import com.arlib.floatingsearchview.FloatingSearchView.OnQueryChangeListener;
import com.arlib.floatingsearchview.FloatingSearchView.OnSearchListener;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.TextUtil;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;
import com.wtf.whatsthatfoodapp.memory.ViewMemoryActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends BasicActivity
        implements FilterDialog.FilterDialogListener {

    private final static String TAG = SearchActivity.class.getSimpleName();

    private SearchTable searchTable;
    private String query;
    private MemoryDao dao;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private ValueEventListener memoriesListener;
    private List<String> resultKeys;
    private List<Memory> results;
    private ArrayAdapter<Memory> resultsAdapter;

    private TextView noResults;
    private Button clearFilters;
    private FloatingSearchView searchView;

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

        dao = new MemoryDao(this);

        resultKeys = new ArrayList<>();
        results = new ArrayList<>();
        resultsAdapter = new ResultAdapter(this, results);

        noResults = (TextView) findViewById(R.id.search_noresults);
        clearFilters = (Button) findViewById(R.id.search_clearfilters);
        clearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingMode = FilterMode.ANY;
                priceMode = FilterMode.ANY;
                updateFilterButton();
                requery();
            }
        });

        final ListView listView = (ListView) findViewById(R.id.search_results);
        listView.setAdapter(resultsAdapter);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        Memory memory = (Memory) listView
                                .getItemAtPosition(position);
                        Intent viewMemory = new Intent(view.getContext(),
                                ViewMemoryActivity.class);
                        viewMemory.putExtra(ViewMemoryActivity.MEMORY_KEY,
                                memory);
                        startActivity(viewMemory);
                    }
                });

        // Set up searchView
        searchView = (FloatingSearchView) findViewById(
                R.id.search_view);
        searchView.setOnSearchListener(searchListener);
        searchView.setOnQueryChangeListener(queryListener);
        searchView.setOnMenuItemClickListener(menuListener);
        searchView.setOnHomeActionClickListener(
                new FloatingSearchView.OnHomeActionClickListener() {
                    @Override
                    public void onHomeClicked() {
                        finish();
                    }
                });

        // Get search table and listener that updates the results
        searchTable = ((App) getApplication()).getSearchTable();
        memoriesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (String key : resultKeys) {
                    results.add(dataSnapshot.child(key).getValue(
                            Memory.class));
                }
                resultsAdapter.sort(sortMode.getComparator());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        // Set initial query
        Uri data = getIntent().getData();
        if (data != null) {
            query = TextUtil.removeScheme(data);
            searchView.setSearchText(query);
            requery();
        } else {
            query = "";
            searchView.setSearchFocused(true);
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private OnSearchListener searchListener = new OnSearchListener() {
        @Override
        public void onSuggestionClicked(SearchSuggestion
                searchSuggestion) {
        }

        @Override
        public void onSearchAction(String currentQuery) {
            query = currentQuery;
            requery();
        }
    };

    private OnQueryChangeListener queryListener = new OnQueryChangeListener() {
        @Override
        public void onSearchTextChanged(String oldQuery, String
                newQuery) {
            query = newQuery;
            requery();
        }
    };

    private OnMenuItemClickListener menuListener = new
            OnMenuItemClickListener() {
                @Override
                public void onActionMenuItemSelected(MenuItem item) {
                    FilterDialog.newInstance(sortMode, ratingMode, ratingVal,
                            priceMode, priceVal)
                            .show(getFragmentManager(), "FilterDialog");
                }
            };

    @Override
    public void onApply(SortMode sortMode, FilterMode ratingMode, int
            ratingVal, FilterMode priceMode, int priceVal) {
        this.sortMode = sortMode;
        this.ratingMode = ratingMode;
        this.ratingVal = ratingVal;
        this.priceMode = priceMode;
        this.priceVal = priceVal;
        updateFilterButton();
        requery();
    }

    private void updateFilterButton() {
        int resColor = filtersApplied()
                ? R.color.colorAccent : R.color.menu_icon_color;
        searchView.setMenuItemIconColor(ContextCompat.getColor(this, resColor));
    }

    private boolean filtersApplied() {
        return !(ratingMode == FilterMode.ANY && priceMode == FilterMode.ANY);
    }

    private void requery() {
        results.clear();

        searchTable.setRating(ratingMode, ratingVal);
        searchTable.setPrice(priceMode, priceVal);
        resultKeys = searchTable.query(query);
        if (resultKeys.isEmpty()) {
            noResults.setVisibility(View.VISIBLE);
            clearFilters.setVisibility(
                    filtersApplied() ? View.VISIBLE : View.GONE);

            resultsAdapter.notifyDataSetChanged();
            return;
        }
        noResults.setVisibility(View.GONE);
        clearFilters.setVisibility(View.GONE);

        dao.getMemoriesRef().addListenerForSingleValueEvent(memoriesListener);
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
