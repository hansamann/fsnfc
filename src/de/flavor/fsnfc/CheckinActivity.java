package de.flavor.fsnfc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import de.flavor.fsnfc.dto.CheckinResponse;
import de.flavor.fsnfc.dto.Venue;
import de.flavor.fsnfc.util.ConnectionUtil;
import de.flavor.fsnfc.util.FoursquareClient;

public class CheckinActivity extends Activity {

	protected FoursquareClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.checkin);

		// token stuff
		SharedPreferences prefs = getSharedPreferences("oauth", MODE_PRIVATE);
		if (prefs.contains("access_token")) {
			String token = prefs.getString("access_token", "NOVALUE");
			Log.d("demo", "Got an access_token: " + token);
			client = new FoursquareClient(token, this);
		} else {
			Log.d("demo", "No access token, please authorize...");
			Toast.makeText(this, "Please link to your Foursquare Account!",
					Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, DashboardActivity.class));
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		//nfc turned off in airplane mode, remove after this is 100% sure
		if (ConnectionUtil.isAirplaneModeOn(this.getApplicationContext()))
		{
			Toast.makeText(this.getApplicationContext(), "Airplane Mode is on, please turn it off to use this app!", Toast.LENGTH_LONG).show();
			return;
		}		
		
		Venue venue = null;
		
		//direct checkin or via nfc tag
		if (getIntent().hasExtra("venue")) //direct checkin
		{
			venue = (Venue)getIntent().getSerializableExtra("venue");
			CheckinTask task = new CheckinTask();
			task.execute(venue);
			return;
		}
		else if (getIntent().hasExtra(NfcAdapter.EXTRA_TAG))
		{
			// tag stuff		
			Tag tag = getIntent().getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

			Ndef ndef = Ndef.get(tag);
			NdefMessage message = ndef.getCachedNdefMessage();
			
			for (NdefRecord record : message.getRecords()) {
				if (record.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
					venue = new Venue(new String(record.getPayload()));
				}
				else if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_URI))
				{
					String url = "http://" + new String(record.getPayload());
					Log.d("demo", "Just payload: " + new String(record.getPayload()));
					
					try {
						venue = new Venue(new URL(url).getPath().substring(1)); //removes initial / of path
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					Log.d("demo", "RTD WELL KNOWN / URI: " + url);
					Log.d("demo", "path/venueid: " + venue.getId());
					
				}
				
			}
			
			//we got a venue, start intent service
			Intent serviceIntent = new Intent(getApplicationContext(), CheckinService.class);
			serviceIntent.putExtra("venue", venue);
			startService(serviceIntent);
					
			Intent next = new Intent(Intent.ACTION_MAIN);
			next.addCategory(Intent.CATEGORY_HOME);
			next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(next);
			
			finish();
		}
		

	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getBooleanExtra("finish", false))
			finish();
	}

	private class CheckinTask extends AsyncTask<Venue, Void, CheckinResponse> {

		@Override
		protected CheckinResponse doInBackground(Venue... params) {
			return CheckinActivity.this.client.checkin(params[0]);
		}

		@Override
		protected void onPostExecute(CheckinResponse response) {

			Intent i = null;
			if (response == null) {
				Toast.makeText(
						CheckinActivity.this,
						"Unable to checkin... Foursquare API might be down or network connection failure occurred. Please try again later.",
						Toast.LENGTH_LONG).show();
				i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://status.foursquare.com"));
			}else
			{
				i = new Intent(CheckinActivity.this, CheckedinActivity.class);
				i.putExtra("checkinResponse", response);
			}

			startActivity(i);
			

		}

	}
}
