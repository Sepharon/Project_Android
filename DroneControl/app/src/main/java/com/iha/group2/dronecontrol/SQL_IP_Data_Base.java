package com.iha.group2.dronecontrol;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;


/*
    REFERENCE :
    http://www.tutorialspoint.com/android/android_content_providers.htm
    http://www.vogella.com/tutorials/AndroidSQLite/article.html
    http://developer.android.com/guide/topics/providers/content-provider-basics.html

*/

public class SQL_IP_Data_Base extends ContentProvider {
    // Creating Uri
    static final String PROVIDER_NAME = "com.example.group13.provider.IPs";
    static final String BASE = "db";
    static final String URL = "content://" + PROVIDER_NAME + "/" +BASE;
    static final Uri CONTENT_URI = Uri.parse(URL);

    // IMPORTANT, FIRST ONE SELECTS EVERYTHING, SECOND SELECTS ONLY ONE
    static final int ALL_ROWS = 1;
    static final int SINGLE_ROW = 2;
    static final UriMatcher uriMatcher;


    private static HashMap<String, String> hash_values;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME,BASE,ALL_ROWS);
        uriMatcher.addURI(PROVIDER_NAME,BASE+"/#",SINGLE_ROW);
    }



    private SQLiteDatabase db;
    // Creating DB
    static final String _ID = "_id";
    static final String IP = "IP";
    static final String DATABASE_NAME = "IPs";
    static final String TABLE_NAME = "ip";
    static int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    " IP TEXT NOT NULL);";

    private static class  DatabaseHelper extends SQLiteOpenHelper{

        DatabaseHelper (Context context) {
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v("CP","Oncreate helper");
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion){
            Log.v("CP","upgrade");
            if (oldVersion == DATABASE_VERSION) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
                DATABASE_VERSION = newVersion;
            }
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int affected_rows;

        switch (uriMatcher.match(uri)){
            case SINGLE_ROW:
                String id = uri.getPathSegments().get(1);
                affected_rows = db.delete(TABLE_NAME, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case ALL_ROWS:
                affected_rows = db.delete(TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return affected_rows;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row_id = db.insert(TABLE_NAME,"",values );
        Log.v("CP","User inserted : " + values);
        if (row_id>0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI,row_id);
            getContext().getContentResolver().notifyChange(_uri, null);
            Log.v("CP", "Done inserting");
            return _uri;
        }
        Log.v("CP","Error inserting");
        throw new SQLException("Failed to add values into db " + uri);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Log.v("CP","Create CP");
        db = dbHelper.getWritableDatabase();
        //Log.v("CP","" + dbFile.exists());
        //return dbFile.exists();
        return (db!=null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qdb = new SQLiteQueryBuilder();

        qdb.setTables(TABLE_NAME);
        Log.v("CP","QUERY");
        switch (uriMatcher.match(uri)){
            // Selecting rows
            case ALL_ROWS:
                Log.v("CP","all_rows");
                qdb.setProjectionMap(hash_values);
                break;
            case SINGLE_ROW:
                Log.v("CP","single_rows");
                qdb.appendWhere( _ID + "=" +uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // Sorting by id
        if (sortOrder ==  null || sortOrder.equals("")) sortOrder=_ID;
        // Starting query
        Cursor c = qdb.query(db,projection,selection,selectionArgs,null,null,sortOrder);
        c.setNotificationUri(getContext().getContentResolver(),uri);

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int affected_rows;

        switch (uriMatcher.match(uri)){
            case SINGLE_ROW:
                affected_rows = db.update(TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            case ALL_ROWS:
                affected_rows = db.update(TABLE_NAME,values,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return affected_rows;
    }
}
