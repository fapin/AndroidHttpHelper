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

	public static HttpUtils getHttpUtil(final String url, final String stringParams, Activity activity,
			IHttpCallback callback) {

		mHttpUtil = new HttpUtils(url, stringParams, activity, callback);
		return mHttpUtil;
	}

	private HttpUtils(final String url, final String stringParams, Activity activity, IHttpCallback callback) {

		mActivity = activity;
		mUrl = url;
		mCallback = callback;
		mStringParams = stringParams;

		// �ж���http������https����
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
							.execute(new String[] { mUrl });
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
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// TODO Auto-generated method stub
		// System.out.println("cert: " + chain[0].toString() + ", authType: "
		// + authType);
	}
};

@SuppressLint("NewApi")
class HttpTask extends AsyncTask<String, Void, String> {
	private ProgressDialog dialog;
	private Activity mActivity = null;
	private IHttpCallback mIHttpCallback = null;
	private HTTP_TYPE mType = HTTP_TYPE.GET;
	private PROTOCOL_TYPE mProtocolType = PROTOCOL_TYPE.HTTP;
	private final int CONNECTION_TIMEOUT = 5000; // �������ӳ�ʱʱ�� 5s
	private final int READ_TIMEOUT = 5000; // ��ݴ��䳬ʱʱ�� 5s
	private String mParams = "";

	public HttpTask(Activity activity, IHttpCallback callback, HTTP_TYPE type, PROTOCOL_TYPE protocolType,
			String params) {
		super();
		mActivity = activity;
		mIHttpCallback = callback;
		mType = type;
		mParams = params;
		mProtocolType = protocolType;
	}

	static TrustManager[] xtmArray = new MytmArray[] { new MytmArray() };

	/**
	 * ������������-�����κ�֤�鶼�������
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
			dialog = ProgressDialog.show(mActivity, "��ʾ", "�����������ڷ��ͣ����Ե�", true, false);
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
				((HttpsURLConnection) httpUrlCon).setHostnameVerifier(DO_NOT_VERIFY);// ������������ȷ��
				break;
			default:
				break;
			}

			// set http configure
			httpUrlCon.setConnectTimeout(CONNECTION_TIMEOUT);// �������ӳ�ʱʱ��
			httpUrlCon.setReadTimeout(READ_TIMEOUT);// ��ݴ��䳬ʱʱ�䣬����Ҫ���������á�
			httpUrlCon.setDoInput(true); // ��������д�����
			httpUrlCon.setDoOutput(true); // �������ж�ȡ���
			httpUrlCon.setUseCaches(false); // ��ֹ����
			httpUrlCon.setInstanceFollowRedirects(true);
			httpUrlCon.setRequestProperty("Charset", "UTF-8");
			httpUrlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			switch (mType) {
			case GET:
				httpUrlCon.setRequestMethod("GET");// ������������Ϊ
				break;
			case POST:
				httpUrlCon.setRequestMethod("POST");// ������������Ϊ
				DataOutputStream out = new DataOutputStream(httpUrlCon.getOutputStream()); // ��ȡ�����
				out.write(mParams.getBytes("utf-8"));// ��Ҫ���ݵ����д����������,��Ҫʹ��out.writeBytes(param);
														// ��������ʱ�����
				out.flush(); // �������
				out.close(); // �ر���������
				break;
			default:
				break;

			}

			httpUrlCon.connect();

			// check the result of connection
			if (httpUrlCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStreamReader in = new InputStreamReader(httpUrlCon.getInputStream()); // ��ö�ȡ������
				BufferedReader buffer = new BufferedReader(in); // ��ȡ����������
				String inputLine = "";
				while ((inputLine = buffer.readLine()) != null) {
					result += inputLine + "\n";
				}
				in.close(); // �ر��ַ�������
			}
		} catch (Exception e) {
			e.printStackTrace();
			// �����Ҫ���?ʱ������������д
		} finally {
			if (httpUrlCon != null) {
				httpUrlCon.disconnect(); // �Ͽ�����
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
