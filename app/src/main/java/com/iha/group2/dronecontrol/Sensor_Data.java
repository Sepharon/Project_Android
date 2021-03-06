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
import android.os.SystemClock;
import android.util.Log;

/*
This class extends a service and implements a Sensor event listener.
It is responsible to get data from the accelerometer of the device
and decide if the user wants to go UP, stay or DOWN
 */

public class Sensor_Data extends Service implements SensorEventListener {

    private SensorManager mSensorManager;

    // Values to calculate position
    float initial_value;
    // Threshold values to check the movement.
    float threshold_high = 1.5f;
    float threshold_low = -1.5f;
    float value = 0;
    boolean first = true;

    String ip;

    Drone drone;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Subscribe to sensor manager to get updates on the sensors.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // We want to get data from the accelerometer
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // We want short delay between updates 0.6S delay
        mSensorManager.registerListener(this, mSensor/*,SensorManager.SENSOR_DELAY_UI*/,1000000,1000000);
        if (intent!=null) {
            value = intent.getFloatExtra("start_value",0);
            Log.v("sensor_",value+"");
            if (value != 0) initial_value = value;
        }
        drone = Drone.getInstance();
        try {
            ip = drone.getIP();
        } catch (NullPointerException es){
            Log.v("SENSOR_DATA: ", "ip null pointer");
            es.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister from sensor
        mSensorManager.unregisterListener(this);
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get values from sensor
        // Our interest is in accel_X
        float x = event.values[0];
        float actual_value;
        int res;
        String u_d;
        Log.v("Sensor, x = ", "" + x);
        // Get ready to send data
        Intent intent = new Intent(getBaseContext(),UDPconnection.class);

        if (initial_value == 0){
            // Set the first value as the initial_value.
            initial_value = x;
            Log.v("Sensor_",""+initial_value);
            broadcast_result(""+initial_value,7);
            first = false;
        }
        // Substract current value and initial_value
        actual_value = x - initial_value;
        // Get movement
        res = calculate_movement(actual_value);
        // Depending on the movement we send Up, Down or Normal
        if (res == 2) {
            u_d = "U";
            intent.putExtra("value",u_d);
            //intent.putExtra("ip", ip);
            intent.putExtra("action","");
            startService(intent);
        }
        else if (res == 1) {
            //D
            u_d = "D";
            intent.putExtra("value",u_d);
            //intent.putExtra("ip", ip);
            intent.putExtra("action","");
            startService(intent);
        }
        else {
            //N
            u_d = "N";
            intent.putExtra("value",u_d);
            //intent.putExtra("ip", ip);
            intent.putExtra("action","");
            startService(intent);

        }


    }

    // This function calculates the movement of the phone
    // In case the value is higher than the threshold_high it means that the user wants to move
    // the drone up
    public int calculate_movement(float value){
        if (value > threshold_high) {
            Log.v("Sensor a_v:", "high");
            return 2;
        }
        // In case it's lower than threshold_low it means that it want to go down.
        else if (value < threshold_low){
            Log.v("Sensor a_v:", "low");
            return 1;
        }
        // Else it wants to stay the same altitude.
        else return 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void broadcast_result(String msg,int act){
        Intent broadcast = new Intent();
        broadcast.setAction("broadcast");
        broadcast.putExtra("action",act);
        broadcast.putExtra("result", msg);
        sendBroadcast(broadcast);
    }
}
