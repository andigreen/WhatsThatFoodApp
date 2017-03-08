package com.wtf.whatsthatfoodapp.search;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.wtf.whatsthatfoodapp.auth.AuthUtils;
import com.wtf.whatsthatfoodapp.memory.Memory;
import com.wtf.whatsthatfoodapp.memory.MemoryDao;

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

    public static final String COL_KEY = "key";
    public static final String COL_TITLE = "title";
    public static final String COL_LOC = "loc";
    public static final String COL_DESC = "desc";
    public static final String COL_RATING = "rating";
    public static final String COL_PRICE = "price";

    private static final String[] QUERY_COLS = {COL_KEY};

    private static final String DATABASE_NAME = "MEMORY_DB";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static final int DATABASE_VERSION = 1;

    private final DatabaseHelper mHelper;

    private int ratingVal = 1;
    private int priceVal = 1;
    private SearchActivity.FilterMode ratingMode = SearchActivity.FilterMode.ANY;
    private SearchActivity.FilterMode priceMode = SearchActivity.FilterMode.ANY;

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
     * Returns a {@link Cursor} of the keys of {@link Memory}s which match
     * the given query string, or null if the query is empty. The query is
     * considered empty if the query string was empty, or if the cursor had
     * no results.
     * <p>
     * TODO describe queryStr format
     */
    public Cursor query(String queryStr) {
        String selection = FTS_VIRTUAL_TABLE + " MATCH ?"
                + getRatingClause() + getPriceClause();

        String[] tokens = SearchUtils.getTokens(queryStr);
        if (tokens.length == 0) return null;
        for (int i = 0; i < tokens.length; i++) tokens[i] += "*";
        String selArgs = TextUtils.join(" ", tokens);

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);
        Cursor cursor = builder.query(mHelper.getReadableDatabase(),
                QUERY_COLS, selection, new String[]{selArgs},
                null, null, null);

        if (cursor == null) return null;
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
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

        // We don't bother with the rest of the events, since none of the
        // data should change while the user is searching

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
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
                "CREATE VIRTUAL TABLE %s USING fts4 (%s, %s, %s, %s, %s, %s)",
                FTS_VIRTUAL_TABLE,
                COL_KEY, COL_TITLE, COL_LOC, COL_DESC, COL_RATING, COL_PRICE
        );

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

            // Load database and re-populate table
            mDatabase = getWritableDatabase();
            mDatabase.execSQL(FTS_TABLE_DROP);
            mDatabase.execSQL(FTS_TABLE_CREATE);

            // Load memories
            MemoryDao dao = new MemoryDao(AuthUtils.getUserUid());
            dao.getMemoriesRef()
                    .addChildEventListener(new MemoriesListener(this));
        }

        /**
         * Adds the memory's relevant fields to the FTS table. Returns the
         * row ID of the inserted row, or -1 if an error occurred.
         */
        long addMemory(Memory memory) {
            ContentValues vals = new ContentValues();
            vals.put(COL_KEY, memory.getKey());
            vals.put(COL_TITLE, memory.getTitle());
            vals.put(COL_LOC, memory.getLoc());
            vals.put(COL_DESC, memory.getDescription());
            vals.put(COL_RATING, memory.getRate());
            vals.put(COL_PRICE, memory.getPrice());
            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, vals);
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
