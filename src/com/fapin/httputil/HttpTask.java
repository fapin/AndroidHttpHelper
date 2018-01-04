
package com.fapin.httputil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.fapin.httputil.HttpUtils.HTTP_TYPE;
import com.fapin.httputil.HttpUtils.PROTOCOL_TYPE;

public @SuppressLint("NewApi")
class HttpTask extends AsyncTask<String, Void, String> {
    private ProgressDialog dialog;

    private Activity mActivity = null;

    private IHttpCallback mIHttpCallback = null;

    private HTTP_TYPE mType = HTTP_TYPE.GET;

    private PROTOCOL_TYPE mProtocolType = PROTOCOL_TYPE.HTTP;

    private final int CONNECTION_TIMEOUT = 5000; // 建立连接超时时间 5s

    private final int READ_TIMEOUT = 5000; // 数据传输超时时间 5s

    private String mParams = "";

    public HttpTask(Activity activity, IHttpCallback callback, HTTP_TYPE type,
            PROTOCOL_TYPE protocolType, String params) {
        super();
        mActivity = activity;
        mIHttpCallback = callback;
        mType = type;
        mParams = params;
        mProtocolType = protocolType;
    }

    static TrustManager[] xtmArray = new MytmArray[] {
        new MytmArray()
    };

    /**
     * 信任所有主机-对于任何证书都不做检查
     */
    private static void trustAllHosts() {
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, xtmArray, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }
    };

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mActivity != null) {
            dialog = ProgressDialog.show(mActivity, "提示", "操作请求正在发送，请稍等", true, false);
        }

    }

    @Override
    protected String doInBackground(String... urls) {

        if (urls == null || urls.length == 0) {
            return null;
        }

        String result = "";
        HttpURLConnection httpUrlCon = null;

        try {

            // new a url connection
            URL httpUrl = new URL(urls[0]);
            switch (mProtocolType) {
                case HTTP:
                    httpUrlCon = (HttpURLConnection) httpUrl.openConnection();
                    break;
                case HTTPS:
                    trustAllHosts();
                    httpUrlCon = (HttpsURLConnection) httpUrl.openConnection();
                    ((HttpsURLConnection) httpUrlCon).setHostnameVerifier(DO_NOT_VERIFY);// 不进行主机名确认
                    break;
                default:
                    break;
            }

            // set http configure
            httpUrlCon.setConnectTimeout(CONNECTION_TIMEOUT);// 建立连接超时时间
            httpUrlCon.setReadTimeout(READ_TIMEOUT);// 数据传输超时时间，很重要，必须设置。
            httpUrlCon.setDoInput(true); // 向连接中写入数据
            httpUrlCon.setDoOutput(true); // 从连接中读取数据
            httpUrlCon.setUseCaches(false); // 禁止缓存
            httpUrlCon.setInstanceFollowRedirects(true);
            httpUrlCon.setRequestProperty("Charset", "UTF-8");
            httpUrlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            switch (mType) {
                case GET:
                    httpUrlCon.setRequestMethod("GET");// 设置请求类型为
                    break;
                case POST:
                    httpUrlCon.setRequestMethod("POST");// 设置请求类型为
                    DataOutputStream out = new DataOutputStream(httpUrlCon.getOutputStream()); // 获取输出流
                    out.write(mParams.getBytes("utf-8"));// 将要传递的数据写入数据输出流,不要使用out.writeBytes(param);
                                                         // 否则中文时会出错
                    out.flush(); // 输出缓存
                    out.close(); // 关闭数据输出流
                    break;
                default:
                    break;

            }

            httpUrlCon.connect();

            // check the result of connection
            if (httpUrlCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader in = new InputStreamReader(httpUrlCon.getInputStream()); // 获得读取的内容
                BufferedReader buffer = new BufferedReader(in); // 获取输入流对象
                String inputLine = "";
                while ((inputLine = buffer.readLine()) != null) {
                    result += inputLine + "\n";
                }
                in.close(); // 关闭字符输入流
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果需要处理超时，可以在这里写
        } finally {
            if (httpUrlCon != null) {
                httpUrlCon.disconnect(); // 断开连接
            }
        }
        Log.d("HttpTask", "result_str: " + result);
        return result;

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (dialog != null && mActivity != null) {
            dialog.dismiss();
        }
        mIHttpCallback.onResponse(result);
    }
}
