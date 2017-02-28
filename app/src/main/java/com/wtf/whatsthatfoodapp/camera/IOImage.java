package com.wtf.whatsthatfoodapp.camera;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;

/**
 * Created by Aitor on 23/02/2017.
 */

public class IOImage implements Runnable{
    private Bitmap bitmapImage;
    private Context context;
    private boolean saveToGallery;

    public IOImage(Context context, Bitmap bitmapImage){
        this.context = context;
        this.bitmapImage = bitmapImage;
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        this.saveToGallery = SP.getBoolean("save_to_gallery",true);
    }
    public String saveImage(){
        long timestamp = System.currentTimeMillis();
        File directory = context.getExternalCacheDir();
        String imageName = "pic"+timestamp+".jpg";
        File cachedImageFile = new File(directory,imageName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cachedImageFile);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG,100,fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e){

            }
        }

        if (saveToGallery){
            Thread thread = new Thread(this);
            thread.start();
        }
        //cachedImageFile.delete();
        return directory.getAbsolutePath()+"/"+imageName;
    }
    private void galleryAddPic() {
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri url = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            OutputStream imageOutput = context.getContentResolver().openOutputStream(url);
            try{
                bitmapImage.compress(Bitmap.CompressFormat.JPEG,100,imageOutput);
                imageOutput.close();
            } catch (IOException e){}
        } catch(FileNotFoundException e){}
    }
    public void run(){
        galleryAddPic();
    }
}