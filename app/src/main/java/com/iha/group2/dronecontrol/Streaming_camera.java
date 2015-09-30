package com.iha.group2.dronecontrol;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class Streaming_camera extends AppCompatActivity {

    static final String url = "http://192.168.0.105:8080/browserfs.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_camera);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        WebView browser = (WebView) findViewById(R.id.webView);

        browser.getSettings().setLoadWithOverviewMode(true);
        browser.getSettings().setUseWideViewPort(true);

        browser.loadUrl(url);
    }
}
