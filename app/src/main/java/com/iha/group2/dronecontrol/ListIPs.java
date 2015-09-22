package com.iha.group2.dronecontrol;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListIPs extends ListActivity{
    List<String> list = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAllEntries();

        // use your custom layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.activity_list_ips, R.id.listView, list);
        setListAdapter(adapter);
    }

    @Override
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
    }
    //When an item is clicked, it shows a dialog with the two options Modify and Delete and it modifies or deletes by querying with the DateTime value
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final String item = (String) getListAdapter().getItem(position);
        CharSequence options[] = new CharSequence[]{"Delete"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    String[] parts = item.split("\n");
                    String[] ip = parts[0].split(": ");
                    String[] args = new String[]{ip[1]};
                    getContentResolver().delete(SQL_IP_Data_Base.CONTENT_URI, "IP=?", args);
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

    //This functions make a query to get all the entries from the SQLite Database and it stores it in a List.
    public void getAllEntries(){
        String URL = "content://com.example.group13.provider.IPs/db";

        Uri notesText = Uri.parse(URL);
        Cursor c = managedQuery(notesText, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                //Toast.makeText(this, c.getString(c.getColumnIndexOrThrow("IP")), Toast.LENGTH_SHORT).show();
                list.add("IP: " + c.getString(c.getColumnIndexOrThrow("IP")));
            } while (c.moveToNext());
        }
    }
}