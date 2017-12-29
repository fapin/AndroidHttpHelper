
package com.fapin.androidtest;

import com.fapin.httputil.HttpUtils;
import com.fapin.httputil.IHttpCallback;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String urlParams = "aa=valueaa&bb=valuebb";
        // "{\"name\":\"Bob\",\"age\":18}"
        String url = "https://www.xx.com";
        HttpUtils httpUtils = HttpUtils.getHttpUtil(url, urlParams, MainActivity.this,
                new IHttpCallback() {

                    public void onResponse(String result) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "string from server: " + result);
                    }

                });

        httpUtils.httpPost();
    }
}
