package com.iha.group2.dronecontrol;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/*REFERENCE:
 * http://www.vogella.com/tutorials/AndroidListView/article.html
 * http://stackoverflow.com/questions/10111166/get-all-rows-from-sqlite
 */

/*This class extends an ListActivity which displays all the IPs entered by the user from the SQL database
* We don't want to create an options menu here*/

public class ListIPs extends ListActivity{

    //One initialization, it is an ArrayList to fill the adapter.
    List<String> list = new ArrayList<>();

    //it gets all entries and setup the adapter
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get entries from the database
        getAllEntries();

        //it setups the ArrayAdapter with the result from the function getAllEntries
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.activity_list_ips, R.id.listView, list);
        setListAdapter(adapter);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_ips, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    //When an item is clicked, it shows a dialog to Delete the item selected and then it deletes by querying with the IP value
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //get the item clicked
        final String item = (String) getListAdapter().getItem(position);
        //options that would appear when you click
        CharSequence options[] = new CharSequence[]{"Delete"};

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
                    String[] ip = item.split(": ");
                    String[] args = new String[]{ip[1]};
                    getContentResolver().delete(SQL_IP_Data_Base.CONTENT_URI_IP, "IP=?", args);
                    reload();
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    //It refresh the activity to show the new contents of the database
    public void reload() {
        Intent reload = new Intent(this, ListIPs.class);
        startActivity(reload);
        this.finish();
    }

    //This functions make a query to get all the entries from the SQLite Database and it stores it in a List in this way:
    // IP: ip_from_database.
    public void getAllEntries(){
        //it refers to the content provider
        String URL = "content://com.example.group13.provider.DB/ip";

        Uri notesText = Uri.parse(URL);
        //it creates a cursor that query the database and get all the values
        Cursor c = getContentResolver().query(notesText, null, null, null, null);
        //loop to add to a list the values from the cursor that corresponds to the IP column
        if (c.moveToFirst()) {
            do {
                list.add("IP: " + c.getString(c.getColumnIndexOrThrow("IP")));
            } while (c.moveToNext());
        }
        c.close();
    }
}
