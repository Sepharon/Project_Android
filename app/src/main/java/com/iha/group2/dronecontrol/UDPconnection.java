package com.iha.group2.dronecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
REFERENCE:
http://developer.android.com/reference/java/net/DatagramSocket.html
 */

/*
This class extends a Service
It only sends message to Arduino (or UDP server) without expecting any answers back
It dies once the message was sent
*/

public class UDPconnection extends Service {

    //Some initializations
    static final int movement_port = 8888;

    Drone drone;
    String ip;
    DatagramSocket client_socket;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //It calls the function send_msg to send the message depending on the "value" to the "ip" written
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // Use drone instance to get IP
        drone= Drone.getInstance();
        ip = drone.getIP();
        // Get the value that we want to send
        final String v = intent.getStringExtra("value");
        Log.v("Service ip: ",ip);
        Log.v("Service value: ",v);
        // Check if drone is connected.
        if (drone.getStatus()) {
            try {
                send_msg(v, ip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        client_socket.close();
        stopSelf();
    }

    //It sends the message to the Arduino (or UDP server)
    public void send_msg (final String msg, final String Ip) throws IOException {
        final int msg_length = msg.length();
        final byte[] message = msg.getBytes();
        // Create a socket
        client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(Ip);
        // Create UDP packet
        DatagramPacket p = new DatagramPacket(message, msg_length, IPAddress, movement_port);
        // Send packet
        Log.v("Service:", "Sending packet");
        client_socket.send(p);
        Log.v("Service:", "Packet sent");
        // Close connection
        client_socket.close();
    }
}
