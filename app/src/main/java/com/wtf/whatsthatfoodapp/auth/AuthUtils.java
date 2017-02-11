package com.wtf.whatsthatfoodapp.auth;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthUtils {

    /**
     * Returns the uid of the current user, or throws an exception if there
     * is no authenticated user.
     */
    @NonNull
    public static String getUserUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) throw new RuntimeException("Tried to get current " +
                "user while not authenticated!");
        return user.getUid();
    }

}
