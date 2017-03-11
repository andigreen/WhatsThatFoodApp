package com.wtf.whatsthatfoodapp.memory;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;

import java.io.File;

public class MemoryDao {

    private static final String TAG = MemoryDao.class.getSimpleName();
    private static final String PHOTOS_PATH = "photos";
    private static final String MEMORIES_PATH = "memories";

    private static final String NO_STORAGE_ACCESS_MSG = "Could not access app " +
            "pictures directory";
    public static final String NO_AUTH_USER_MSG = "Constructed without an authenticated user";

    private final File storageDir;
    private final String userId;

    public MemoryDao(@NonNull Context context) {
        userId = AuthUtils.getUserUid();
        if (userId == null) {
            Log.e(TAG, NO_AUTH_USER_MSG);
            throw new RuntimeException(NO_AUTH_USER_MSG);
        }

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

}
