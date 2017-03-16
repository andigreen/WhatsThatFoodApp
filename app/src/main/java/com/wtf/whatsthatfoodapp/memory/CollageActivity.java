package com.wtf.whatsthatfoodapp.memory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.LayerDrawable;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.BasicActivity;
import com.wtf.whatsthatfoodapp.Manifest;
import com.wtf.whatsthatfoodapp.PairsMemoryGameActivity;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;
import com.wtf.whatsthatfoodapp.auth.LogoutActivity;
import com.wtf.whatsthatfoodapp.R;
import com.wtf.whatsthatfoodapp.auth.SettingsActivity;
import com.wtf.whatsthatfoodapp.notification.ViewNotificationsActivity;
import com.wtf.whatsthatfoodapp.search.SearchActivity;
import com.wtf.whatsthatfoodapp.user.UserSettings;
import com.wtf.whatsthatfoodapp.user.UserSettingsDao;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.wtf.whatsthatfoodapp.memory.CreateMemoryActivity.PREFS;

public class CollageActivity extends BasicActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    private static final String TAG = CollageActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_GALLERY = 4843;
    private static final int REQUEST_IMAGE_CAMERA = 9924;
    public static final String REMINDERS_COUNT = "REMINDERS_COUNT";
    private static final int GALLERY = 1;
    public static final int REQUEST_PERMISSION = 123;

    private Uri imageUri;
    private FloatingActionsMenu createMenu;
    private MemoryDao dao;
    private ActionBarDrawerToggle drawerToggle;
    private CircleImageView photo_button;
    private Menu menu;
    private UserSettingsDao userDao;
    private EditText nameField;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        if (Build.VERSION.SDK_INT >= 23){
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA},REQUEST_PERMISSION);
        }

                // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        View nav_header = nav_view.getHeaderView(0);

        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

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

        photo_button = (CircleImageView) (nav_header.findViewById(
                R.id.profile_photo));
        photo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"),
                        GALLERY);
            }
        });

//        Menu nav_menu = nav_view.getMenu();
//        DrawerLayout drawer = (DrawerLayout) findViewById(
//                R.id.activity_collage);
        final TextView nav_name = (TextView) nav_view.getHeaderView(0)
                .findViewById(R.id.profile_name);
        nav_view.setNavigationItemSelectedListener(this);
        ValueEventListener nameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserSettings userInfo = dataSnapshot.getValue(
                        UserSettings.class);
                nav_name.setText(userInfo.getUsername());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        userDao = new UserSettingsDao(AuthUtils.getUserUid());
        userDao.getUserInfoRef().addValueEventListener(nameListener);

        userDao.getPhotoRef().getDownloadUrl().addOnSuccessListener(
                new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(CollageActivity.this)
                                .load(uri)
                                .centerCrop()
                                .dontAnimate() // required by CircleImageView
                                .into(photo_button);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Glide.with(CollageActivity.this)
                        .load(user.getPhotoUrl())
                        .centerCrop()
                        .dontAnimate() // required by CircleImageView
                        .into(photo_button);
            }
        });


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

        // require photo from GALLERY
        if (requestCode == GALLERY && resultCode != 0) {
            Uri photoUri = data.getData();
            Glide.with(this).load(photoUri).dontAnimate().into(photo_button);

            UploadTask uploadTask = userDao.getPhotoRef().putFile(photoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failed to upload profile photo for user "
                            + AuthUtils.getUserUid());
                    Toast error = Toast.makeText(getApplicationContext(),
                            "Photo upload failed. We'll try again later.",
                            Toast.LENGTH_SHORT);
                    error.show();
                }
            });
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.main_menu, menu);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final View v = findViewById(R.id.collage_search);

                if (v != null){
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Intent gameIntent = new Intent(getApplicationContext(), PairsMemoryGameActivity.class);
                            startActivity(gameIntent);
                            return true;
                        }
                    });
                }
            }
        });

        // Update LayerDrawable's BadgeDrawable
//        MenuItem item = menu.findItem(R.id.nav_notifications);
//        LayerDrawable icon = (LayerDrawable) item.getIcon();
//        SharedPreferences sp = getSharedPreferences(PREFS,
//                Context.MODE_PRIVATE);
//        Utils2.setBadgeCount(this, icon, sp.getInt(REMINDERS_COUNT, 0));
        return true;
    }

    public void updateRemindersIcon() {
        // Update LayerDrawable's BadgeDrawable
//        if (menu != null) {
//            MenuItem item = menu.findItem(R.id.nav_notifications);
//            LayerDrawable icon = (LayerDrawable) item.getIcon();
//            SharedPreferences sp = getSharedPreferences(PREFS,
//                    Context.MODE_PRIVATE);
//            Utils2.setBadgeCount(this, icon, sp.getInt(REMINDERS_COUNT, 0));
//
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int item_id = item.getItemId();
        if (item_id == R.id.collage_search) {
            Intent searchIntent = new Intent(this, SearchActivity.class);
            startActivity(searchIntent);
            return true;
        } else if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item_id == R.id.nav_notifications) {
            Intent notificationsIntent = new Intent(this,
                    ViewNotificationsActivity.class);
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
        // Add value event listener to the post
        // [START post_value_event_listener]
        /*ValueEventListener userInfoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserSettings userInfo = dataSnapshot.getValue(
                        UserSettings.class);
                nameField.setText(userInfo.getUsername());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadData:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(CollageActivity.this, "Failed to load data.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        userDao.getUserInfoRef().addListenerForSingleValueEvent
        (userInfoListener);*/
        // [END post_value_event_listener]
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
        // NOTE: Make sure you pass in a valid toolbar reference.
        // ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this,
                (DrawerLayout) findViewById(R.id.activity_collage),
                (Toolbar) findViewById(R.id.toolbar), R.string.drawer_open,
                R.string.drawer_close);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_name:
                changeNameDialog();
                break;
            case R.id.nav_notifications:
                Intent notificationsIntent = new Intent(this,
                        ViewNotificationsActivity.class);
                startActivity(notificationsIntent);
                return true;
            case R.id.nav_logout:
                Intent intent = new Intent(this, LogoutActivity.class);
                startActivity(intent);
                return true;
            case R.id.nav_settings:
                viewSettings();
        }

        return false;
    }

    /**
     * Displays a dialog allowing the user to change their profile name.
     */
    private void changeNameDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_change_name, null);
        final EditText nameEdit = (EditText) v.findViewById(
                R.id.change_name_text);

        DatabaseReference userRef = userDao.getUserInfoRef();
        userRef.child("username").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        nameEdit.setText(dataSnapshot.getValue().toString());
                        nameEdit.setSelection(nameEdit.length());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        new AlertDialog.Builder(this)
                .setTitle("Change name to")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userDao.getUserInfoRef().child("username").setValue(
                                nameEdit.getText().toString());
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            Intent intent = new Intent(this, LogoutActivity.class);
            startActivity(intent);
        }
        else { Toast.makeText(getBaseContext(), "Tap back button twice in order to exit", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }
}

