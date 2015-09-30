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

public class Sensor_Data extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    float initial_value;
    float threshold_high = 0.5f;
    float threshold_low = -0.5f;
    boolean first = true;

    String ip;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);

        try {
            ip = intent.getStringExtra("ip");
        } catch (NullPointerException es){
            es.printStackTrace();
        }
        if (ip.equals("")) {
            // Unregister from sensor
            mSensorManager.unregisterListener(this);
            stopSelf();
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
        // Our interest is in accel_X or accel_Z
        float x = event.values[0];
        float actual_value;
        int res;
        String u_d;
        Log.v("Sensor, x = ", "" + x);
        Intent intent = new Intent(getBaseContext(),UDPconnection.class);

        if (first){
            initial_value = x;
            first = false;
        }
        actual_value = x - initial_value;
        res = calculate_movement(actual_value);
        if (res == 2) {
            u_d = "U";
            intent.putExtra("value",u_d);
            intent.putExtra("ip", ip);
            intent.putExtra("action","");
            startService(intent);
        }
        else if (res == 1) {
            u_d = "D";
            intent.putExtra("value",u_d);
            intent.putExtra("ip", ip);
            intent.putExtra("action","");
            startService(intent);
        }


    }

    public int calculate_movement(float value){

        int result;
        if (value > threshold_high) {
            Log.v("Sensor a_v:", "high");
            result = 2;
        }

        else if (value < threshold_low){
            Log.v("Sensor a_v:", "low");
            result = 1;
        }
        else return 0;
        threshold_high = value+0.25f;
        threshold_low = value-0.5f;
        return result;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
