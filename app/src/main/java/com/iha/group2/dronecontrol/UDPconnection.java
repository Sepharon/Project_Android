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


// TODO : Might need to receive packets?

public class UDPconnection extends Service {

    static final int port = 8888;
    static final String _ip = "192.168.1.8";
    static final int timeout = 10000;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {
        String ip = intent.getStringExtra("ip");
        String v = intent.getStringExtra("value");
        String action = intent.getStringExtra("action");
        String msg = null;
        Log.v("Service ip: ",ip);
        Log.v("Service value: ",v);
        switch (action) {
            case "":
                try {
                    send_msg(v, ip);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                try {
                    msg = get_msg(ip,action);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        Log.v("Service:", "Msg = " + msg);
        stopSelf();
        return START_STICKY;
    }


    // Sending messages funciont.
    public void send_msg (String msg, String Ip) throws IOException {
        /*
        Might have to go to a service
         */

        int msg_length = msg.length();
        byte[] message = msg.getBytes();

        DatagramSocket client_socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(Ip);

        Log.v("Service:", "Sending packet");
        DatagramPacket p = new DatagramPacket(message, msg_length, IPAddress, port);
        client_socket.send(p);
        Log.v("Service:","Packet sent");
        client_socket.close();

    }

    public String get_msg (String ip, String msg) throws IOException{
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

        Log.v("Service:", "Sending connection packet");
        // Sending msg to server, the msg will tell which data do we want
        DatagramPacket p = new DatagramPacket(message, msg_length,IPAddress, port);
        socket.send(p);

        Log.v("Service:","Connection data sent");

        Log.v("Service:", "Recieving packet");
        // Preparing packet to receive data
        DatagramPacket recieve_pkt = new DatagramPacket(recieve_data,recieve_data.length);
        // Timeout
        socket.setSoTimeout(timeout);
        // We wait until we receive a packet or timeout happens

        while (!pck_rec){
            try {
                Log.v("Service:","Waiting for data");
                socket.receive(recieve_pkt);
                Log.v("Service:", "Data received");
                rec_msg = new String(recieve_pkt.getData());
                Log.v("Service", "Data recieved :" + rec_msg);
                pck_rec = true;
            }
            catch (SocketTimeoutException e){
                Log.v("Service:", "Timeout");
                pck_rec = true;

            }

        }
        Log.v("Client:", "Out of loop");
        socket.close();
        return rec_msg;
    }
}
