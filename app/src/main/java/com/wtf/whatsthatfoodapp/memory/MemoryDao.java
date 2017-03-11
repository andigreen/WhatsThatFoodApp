package com.wtf.whatsthatfoodapp.memory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.icu.util.Output;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MemoryDao {

    private static final String TAG = MemoryDao.class.getSimpleName();
    private static final String PHOTOS_PATH = "photos";
    private static final String MEMORIES_PATH = "memories";

    private static final String NO_STORAGE_ACCESS_MSG
            = "Could not access app pictures directory";
    private static final String NO_AUTH_USER_MSG
            = "Constructed without an authenticated user";
    private static final int MAX_IMAGE_SIZE = 2048;

    private final Context context;
    private File storageDir;
    private final String userId;

    public MemoryDao(@NonNull Context context) {
        this.context = context;

        userId = AuthUtils.getUserUid();
        if (userId == null) {
            Log.e(TAG, NO_AUTH_USER_MSG);
            throw new RuntimeException(NO_AUTH_USER_MSG);
        }
    }

    private void ensureStorageDir() {
        if (storageDir != null) return;

        storageDir = context.getExternalFilesDir(Environment
                .DIRECTORY_PICTURES);
        if (storageDir == null) {
            Log.e(TAG, NO_STORAGE_ACCESS_MSG);
            throw new RuntimeException(NO_STORAGE_ACCESS_MSG);
        }
    }

    /**
     * Updates the memory in the database if the memory has a key; otherwise,
     * creates a new memory in the database and sets the memory's key.
     * <p>
     * Calling this method with a memory object will therefore ensure that
     * the memory is in the database, whether it was previously in the
     * database or not.
     *
     * @param memory a memory to write to the database
     */
    public void writeMemory(Memory memory) {
        DatabaseReference db = getMemoriesRef();

        // If key is null, then we need to create a new key and mark as created
        if (memory.getKey() == null) {
            memory.markCreated();
            memory.setKey(db.push().getKey());
        }

        // Always mark as modified, and set the value
        memory.markModified();
        db.child(memory.getKey()).setValue(memory);
    }

    /**
     * Deletes the memory from the database if the memory has a key;
     * otherwise, this is a no-op.
     * <p>
     * If the key exists, this method will also attempt to delete the
     * corresponding photo.
     *
     * @param memory a memory to delete from the database
     */
    public void deleteMemory(Memory memory) {
        if (memory.getKey() == null) return;

        getMemoriesRef().child(memory.getKey()).removeValue();
        getPhotoRef(memory).delete();
    }

    /**
     * Returns a {@link StorageReference} to the path corresponding to the
     * memory. The target file is not guaranteed to exist.
     */
    public StorageReference getPhotoRef(Memory memory) {
        return FirebaseStorage.getInstance()
                .getReference()
                .child(PHOTOS_PATH)
                .child(userId)
                .child(memory.getKey());
    }

    /**
     * Returns a {@link DatabaseReference} to the user's memories object.
     */
    public DatabaseReference getMemoriesRef() {
        return FirebaseDatabase.getInstance().getReference().child
                (MEMORIES_PATH).child(userId);
    }

    public DrawableRequestBuilder<StorageReference> loadImage(Memory memory) {
        if (memory.getKey() == null) return null;

        return Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(getPhotoRef(memory))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .signature(new StringSignature(memory.getKey()));
    }

    /**
     * Returns a File containing the cached version of the image associated
     * with the given Memory, or null if the cached version could not be
     * accessed.
     * <p>
     * WARNING: This file cannot be directly read by other applications.
     */
    public File getImageFile(Memory memory) {
        try {
            return Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(getPhotoRef(memory))
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            Log.w(TAG, "Could not get image file for " + memory.getKey());
            return null;
        }
    }

    public interface LocalImageUriListener {
        void onSuccess(Uri uri);

        void onFailure();
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, File> {
        private String memKey;
        private LocalImageUriListener listener;

        SaveImageTask(Memory memory, LocalImageUriListener listener) {
            this.memKey = memory.getKey();
            this.listener = listener;
        }

        @Override
        protected File doInBackground(byte[]... params) {
            File target = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "local_" + memKey + ".jpg");
            if (target.exists()) return target;

            try {
                target.createNewFile();
                OutputStream out = new FileOutputStream(target);
                out.write(params[0]);
                return target;
            } catch (IOException ignored) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File file) {
            if (file == null) {
                listener.onFailure();
            } else {
                listener.onSuccess(FileProvider.getUriForFile(context,
                        "com.wtf.whatsthatfoodapp.fileprovider", file));
            }
        }
    }

    public void getLocalImageUri(final Memory memory,
            final LocalImageUriListener listener) {
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(getPhotoRef(memory))
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, 85)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .atMost()
                .override(MAX_IMAGE_SIZE, MAX_IMAGE_SIZE)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<byte[]>() {
                    @Override
                    public void onResourceReady(byte[] resource,
                            GlideAnimation<? super byte[]> glideAnimation) {
                        new SaveImageTask(memory, listener).execute(resource);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable d) {
                        listener.onFailure();
                    }
                });
    }

}
