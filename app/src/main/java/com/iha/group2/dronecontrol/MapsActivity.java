package com.iha.group2.dronecontrol;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/*REFERENCE:
https://developer.android.com/training/system-ui/immersive.html
https://developer.android.com/training/maps/index.html
 */

/*This Activity is responsible to monitor the Drone and it gets the current position of it and set up a
Google Maps with a Marker showing the location of the Drone.
It also gets weather's data and stores it in the database.
 */
public class MapsActivity extends FragmentActivity {

    //Some initializations
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker marker;
    //boolean ask_camera=false;
    String ip;

    ContentValues values;
    Button forward;
    Button backward;
    Button right;
    Button left;
    Button less_v;
    Button more_v;
    Button stream;
    Button save;
    Button RR;
    Button RL;
    TextView speed_text;
    TextView Altitude_text;

    IntentFilter filter;
    private MyReceiver receiver;

    CountDownTimer t;
    CountDownTimer t_internet;

    boolean restore = false;
    boolean isPressed;
    boolean camera_activity_halt = false;

    Thread t_move;
    Thread con;

    RelativeLayout layout;
    float initial_value = 0;
    //Drone class
    Drone drone;

    //This functions register our Receiver, it setups the Google Maps, it starts the Sensor service and it implements some onClickListeners
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MapsActivity2", "onCreate");
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        //Get instance for Drone class
        drone = Drone.getInstance();

        //Register receiver
        filter = new IntentFilter("broadcast");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);

        isPressed=false; //this one determines if a button is pressed

        //Get Drone IP
        ip=drone.getIP();
        drone.setStatus(true);

        //get some buttons ID
        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);
        right = (Button) findViewById(R.id.right);
        left = (Button) findViewById(R.id.left);

        stream = (Button) findViewById(R.id.take_photo);
        save = (Button) findViewById(R.id.save_button);

        less_v = (Button)findViewById(R.id.less_button);
        more_v = (Button)findViewById(R.id.more_button);

        RR = (Button)findViewById(R.id.rotate_right_bt);
        RL = (Button)findViewById(R.id.rotate_left_bt);

        // Get textview

        speed_text = (TextView)findViewById(R.id.speed_value);
        Altitude_text = (TextView)findViewById(R.id.Altitude_value);

        //this one will be used for full screen mode
        layout = (RelativeLayout)findViewById(R.id.map_layout);

        restore = false;

        Log.v("Drone Control ip: ", ip);
        Log.v("Drone connected", drone.getStatus() ? "connected" : "not connected");

        /*here we have some listeners, it determines if a button was touched and
        depending on the motion event, it does one thing or another.
        In case that the Motion Event is that the Button is Down (pressed), it starts a thread
        which keeps sending the same message til an Action Up event (the button is not pressed) occurs.
        It allows to send to the Arduino to do something repeatedly while a Button is pressed, such as
        if you want to move forward, you will be pressing the Button forward to go in that direction
        and when you want to stop, you will released the button.
        They also check if the status of the Drone class is connected, which means that the device is connected
        to a network and it can send messages
         */
        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (drone.getStatus()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("forwardButton", "actionDOWN");
                        if (t_move != null) t_move.interrupt();
                        isPressed = true;
                        moving("F");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("forwardButton", "actionReleased");
                        isPressed = false;
                        t_move.interrupt();
                    }
                }
                return false;
            }
        });
        backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (drone.getStatus()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("forwardButton", "actionDOWN");
                        if (t_move != null) t_move.interrupt();
                        isPressed = true;
                        moving("B");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("forwardButton", "actionReleased");
                        isPressed = false;
                        t_move.interrupt();
                    }
                }
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (drone.getStatus()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("forwardButton", "actionDOWN");
                        if (t_move != null) t_move.interrupt();
                        isPressed = true;
                        moving("R");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("forwardButton", "actionReleased");
                        isPressed = false;
                        t_move.interrupt();
                    }
                }
                return false;
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (drone.getStatus()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("forwardButton", "actionDOWN");
                        if (t_move != null) t_move.interrupt();
                        isPressed = true;
                        moving("L");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("forwardButton", "actionReleased");
                        isPressed = false;
                        t_move.interrupt();
                    }
                }
                return false;
            }
        });

        RR.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (drone.getStatus()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("forwardButton", "actionDOWN");
                        if (t_move != null) t_move.interrupt();
                        isPressed = true;
                        moving("SR");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("forwardButton", "actionReleased");
                        isPressed = false;
                        t_move.interrupt();
                    }
                }
                return false;
            }
        });
        RL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (drone.getStatus()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("forwardButton", "actionDOWN");
                        if (t_move != null) t_move.interrupt();
                        isPressed = true;
                        moving("SL");
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("forwardButton", "actionReleased");
                        isPressed = false;
                        t_move.interrupt();
                    }
                }
                return false;
            }
        });

        //these ones are send only one time, they also check if the status of the Drone class is connected,
        // which means that the device is connected to a network and it can send messages
        less_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drone.getStatus()) {
                    send_data("VD");
                }else {
                    Toast.makeText(MapsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        more_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drone.getStatus()) {
                    send_data("IV");
                }else {
                    Toast.makeText(MapsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*this listener calls the Streaming_camera Activity, but before this, it stops all the services
        that can be running, it cancels the counters for the GPS and network check
         */

        stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drone.getStatus()) {
                    t.cancel();
                    t_internet.cancel();

                    camera_activity_halt = true;
                    send_data("Halt");
                    Intent in = new Intent(MapsActivity.this, UDP_Receiver.class);
                    stopService(in);
                    Intent intent2 = new Intent(MapsActivity.this, Sensor_Data.class);
                    stopService(intent2);
                    Intent intent = new Intent(MapsActivity.this, Streaming_camera.class);
                    startActivity(intent);
                    // No IP camera available
                    //receive_data("camera", ip);
                }
            }
        });


        //This listener request for weather data
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drone.getStatus()) {
                    receive_data("Weather");
                    Toast.makeText(MapsActivity.this, "Data saved on the app", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // It gets the current position of the drone on create the activity
        //receive_data("GPS");

        // This counter asks for GPS data every 20 seconds
        t = new CountDownTimer(10000,1000){
            public void onTick (long millisUntilFinished){}
            public void onFinish(){
                receive_data("GPS");
                start();
                }
        }.start();

        // This counter checks if the device is connected to a network every 10 seconds
        t_internet = new CountDownTimer(10000,1000){
            public void onTick (long millisUntilFinished){}
            public void onFinish(){
                // Check connection
                receive_data("Check");
                start();
            }
        }.start();

        // Start Sensors_Data service
        Intent intent = new Intent(getBaseContext(), Sensor_Data.class);
        intent.putExtra("start_value",0);
        startService(intent);
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call  once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map and put initial marker in 0,0
            if (mMap != null) setUpMap(0,0);
        }
        // Put maps in Satellite mode
        if (mMap != null) mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(float lat , float lng) {
        LatLng pos = new LatLng(lat,lng);
        // Needed since first time there's no marker
        if (marker != null) {
            // Remove current marker
            marker.remove();
        }
        // Create new marker
        marker = mMap.addMarker(new MarkerOptions().position(pos).title("Drone"));
        // Move camera to new position
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 1));
        // Change zoom factor if needed
        CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);

    }

    // Sends data without expecting incoming messages
    public void send_data(String v){
        Intent intent = new Intent(getBaseContext(),UDPconnection.class);
        intent.putExtra("value", v);
        startService(intent);
    }

    // Sends data expecting incoming messages
    public void receive_data (String action){
        Intent intent = new Intent(getBaseContext(),UDP_Receiver.class);
        intent.putExtra("action", action);
        startService(intent);
    }


    /*this class extends our Receiver
    it processes GPS requests and Stop action.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra("action", 1);
            String result = intent.getStringExtra("result");

            switch (action) {
                // GPS result
                case 0:
                    /* Way to send data = GPS!Lat;Lng;Altitude;Speed;
                    Thus we need to split the messages
                    with ';'
                    */
                    String lat = result.split(";")[0];
                    String lng = result.split(";")[1];
                    String alt = result.split(";")[2];
                    String speed = result.split(";")[3];
                    Log.v("Map Activity: ", "lat: " + lat);
                    Log.v("Map Activity: ", "lng: " + lng);
                    Log.v("Map Activity: ", "Alt: " + alt);
                    Log.v("Map Activity: ", "Speed (knots): " + speed);
                    // Create new marker in the new position
                    try {
                        setUpMap(Float.parseFloat(lat), Float.parseFloat(lng));
                    }catch(NumberFormatException e){
                        e.printStackTrace();
                        Log.v("Maps Activity: ","GPS N error");

                    }
                    // Go from knots to m/s
                    speed_text.setText(((float) (Float.parseFloat(speed) * 0.514444)) + " m/s");
                    Altitude_text.setText(alt+" m");
                    drone.setStatus(true);
                    break;
                // Stop result
                case 1:
                    Toast.makeText(MapsActivity.this, "UDP connection closed", Toast.LENGTH_SHORT).show();
                    break;
                // No connection
                case 2:
                    // Set connection state from the drone class to false
                    drone.setStatus(false);
                    restore = true;
                    Toast.makeText(MapsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    break;
                //the connection was restored
                case 3:
                    // If connection was offline before, show message saying it's been restored
                    if (restore) {
                        Toast.makeText(MapsActivity.this, "Internet connection restored", Toast.LENGTH_SHORT).show();
                        send_data("Restore");
                        drone.setStatus(true);
                        restore=false;
                    }
                    break;
                //weather data received
                case 4:
                    /*
                    It receives the data this way:
                    Weather!GPS;Hour;Temp; (GPS/Humidity/Speed/Temperature)
                    so we want to split to -
                     */
                    Log.v("Map Activity: ", result);
                    //split the result

                    String LAT = result.split(";")[0];
                    String LNG = result.split(";")[1];
                    String HOUR = result.split(";")[2];
                    String TEMP = result.split(";")[3];
                    double celsius_temp;
                    try {
                         celsius_temp= Float.parseFloat(TEMP) * 0.0625;
                    }catch(NumberFormatException e){
                        e.printStackTrace();
                        celsius_temp = -0.0625;
                        Log.v("Maps Activity: ","N in floats error");
                    }
                    String ts="";
                    // We receive the time in the following format : HHMMSS
                    // thus we want to get HH:MM:SS
                    for (int i=0;i<HOUR.length();i++){
                        // if the value is not between a '0' or a '9' it means we have interferences
                        if (HOUR.charAt(i)> 0x39 || HOUR.charAt(i) < 0x30){
                            ts = "00:00:00";
                            break;
                        }
                        ts+=HOUR.charAt(i);
                        if (i==1 | i==3) ts+=":";
                        else if (i==5) break;

                    }
                    try {
                        setUpMap(Float.parseFloat(LAT), Float.parseFloat(LNG));
                    }catch(NumberFormatException e){
                        e.printStackTrace();
                        Log.v("Maps Activity: ","GPS N error");
                        LAT = "0.00";
                        LNG = "0.00";

                    }
                    //create new content values to store in the database
                    values = new ContentValues();
                    Log.v("MapsActivity","timestamp: " + ts);
                    values.put(SQL_IP_Data_Base.DateTime, ts);
                    values.put(SQL_IP_Data_Base.GPS, LAT+", "+LNG);
                    try {
                        if (celsius_temp == -0.0625) values.put(SQL_IP_Data_Base.Temperature,"No data, check the sensor");
                        else values.put(SQL_IP_Data_Base.Temperature,celsius_temp+"");
                    } catch (NumberFormatException es){
                        values.put(SQL_IP_Data_Base.Temperature, "NS ");
                    }
                    //insert to the database
                    getContentResolver().insert(SQL_IP_Data_Base.CONTENT_URI_DATA, values);
                    Log.v("Map Activity: ", result);
                    drone.setStatus(true);
                    break;

                // Deprecated
                case 6:
                    drone.setStatus(false);
                    if (con == null) {
                        Toast.makeText(MapsActivity.this, "Internet connection lost", Toast.LENGTH_SHORT).show();
                        con = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                t.cancel();
                                t_internet.cancel();
                                Intent intent = new Intent(getBaseContext(), Sensor_Data.class);
                                stopService(intent);
                                while (!drone.getStatus()) {
                                    receive_data("lostC");
                                    SystemClock.sleep(1000);
                                }

                            }
                        });
                        con.start();
                    }
                    break;
                // Deprecated
                case 7:
                    initial_value = Float.parseFloat(result);
                    break;

                default:
                    Log.v("Map Activity:","Unknown result = " +result);
            }
        }
    }


    /*it registers our receiver, it restarts the counters and it restarts the Sensor service and gets GPS data.
     */

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("MapsActivity2", "onResume");
        // Register received again
        this.registerReceiver(receiver, filter);

        camera_activity_halt = false;
        drone.setStatus(true);

        //Check network connection
        receive_data("Check");
        // Start service class
        Intent intent = new Intent(getBaseContext(),Sensor_Data.class);
        intent.putExtra("start_value",initial_value);
        startService(intent);
        // Ask GPS data
        receive_data("GPS");
        // Start counters again
        t.start();
        t_internet.start();
        // Set up map
        setUpMapIfNeeded();
    }


    /*It sends Stop to Arduino to stop the motors, this allows to process events like
    incoming calls, the app is going background, etc.
    It stops the running services and it unregisters our receiver
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.v("MapsActivity2", "onPause");
        // Tell arduino no more data is coming
        // We do this because when clicking the stream button we do not want to send "Stop" since that stops the motors
        if (!camera_activity_halt) receive_data("Stop");
        // Connection set to false, no more data is going to be sent
        drone.setStatus(false);
        //Cancel counters
        if (con != null) con.interrupt();
        t.cancel();
        t_internet.cancel();
        // Stop services
        Intent intent = new Intent(getBaseContext(),Sensor_Data.class);
        stopService(intent);
        Intent in = new Intent(getBaseContext(),UDP_Receiver.class);
        stopService(in);
        Intent ing = new Intent(getBaseContext(),UDPconnection.class);
        stopService(ing);
        // Unregister receiver
        this.unregisterReceiver(receiver);
    }

    // Nedded since pressing the back button to go to InitActivity would not stop the services
    @Override
    public void onBackPressed(){
        // Needed
        super.onBackPressed();
        // Tell arduino no more data is coming
        if (!camera_activity_halt) receive_data("Stop");
        // Connection set to false, no more data is going to be sent
        drone.setStatus(false);
        //Cancel counters
        if (con != null) con.interrupt();
        t.cancel();
        t_internet.cancel();
        // Stop services
        Intent intent = new Intent(getBaseContext(),Sensor_Data.class);
        stopService(intent);
        Intent in = new Intent(getBaseContext(),UDP_Receiver.class);
        stopService(in);
        Intent ing = new Intent(getBaseContext(),UDPconnection.class);
        stopService(ing);
    }


    /*this function keeps sending the same message every half second to the Arduino (or UDP server) til
    it is interrupted or isPressed equals false, that means that the user is no longer pressing that button
     */
    private void moving(final String movement){

       if (t_move!=null) t_move.interrupt();
       t_move = new Thread(new Runnable() {
           @Override
           public void run() {
               Intent intent = new Intent(getBaseContext(),Sensor_Data.class);
               stopService(intent);
               while(isPressed) {
                   send_data(movement);
                   SystemClock.sleep(500);
               }
               intent.putExtra("start_value",initial_value);
               startService(intent);
           }
       }); t_move.start();

    }

    //This functions make a fullscreen view, some parameters requires API level 16
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Set the IMMERSIVE flag.
            // Set the content to appear under the system bars so that the content
            // doesn't resize when the system bars hide and show.
            layout.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //hide navigation bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN //hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
