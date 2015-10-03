package com.iha.group2.dronecontrol;

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

    // Checks if connection with Arduino is OK
    boolean state = false;

    final String action = "connect";

    //Some initializations
    ContentValues values;
    AutoCompleteTextView ip;
    ArrayAdapter<String> myAdapter;
    IntentFilter filter;
    private MyReceiver receiver;
    Button connect;
    Button on;
    String[] ips;
    Thread t;

    /* Functions starts
    it registers our Receiver, gets all Views, gets all entries from the SQL database for
     the AutoCompleteText and setups two onClickListeners functions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        //register Receiver
        filter = new IntentFilter("init");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);



        // P NECESSARY
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



        // Selecting buttons and text input
        connect = (Button) findViewById(R.id.button_con);
        on = (Button) findViewById(R.id.button_on);
        ip = (AutoCompleteTextView)findViewById(R.id.ip_field);

        //new content values to store data in database
        values = new ContentValues();

        //this part build the AutoCompleteText with the entries from the database
        try {
            ips = getAllEntries();
            // set our adapter
            myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ips);
            ip.setAdapter(myAdapter);
        }
        catch (NullPointerException es){
            es.printStackTrace();
        }


        // to allow On button to do something connect has to be pressed first

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If we clicked connected first and everything was OK...
                if (state) {
                    state = false;
                    //start MapsActivity sending the IP entered
                    Intent second_act = new Intent(InitActivity.this, MapsActivity.class);
                    second_act.putExtra("ip", ip.getText().toString());
                    startActivity(second_act);
                } else //otherwise
                    Toast.makeText(InitActivity.this, "You must click connect first", Toast.LENGTH_LONG).show();
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Once clicked, it sends message to Anduino using a service

                Toast.makeText(getApplicationContext(), "Sending message to Arduino", Toast.LENGTH_LONG).show();

                //store IP to database
                try{
                    values.put(SQL_IP_Data_Base.IP, ip.getText().toString());
                    getContentResolver().insert(SQL_IP_Data_Base.CONTENT_URI, values);
                }
                catch (SQLException se){ //if it is repeated, and exception will occur and we don't want the app to crash
                    se.printStackTrace();
                }
                Log.v("Activity One:", "Starting service");

                //this thread starts the service, this way it won't block the UI
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getBaseContext(), UDP_Receiver.class);
                        intent.putExtra("ip", ip.getText().toString());
                        intent.putExtra("value", "");
                        intent.putExtra("action", action);
                        startService(intent);
                    }
                });
                t.start();
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
        if (id == R.id.ListIPs){
            listIPs();
        }
        else if (id == R.id.action_about) {
            open_about();
        }

        return super.onOptionsItemSelected(item);
    }


    //our Receiver, it checks if it receives "alive" from Arduino (or UDP server)
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result").split("\n")[0];
            // Put this empty again ,  don't think is needed tho
            Log.v("Activity One result", result);
            switch (result) {
                case "alive":
                    state = true; //now ON is available
                    Toast.makeText(InitActivity.this, "Connected", Toast.LENGTH_LONG).show();
                    break;
                default: //timeout or other messages received
                    Toast.makeText(InitActivity.this, "Error: Timeout", Toast.LENGTH_LONG).show();
                    //state = false;
                    break;
            }
            Log.v("Activity one value: ", "" + state);


        }
    }

    //it starts the ListActivity to show all IPs entered by the user
    public void listIPs(){
        Intent intent = new Intent(this, ListIPs.class);
        startActivity(intent);
    }

    //it starts AboutActivity
    public void open_about(){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    //it gets all entries from the SQL database, it stores it in a String[].
    public String[] getAllEntries(){
        String URL = "content://com.example.group13.provider.IPs/db";
        Uri notesText = Uri.parse(URL);
        Cursor c = getContentResolver().query(notesText, null, null, null, null);
        if (c.getCount() > 0){
            String[] ips = new String[c.getCount()];
            int i = 0;
            while (c.moveToNext()){
                ips[i] = c.getString(c.getColumnIndexOrThrow(SQL_IP_Data_Base.IP));
                i++;
            }
            c.moveToFirst();
            c.close();
            return ips;
        }
        else {
            c.moveToFirst();
            c.close();
            return new String[] {};
        }
    }

    //onResume we get all entries from the database again, we setup the adapter and we register our receiver
    @Override
    protected void onResume() {
        super.onResume();
        try {
            String[] ips = getAllEntries();
            // set our adapter
            myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ips);
            ip.setAdapter(myAdapter);
        }
        catch (NullPointerException es){
            es.printStackTrace();
        }
        this.registerReceiver(receiver, filter);

    }

    //we unregister our receiver
    @Override
    public void onPause() {
        super.onPause();
        Intent in = new Intent(getBaseContext(),UDP_Receiver.class);
        stopService(in);
        t.interrupt();
        this.unregisterReceiver(receiver);

    }

}