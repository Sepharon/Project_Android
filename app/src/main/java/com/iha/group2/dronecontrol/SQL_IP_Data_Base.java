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

/*This class extends a ContentProvider
it has two tables:
-IP table to store all the IPs entered by the user
-Weather table to store sensor's data related to weather
 */

public class SQL_IP_Data_Base extends ContentProvider {
    // Creating Uri
    static final String PROVIDER_NAME = "com.example.group13.provider.DB";
    //static final String BASE = "db";

    static final String URL_IP = "content://" + PROVIDER_NAME + "/ip";
    static final Uri CONTENT_URI_IP = Uri.parse(URL_IP);

    static final String URL_DATA = "content://" + PROVIDER_NAME + "/data";
    static final Uri CONTENT_URI_DATA = Uri.parse(URL_DATA);

    // IMPORTANT, FIRST ONE SELECTS EVERYTHING, SECOND SELECTS ONLY ONE
    static final int ALL_ROWS_IP = 1;
    static final int SINGLE_ROW_IP = 2;
    static final int ALL_ROWS_DATA = 3;
    static final int SINGLE_ROW_DATA = 4;


    static final UriMatcher uriMatcher;


    static HashMap<String, String> hash_values;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME,"/ip", ALL_ROWS_IP);
        uriMatcher.addURI(PROVIDER_NAME, "/ip/#", SINGLE_ROW_IP);
        uriMatcher.addURI(PROVIDER_NAME, "/data", ALL_ROWS_DATA);
        uriMatcher.addURI(PROVIDER_NAME,"data/#", SINGLE_ROW_DATA);
    }



    private SQLiteDatabase db;
    // Creating DB
    static int DATABASE_VERSION = 1;

    static final String _ID = "_id";
    static final String DATABASE_NAME= "database.db";

    static final String IP = "IP";

    static final String TABLE_NAMEIP = "ip";
    static final String CREATE_IP_TABLE =
            " CREATE TABLE " + TABLE_NAMEIP + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    " IP TEXT NOT NULL, UNIQUE(IP));";

    static final String DateTime = "DateTime";
    static final String GPS = "GPS";
    static final String Humidity = "Humidity";
    static final String Speed = "Speed";
    static final String Temperature = "Temperature";

    static final String TABLE_NAMEDATA = "Data";
    public static String CREATE_DATA_TABLE = "CREATE TABLE " + TABLE_NAMEDATA
            + "(id INTEGER PRIMARY KEY, DateTime TEXT, GPS TEXT, Humidity TEXT, Speed TEXT, Temperature TEXT)";




    private static class  DatabaseHelper extends SQLiteOpenHelper{

        DatabaseHelper (Context context) {
            super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v("CP","Oncreate helper");
            db.execSQL(CREATE_IP_TABLE);
            db.execSQL(CREATE_DATA_TABLE);
        }

        @Override
        public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v("CP", "upgrade");
            if (oldVersion == DATABASE_VERSION) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAMEIP);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAMEDATA);
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
            case SINGLE_ROW_IP:
                String id = uri.getPathSegments().get(1);
                affected_rows = db.delete(TABLE_NAMEIP, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case ALL_ROWS_IP:
                affected_rows = db.delete(TABLE_NAMEIP,selection,selectionArgs);
                break;
            case SINGLE_ROW_DATA:
                String id2 = uri.getPathSegments().get(1);
                affected_rows = db.delete(TABLE_NAMEDATA, _ID +  " = " + id2 +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case ALL_ROWS_DATA:
                affected_rows = db.delete(TABLE_NAMEDATA,selection,selectionArgs);
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
        Uri _uri = null;

        switch (uriMatcher.match(uri)) {
            case ALL_ROWS_IP:
                long row_id = db.insert(TABLE_NAMEIP, "", values);
                Log.v("CP", "User inserted : " + values);
                if (row_id > 0) {
                    _uri = ContentUris.withAppendedId(CONTENT_URI_IP, row_id);
                    getContext().getContentResolver().notifyChange(_uri, null);
                    Log.v("CP", "Done inserting");
                }
                break;

            case ALL_ROWS_DATA:
                long row_data = db.insert(TABLE_NAMEDATA, "", values);
                Log.v("CP", "User inserted : " + values);
                if (row_data > 0) {
                    _uri = ContentUris.withAppendedId(CONTENT_URI_DATA, row_data);
                    getContext().getContentResolver().notifyChange(_uri, null);
                    Log.v("CP", "Done inserting");
                }
                break;
            default:
                throw new SQLException("Failed to add values into db " + uri);
        }
        return _uri;


    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Log.v("CP","Create CP");
        db = dbHelper.getWritableDatabase();
        return (db!=null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qdb = new SQLiteQueryBuilder();

        qdb.setTables(TABLE_NAMEIP);
        Log.v("CP","QUERY");
        switch (uriMatcher.match(uri)){
            // Selecting rows
            case ALL_ROWS_IP:
                Log.v("CP","all_rows");
                qdb.setProjectionMap(hash_values);
                break;
            case SINGLE_ROW_IP:
                Log.v("CP","single_rows");
                qdb.appendWhere( _ID + "=" +uri.getPathSegments().get(1));
                break;
            case ALL_ROWS_DATA:
                Log.v("CP","all_rows");
                qdb.setProjectionMap(hash_values);
                break;
            case SINGLE_ROW_DATA:
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
            case SINGLE_ROW_IP:
                affected_rows = db.update(TABLE_NAMEIP, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            case ALL_ROWS_IP:
                affected_rows = db.update(TABLE_NAMEIP,values,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return affected_rows;
    }

}
