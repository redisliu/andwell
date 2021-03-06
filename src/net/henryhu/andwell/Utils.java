package net.henryhu.andwell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class Utils {
	public static final String PREFS_FILE = "MainPref";
	public static final int soTimeoutMs = 10000;
	public static final int connTimeoutMs = 20000;
	public static boolean debug = false;
	
	static Object clientLock = new Object();
	static HttpClient client = null;

	public static String getOAuthRedirectURI(String basePath)
	{
		return "andwell://andwell/oauth_redirect";
//		return basePath + "/auth/displaycode";
	}

	public static HttpClient getNewHttpClient() {
		HttpClient result = null;
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new UnsafeSSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(params, connTimeoutMs);
		    HttpConnectionParams.setSoTimeout(params, soTimeoutMs);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 8080));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        result = new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        result = new DefaultHttpClient();
	    }
	    return result;
	}

	public static HttpResponse doGet(String basePath, String path, List<NameValuePair> params)
	throws IOException 
	{
		String args = URLEncodedUtils.format(params, "UTF-8");
		
		HttpGet get = new HttpGet(basePath + path + "?" + args);
		
		if (debug)
			Log.d("get path", basePath + path + "?" + args);

		HttpResponse resp = null;
		synchronized(clientLock) {
			if (client == null) {
				client = getNewHttpClient();
			}
			resp = client.execute(get);
		}
		return resp;
	}

	public static HttpResponse doPost(String basePath, String path, List<NameValuePair> params)
	throws IOException 
	{
		HttpPost post = new HttpPost(basePath + path);
		StringEntity ent = null;
		try {
			ent = new StringEntity(URLEncodedUtils.format(params, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)	{ }
		ent.setContentType(URLEncodedUtils.CONTENT_TYPE);
		post.setEntity(ent);
		HttpResponse resp = null;
		synchronized(clientLock) {
			if (client == null) {
				client = getNewHttpClient();
			}

			resp = client.execute(post);
		}
		return resp;
	}
	
	public static String fillTo(String orig, int target)
	{
		return String.format("%" + String.valueOf(target) + "s", orig);
	}
	
	public static String readAll(InputStream is) throws IOException
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder(1024);
			char[] buf = new char[1024];
			int nread = 0;
			while ((nread = br.read(buf)) != -1)
			{
				sb.append(buf, 0, nread);
			}
			br.close();
			if (debug)
				Log.d("ReadAll", "ret: " + sb.toString());
			return sb.toString();
		} finally {
			is.close();
		}
	}
	
	public static void checkResult(HttpResponse resp)
			throws NotFoundException, OutOfRangeException, ServerErrorException {
		int respCode = resp.getStatusLine().getStatusCode();
		if (respCode == 200)
		{
			return;
		} else {
			HttpEntity ent = resp.getEntity();
			if (ent != null) {
				try {
					ent.getContent().close();
				} catch (IllegalStateException e) {
				} catch (IOException e) {
				}
			}
			if (respCode == 404)
				throw new NotFoundException(resp.getStatusLine().getReasonPhrase());
			if (respCode == 416)
				throw new OutOfRangeException(resp.getStatusLine().getReasonPhrase());
			else
				throw new ServerErrorException(resp.getStatusLine().getReasonPhrase());
		}
	}
	
	public static String readResp(HttpResponse resp) throws IllegalStateException, IOException {
		return readAll(resp.getEntity().getContent());
	}
	
	public static void showToast(Context context, String message) {
		Toast toast = Toast.makeText(context,
				message,
				Toast.LENGTH_LONG);
		toast.show();
	}
	
	public static boolean useDualPane(Context context, double inchLeft, double inchRight) {
		DisplayMetrics dm = new DisplayMetrics();
	    WindowManager wm = (WindowManager)(context.getSystemService(Context.WINDOW_SERVICE));
	    wm.getDefaultDisplay().getMetrics(dm);
	    double x = dm.widthPixels/dm.xdpi;
	    // XXX: shall we depend on the real requirements of both panes?
	    // XXX: what the hell is this magic number?
	    // TODO: automatic resize
	    if (x >= inchLeft + inchRight)
	    	return true;
	    else
	    	return false;
//	    double y = dm.heightPixels/dm.ydpi;
	    
	    
	}

}
