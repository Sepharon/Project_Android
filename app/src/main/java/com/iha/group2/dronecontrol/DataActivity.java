package com.iha.group2.dronecontrol;


import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by sophie on 05/10/2015.
 */
public class DataActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);


        Button save_button = (Button) findViewById(R.id.save_button);


        /*save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                if (isExternalStorageWritable()) {
                    getFileStorageDir("Weather's data" + date.toString());
                } else {
                    Toast.makeText(DataActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }

            }
        }


            Checks if external storage is available for read and write
            public boolean isExternalStorageWritable() {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    return true;
                }
                return false;
            }

            public File getFileStorageDir(String fileName) {
                // Get the directory for the user's public pictures directory.
                //File file = new File(Environment.getExternalStoragePublicDirectory(
                if (!file.mkdirs()) {
                    Log.e("Error : ", "Directory not created");
                }
                return file;
            }
        });

        Environment.DIRECTORY_PICTURES, fileName);*/
    }
}

