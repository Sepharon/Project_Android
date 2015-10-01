package com.iha.group2.dronecontrol;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class UDP_Receiver extends Service {

    static final int UDP_port = 8888;
    static final int timeout = 10000;

//    boolean first = true;
//    static final int TCP_port = 10000;
//    Socket socket_tcp;
//    OutputStream out;
//    PrintWriter output;

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
                    msg = get_msg(ip, action, UDP_port);
                    Log.v("Service:", "Msg = " + msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            /*
            case "camera":
                try {
                    msg = get_msg(ip, action, UDP_port);
                    //msg = tcp_client(ip,action,TCP_port);
                    Log.v("Service:", "Msg = " + msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;*/
            case "GPS":
                try {
                    //tcp_client(ip,action,UDP_port);
                    get_msg(ip, action, UDP_port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "Stop":
                try {
                    get_msg(ip, action, UDP_port);
                    //tcp_client(ip, action, TCP_port);
                    //socket_tcp.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                stopSelf();

            default:
                Log.v("Service Receiver:", "Unknown Action " + action);
        }
        return START_STICKY;
    }


    public String get_msg (String ip, String msg, int port) throws IOException{
        /*
        Variables declaration
         */
        byte[] receive_data = new byte[64];
        String rec_msg = "";
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
        DatagramPacket receive_pkt = new DatagramPacket(receive_data,receive_data.length);
        // Timeout
        socket.setSoTimeout(timeout);
        // We wait until we receive a packet or timeout happens

        try {
            Log.v("Service Receiver:", "Waiting for data");
            socket.receive(receive_pkt);
            Log.v("Service Receiver:", "Data received");
            rec_msg = new String(receive_pkt.getData());
            Log.v("Service Receiver", "Data received: " + rec_msg.split("\n")[0]);
            socket.close();
            String ms = rec_msg.split("\n")[0];
            switch (ms) {
                case "Stop":
                    broadcast_toInit(rec_msg, 0);
                    break;
                case "alive":
                    broadcast_toInit(rec_msg, 0);
                    break;
                default:
                    broadcast_result(rec_msg, 0);
                    break;
            }


        }
        catch (SocketTimeoutException e) {
            Log.v("Service Receiver:", "Timeout");
            socket.close();
            if (msg.equals("connect")) broadcast_toInit("error",0);
        }

        Log.v("Client:", "Out of loop");
        return rec_msg;
    }

    /*public String tcp_client(String ip, String msg, int port) throws  IOException{


        InetAddress IP = InetAddress.getByName(ip);
        if (first) {
            Log.v("TCP_connection:", ""+first);
            socket_tcp = new Socket(IP, port);
            first=false;
        }
        out = socket_tcp.getOutputStream();
        socket_tcp.setSoTimeout(10000);
        output = new PrintWriter(out);
        BufferedReader in;
        String response;

        output.println(msg);
        output.flush();
        Log.v("TCP_connection:", "Sent msg");
        in = new BufferedReader(new InputStreamReader(socket_tcp.getInputStream()));

        response = in.readLine();
        Log.v("TCP_connection:", "Message received");
        Log.v("TCP_connection:", response);

        if (response.equals("Stop")){
            broadcast_toInit(response,0);
        }
        else if (response.equals("alive")){
            broadcast_toInit(response, 0);
        }

        return response;
    }*/

    public void broadcast_result(String msg,int act){
        Intent broadcast = new Intent();
        broadcast.setAction("broadcast");
        broadcast.putExtra("action",act);
        broadcast.putExtra("result", msg);
        sendBroadcast(broadcast);
    }

    public void broadcast_toInit(String msg,int act){
        Intent broadcast = new Intent();
        broadcast.setAction("init");
        broadcast.putExtra("action",act);
        broadcast.putExtra("result", msg);
        sendBroadcast(broadcast);
    }
}
