package com.wtf.whatsthatfoodapp.memory;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.regex.Pattern;

@IgnoreExtraProperties
public class Memory implements Parcelable {

    private static final String TAG = Memory.class.getSimpleName();

    public static final String TS_KEY_NEWEST = "tsCreatedNeg";

    private String key;
    @NonNull private String title;
    @NonNull private String loc;
    private ArrayList<String> tags;
    private int freq;
    private boolean savedForNextTime;
    // UNIX timestamps
    private long tsCreated;
    private long tsCreatedNeg; // Negative timestamp, so we can sort descending
    private long tsModified;

    private String description;

    private int rate;
    private int price;

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

    public void setRate(int rate){
        this.rate = rate;
    }

    public int getRate(){
        return this.rate;
    }

    public void setPrice(int price){
        this.price = price;
    }

    public int getPrice(){
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

    /*
     * Parcelable implementation
     */

    protected Memory(Parcel in) {
        key = in.readString();
        title = in.readString();
        loc = in.readString();
        tags = in.createStringArrayList();
        freq = in.readInt();
        savedForNextTime = in.readByte() != 0;
        tsCreated = in.readLong();
        tsCreatedNeg = in.readLong();
        tsModified = in.readLong();
        description = in.readString();
        rate = in.readInt();
        price = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(title);
        dest.writeString(loc);
        dest.writeStringList(tags);
        dest.writeInt(freq);
        dest.writeByte((byte) (savedForNextTime ? 1 : 0));
        dest.writeLong(tsCreated);
        dest.writeLong(tsCreatedNeg);
        dest.writeLong(tsModified);
        dest.writeString(description);
        dest.writeInt(rate);
        dest.writeInt(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Memory> CREATOR = new Creator<Memory>() {
        @Override
        public Memory createFromParcel(Parcel in) {
            return new Memory(in);
        }

        @Override
        public Memory[] newArray(int size) {
            return new Memory[size];
        }
    };

}
