package com.wtf.whatsthatfoodapp.memory;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Memory {

    private String key;
    @NonNull private String title;
    @NonNull private String loc;

    public Memory() {
        title = loc = "";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getLoc() {
        return loc;
    }

    public void setLoc(@NonNull String loc) {
        this.loc = loc;
    }

}
