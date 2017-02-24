package com.wtf.whatsthatfoodapp.memory;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.auth.BasicActivity;
import com.wtf.whatsthatfoodapp.auth.LogoutActivity;
import com.wtf.whatsthatfoodapp.auth.MainActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.search.SearchActivity;
import com.wtf.whatsthatfoodapp.search.SearchTable;

public class CollageActivity extends BasicActivity {

    private static final String TAG = CollageActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String signInMethod;
    private GoogleApiClient mGoogleApiClient;
    private AppEventsLogger logger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        sharedPrefs = getSharedPreferences(PREFS_NAME,0);
        signInMethod = sharedPrefs.getString("signInMethod","default");
        Log.d(TAG, signInMethod);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.collage_toolbar);
        setSupportActionBar(toolbar);

        App app = (App)getApplicationContext();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, app.getGso())
                .build();

        app.setClient(mGoogleApiClient);
        app.getClient().connect();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    return;
                }
                // ...
            }
        };

        if(BasicActivity.getProvider().equals("Facebook")){
            logger = AppEventsLogger.newLogger(this);
            logger.logEvent("User logged in with Facebook");

        }

        if(BasicActivity.getProvider().equals("Google")){
            mGoogleApiClient = app.getClient();
        }

        // Set up list and adapter
        MemoryDao dao = new MemoryDao(mAuth.getCurrentUser().getUid());
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
                Intent intent = new Intent(this, LogoutActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            case R.id.collage_search:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                return true;
        }

        // Other options not handled
        return super.onOptionsItemSelected(item);
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }
    // [END on_stop_remove_listener]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
