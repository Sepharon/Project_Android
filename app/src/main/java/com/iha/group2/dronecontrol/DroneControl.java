package com.iha.group2.dronecontrol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


// TODO : NOT IMPLEMENTED YET

public class DroneControl extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_control);

        Intent in = getIntent();
        final String ip = in.getStringExtra("ip");
        Button forward = (Button) findViewById(R.id.forward);
        Button backward = (Button) findViewById(R.id.backward);
        Button right = (Button) findViewById(R.id.Right);
        Button left = (Button) findViewById(R.id.Left);

        Button up = (Button) findViewById(R.id.up_button);
        Button down = (Button) findViewById(R.id.down_button);

        Log.v("Drone Control ip: ",ip);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data("F", ip,"s");
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
                send_data("D",ip,"");
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drone_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    public void send_data(String v,String ip, String action){
        Intent intent = new Intent(getBaseContext(),UDPconnection.class);
        intent.putExtra("value",v);
        intent.putExtra("ip",ip);
        intent.putExtra("action",action);
        startService(intent);

    }
}
