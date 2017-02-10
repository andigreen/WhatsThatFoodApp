package com.wtf.whatsthatfoodapp.memory;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Memory {

    private static final String TAG = Memory.class.getSimpleName();
    private static final String TS_KEY = "ts";

    public static final String TS_CREATED = "tsCreated";
    public static final String TS_MODIFIED = "tsModified";
    public static final long TS_INVALID = -1L;

    private String key;
    @NonNull private String title;
    @NonNull private String loc;
    private Map<String, Object> tsCreated;
    private Map<String, Object> tsModified;

    public Memory() {
        title = loc = "";
        tsCreated = new HashMap<>();
        tsModified = new HashMap<>();
    }

    /**
     * Returns the creation timestamp, in UNIX time. If this memory has not
     * been written to the database before, or if this memory is marked as
     * just-created (using {@link #markCreated()}, then the timestamp will
     * not be available and this method will return TS_INVALID.
     */
    @Exclude
    public long getTimeCreated() {
        Object ts = tsCreated.get(TS_KEY);
        if (ts instanceof Long) return (long) ts;

        Log.e(TAG, "getTimeCreated: timeCreated in an invalid state");
        return TS_INVALID;
    }

    /**
     * Returns the modification timestamp, in UNIX time. If this memory has not
     * been written to the database before, or if this memory is marked as
     * just-modified (using {@link #markModified()}, then the timestamp will
     * not be available and this method will return TS_INVALID.
     */
    @Exclude
    public long getTimeModified() {
        Object ts = tsModified.get(TS_KEY);
        if (ts instanceof Long) return (long) ts;

        Log.e(TAG, "getTimeModified: timeModified in an invalid state");
        return TS_INVALID;
    }

    /**
     * Marks this memory as just-created when written to the database.
     */
    @Exclude
    void markCreated() {
        tsCreated.put(TS_KEY, ServerValue.TIMESTAMP);
    }

    /**
     * Marks this memory as just-modified when written to the database.
     */
    @Exclude
    void markModified() {
        tsModified.put(TS_KEY, ServerValue.TIMESTAMP);
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

    /**
     * @deprecated This getter is only for use by the DAO. Please use
     * {@link #getTimeCreated()} instead.
     */
    public Map<String, Object> getTsCreated() {
        return tsCreated;
    }

    /**
     * @deprecated This getter is only for use by the DAO. Please use
     * {@link #getTimeModified()} instead.
     */
    public Map<String, Object> getTsModified() {
        return tsModified;
    }

}
