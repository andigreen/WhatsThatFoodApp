package com.wtf.whatsthatfoodapp.memory;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.regex.Pattern;

@IgnoreExtraProperties
public class Memory {

    private static final String TAG = Memory.class.getSimpleName();

    public static final String TS_KEY_NEWEST = "tsCreatedNeg";

    private String key;
    @NonNull private String title;
    @NonNull private String loc;
    private ArrayList<String> tags;
    private int freq;
    private boolean savedForNextTime;
    private boolean reminder;
    // UNIX timestamps
    private long tsCreated;
    private long tsCreatedNeg; // Negative timestamp, so we can sort descending
    private long tsModified;

    private String description;

    private float rate;
    private float price;

    public Memory() {
        title = loc = "";
        tsCreated = tsModified = 0L;
        freq = 0;
        tags = new ArrayList<>();
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

    public ArrayList<String> getTags(){
        return tags;
    }

    public void setTag(String description){
        String[] tagsArr = description.split("\\s");
        if(tagsArr == null) {
            System.out.println("tags array is empty");
            return;
        }
        for (String aTagsArr : tagsArr) {
            System.out.println("Parsing description");
            if (Pattern.matches("(\\s|\\A)#(\\w+)", aTagsArr)) {
                tags.add(aTagsArr);
            }
        }
    }

    public int getFreq(){
        return freq;
    }

    public void addFreq(){
        freq++;
    }

    public boolean getSavedForNextTime(){
        return savedForNextTime;
    }

    public void setSavedForNextTime(boolean save){
        savedForNextTime = save;
    }

    public boolean getReminder(){
        return reminder;
    }

    public void setReminder(boolean remind){
        reminder = remind;
    }
    // Helper methods

    public void setRate(float rate){
        this.rate = rate;
    }

    public float getRate(){
        return this.rate;
    }

    public void setPrice(float price){
        this.price = price;
    }

    public float getPrice(){
        return this.price;
    }

    private static long unixTime() {
        return System.currentTimeMillis() / 1000;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

}
