package net.henryhu.andwell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.List;

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
import org.apache.http.params.HttpParams;

import android.util.Log;

public class Utils {
	public static final String PREFS_FILE = "MainPref";

	public static String getOAuthRedirectURI(String basePath)
	{
		return basePath + "/auth/displaycode";
	}

	public static HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new UnsafeSSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 8080));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}

	public static HttpResponse doGet(String basePath, String path, List<NameValuePair> params)
	throws IOException 
	{
		String args = URLEncodedUtils.format(params, "UTF-8");
		
		HttpGet get = new HttpGet(basePath + path + "?" + args);
		
		Log.d("get path", basePath + path + "?" + args);
		
		HttpClient hc = Utils.getNewHttpClient();
		HttpResponse resp = null;
		resp = hc.execute(get);
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
		HttpClient hc = Utils.getNewHttpClient();
		HttpResponse resp = null;
		resp = hc.execute(post);
		return resp;
	}
	
	public static String fillTo(String orig, int target)
	{
		StringBuilder sb = new StringBuilder(orig);
		for (int i=0; i<target - orig.length(); i++)
			sb.append(' ');
		return sb.toString();
	}
	
	public static String readAll(InputStream is) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder(1024);
		char[] buf = new char[1024];
		int nread = 0;
		while ((nread = br.read(buf)) != -1)
		{
			sb.append(buf, 0, nread);
		}
		br.close();
		return sb.toString();
	}

}