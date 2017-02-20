package com.wtf.whatsthatfoodapp.auth;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by peiranli on 2/20/17.
 */

public class UserSettingsDAO {

    private static final String TAG = UserSettings.class.getSimpleName();
    private static final String INFO_PATH = "users";

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
}
