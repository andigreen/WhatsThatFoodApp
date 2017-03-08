package com.wtf.whatsthatfoodapp.camera;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.preference.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by Aitor on 23/02/2017.
 */

public class IOImage {
    private Image capturedImage;
    private Bitmap bitmapImage;
    private Context context;
    private boolean saveToGallery;
    private Thread backgroundThread;

    public IOImage(Context context, Image capturedImage, int previewOrientation) {
        // Constructor for when the photo is an Image object
        // Called from TakePhotoAPI21Activity
        this.context = context;
        this.capturedImage = capturedImage;
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        this.saveToGallery = SP.getBoolean("save_to_gallery", true);
        backgroundThread = new Thread(new ImageBitmap(previewOrientation));
        backgroundThread.setPriority(THREAD_PRIORITY_BACKGROUND);
        backgroundThread.start();
    }
    public IOImage(Context context, Bitmap bitmapImage){
        // Constructor for when the photo is a Bitmap Object
        // Called from CreateMemoryActivity
        this.context = context;
        this.bitmapImage = bitmapImage;
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        this.saveToGallery = SP.getBoolean("save_to_gallery", true);
    }

    public Bitmap getBitmapImage(){
        try{
            backgroundThread.join();
        } catch (InterruptedException e){

        }
        return bitmapImage;
    }

    public String saveImage() {
        long timestamp = System.currentTimeMillis();
        File directory = context.getExternalCacheDir();
        String imageName = "pic" + timestamp + ".jpg";
        File cachedImageFile = new File(directory, imageName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(cachedImageFile);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
        }

        if (saveToGallery) {
            Thread thread = new Thread(new Gallery());
            thread.setPriority(THREAD_PRIORITY_BACKGROUND);
            thread.start();
        }
        //cachedImageFile.delete();
        return directory.getAbsolutePath() + "/" + imageName;
    }

    public Bitmap correctImageRotation(Bitmap bitmapImage, int rotation){
        // Correct the captured image rotation
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        this.bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(),
                matrix, true);
        return this.bitmapImage;
    }

    private class Gallery implements Runnable{
        public void run(){
            addPhoto();
        }

        private void addPhoto() {
            ContentValues values = new ContentValues();

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            Uri url = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try {
                OutputStream imageOutput = context.getContentResolver().openOutputStream(url);
                try{
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG,90,imageOutput);
                    imageOutput.close();
                } catch (IOException e){}
            } catch(FileNotFoundException e){}
        }
    }
    @TargetApi(21)
    private class ImageBitmap implements Runnable{
        // Runnable used in TakePhotoAPI21Activity to decode the Image and convert it to a Bitmap
        private int previewOrientation;
        public ImageBitmap(int previewOrientation){
            this.previewOrientation = previewOrientation;
        }
        public void run(){
           convertImageToBitmap();
        }
        private void convertImageToBitmap(){
            // Using capturedImage passed from camera thread, create a bitmapImage
            // Store it in bitmapImage
            ByteBuffer buffer = capturedImage.getPlanes()[0].getBuffer();
            byte[] imageBytes = new byte[buffer.capacity()];
            buffer.get(imageBytes);
            Bitmap bImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, null);
            bitmapImage = correctImageRotation(bImage, previewOrientation);
            capturedImage.close();
        }
    }
}