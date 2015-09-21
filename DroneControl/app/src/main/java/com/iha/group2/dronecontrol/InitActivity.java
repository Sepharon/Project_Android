package com.iha.group2.dronecontrol;

/*

OK so this is weird as fuck, this code basically try to contact an IP using UDP protocol
(you can use your computer as a server running "netcat -ul 8888" for example, although it's a bit buggy
and i had to close the netcat connection everytime

What does this do :

Click connect button -> Send message to "Arduino" (or whatever udp server) -> waits for a message ->
if message received YEAAAH everything is good . If no message received that fuck. There's a timeout implemented that after
10 seconds it will close the connection (if any of my teachers see's how i implemented that, I'm sorry)
 */

/*
I know that the "network" tasks should be done in a service, this si begin implemented in the UDPconnection class,
right now I'm testing this in an activity.
 */


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class InitActivity extends AppCompatActivity {

    //Global variables from class;
    DatagramSocket client_socket;
    // Checks if connection with arduino is OK
    boolean state = false;
    boolean packet_received = false;
    // String values
    String result = "";
    String rec_msg = "";

    // Timeout value
    static final int timeout = 10000;

    // Default port and ip values, need to be user input
    static final int port = 8888;
    final String action = "connect";
    ContentValues values;
    AutoCompleteTextView ip;
    ArrayAdapter<String> myAdapter;


    // Functions start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        IntentFilter filter = new IntentFilter("broadcast");
        this.registerReceiver(new MyReceiver(), filter);



        // UDP NECESSARY
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



        // Selecting buttons and text input
        Button connect = (Button) findViewById(R.id.button_con);
        Button on = (Button) findViewById(R.id.button_on);
        ip = (AutoCompleteTextView)findViewById(R.id.ip_field);
        //final EditText ip = (EditText) findViewById(R.id.ip_field);

        // TODO: ADD IP TO DB
        // TODO: SHOW IP WHEN WRITING IP



        values = new ContentValues();

        try {
            String[] ips = getAllEntries();
            for (int i = 0; i < ips.length; i++) {
                Log.i(this.toString(), ips[i]);
            }
            // set our adapter
            myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, ips);
            ip.setAdapter(myAdapter);
        }
        catch (NullPointerException es){
            es.printStackTrace();
        }


        // In order for the On button to do something connect has to be pressed first

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If we clicked connected first and everything was OK...
                if (state) {
                    state = false;
                    Intent second_act = new Intent(InitActivity.this, DroneControl.class);
                    second_act.putExtra("ip", ip.getText().toString());
                    // You won't be able to see this toast but whatever
                    Toast.makeText(InitActivity.this, "Starting second activity", Toast.LENGTH_LONG).show();
                    startActivity(second_act);
                } else
                    Toast.makeText(InitActivity.this, "You must click connect first", Toast.LENGTH_LONG).show();
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Once clicked send message to arduino using a service
                Toast.makeText(InitActivity.this, "Sending message to Arduino", Toast.LENGTH_LONG).show();
                // We are going to start a thread to act as a timeout
                //time_out();
                try{
                    values.put(SQL_IP_Data_Base.IP, ip.getText().toString());
                    Uri uri = getContentResolver().insert(SQL_IP_Data_Base.CONTENT_URI, values);
                    //Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_SHORT).show();
                }
                catch (SQLException se){
                    se.printStackTrace();
                }

                Intent intent = new Intent(getBaseContext(), UDPconnection.class);
                intent.putExtra("ip", ip.getText().toString());
                intent.putExtra("value", "");
                intent.putExtra("action", action);
                startService(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_init, menu);
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
        else if (id == R.id.ListIPs){
            listIPs();
        }

        return super.onOptionsItemSelected(item);
    }


    // If we pause the app we get out of the loop (cancel connection attempt)
    @Override
    public void onPause() {
        super.onPause();
        packet_received = true;
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result");
            // Put this empty again ,  don't think is needed tho
            Log.v("Activity One result", result);
            if (result.equals("alive")) {
                state = true;
                Toast.makeText(InitActivity.this, "Connected", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(InitActivity.this, "Error: Timeout", Toast.LENGTH_LONG).show();
                //state = false;
            }
            Log.v("Activity one value: ", "" + state);


        }
    }

    public void listIPs(){
        Intent intent = new Intent(this, ListIPs.class);
        startActivity(intent);
    }

    public String[] getAllEntries(){
        String URL = "content://com.example.group13.provider.IPs/db";
        Uri notesText = Uri.parse(URL);
        Cursor c = managedQuery(notesText, null, null, null, null);
        if (c.getCount() > 0){
            String[] ips = new String[c.getCount()];
            int i = 0;
            while (c.moveToNext()){
                ips[i] = c.getString(c.getColumnIndexOrThrow(SQL_IP_Data_Base.IP));
                i++;
            }
            c.moveToFirst();
            return ips;
        }
        else {
            c.moveToFirst();
            return new String[] {};
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String[] ips = getAllEntries();
            for (int i = 0; i < ips.length; i++) {
                Log.i(this.toString(), ips[i]);
            }
            // set our adapter
            myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, ips);
            ip.setAdapter(myAdapter);
        }
        catch (NullPointerException es){
            es.printStackTrace();
        }
    }
}