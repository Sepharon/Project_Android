package com.iha.group2.dronecontrol;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/*REFERENCE:
http://developer.android.com/training/basics/network-ops/connecting.html
 */

/*This class extends a Service
It sends a message to Arduino (or UDP server) and then it receives an answer from it
 */

public class UDP_Receiver extends Service {

    //Some initializations
    static final int UDP_port = 8888;
    static final int timeout = 10000;

    Drone drone;
    String ip;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Depending on the Action, it does one thing or another
    public int onStartCommand(final Intent intent, int flags, int startId) {
        //final String ip = intent.getStringExtra("ip");
        drone = Drone.getInstance();
        ip = drone.getIP();
        Log.v("UDP_receiver", ip);
        final String action = intent.getStringExtra("action");
        Log.v("actionUDP", ""+action);
        String msg;

        switch (action) {
            case "connect": //this comes from InitActivity when you want to connect
                try {
                    msg = get_msg(ip, action, UDP_port);
                    Log.v("Service:", "Msg = " + msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "Check":
                //Check internet connection
                if (!isNetworkAvailable()){ //returns false if internet is not available
                    broadcast_toInit("NoInternet", 0);
                    broadcast_result("NoInternet", 2);
                }
                else{
                    broadcast_result("Internet", 3);
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
            case "GPS": //this comes from MapsActivity when you request GPS data
                try {
                    //tcp_client(ip,action,UDP_port);
                    get_msg(ip, action, UDP_port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "Stop": //this comes from MapsActivity when you want to say to Arduino to stop the movement of the drone
                try {
                    get_msg(ip, action, UDP_port);
                    //tcp_client(ip, action, TCP_port);
                    //socket_tcp.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                drone.setStatus(false);
                stopSelf();

            default:
                Log.v("Service Receiver:", "Unknown Action " + action);
        }
        return START_STICKY;
    }


    //It sends data and processes the received message
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
            //this variable splits the messages received by \n because the buffer can contain others undesired characters
            String ms = rec_msg.split("\n")[0];
            // Check which message we've received and react accodingly
            switch (ms) {
                //TODO: before we had unregistered our receivers, when the message was Stop it sent to InitActivity
                // TODO: this message and a Toast appears in InitActivity saying that the UDP connection was closed,
                // TODO: now, the message never arrive because the Receiver from Init is unregistered.
                case "Stop":
                    broadcast_result(rec_msg, 1);
                    Toast.makeText(getApplicationContext(),"Stop",Toast.LENGTH_LONG).show();
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
            drone.setStatus(false);
            socket.close();
            if (msg.equals("connect")) broadcast_toInit("error",0);
        }

        Log.v("Client:", "Out of loop");
        return rec_msg;
    }

    //It broadcast the result to MapsActivity
    public void broadcast_result(String msg,int act){
        Intent broadcast = new Intent();
        broadcast.setAction("broadcast");
        broadcast.putExtra("action",act);
        broadcast.putExtra("result", msg);
        sendBroadcast(broadcast);
    }

    //It broadcast the result to InitActivity
    public void broadcast_toInit(String msg,int act){
        Intent broadcast = new Intent();
        broadcast.setAction("init");
        broadcast.putExtra("action",act);
        broadcast.putExtra("result", msg);
        sendBroadcast(broadcast);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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
}
