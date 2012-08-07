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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class DashboardActivity extends Activity {

	private final String CALLBACK_URL = "sven://foursquare";
	private final String PREF_ACCESS_TOKEN = "access_token";
	
	private static final int DIALOG_UPDATE = 0;
	
	private final String PREF_VERSION = "version";
	private Button searchButton;
	private Button authorizeButton;
	private SharedPreferences prefs; 
	private int currentVersion;
	private String currentVersionName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		searchButton = (Button) findViewById(R.id.searchButton);
		authorizeButton = (Button) findViewById(R.id.authorizeButton);
		
		prefs = getSharedPreferences("oauth", MODE_PRIVATE);
		
		PackageInfo pinfo;
		try {
			pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			currentVersion = pinfo.versionCode;
			currentVersionName = pinfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e("demo", "Unable to access versionNumber", e);
		}
				
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		int lastVersion = prefs.getInt(PREF_VERSION, -1);
        if(currentVersion > lastVersion ){
        	showDialog(DIALOG_UPDATE);
        	Editor editor = prefs.edit();
        	editor.putInt(PREF_VERSION, currentVersion);
        	editor.commit();
        }

		Uri uri = this.getIntent().getData();
		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
			String code = uri.getQueryParameter("code");
			Log.d("demo", "Authorization Code: " + code);

			String tokenRequest = "https://foursquare.com/oauth2/access_token?grant_type=authorization_code&redirect_uri=sven%3A%2F%2Ffoursquare&client_id="+getClientId()+"&client_secret="+getClientSecret()+"&code="
					+ code;
			HttpClient http = new DefaultHttpClient();
			HttpGet get = new HttpGet();
			get.setURI(URI.create(tokenRequest));
			try {
				HttpResponse response = http.execute(get);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				StringBuffer sb = new StringBuffer();
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
				String page = sb.toString();
				
				JSONObject json = new JSONObject(page);
				String token = json.getString("access_token");
				Log.d("demo", "Access Token: " + token);
				
				SharedPreferences prefs = getSharedPreferences("oauth", MODE_PRIVATE);
				Editor editor = prefs.edit();
				editor.putString(PREF_ACCESS_TOKEN, token);
				
				if (editor.commit())
					Log.d("demo", "Access Token saved to preferences!");
				
				authorizeButton.setVisibility(View.GONE);
				searchButton.setVisibility(View.VISIBLE);
				
				
			} catch (ClientProtocolException e) {
				Log.e("demo", "Unable to get oauth token...", e);
			} catch (IOException e) {
				Log.e("demo", "Unable to get oauth token...", e);
			} catch (JSONException e) {
				Log.e("demo", "Unable parse json...", e);
			}

		}
		else
		{
			
			if (prefs.contains("access_token"))
			{
				Log.d("demo", "Got an access_token: " + prefs.getString("access_token", "NOVALUE"));
				authorizeButton.setVisibility(View.GONE);
				searchButton.setVisibility(View.VISIBLE);		
			}
			else
			{
				Log.d("demo", "No access token, please authorize...");
				authorizeButton.setVisibility(View.VISIBLE);
				searchButton.setVisibility(View.GONE);				
			}
		}

	}
	
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_UPDATE:
	        dialog = createUpdateDialog();
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}	

	private Dialog createUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.news, this.currentVersionName) )
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		           }
		       });
		       
		AlertDialog alert = builder.create();		
		return alert;
	}

	public void authorize(View v) {
		String authUrl = "https://foursquare.com/oauth2/authorize?response_type=code&client_id="+getClientId()+"&redirect_uri=sven%3A%2F%2Ffoursquare";
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
	}
	
	public void search(View v) {
		startActivity(new Intent(this, SearchVenuesActivity.class));
	}
	
	private String getClientSecret() {
		return getString(R.string.client_secret);
	}

	private String getClientId() {
		return getString(R.string.client_id);
	}	
}