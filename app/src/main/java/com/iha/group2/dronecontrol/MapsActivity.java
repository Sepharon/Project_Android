package com.iha.group2.dronecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
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
 */

/*This Activity is responsible to monitor the Drone and it gets the current position of it and set up a
Google Maps with a Marker showing the location of the Drone.
It also gets weather's data and stores it in the database.
 */
public class MapsActivity extends FragmentActivity {

    //Some initializations
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker marker;
    boolean ask_camera=false;
    String ip;

    Button forward;
    Button backward;
    Button right;
    Button left;
    Button up;
    Button down;
    Button less_v;
    Button more_v;
    Button photo;
    Button save;
    Button RR;
    Button RL;

    IntentFilter filter;
    CountDownTimer t;
    private MyReceiver receiver;
    boolean connected;
    Thread t_move;
    Thread t_internet;
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

        //Get instance for Drone class and set status to connected
        drone = Drone.getInstance();
        drone.setStatus(true);

        //Register receiver
        filter = new IntentFilter("broadcast");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);

        isPressed=false; //this one determines if a button is pressed

        //Intent in = getIntent();
        //ip = in.getStringExtra("ip");

        //Get Drone IP
        ip=drone.getIP();

        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);
        right = (Button) findViewById(R.id.right);
        left = (Button) findViewById(R.id.left);

        up = (Button) findViewById(R.id.up_bt);
        down = (Button) findViewById(R.id.down);

        photo = (Button) findViewById(R.id.take_photo);
        save = (Button) findViewById(R.id.save_button);

        less_v = (Button)findViewById(R.id.less_button);
        more_v = (Button)findViewById(R.id.more_button);

        RR = (Button)findViewById(R.id.rotate_right_bt);
        RL = (Button)findViewById(R.id.rotate_left_bt);

        layout = (RelativeLayout)findViewById(R.id.map_layout);

        connected=true;

        Log.v("Drone Control ip: ", ip);
        Log.v("Drone connected", drone.getStatus() ? "connected" : "not connected");

        /*here we have some listeners, it determines if a button was touched and
        depending on the motion event, it does one thing or another.
        In case that the Motion Event is that the Button is Down (pressed), it starts a thread
        which keeps sending the same message til an Action Up event (the button is not pressed) occurs.
        It allows to send to the Arduino to do something repeatedly while a Button is pressed, such as
        if you want to move forward, you will be pressing the Button forward to go in that direction
        and when you want to stop, you will released the button.
         */
        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("forwardButton", "actionDOWN");
                    isPressed = true;
                    moving("F");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("forwardButton", "actionReleased");
                    isPressed = false;
                    t_move.interrupt();
                }
                return false;
            }
        });
        backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("forwardButton", "actionDOWN");
                    isPressed = true;
                    moving("B");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("forwardButton", "actionReleased");
                    isPressed = false;
                    t_move.interrupt();
                }
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("forwardButton", "actionDOWN");
                    isPressed = true;
                    moving("R");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("forwardButton", "actionReleased");
                    isPressed = false;
                    t_move.interrupt();
                }
                return false;
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("forwardButton", "actionDOWN");
                    isPressed = true;
                    moving("L");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("forwardButton", "actionReleased");
                    isPressed = false;
                    t_move.interrupt();
                }
                return false;
            }
        });
        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("forwardButton", "actionDOWN");
                    isPressed = true;
                    moving("U");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("forwardButton", "actionReleased");
                    isPressed = false;
                    t_move.interrupt();
                }
                return false;
            }
        });
        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("forwardButton", "actionDOWN");
                    isPressed = true;
                    moving("D");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("forwardButton", "actionReleased");
                    isPressed = false;
                    t_move.interrupt();
                }
                return false;
            }
        });

        RR.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("forwardButton", "actionDOWN");
                    isPressed = true;
                    moving("RR");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("forwardButton", "actionReleased");
                    isPressed = false;
                    t_move.interrupt();
                }
                return false;
            }
        });
        RL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("forwardButton", "actionDOWN");
                    isPressed = true;
                    moving("RL");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("forwardButton", "actionReleased");
                    isPressed = false;
                    t_move.interrupt();
                }
                return false;
            }
        });

        //this ones are send only one time
        less_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("LV", "");
            }
        });
        more_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("MV", "");
            }
        });
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.cancel();
                ask_camera = true;
                Intent in = new Intent(MapsActivity.this, UDP_Receiver.class);
                stopService(in);
                Intent intent2 = new Intent(MapsActivity.this, Sensor_Data.class);
                stopService(intent2);
                Intent intent = new Intent(MapsActivity.this, Streaming_camera.class);
                startActivity(intent);
                //receive_data("camera", ip);
            }
        });



        // It gets the current position of the drone
        receive_data("GPS");

        //this counter asks for GPS data every 20 seconds
        t = new CountDownTimer(20000,1000){
            public void onTick (long millisUntilFinished){}
            public void onFinish(){
                if (!ask_camera && connected ) {
                    receive_data("GPS");
                    start();
                }
            }
        }.start();

        Intent intent = new Intent(getBaseContext(), Sensor_Data.class);
        //intent.putExtra("ip", ip);
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
            // Check if we were successful in obtaining the map.
            if (mMap != null) setUpMap(0,0);
        }
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
            marker.remove();
        }catch (Exception e){
            e.printStackTrace();
        }
        marker = mMap.addMarker(new MarkerOptions().position(pos).title("Drone"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 1));
        // Change zoom factor if needed
        CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
    }

    //It sends data without expecting incoming messages
    public void send_data(String v, String action){
        Intent intent = new Intent(getBaseContext(),UDPconnection.class);
        intent.putExtra("value", v);
        //intent.putExtra("ip", ip);
        intent.putExtra("action", action);
        startService(intent);
    }

    //it sends data expecting incoming messages
    public void receive_data (String action){
        Intent intent = new Intent(getBaseContext(),UDP_Receiver.class);
        //intent.putExtra("ip", ip);
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
            // May need to change
            switch (action) {
                case 0: //GPS
                    // Way to send data = 50.2-45.0-
                    String lat = result.split("-")[0];
                    String lng = result.split("-")[1];
                    Log.v("Map Activity: ", result);
                    Log.v("Map Activity: ", "lat: " + lat);
                    Log.v("Map Activity: ", "lng: " + lng);
                    setUpMap(Float.parseFloat(lat), Float.parseFloat(lng));
                    break;
                case 1: //stop
                    Toast.makeText(MapsActivity.this, "UDP connection closed", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.v("Map Activity:","Unknown action = " +action);
            }
        }
    }


    /*it registers our receiver, it sets that we are connected and we are not streaming video,
    it restarts the Sensor service and gets GPS data.
     */

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("MapsActivity2", "onResume");
        this.registerReceiver(receiver, filter);
        connected=true;
        ask_camera=false;
        drone.setStatus(true);
        Intent intent = new Intent(getBaseContext(),Sensor_Data.class);
        //intent.putExtra("ip", ip);
        startService(intent);
        receive_data("GPS");
        t.start();
        setUpMapIfNeeded();
    }


    /*It sends Stop to Arduino to stop the motors, this allows to process events like
    incoming calls, the app is going background, etc.
    It sets that is not connected, it stops the running services and it unregisters our receiver
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.v("MapsActivity2", "onPause");
        receive_data("Stop");
        drone.setStatus(false);
        t.cancel();
        t_internet.interrupt();
        connected=false;
        Intent intent = new Intent(getBaseContext(),Sensor_Data.class);
        stopService(intent);
        Intent in = new Intent(getBaseContext(),UDP_Receiver.class);
        stopService(in);
        this.unregisterReceiver(receiver);
    }
/*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.v("MapsActivity2", "onBackPressed");
        t.cancel();
        connected=false;
        receive_data("Stop", ip);
    }
*/


    /*this function keeps sending the same message every half second to the Arduino (or UDP server) til
    it is interrupted or isPressed equals false, that means that the user is no longer pressing that button
     */
    private void moving(final String movement){
       t_move = new Thread(new Runnable() {
           @Override
           public void run() {
               while(isPressed) {
                   Log.v("thread t_move", "moving");
                   send_data(movement, "");
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
            layout.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


}
