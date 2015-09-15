package com.iha.group2.dronecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPconnection extends Service {
    static final int port = 8888;
    static final String _ip = "192.168.1.8";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {
        String ip = intent.getStringExtra("ip_address");
        try {
            client("Alive",_ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopSelf();
        return START_STICKY;
    }


    // Sending messages funciont.
    public void client (String msg, String Ip) throws IOException {
        /*
        Might have to go to a service
         */
        boolean packet_received = false;
        DatagramSocket client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(Ip);

        // Creating broadcast
        Intent broadcast = new Intent();
        broadcast.setAction("miss_temps");
        sendBroadcast(broadcast);

        int msg_length = msg.length();
        byte[] message = msg.getBytes();
        byte[] recieve_data = new byte[5];
        String rec_msg = "not";
        Log.v("Service:", "Sending packet");
        DatagramPacket p = new DatagramPacket(message, msg_length, IPAddress, port);
        client_socket.send(p);

        Log.v("Service:", "Recieving packet");
        DatagramPacket recieve_pkt = new DatagramPacket(recieve_data,recieve_data.length);
        while (!packet_received){
            client_socket.receive(recieve_pkt);
            rec_msg = new String(recieve_pkt.getData());
            Log.v("Service:","Data recieved :" + rec_msg);

            if (rec_msg != null || rec_msg.equals("") == false) packet_received = true;
        }
        // Might not be needed (waiting for answer)
        Log.v("Service:","Out of loop");
        client_socket.close();

        broadcast.putExtra("msg", rec_msg);
        sendBroadcast(broadcast);

        Log.v("msg", rec_msg);

    }
}
