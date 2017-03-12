package com.wtf.whatsthatfoodapp.memory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.auth.LogoutActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.SettingsActivity;
import com.wtf.whatsthatfoodapp.notification.Utils2;
import com.wtf.whatsthatfoodapp.notification.ViewNotificationsActivity;
import com.wtf.whatsthatfoodapp.search.SearchActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.wtf.whatsthatfoodapp.memory.CreateMemoryActivity.PREFS;

public class CollageActivity extends BasicActivity {

    private static final String TAG = CollageActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_GALLERY = 4843;
    private static final int REQUEST_IMAGE_CAMERA = 9924;
    public static final String REMINDERS_COUNT ="REMINDERS_COUNT";

    private Uri imageUri;
    private FloatingActionsMenu createMenu;
    private MemoryDao dao;
    private ActionBarDrawerToggle drawerToggle;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_collage);
        drawerToggle = setupDrawerToggle();

        if (BasicActivity.getProvider().equals("Google")) {
            App app = (App) getApplicationContext();
            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // [END config_signin]
            app.setGso(gso);
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /*
                    OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, app.getGso())
                    .build();

            app.setClient(mGoogleApiClient);
            app.getClient().connect();
        }

        // Set up list and adapter
        dao = new MemoryDao(this);
        ListAdapter collageListAdapter = new MemoryAdapter(this, Memory.class,
                dao.getMemoriesRef().orderByChild(Memory.TS_KEY_NEWEST),
                dao);

        final ListView collageList = (ListView) findViewById(R.id.collage_list);
        collageList.setAdapter(collageListAdapter);

        collageList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        Memory memory = (Memory) collageList
                                .getItemAtPosition(position);
                        Intent viewMemory = new Intent(view.getContext(),
                                ViewMemoryActivity.class);
                        viewMemory.putExtra(ViewMemoryActivity.MEMORY_KEY,
                                memory);
                        startActivity(viewMemory);
                    }
                });

        // Set up buttons
        createMenu = (FloatingActionsMenu) findViewById(
                R.id.collage_create_menu);
        FloatingActionButton cameraButton = (FloatingActionButton) findViewById(
                R.id.collage_create_camera);
        FloatingActionButton galleryButton = (FloatingActionButton)
                findViewById(R.id.collage_create_gallery);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageFromCamera();
            }
        });
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageFromGallery();
            }
        });
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }


    /**
     * Opens the native camera UI to get an image.
     */
    private void imageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) return;

        @SuppressLint("SimpleDateFormat")
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile;
        try {
            imageFile = File.createTempFile("WTF_" + ts, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "Could not create image file.");
            return;
        }

        imageUri = FileProvider.getUriForFile(this,
                "com.wtf.whatsthatfoodapp.fileprovider", imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
    }

    /**
     * Opens the native image gallery to get an image.
     */
    private void imageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, null),
                REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        // Send image URI from camera to CreateMemoryActivity
        if (requestCode == REQUEST_IMAGE_CAMERA && resultCode == RESULT_OK) {
            Intent createMemory = new Intent(this, CreateMemoryActivity.class);
            createMemory.putExtra(CreateMemoryActivity.IMAGE_URI_KEY, imageUri);
            startActivity(createMemory);
        }

        // Send image URI from gallery to CreateMemoryActivity
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            Intent createMemory = new Intent(this, CreateMemoryActivity.class);
            createMemory.putExtra(CreateMemoryActivity.IMAGE_URI_KEY,
                    data.getData());
            startActivity(createMemory);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.main_menu, menu);

        // Update LayerDrawable's BadgeDrawable
        MenuItem item = menu.findItem(R.id.notifications_icon);
        LayerDrawable icon = (LayerDrawable) item.getIcon();
        SharedPreferences sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Utils2.setBadgeCount(this, icon, sp.getInt(REMINDERS_COUNT,0));
        return true;
    }
    public void updateRemindersIcon(){
        // Update LayerDrawable's BadgeDrawable
        if (menu != null){
            MenuItem item = menu.findItem(R.id.notifications_icon);
            LayerDrawable icon = (LayerDrawable) item.getIcon();
            SharedPreferences sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            Utils2.setBadgeCount(this, icon, sp.getInt(REMINDERS_COUNT,0));

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int item_id = item.getItemId();
        if (item_id == R.id.settings ){
            viewSettings();
            return true;
        }
        else if (item_id == R.id.logout){
            Intent intent = new Intent(this, LogoutActivity.class);
            startActivity(intent);
            return true;
        }
        else if (item_id == R.id.collage_search){
            Intent searchIntent = new Intent(this, SearchActivity.class);
            startActivity(searchIntent);
            return true;
        }
        else if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        } else if (item_id == R.id.notifications_icon){
            Intent notificationsIntent = new Intent(this,ViewNotificationsActivity.class);
            startActivity(notificationsIntent);
            return true;
        }
        //default
        return super.onOptionsItemSelected(item);

    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
    }
    // [END on_start_add_listener]

    @Override
    protected void onResume() {
        super.onResume();
        updateRemindersIcon();
        createMenu.collapseImmediately();
    }

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
    }
    // [END on_stop_remove_listener]

    private void viewSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.",
                Toast.LENGTH_SHORT).show();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this,(DrawerLayout)findViewById(R.id.activity_collage),
                (Toolbar) findViewById(R.id.toolbar), R.string.drawer_open,
                 R.string.drawer_close);
    }

}

