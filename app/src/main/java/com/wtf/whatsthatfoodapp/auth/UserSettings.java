package com.wtf.whatsthatfoodapp.auth;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

/**
 * Created by peiranli on 2/20/17.
 */

public class UserSettings {
    private String email;
    private String username;

    private boolean toAlbum;

    private String Uid;

    private String key;

    public UserSettings() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public UserSettings(String email,String username, String Uid) {
        this.email = email;
        this.username = username;
        this.toAlbum = true;
        this.Uid = Uid;
        this.key = this.Uid;
    }

    public String getUid(){
        return this.Uid;
    }

    public void setUid(String uid){
        this.Uid = uid;
        this.key = this.Uid;
    }

    public String getKey(){
        return this.key;
    }
    public String getEmail(){
        return this.email;
    }

    public String getUsername() {
        return this.username;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public boolean isToAlbum() {
        return this.toAlbum;
    }

    public void setToAlbum(boolean toAlbum) {
        this.toAlbum = toAlbum;
    }



}
