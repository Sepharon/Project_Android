package com.iha.group2.dronecontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

/*REFERENCE:
https://developer.android.com/training/system-ui/immersive.html
 */

/* This Activity creates a WebView to stream video from the IP camera */

public class Streaming_camera extends AppCompatActivity {

    //Some initializations
    static final String url = "http://192.168.0.105:8080/browserfs.html";
    RelativeLayout layout;
    WebView browser;

    //it configures the WebView and loads the url
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_camera);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        try{
            actionBar.hide();
        } catch (NullPointerException es){
            es.printStackTrace();
        }

        browser = (WebView) findViewById(R.id.webView);
        layout = (RelativeLayout)findViewById(R.id.stream_layout);

        browser.setInitialScale(25);
        browser.getSettings().setLoadWithOverviewMode(true);
        browser.getSettings().setUseWideViewPort(true);
        browser.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        browser.loadUrl(url);
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
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }
}
