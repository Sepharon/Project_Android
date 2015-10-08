package com.iha.group2.dronecontrol;

/* REFERENCE
http://stackoverflow.com/questions/10111166/get-all-rows-from-sqlite
 */

/*

This code basically try to contact an IP using UDP protocol
(you can use your computer as a server running "netcat -ul 8888" for example, although it's a bit buggy
and i had to close the netcat connection every time)

What does this do :

Click connect button -> Send message to "Arduino" (or whatever UDP server) -> waits for a message ->
if message received a message appears saying "Connected". If it does not receive any message, there's a timeout implemented that after
10 seconds it will close the connection and it appears a message saying "Error: Timeout".

In case that not having internet connection in the device, the message cannot be send and a Timeout will show up. The same case if you enter a wrong IP, or
the message from Arduino is not send to the device.

In this activity, we also enter entries to the SQL database, where we store the IP entered from the user and this database is used
to implement an AutoCompleteText that will help the user to remember the IPs entered before.

If you press button ON without connecting, a message will appear saying "You must click connect first". Once you have connected to the Arduino and the message form Arduino arrive,
then you can press ON to go to MapsActivity.

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
import android.widget.Toast;

public class InitActivity extends AppCompatActivity {

    /*
    Global variables declaration
     */
    // Checks if connection has been established with Arduino
    boolean state = false;
    // Message to send to Arduino for the Handshake
    final String action = "connect";

    //Some initializations
    ContentValues values;
    AutoCompleteTextView ip;
    ArrayAdapter<String> myAdapter;
    IntentFilter filter;
    private MyReceiver receiver;
    Button connect;
    Button on;
    Button data;
    String[] ips;
    Thread t;

    //Drone class
    Drone drone;


    /* Functions starts
    it registers our Receiver, gets all Views, gets all entries from the SQL database for
     the AutoCompleteText and setups two onClickListeners functions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        // Register Receiver
        filter = new IntentFilter("init");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);



        // UDP necessary
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Get instance for Drone class
        drone= Drone.getInstance();

        // Selecting buttons and text input
        connect = (Button) findViewById(R.id.button_con);
        on = (Button) findViewById(R.id.button_on);
        data = (Button) findViewById(R.id.button_data);
        ip = (AutoCompleteTextView)findViewById(R.id.ip_field);


        // New content values to store data in database
        values = new ContentValues();

        // This part build the AutoCompleteText with the entries from the database
        try {
            ips = getAllEntries();
            // set our adapter
            myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ips);
            ip.setAdapter(myAdapter);
        }
        catch (NullPointerException es){
            es.printStackTrace();
        }


        //Some on click listeners

        // To allow On button to do something, connect has to be pressed first
        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If we clicked connected first and everything was OK...
                if (state) {
                    state = false;
                    // Start MapsActivity
                    Intent second_act = new Intent(InitActivity.this, MapsActivity.class);
                    startActivity(second_act);
                }
                // Otherwise show message
                else
                    Toast.makeText(InitActivity.this, "You must click connect first", Toast.LENGTH_LONG).show();
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Once clicked, it sends message to Anduino using a service
                Toast.makeText(getApplicationContext(), "Sending message to Arduino", Toast.LENGTH_LONG).show();

                // Store IP to database
                try {
                    values.put(SQL_IP_Data_Base.IP, ip.getText().toString());
                    getContentResolver().insert(SQL_IP_Data_Base.CONTENT_URI_IP, values);
                } catch (SQLException se) { //if it is repeated, an exception will occur and we don't want the app to crash
                    se.printStackTrace();
                }
                Log.v("Activity One:", "Starting service");

                // Set Drone IP
                drone.setIP(ip.getText().toString());

                //before sending the message, it checks if the device is connected to a network
                Intent intent = new Intent(getBaseContext(), UDP_Receiver.class);
                intent.putExtra("value", "");
                intent.putExtra("action", "Check");
                startService(intent);

                // This thread starts the service by sending the action "connect", this way it won't block the UI
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getBaseContext(), UDP_Receiver.class);
                        intent.putExtra("value", "");
                        intent.putExtra("action", action);
                        startService(intent);
                    }
                });
                t.start();

                //If everything was OK, set status of the drone to connected
                drone.setStatus(true);
            }
        });
        data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent third_act = new Intent(InitActivity.this, DataActivity.class);
                    startActivity(third_act);

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

        /*These two refers to ListIPs and AboutActivity activities, if
        you select one of these items, it will call the appropiate function
        to start them.
         */
        if (id == R.id.ListIPs){
            listIPs();
        }
        else if (id == R.id.action_about) {
            open_about();
        }

        return super.onOptionsItemSelected(item);
    }


    // Receiver class, it checks if it receives "alive" from Arduino, or if there's no connection
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Split the result from the '\n'
            String result = intent.getStringExtra("result").split("\n")[0];
            Log.v("Activity One result", result);
            // Do something depending of the result
            switch (result) {
                // In case of "alive" it means connection is established
                case "alive":
                    //now ON is available
                    state = true;
                    Toast.makeText(InitActivity.this, "Connected", Toast.LENGTH_LONG).show();
                    break;
                // If it is not connected to a network it will display this message
                case "NoInternet":
                    Toast.makeText(InitActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                    break;
                // In case of timeout or other messages received
                default:
                    Toast.makeText(InitActivity.this, "Error: Timeout", Toast.LENGTH_LONG).show();
                    break;
            }
            Log.v("Activity one value: ", "" + state);
       }
    }

    // Starts the ListActivity to show all IPs entered by the user
    public void listIPs(){
        Intent intent = new Intent(this, ListIPs.class);
        startActivity(intent);
    }

    // Starts AboutActivity
    public void open_about(){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    // Gets all entries from the SQL database, it stores it in a String[].
    public String[] getAllEntries(){
        //this URL refers to the content provider
        String URL = "content://com.example.group13.provider.DB/ip";
        Uri notesText = Uri.parse(URL);
        //it creates a cursor with the result of the query
        Cursor c = getContentResolver().query(notesText, null, null, null, null);
        /*if there is something in the database, it stores the result of the query
        in a String[]
         */
        if (c.getCount() > 0){
            String[] ips = new String[c.getCount()];
            int i = 0;
            /*iteration to all the values of the cursor, it gets a string formed by
            the values of IP column from the database
             */
            while (c.moveToNext()){
                ips[i] = c.getString(c.getColumnIndexOrThrow(SQL_IP_Data_Base.IP));
                i++;
            }
            //when it is finished, we move to first and close the cursor
            c.moveToFirst();
            c.close();
            return ips;
        }
        //if there is nothing in the cursor, which means that the result of the query is zero elements
        else {
            c.moveToFirst();
            c.close();
            return new String[] {};
        }
    }

    // onResume method, we get all entries from the database again, we setup the adapter and we register our receiver
    @Override
    protected void onResume() {
        super.onResume();
        try {
            String[] ips = getAllEntries();
            // Set our adapter
            myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ips);
            ip.setAdapter(myAdapter);
        }
        catch (NullPointerException es){
            es.printStackTrace();
        }
        // Register receiver again
        this.registerReceiver(receiver, filter);

    }

    // onPause method, it stops the service UDP_Receiver in case it has been started
    @Override
    public void onPause() {
        super.onPause();
        // Stop UDP client
        Intent in = new Intent(getBaseContext(),UDP_Receiver.class);
        stopService(in);
        //needed, or else app crashes
        try {
            t.interrupt();
        } catch (NullPointerException es){
            es.printStackTrace();
        }
        // Unregister receiver
        this.unregisterReceiver(receiver);

    }

}