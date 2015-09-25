package com.iha.group2.dronecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/*
 FINISHED
*/

public class UDPconnection extends Service {

    static final int movment_port = 8888;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {
        final String ip = intent.getStringExtra("ip");
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


    public void send_msg (final String msg, final String Ip) throws IOException {
        /*
        Might have to go to a service
         */

        final int msg_length = msg.length();
        final byte[] message = msg.getBytes();

        DatagramSocket client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(Ip);

        Log.v("Service:", "Sending packet");
        DatagramPacket p = new DatagramPacket(message, msg_length, IPAddress, movment_port);
        client_socket.send(p);
        Log.v("Service:","Packet sent");
        client_socket.close();
    }
}
