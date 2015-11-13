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

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;


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
    CountDownTimer t;
    CountDownTimer t_internet;
    private MyReceiver receiver;
    //boolean connected;
    boolean restore;
    Thread t_move;
    boolean isPressed;

    RelativeLayout layout;

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

        //up = (Button) findViewById(R.id.up_bt);
        //down = (Button) findViewById(R.id.down);

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

        //connected=true;
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
                        isPressed = true;
                        moving("RR");
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
                        isPressed = true;
                        moving("RL");
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
                    send_data("LV");
                }else {
                    Toast.makeText(MapsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        more_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drone.getStatus()) {
                    send_data("MV");
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
                    //ask_camera = true;
                    Intent in = new Intent(MapsActivity.this, UDP_Receiver.class);
                    stopService(in);
                    Intent intent2 = new Intent(MapsActivity.this, Sensor_Data.class);
                    stopService(intent2);
                    Intent intent = new Intent(MapsActivity.this, Streaming_camera.class);
                    startActivity(intent);
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
        receive_data("GPS");

        // This counter asks for GPS data every 20 seconds
        t = new CountDownTimer(20000,1000){
            public void onTick (long millisUntilFinished){}
            public void onFinish(){
                //if (!ask_camera /*&& connected*/ ) {
                 // Ask GPS data
                 receive_data("GPS");
                 // Start counter again
                 start();
                //}
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
        try {
            // Remove current marker
            marker.remove();
        }catch (Exception e){
            e.printStackTrace();
        }
        // Create new marker
        marker = mMap.addMarker(new MarkerOptions().position(pos).title("Drone"));
        // Move camera to new position
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 1));
        // Change zoom factor if needed
        CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(7.0f).build();
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
                    /* Way to send data = GPS!50.2-45.0-Altitude-Speed
                    Thus we need to split the messages
                    with '-'
                    */
                    String lat = result.split("-")[0];
                    String lng = result.split("-")[1];
                    String alt = result.split("-")[2];
                    String speed = result.split("-")[3];
                    Log.v("Map Activity: ", result);
                    Log.v("Map Activity: ", "lat: " + lat);
                    Log.v("Map Activity: ", "lng: " + lng);
                    Log.v("Map Activity: ", "Alt: " + alt);
                    Log.v("Map Activity: ", "Speed (knots): " + speed);
                    // Create new marker in the new position
                    setUpMap(Float.parseFloat(lat), Float.parseFloat(lng));
                    speed_text.setText(((float) (Float.parseFloat(speed)*0.514444))+" m/s");
                    Altitude_text.setText(alt+" m");
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
                    Weather\n50.1-20.1-\n80\n5.1\n20\n (GPS/Humidity/Speed/Temperature)
                    so we want to split to \n
                     */
                    //get current date and time
                    String ts = DateFormat.getDateTimeInstance().format(new Date());

                    //split the result
                    String GPS = result.split("\n")[1];
                    String HUMIDITY = result.split("\n")[2];
                    String SPEED = result.split("\n")[3];
                    String TEMP = result.split("\n")[4];

                    //create new content values to store in the database
                    values = new ContentValues();

                    values.put(SQL_IP_Data_Base.DateTime, ts);
                    values.put(SQL_IP_Data_Base.GPS, GPS);
                    values.put(SQL_IP_Data_Base.Humidity, HUMIDITY);
                    values.put(SQL_IP_Data_Base.Speed, SPEED);
                    values.put(SQL_IP_Data_Base.Temperature, TEMP);

                    //insert to the database
                    getContentResolver().insert(SQL_IP_Data_Base.CONTENT_URI_DATA, values);
                    Log.v("Map Activity: ", result);
                    Log.v("Map Activity: ", "GPS: " + GPS);
                    Log.v("Map Activity: ", "HUMI: " + HUMIDITY);
                    Log.v("Map Activity: ", "SPEED: " + SPEED);
                    Log.v("Map Activity: ", "TEMP: " + TEMP);

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
        // Assume connection is established.
        //connected=true;
        //ask_camera=false;
        drone.setStatus(true);
        //Check network connection
        receive_data("Check");
        // Start service class
        Intent intent = new Intent(getBaseContext(),Sensor_Data.class);
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
        receive_data("Stop");
        // Connection set to false, no more data is going to be sent
        drone.setStatus(false);
        //connected=false;
        //Cancel counters
        t.cancel();
        t_internet.cancel();
        // Stop services
        Intent intent = new Intent(getBaseContext(),Sensor_Data.class);
        stopService(intent);
        Intent in = new Intent(getBaseContext(),UDP_Receiver.class);
        stopService(in);
        // Unregister receiver
        this.unregisterReceiver(receiver);
    }


    /*this function keeps sending the same message every half second to the Arduino (or UDP server) til
    it is interrupted or isPressed equals false, that means that the user is no longer pressing that button
     */
    private void moving(final String movement){
       t_move = new Thread(new Runnable() {
           @Override
           public void run() {
               while(isPressed) {
                   Log.v("thread t_move", "moving");
                   send_data(movement);
                   SystemClock.sleep(500);

               }
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
