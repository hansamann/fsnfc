package de.flavor.fsnfc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class AccessTokenTask extends AsyncTask<String, Void, String> {

	private DashboardActivity activity;
	private final String PREF_ACCESS_TOKEN = "access_token";
	
	
	
	public AccessTokenTask(DashboardActivity activity) {
		super();
		this.activity = activity;
	}

	@Override
	protected String doInBackground(String... params) {
		String authorization_code = params[0];
		String client_id = activity.getString(R.string.client_id);
		String client_secret = activity.getString(R.string.client_secret);
		
		
		String access_token = null;
		String tokenRequest = "https://foursquare.com/oauth2/access_token?grant_type=authorization_code&redirect_uri=sven%3A%2F%2Ffoursquare&client_id="+client_id+"&client_secret="+client_secret+"&code=" + authorization_code;
		
		HttpClient http = new DefaultHttpClient();
		HttpGet get = new HttpGet();
		get.setURI(URI.create(tokenRequest));
		try {
			HttpResponse response = http.execute(get);
			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer();
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			String page = sb.toString();
			
			JSONObject json = new JSONObject(page);
			access_token = json.getString("access_token");
			Log.d("demo", "Access Token: " + access_token);
			
			
		} catch (ClientProtocolException e) {
			Log.e("demo", "Unable to get oauth access token...", e);
		} catch (IOException e) {
			Log.e("demo", "Unable to get oauth access token...", e);
		} catch (JSONException e) {
			Log.e("demo", "Unable parse json...", e);
		}		
		
		return access_token;
	}

	@Override
	protected void onPostExecute(String access_token) {
		super.onPostExecute(access_token);
		
		SharedPreferences prefs = activity.getSharedPreferences("oauth", Activity.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(PREF_ACCESS_TOKEN, access_token);
		
		if (editor.commit())
			Log.d("demo", "Access Token saved to preferences!");
		
		activity.allowSearchUI();
	}

}
