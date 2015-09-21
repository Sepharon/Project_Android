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


import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
                // If we clicked connected first and everything was OK...
                if (state){
                    state = false;
                    Intent second_act = new Intent(InitActivity.this, DroneControl.class);
                    second_act.putExtra("ip",ip.getText().toString());
                    // You won't be able to see this toast but whatever
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
                // We are going to start a thread to act as a timeout
                time_out();
                try {
                    // Start client connection
                    result = client("Alive", ip.getText().toString());
                    // Needed since if there's a timeout we close the socket
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Put this empty again ,  don't think is needed tho
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


    /*
    Here is the part that you should look at, the client function sends and waits for a message
    I know that this should be implemented in a service, that's why i'm porting this code to the
    UDPconnection service.
     */

    public String client (String msg, String Ip) throws IOException {
        /*
        Might have to go to a service
         */

        // We get the IP address
        InetAddress IPAddress = InetAddress.getByName(Ip);
        // Create socket
        client_socket = new DatagramSocket();
        // Init variables
        int msg_length = msg.length();
        byte[] message = msg.getBytes();
        byte[] recieve_data = new byte[5];

        // Starting to do actual things
        Log.v("Activity:", "Sending packet");
        DatagramPacket p = new DatagramPacket(message, msg_length, IPAddress, port);
        // And we send a message
        client_socket.send(p);

        Log.v("Activity:", "Recieving packet");
        DatagramPacket recieve_pkt = new DatagramPacket(recieve_data,recieve_data.length);
        Log.v("Activity",""+packet_received);

        // This is actually needed since the timeout fucked me over with this
        packet_received=false;

        // We wait until we receive a packet or timeout happens
        while (!packet_received){
            client_socket.receive(recieve_pkt);
            rec_msg = new String(recieve_pkt.getData());
            Log.v("Client","Data recieved :" + rec_msg);
            // If we receive a packet we exit the loop.
            if (rec_msg != null || !rec_msg.equals("")) packet_received = true;
        }

        Log.v("Client:", "Out of loop");
        client_socket.close();
        Log.v("msg", rec_msg);
        return rec_msg;
    }

    public void time_out(){
        /*
        This works the following way -> packet_received is set to false , the client function gets in a loop
        waiting to receive a packet.

        The timeout start, after ten second sets the variable packet_received to true, this making the loop
        from the client() function stop.

         */

        // So, we create thread since this will sleep for X seconds.
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
                    // Change the boolean variable so the client function exits the loop.
                    packet_received = true;
                    // For some reason i have to close to socket here else it blocks the whole app
                    client_socket.close();
                }
            }
        });
        t.start();
    }
    // If we pause the app we get out of the loop (cancel connection attempt)
    @Override
    public void onPause(){
        super.onPause();
        packet_received = true;
    }
}
