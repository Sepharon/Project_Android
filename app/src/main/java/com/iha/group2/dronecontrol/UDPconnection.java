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
This class extends a Service
It only sends message to Arduino (or UDP server) without expecting any answers back
*/

public class UDPconnection extends Service {

    static final int movement_port = 8888;

    Drone drone;

    String ip;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //It calls the function send_msg to send the message depending on the "value" to the "ip" written
    public int onStartCommand(final Intent intent, int flags, int startId) {
        //final String ip = intent.getStringExtra("ip");
        drone= Drone.getInstance();
        ip = drone.getIP();
        final String v = intent.getStringExtra("value");
        Log.v("Service ip: ",ip);
        Log.v("Service value: ",v);

        try {
            send_msg(v, ip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopSelf();
        return START_STICKY;
    }


    //It sends the message to the Arduino (or UDP server)
    public void send_msg (final String msg, final String Ip) throws IOException {
        final int msg_length = msg.length();
        final byte[] message = msg.getBytes();

        DatagramSocket client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(Ip);

        Log.v("Service:", "Sending packet");
        DatagramPacket p = new DatagramPacket(message, msg_length, IPAddress, movement_port);
        client_socket.send(p);
        Log.v("Service:","Packet sent");
        client_socket.close();
    }
}
