package com.wtf.whatsthatfoodapp.camera;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Aitor on 23/02/2017.
 */

public class IOImage {
    public static String saveImage(Context context, Bitmap bitmapImage, boolean saveToGallery){
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
        Log.e("PIC saved path",""+directory.getAbsolutePath());

        if (saveToGallery){
            galleryAddPic(context,bitmapImage);
        }
        //cachedImageFile.delete();
        return directory.getAbsolutePath()+"/"+imageName;
    }
    private static void galleryAddPic(Context context, Bitmap bitmapImage) {
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
        Log.e("Pic Gallery ADDED", "True");
    }
}
