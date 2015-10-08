package com.iha.group2.dronecontrol;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DataActivity extends ListActivity {

    List<String> list = new ArrayList<>();

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

    //When an item is clicked, it shows a dialog to Delete the item selected and then it deletes by querying with the IP value
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //get the item clicked
        final String item = (String) getListAdapter().getItem(position);
        //options that would appear when you click
        CharSequence options[] = new CharSequence[]{"Save"};

        //new Dialog
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) { //delete option
                    /*when you click, you will get
                    item = "IP: 192.168.0.0"
                    we want to split it by ": " to get IP or 192.168.0.0
                    and then we want to delete from the database with the second value of the string (192.168.0.0)
                    finally, reload the activity to show the new items from the database
                     */
                    if (isExternalStorageWritable()) {
                        try {
                            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            File file = new File(path,"weatherdata.txt"); // définir l'arborescence

                            if (!file.exists()) {
                                file.createNewFile();
                                Log.v("Data","file created");
                            }

                            FileWriter filewriter = new FileWriter(file,true);
                            filewriter.write("******");
                            filewriter.write("\n");
                            filewriter.write(item.toString());
                            filewriter.write("\n");
                            filewriter.close();
                            Log.v("DataActivity:", "" + file.getAbsolutePath());
                            Log.v("DataActivity:","asdas");
                            Log.v("DataActivity item:",""+ item.toString());
                        } catch (Exception e) {
                        }
                    } else {
                        Toast.makeText(DataActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    String[] args = {"date"};
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

