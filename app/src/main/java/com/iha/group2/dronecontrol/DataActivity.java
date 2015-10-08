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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sophie on 05/10/2015.
 */
public class DataActivity extends ListActivity {

    List<String> list = new ArrayList<>();
    Button save_button;
    TextView DataList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        //get entries from the database
        getAllEntries();

        //it setups the ArrayAdapter with the result from the function getAllEntries
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.activity_data, R.id.Datalist, list);
        setListAdapter(adapter);


        save_button = (Button) findViewById(R.id.button_save_txt);
        DataList = (TextView) findViewById(R.id.Datalist);


        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isExternalStorageWritable()) {
                    try {
                        File file = new File("/weatherdata.txt"); // définir l'arborescence
                        file.createNewFile();
                        FileWriter filewriter = new FileWriter(file);
                        filewriter.write("******");
                        filewriter.write("\n");
                        filewriter.write(DataList.getText().toString());
                        filewriter.write("\n");
                        filewriter.close();
                    } catch (Exception e) {
                    }
                } else {
                    Toast.makeText(DataActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



            //Checks if external storage is available for read and write
            public boolean isExternalStorageWritable() {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    return true;
                }
                return false;
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
                list.add("Date: " + c.getString(c.getColumnIndexOrThrow("DateTime")) +
                        "\nGPS: " + c.getString(c.getColumnIndexOrThrow("GPS")) +
                        "\nHumidity: " + c.getString(c.getColumnIndexOrThrow("Humidity")) +
                        "\nSpeed: " + c.getString(c.getColumnIndexOrThrow("Speed")) +
                        "\nTemperature: " + c.getString(c.getColumnIndexOrThrow("Temperature")));

            } while (c.moveToNext());
        }
        c.close();

    }
}

