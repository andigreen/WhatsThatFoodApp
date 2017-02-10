package com.wtf.whatsthatfoodapp.memory;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MemoryDao {

    private static final String TAG = MemoryDao.class.getSimpleName();
    private static final String PHOTOS_PATH = "photos";
    private static final String MEMORIES_PATH = "memories";

    private String userId;

    public MemoryDao(@NonNull String userId) {
        this.userId = userId;
    }

    /**
     * Updates the memory in the database if the memory has a key; otherwise,
     * creates a new memory in the database and sets the memory's key.
     *
     * Calling this method with a memory object will therefore ensure that
     * the memory is in the database, whether it was previously in the
     * database or not.
     *
     * @param memory a memory to write to the database
     */
    public void writeMemory(Memory memory) {
        DatabaseReference db = getMemoriesRef();
        if (memory.getKey() == null) {
            memory.setKey(db.push().getKey());
        }
        db.child(memory.getKey()).setValue(memory);
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
