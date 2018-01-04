
package com.fapin.httputil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fapin.httputil.HttpUtils.HTTP_TYPE;
import com.fapin.httputil.HttpUtils.PROTOCOL_TYPE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class HttpUtils {
    private static final String TAG = HttpUtils.class.getSimpleName();

    public Activity mActivity = null;

    public IHttpCallback mCallback = null;

    private String mUrl = null;

    private String mStringParams = null;

    public static HttpUtils mHttpUtil = null;

    public enum HTTP_TYPE {
        GET, POST
    }

    public enum PROTOCOL_TYPE {
        HTTP, HTTPS
    }

    private static HTTP_TYPE mType = HTTP_TYPE.GET;

    private static PROTOCOL_TYPE mProtocolType = PROTOCOL_TYPE.HTTP;

    public static HttpUtils getInstance() {
        if (mHttpUtil != null) {
            return mHttpUtil;
        }
        Log.d(TAG, "please new HttpUtil first!");
        return null;
    }

    public static void deleteHttpUtil() {
        if (mHttpUtil != null) {
            mHttpUtil = null;
        }
    }

    public static HttpUtils getHttpUtil(final String url, final String stringParams,
            Activity activity, IHttpCallback callback) {

        mHttpUtil = new HttpUtils(url, stringParams, activity, callback);
        return mHttpUtil;
    }

    private HttpUtils(final String url, final String stringParams, Activity activity,
            IHttpCallback callback) {

        mActivity = activity;
        mUrl = url;
        mCallback = callback;
        mStringParams = stringParams;

        // 判断是http请求还是https请求
        try {
            URL httpUrl = new URL(mUrl);
            if (httpUrl.getProtocol().toLowerCase().equals("https")) {
                mProtocolType = PROTOCOL_TYPE.HTTPS;
            } else if (httpUrl.getProtocol().toLowerCase().equals("http")) {
                mProtocolType = PROTOCOL_TYPE.HTTP;
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (mActivity == null) {
            Log.e(TAG, "activity is null");
            return;
        }

        if (callback == null) {
            Log.e(TAG, "callback is null");
            return;
        }

    }

    public void httpGet() {
        mType = HTTP_TYPE.GET;
        if (!mUrl.contains("?")) {
            mUrl = mUrl + "?" + mStringParams;
        } else if (mUrl.substring(mUrl.length() - 1).equals("?")) {
            mUrl = mUrl + mStringParams;
        }
        httpAccess();
    }

    public void httpPost() {
        mType = HTTP_TYPE.POST;
        httpAccess();
    }

    private void httpAccess() {

        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new HttpTask(mActivity, mCallback, mType, mProtocolType, mStringParams)
                            .execute(new String[] {
                                mUrl
                            });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class MytmArray implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
        // return null;
        return new X509Certificate[] {};
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // TODO Auto-generated method stub
        // System.out.println("cert: " + chain[0].toString() + ", authType: "
        // + authType);
    }
};
