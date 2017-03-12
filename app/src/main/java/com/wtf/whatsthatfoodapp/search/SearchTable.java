package com.wtf.whatsthatfoodapp.search;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.wtf.whatsthatfoodapp.App;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

import java.util.ArrayList;
import java.util.List;

/**
 * A table of Memories that uses SQLite full-text search (FTS) to facilitate
 * efficient complex queries.
 * <p>
 * To use this in an Activity (or anything with a {@link Context}), create an
 * Activity-lifetime instance (passing in the Context) and then call
 * {@link #query(String)} passing a non-empty string to query.
 */
public class SearchTable {

    private static final String TAG = SearchTable.class.getSimpleName();

    private static final String COL_KEY = "key";
    private static final String COL_CONTENT = "content";
    private static final String COL_RATING = "rating";
    private static final String COL_PRICE = "price";
    private static final String COL_TS = "ts";

    private static final String[] QUERY_COLS = {COL_KEY};

    private static final String DATABASE_NAME = "MEMORY_DB";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static final int DATABASE_VERSION = 1;

    private final DatabaseHelper mHelper;

    private int ratingVal = 1;
    private int priceVal = 1;
    private SearchActivity.FilterMode ratingMode = SearchActivity.FilterMode.ANY;
    private SearchActivity.FilterMode priceMode = SearchActivity.FilterMode.ANY;

    /**
     * Do not call this directly. Instead, get the Application-singleton
     * instance from {@link App#getSearchTable()}.
     */
    public SearchTable(Context context) {
        mHelper = new DatabaseHelper(context);
    }

    public void setRating(SearchActivity.FilterMode mode, int val) {
        this.ratingVal = val;
        this.ratingMode = mode;
    }

    public void setPrice(SearchActivity.FilterMode mode, int val) {
        this.priceVal = val;
        this.priceMode = mode;
    }

    @SuppressLint("DefaultLocale")
    private String getRatingClause() {
        if (ratingMode == SearchActivity.FilterMode.ANY) return "";
        return String.format(" AND CAST(%s AS NUMERIC) %s %d",
                COL_RATING, ratingMode.getOperator(), ratingVal);
    }

    @SuppressLint("DefaultLocale")
    private String getPriceClause() {
        if (priceMode == SearchActivity.FilterMode.ANY) return "";
        return String.format(" AND CAST(%s AS NUMERIC) %s %d",
                COL_PRICE, priceMode.getOperator(), priceVal);
    }

    /**
     * Returns a {@link List} of the keys of {@link Memory}s which match
     * the given query string, or null if the query string was empty.
     */
    @NonNull
    public List<String> query(String queryStr) {
        List<String> keys = new ArrayList<>();

        String[] tokens = SearchUtils.getTokens(queryStr);
        if (tokens.length == 0) return keys;
        for (int i = 0; i < tokens.length; i++) tokens[i] += "*";
        String selArgs = TextUtils.join(" ", tokens);

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        String selection = COL_CONTENT + " MATCH ?"
                + getRatingClause() + getPriceClause();
        Cursor cursor = builder.query(mHelper.getReadableDatabase(),
                QUERY_COLS, selection, new String[]{selArgs},
                null, null, null);

        if (cursor == null) return keys;
        if (!cursor.moveToFirst()) {
            cursor.close();
            return keys;
        }

        int keyCol = cursor.getColumnIndex(COL_KEY);
        do {
            keys.add(cursor.getString(keyCol));
        } while (cursor.moveToNext());
        return keys;
    }

    private static class MemoriesListener implements ChildEventListener {
        private DatabaseHelper mHelper;

        MemoriesListener(DatabaseHelper helper) {
            mHelper = helper;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Memory m = dataSnapshot.getValue(Memory.class);
            if (mHelper.addMemory(m) < 0) {
                Log.e(TAG, "Failed to add memory " + m.getKey());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Memory m = dataSnapshot.getValue(Memory.class);
            if (mHelper.updateMemory(m) < 0) {
                Log.e(TAG, "Failed to add memory " + m.getKey());
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            if (mHelper.deleteMemory(dataSnapshot.getKey()) < 0) {
                Log.e(TAG, "Failed to delete memory " + dataSnapshot.getKey());
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "MemoriesListener:onCancelled",
                    databaseError.toException());
        }

    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_DROP =
                "DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE;
        private static final String FTS_TABLE_CREATE = String.format(
                "CREATE VIRTUAL TABLE %s USING fts4 (%s, %s, %s, %s, %s)",
                FTS_VIRTUAL_TABLE,
                COL_KEY, COL_CONTENT, COL_RATING, COL_PRICE, COL_TS
        );

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

            // Load database and re-populate table
            mDatabase = getWritableDatabase();
            mDatabase.execSQL(FTS_TABLE_DROP);
            mDatabase.execSQL(FTS_TABLE_CREATE);

            // Load memories
            MemoryDao dao = new MemoryDao(context);
            dao.getMemoriesRef()
                    .addChildEventListener(new MemoriesListener(this));
        }

        private static ContentValues memoryToVals(Memory memory) {
            String memoryContent = memory.getTitle() + " "
                    + memory.getLoc() + " " + memory.getDescription();

            ContentValues vals = new ContentValues();
            vals.put(COL_KEY, memory.getKey());
            vals.put(COL_CONTENT, memoryContent);
            vals.put(COL_RATING, memory.getRate());
            vals.put(COL_PRICE, memory.getPrice());
            vals.put(COL_TS, memory.getTsCreated());
            return vals;
        }

        /**
         * Adds the memory's relevant fields to the FTS table. Returns the
         * row ID of the inserted row, or -1 if an error occurred.
         */
        private long addMemory(Memory memory) {
            return mDatabase.insert(FTS_VIRTUAL_TABLE, null,
                    memoryToVals(memory));
        }

        private long updateMemory(Memory memory) {
            return mDatabase.update(FTS_VIRTUAL_TABLE, memoryToVals(memory),
                COL_KEY + " = ?", new String[]{memory.getKey()});
        }

        private long deleteMemory(String key) {
            return mDatabase.delete(FTS_VIRTUAL_TABLE, COL_KEY + " = ?",
                    new String[]{key});
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int
                newVersion) {
            Log.w(TAG, String.format(
                    "Upgrading db from version %d to %d, destroying old data",
                    oldVersion, newVersion
            ));
            db.execSQL(FTS_TABLE_DROP);
            onCreate(db);
        }
    }

}
