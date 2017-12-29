package com.fapin.androidtest;


import com.fapin.httputil.HttpUtils;
import com.fapin.httputil.IHttpCallback;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String urlParams = "imei=1134567890&componentVersion=testforchina_v1.0&vendor=SPRD&locale=en&model=testforchina&mac=00:01:02:03:04:05"; //Ҳ���� "{\"name\":\"Bob\",\"age\":18}"  
	    String url = "https://api2.thunderfota.com/ThunderFOTA/api/check";//Ҳ����https://XXX  
	    HttpUtils httpUtils = HttpUtils.getHttpUtil(url, urlParams, MainActivity.this, new IHttpCallback(){  
	  
	        public void onResponse(String result) {  
	            // TODO Auto-generated method stub  
	                Log.d("hhp", "string from server: " + result);  
	        }  
	          
	    });  
	      
	    httpUtils.httpPost();
	}
}
