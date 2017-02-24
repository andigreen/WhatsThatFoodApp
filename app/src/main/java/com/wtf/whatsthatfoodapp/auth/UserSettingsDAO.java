package com.wtf.whatsthatfoodapp.auth;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by peiranli on 2/20/17.
 */

public class UserSettingsDAO {

    private static final String TAG = UserSettings.class.getSimpleName();
    private static final String INFO_PATH = "users";
    private static final String PROFILE_PHOTO_PATH = "profilePhoto";

    private String userId;

    public UserSettingsDAO(@NonNull String userId) {
        this.userId = userId;
    }

    public void writeUser(UserSettings user) {
        DatabaseReference db = getUserInfoRef();

        db.setValue(user);

    }

    public DatabaseReference getUserInfoRef(){
        return FirebaseDatabase.getInstance().getReference().child(INFO_PATH).child(this.userId);
    }

    public StorageReference getPhotoRef(UserSettings user) {
        return FirebaseStorage.getInstance()
                .getReference()
                .child(PROFILE_PHOTO_PATH)
                .child(userId)
                .child(user.getKey());
    }
}
