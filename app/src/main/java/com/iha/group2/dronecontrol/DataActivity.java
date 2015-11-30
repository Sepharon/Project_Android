package com.iha.group2.dronecontrol;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;


import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/*
This class extends a ListActivity.
It shows the values stored in the database and it allows to save an item to a .txt file when you click it
 */

public class DataActivity extends ListActivity {

    List<String> list = new ArrayList<>();
    boolean file_created;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //get entries from the database
        getAllEntries();

        //it setups the ArrayAdapter with the result from the function getAllEntries
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.activity_data, R.id.listView2, list);
        setListAdapter(adapter);

    }


   //Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }


    //This functions make a query to get all the entries from the SQLite Database and it stores it in a List in this way:
    // DATA: data_from_database.
    public void getAllEntries() {
        //it refers to the content provider
        String URL = "content://com.example.group13.provider.DB/data";

        Uri notesText = Uri.parse(URL);
        //it creates a cursor that query the database and get all the values
        Cursor c = getContentResolver().query(notesText, null, null, null, null);
        //loop to add to a list the values from the cursor that corresponds to the Data table
        if (c.moveToFirst()) {
            do {
                list.add("Date: " + c.getString(c.getColumnIndexOrThrow("DateTime")) +
                        "\nGPS: " + c.getString(c.getColumnIndexOrThrow("GPS")) +
                        "\nTemperature: " + c.getString(c.getColumnIndexOrThrow("Temperature")) +"ºC");

            } while (c.moveToNext());
        }
        c.close();

    }

    //When an item is clicked, it shows a dialog to Delete the item selected and then it deletes by querying with the IP value
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //get the item clicked
        final String item = (String) getListAdapter().getItem(position);
        //options that would appear when you click
        CharSequence options[] = new CharSequence[]{"Save", "Delete"};

        //new Dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) { //save option
                    /*when you click, you will get
                    item = "Date: 2015-01-12\n GPS: 50.1-20.1-\nTemperature: 20ºC\n"
                    we want to split it by "\n" and then ": " to get the values
                    then it stores the selected item to the file, delete it from the database and
                    finally, reload the activity to show the new items from the database
                     */
                    if (isExternalStorageWritable()) {
                        try {
                            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            File file = new File(path,"weatherdata.txt");

                            if (!file.exists()) {
                                file_created = file.createNewFile();
                                Log.v("Data","file created");
                            }

                            FileWriter filewriter = new FileWriter(file, true);
                            filewriter.write("******");
                            filewriter.write("\n");
                            filewriter.write(item);
                            filewriter.write("\n");
                            filewriter.close();

                            Log.v("DataActivity:", "" + file.getAbsolutePath());
                            Log.v("DataActivity item:",""+ item);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(DataActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(DataActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                }
                else if (which==1){ //delete
                    String[] parts = item.split("\n");
                    String[] date = parts[0].split(": ");
                    String[] args = new String[]{date[1]};
                    getContentResolver().delete(SQL_IP_Data_Base.CONTENT_URI_DATA, "DateTime=?", args);
                    reload();
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    //It refresh the activity to show the new contents of the database
    public void reload() {
        Intent reload = new Intent(this, DataActivity.class);
        startActivity(reload);
        this.finish();
    }
}

