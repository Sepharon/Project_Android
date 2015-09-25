package com.iha.group2.dronecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class UDP_Receiver extends Service {
    static final int camera_port = 8889;
    static final int gps_port = 10000;
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
                break;
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
                    //tcp_client(ip,action,gps_port);
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

        Log.v("Service Receiver:", "Receiving packet");
        // Preparing packet to receive data
        DatagramPacket recieve_pkt = new DatagramPacket(recieve_data,recieve_data.length);
        // Timeout
        socket.setSoTimeout(timeout);
        // We wait until we receive a packet or timeout happens

        //while (!pck_rec){
            try {
                Log.v("Service Receiver:", "Waiting for data");
                socket.receive(recieve_pkt);
                Log.v("Service Receiver:", "Data received");
                rec_msg = new String(recieve_pkt.getData());
                Log.v("Service Receiver", "Data recieved :" + rec_msg);
                pck_rec = true;
                socket.close();
                Intent broadcast = new Intent();
                broadcast.setAction("broadcast");
                //String messageR = rec_msg.split("\n")[0];
                //Log.v("receivedMessage", messageR);
                broadcast.putExtra("result", rec_msg);
                sendBroadcast(broadcast);
            }
            catch (SocketTimeoutException e){
                Log.v("Service Receiver:", "Timeout");
                pck_rec = true;
            }
        //}
        Log.v("Client:", "Out of loop");

        return rec_msg;
    }

    public String tcp_client(String ip, String msg, int port) throws  IOException{
        InetAddress IP = InetAddress.getByName(ip);
        Socket socket = new Socket(IP,port);
        BufferedReader in;
        boolean msg_rec = false;
        String response=null;
        OutputStream out = socket.getOutputStream();
        PrintWriter output = new PrintWriter(out);

        output.println(msg);
        Log.v("TCP_connection:", "Sent msg");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socket.setSoTimeout(10000);
        //while(!msg_rec){
            response = in.readLine();
         //   if (response != null){
                Log.v("TCP_connection:", "Message received");
          //      msg_rec=true;
           // }
        //}
        Log.v("TCP_connection:" ,response);
        return response;
    }
}
