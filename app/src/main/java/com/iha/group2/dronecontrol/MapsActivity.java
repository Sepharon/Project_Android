package com.iha.group2.dronecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker marker;
    boolean ask_camera=false;
    String ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        IntentFilter filter = new IntentFilter("broadcast");
        this.registerReceiver(new MyReceiver(), filter);

        Intent in = getIntent();
        ip = in.getStringExtra("ip");
        Button forward = (Button) findViewById(R.id.forward);
        Button backward = (Button) findViewById(R.id.backward);
        Button right = (Button) findViewById(R.id.right);
        Button left = (Button) findViewById(R.id.left);

        Button up = (Button) findViewById(R.id.up);
        Button down = (Button) findViewById(R.id.down);

        Button photo = (Button) findViewById(R.id.take_photo);
        Button save = (Button) findViewById(R.id.save_button);

        // TODO: implement off function

        Log.v("Drone Control ip: ", ip);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("F", ip, "");
            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("B",ip,"");
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("R",ip,"");
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("L",ip,"");
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("U",ip,"");
            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("D", ip, "");
            }
        });
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receive_data("camera",ip);
            }
        });
        // Might need to this in the beginning
        //receive_data("GPS",ip);

        new CountDownTimer(20000,1000){
            public void onTick (long millisUntilFinished){}
            public void onFinish(){
                if (!ask_camera) {
                    receive_data("GPS", ip);
                    start();
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( pos, 1));
        // Change zoom factor if needed
        CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
    }

    public void send_data(String v,String ip, String action){
        Intent intent = new Intent(getBaseContext(),UDPconnection.class);
        intent.putExtra("value",v);
        intent.putExtra("ip",ip);
        intent.putExtra("action",action);
        startService(intent);
    }

    public void receive_data (String action, String ip){
        Intent intent = new Intent(getBaseContext(),UDP_Receiver.class);
        intent.putExtra("ip",ip);
        intent.putExtra("action",action);
        startService(intent);
    }


    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra("action",2);
            String result = intent.getStringExtra("result");
            // May need to change
            switch (action) {
                case 0:
                    // Way to send data = 50,45,
                    String lat = result.split("-")[0];
                    String lng = result.split("-")[1];
                    Log.v("Map Activity: ", result);
                    Log.v("Map Activity: ", "lat: " + lat);
                    Log.v("Map Activity: ", "lng: " + lng);
                    setUpMap(Float.parseFloat(lat), Float.parseFloat(lng));
                    break;

                case 1:
                    //TODO do whatever it takes for TCP connection (CAMERA)
                    break;

                default:
                    Log.v("Map Activity:","Unknown action = " +action);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        send_data("OFF", ip, "");
    }

    public void stream(){
        Intent intent = new Intent(this, Streaming_camera.class);
        startActivity(intent);
    }
}
