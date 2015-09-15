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
                    // TODO: IMPLEMENT SECOND ACTIVITY
                    //Intent second_act = new Intent(InitActivity.this, SecondActivity.class);
                    Toast.makeText(InitActivity.this, "Starting second activity", Toast.LENGTH_LONG).show();
                    //startActivity(second_act);
                }
                else Toast.makeText(InitActivity.this,"You must click connect first",Toast.LENGTH_LONG).show();
            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Once clicked send message to arduino using a service
                Toast.makeText(InitActivity.this, "Sending message to Arduino", Toast.LENGTH_LONG).show();
                try {
                    result = client("Alive",_ip);
                    Log.v("Activity One",result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (result.equals("alive")){
                    state=true;
                    Toast.makeText(InitActivity.this,"aliveeee",Toast.LENGTH_LONG).show();
                }
                else Toast.makeText(InitActivity.this,"Error",Toast.LENGTH_LONG).show();
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
        String rec_msg = "not";
        Log.v("Activity:", "Sending packet");
        DatagramPacket p = new DatagramPacket(message, msg_length, IPAddress, port);
        client_socket.send(p);

        Log.v("Activity:", "Recieving packet");
        DatagramPacket recieve_pkt = new DatagramPacket(recieve_data,recieve_data.length);
        while (!packet_received){
            client_socket.receive(recieve_pkt);
            rec_msg = new String(recieve_pkt.getData());
            Log.v("Service:","Data recieved :" + rec_msg);

        if (rec_msg != null || rec_msg.equals("") == false) packet_received = true;
        }
        // Might not be needed (waiting for answer)
        Log.v("Service:", "Out of loop");
        client_socket.close();
        Log.v("msg", rec_msg);
        return rec_msg;
    }

    @Override
    public void onPause(){
        super.onPause();
        client_socket.close();
        packet_received = true;

    }
}
