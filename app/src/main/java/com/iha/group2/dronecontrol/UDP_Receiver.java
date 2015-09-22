package com.iha.group2.dronecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDP_Receiver extends Service {
    static final int camera_port = 8889;
    static final int gps_port = 8080;
    static final int movment_port = 8888;
    static final int timeout = 5000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public int onStartCommand(final Intent intent, int flags, int startId) {
        final String ip = intent.getStringExtra("ip");
        final String action = intent.getStringExtra("action");
        String msg;

        switch (action) {
            case "connect":
                try {
                    msg = get_msg(ip, action, movment_port);
                    Log.v("Service:", "Msg = " + msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            case "camera":
                try {
                    msg = get_msg(ip, action, camera_port);
                    Log.v("Service:", "Msg = " + msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "GPS":
                try {
                    get_msg(ip, action, gps_port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Log.v("Service Receiver:", "Unknown Action " + action);
        }

        stopSelf();
        return START_STICKY;
    }


    public String get_msg (String ip, String msg, int port) throws IOException{
        // TODO: FIX MESSAGE GARBAGE AT THE END
        /*
        Variables declaration
         */
        byte[] recieve_data = new byte[64];
        String rec_msg = "";
        boolean pck_rec = false;
        int msg_length = msg.length();
        byte[] message = msg.getBytes();
        InetAddress IPAddress = InetAddress.getByName(ip);
        // Create new socket
        final DatagramSocket socket = new DatagramSocket();

        Log.v("Service Receiver:", "Sending connection packet");
        // Sending msg to server, the msg will tell which data do we want
        DatagramPacket p = new DatagramPacket(message, msg_length,IPAddress, port);
        socket.send(p);

        Log.v("Service Receiver:","Connection data sent");

        Log.v("Service Receiver:", "Recieving packet");
        // Preparing packet to receive data
        DatagramPacket recieve_pkt = new DatagramPacket(recieve_data,recieve_data.length);
        // Timeout
        socket.setSoTimeout(timeout);
        // We wait until we receive a packet or timeout happens

        while (!pck_rec){
            try {
                Log.v("Service Receiver:", "Waiting for data");
                socket.receive(recieve_pkt);
                Log.v("Service Receiver:", "Data received");
                rec_msg = new String(recieve_pkt.getData());
                Log.v("Service Receiver", "Data recieved :" + rec_msg);
                pck_rec = true;
                Intent broadcast = new Intent();
                broadcast.setAction("broadcast");
                String messageR = rec_msg.split("\n")[0];
                Log.v("receivedMessage", messageR);
                broadcast.putExtra("result", messageR);
                sendBroadcast(broadcast);
            }
            catch (SocketTimeoutException e){
                Log.v("Service Receiver:", "Timeout");
                pck_rec = true;
            }
        }
        Log.v("Client:", "Out of loop");
        socket.close();
        return rec_msg;
    }
}
