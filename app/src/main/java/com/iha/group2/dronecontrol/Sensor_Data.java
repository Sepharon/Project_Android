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


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister from sensor
        mSensorManager.unregisterListener(this);
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
        //float y = event.values[1];
        //float z = event.values[2];
        Log.v("Sensor, x = ", "" + x);
        //Log.v("Sensor, y = ",""+y);
        //Log.v("Sensor, z = ",""+z);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
