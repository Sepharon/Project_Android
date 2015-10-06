package com.iha.group2.dronecontrol;

/*
Code from http://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-accel
 */


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

/*This class extends a Service that implements a sensor listener to get
accelerometer sensor values from the device
 */

public class Sensor_Data extends Service implements SensorEventListener {

    //Some initializations
    private SensorManager mSensorManager;

    Drone drone;

    float initial_value; //it gets the initial position of the device
    float threshold_high = 1.5f; //it determines the threshold which if one value is higher than this, we consider it as going UP
    float threshold_low = -1.5f; //it determines the threshold which if one value is lower than this, we consider it as going DOWN
    boolean first = true;

    String ip;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //It initializes the sensor listener from the accelerometer sensor
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);

        drone = Drone.getInstance();

        /*try {
            ip = intent.getStringExtra("ip");
        } catch (NullPointerException es){
            Log.v("SENSOR_DATA: ", "ip null pointer");
            es.printStackTrace();
        }*/
        ip=drone.getIP();

        return START_STICKY;
    }

    //it unregisters the listener
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //it processes a sensor event to determine if we are going UP or DOWN
    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get values from sensor
        // Our interest is in accel_X or accel_Z
        float x = event.values[0];
        float actual_value;
        int res;
        String u_d;
        Log.v("Sensor, x = ", "" + x);
        Intent intent = new Intent(getBaseContext(),UDPconnection.class);

        //it determines the initial value of the device
        if (first){
            initial_value = x;
            first = false;
        }

        //it calculates the difference between the initial value and the event sensor value
        actual_value = x - initial_value;
        res = calculate_movement(actual_value);
        if (res == 2) { //Going UP
            u_d = "U";
            intent.putExtra("value",u_d);
            //intent.putExtra("ip", ip);
            intent.putExtra("action","");
            startService(intent);
        }
        else if (res == 1) { //Going DOWN
            u_d = "D";
            intent.putExtra("value",u_d);
            //intent.putExtra("ip", ip);
            intent.putExtra("action","");
            startService(intent);
        }
        else { //we are in the initial value
            u_d = "N";
            intent.putExtra("value",u_d);
            //intent.putExtra("ip", ip);
            intent.putExtra("action","");
            startService(intent);

        }
    }

    /*it calculates the difference between the current position and the initial to process it and see
    if its higher or less than the thresholds
     */
    public int calculate_movement(float value){

        int result;
        if (value > threshold_high) { //going UP
            Log.v("Sensor a_v:", "high");
            result = 2;
        }

        else if (value < threshold_low){ //going DOWN
            Log.v("Sensor a_v:", "low");
            result = 1;
        }
        else return 0; //not moving
        //threshold_high = value+0.5f;
        //threshold_low = value-0.5f;
        return result;
    }

    //this is not doing anything
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
