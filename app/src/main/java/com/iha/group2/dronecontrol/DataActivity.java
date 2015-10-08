package com.iha.group2.dronecontrol;


import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sophie on 05/10/2015.
 */
public class DataActivity extends ListActivity {

    List<String> list = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_data);

        //get entries from the database
        getAllEntries();

        //it setups the ArrayAdapter with the result from the function getAllEntries
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.activity_data, R.id.Datalist, list);
        setListAdapter(adapter);


        Button save_button = (Button) findViewById(R.id.save_button);


        /*save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                if (isExternalStorageWritable()) {
                    getFileStorageDir("Weather's data" + date.toString());
                } else {
                    Toast.makeText(DataActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }

            }
        }


            Checks if external storage is available for read and write
            public boolean isExternalStorageWritable() {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    return true;
                }
                return false;
            }

            public File getFileStorageDir(String fileName) {
                // Get the directory for the user's public pictures directory.
                //File file = new File(Environment.getExternalStoragePublicDirectory(
                if (!file.mkdirs()) {
                    Log.e("Error : ", "Directory not created");
                }
                return file;
            }
        });

        Environment.DIRECTORY_PICTURES, fileName);*/
    }

    //This functions make a query to get all the entries from the SQLite Database and it stores it in a List in this way:
    // DATA: data_from_database.
    public void getAllEntries() {
        //it refers to the content provider
        String URL = "content://com.example.group13.provider.DB/data";

        Uri notesText = Uri.parse(URL);
        //it creates a cursor that query the database and get all the values
        Cursor c = getContentResolver().query(notesText, null, null, null, null);
        //loop to add to a list the values from the cursor that corresponds to the Data column
        if (c.moveToFirst()) {
            do {
                list.add("Date: " + c.getString(c.getColumnIndexOrThrow("Date")));
                list.add("GPS: " + c.getString(c.getColumnIndexOrThrow("GPS")));
                list.add("Humidity: " + c.getString(c.getColumnIndexOrThrow("Humidity")));
                list.add("Speed: " + c.getString(c.getColumnIndexOrThrow("Speed")));
                list.add("Temperature: " + c.getString(c.getColumnIndexOrThrow("Temperature")));

            } while (c.moveToNext());
        }
        c.close();

    }
}

