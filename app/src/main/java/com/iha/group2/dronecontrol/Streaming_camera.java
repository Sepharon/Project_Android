package com.iha.group2.dronecontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

/*REFERENCE:
https://developer.android.com/training/system-ui/immersive.html
http://developer.android.com/reference/android/webkit/WebView.html
 */

/* This Activity creates a WebView to stream video from the IP camera */

public class Streaming_camera extends AppCompatActivity {

    //Some initializations
    static final String url = "http://192.168.43.251:8080/browserfs.html";
    RelativeLayout layout;
    WebView browser;
    int sX;
    int sY;

    //it configures the WebView and loads the url
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_camera);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // Hide ActionBar
        try{
            actionBar.hide();
        } catch (NullPointerException es){
            es.printStackTrace();
        }

        sX=640;
        sY=480;
        // Setting up WebView
        browser = (WebView) findViewById(R.id.webView);
        layout = (RelativeLayout)findViewById(R.id.stream_layout);
        browser.setInitialScale(getScale());
        browser.getSettings().setLoadWithOverviewMode(true);
        browser.getSettings().setUseWideViewPort(true);
        browser.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        //Load WebView
        browser.loadUrl(url);
    }


    //This functions make a fullscreen view, some parameters requires API level 16
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Fullscreen activity
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
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    private int getScale() {
        DisplayMetrics size = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(size);
        Double val = new Double(size.widthPixels) / new Double(sX);
        Double val2 = new Double(size.heightPixels) / new Double(sY);
        val = Math.min(val, val2);
        val = val * 100d;
        return val.intValue();
    }
}
