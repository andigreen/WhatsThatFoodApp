package com.wtf.whatsthatfoodapp.memory;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.auth.LogoutActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.SettingsActivity;
import com.wtf.whatsthatfoodapp.search.SearchActivity;

public class CollageActivity extends BasicActivity {

    private static final String TAG = CollageActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String signInMethod;
    private GoogleApiClient mGoogleApiClient;
    private AppEventsLogger logger;
    private App app;
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

        app = (App)getApplicationContext();

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
            App app = (App)getApplicationContext();
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // [END config_signin]
            app.setGso(gso);
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, app.getGso())
                    .build();

            app.setClient(mGoogleApiClient);
            app.getClient().connect();
        }

        // Set up list and adapter
        MemoryDao dao = new MemoryDao(mAuth.getCurrentUser().getUid());
        ListAdapter collageListAdapter = new MemoryAdapter(this, Memory.class,
                R.layout.memory_list_item,
                dao.getMemoriesRef().orderByChild(Memory.TS_KEY_NEWEST),
                dao);

        ListView collageList = (ListView) findViewById(R.id.collage_list);
        collageList.setAdapter(collageListAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                viewSettings();
                return true;
            case R.id.logout:
                Intent intent = new Intent(this, LogoutActivity.class);
                startActivity(intent);
                return true;
            case R.id.collage_search:
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void viewSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    public void viewMemory(View v){
        //Intent intent = new Intent(this, ViewMemoryActivity.class);
        //startActivity(intent);
    }

    public void addMemory(View v){
        PopupMenu popupMenu = new PopupMenu(this,v);

        popupMenu.getMenuInflater().inflate(R.menu.collage_create_memory_button_menu,popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item){
                Intent intent = new Intent(getApplicationContext(), CreateMemoryActivity.class);

                if (item.getItemId() == R.id.camera){
                    intent.putExtra("camera",true);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.gallery){
                    intent.putExtra("camera",false);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
