package com.iha.group2.dronecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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
    String result = "";
    String rec_msg = "";

    // Timeout value
    static final int timeout = 10000;

    // Default port and ip values, need to be user input
    static final int port = 8888;
    static final String _ip = "192.168.1.8";

    // Functions start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        // UDP NECESSARY
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Selecting buttons and text input
        Button connect = (Button) findViewById(R.id.button_con);
        Button on = (Button) findViewById(R.id.button_on);
        final EditText ip = (EditText) findViewById(R.id.ip_field);

        // In order for the On button to do something connect has to be pressed first

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state){
                    state = false;
                    // TODO: IMPLEMENT SECOND ACTIVITY
                    Intent second_act = new Intent(InitActivity.this, DroneControl.class);
                    Toast.makeText(InitActivity.this, "Starting second activity", Toast.LENGTH_LONG).show();
                    startActivity(second_act);
                }
                else Toast.makeText(InitActivity.this,"You must click connect first",Toast.LENGTH_LONG).show();
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Once clicked send message to arduino using a service
                Toast.makeText(InitActivity.this, "Sending message to Arduino", Toast.LENGTH_LONG).show();
                // We are going to start a thread to act asa  timeout
                time_out();
                try {
                    result = client("Alive", _ip);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                rec_msg = "";
                Log.v("Activity One result", result);
                if (result.equals("alive")) {
                    state = true;
                    Toast.makeText(InitActivity.this, "Connected", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(InitActivity.this, "Error: Timeout", Toast.LENGTH_LONG).show();
                    state = false;
                }
                Log.v("Activity one value: ","" + state);
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

        return super.onOptionsItemSelected(item);
    }

    public String client (String msg, String Ip) throws IOException {
        /*
        Might have to go to a service
         */


        InetAddress IPAddress = InetAddress.getByName(Ip);
        client_socket = new DatagramSocket();
        int msg_length = msg.length();
        byte[] message = msg.getBytes();
        byte[] recieve_data = new byte[5];

        Log.v("Activity:", "Sending packet");
        DatagramPacket p = new DatagramPacket(message, msg_length, IPAddress, port);
        client_socket.send(p);

        Log.v("Activity:", "Recieving packet");
        DatagramPacket recieve_pkt = new DatagramPacket(recieve_data,recieve_data.length);
        Log.v("Activity",""+packet_received);
        packet_received=false;
        while (!packet_received){
            client_socket.receive(recieve_pkt);
            rec_msg = new String(recieve_pkt.getData());
            Log.v("Service:","Data recieved :" + rec_msg);
            Log.v("Service:","Data recieved :" + packet_received);

            if (rec_msg != null || !rec_msg.equals("")) packet_received = true;
        }
        if (rec_msg.equals("")) packet_received = false;
        // Might not be needed (waiting for answer)
        Log.v("Service:", "Out of loop");
        client_socket.close();
        Log.v("msg", rec_msg);
        return rec_msg;
    }

    public void time_out(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v("Thread Activity One","Starting thread");
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!state){
                    Log.v("Thread Activity One","Timeout");
                    packet_received = true;
                    client_socket.close();
                }
            }
        });
        t.start();
    }
    @Override
    public void onPause(){
        super.onPause();
        packet_received = true;
    }
}
