package com.wtf.whatsthatfoodapp.memory;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Memory {

    private static final String TAG = Memory.class.getSimpleName();

    public static final String TS_KEY_NEWEST = "tsCreatedNeg";

    private String key;
    @NonNull private String title;
    @NonNull private String loc;
    // UNIX timestamps
    private long tsCreated;
    private long tsCreatedNeg; // Negative timestamp, so we can sort descending
    private long tsModified;

    public Memory() {
        title = loc = "";
        tsCreated = tsModified = 0L;
    }

    /**
     * Marks this memory as just-created.
     */
    @Exclude
    void markCreated() {
        tsCreated = unixTime();
        tsCreatedNeg = -1 * tsCreated;
    }

    /**
     * Marks this memory as just-modified.
     */
    @Exclude
    void markModified() {
        tsModified = unixTime();
    }



    // Getters setters required for Firebase POJOs, and setters (not required)

    public String getKey() {
        return key;
    }

    // Package-private since key should only be set by the dao
    void setKey(@NonNull String key) {
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

    public long getTsCreated() {
        return tsCreated;
    }

    public long getTsCreatedNeg() {
        return tsCreatedNeg;
    }

    public long getTsModified() {
        return tsModified;
    }



    // Helper methods

    private static long unixTime() {
        return System.currentTimeMillis() / 1000;
    }

}
