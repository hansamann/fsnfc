package de.flavor.fsnfc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import de.flavor.fsnfc.dto.Venue;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.util.Log;
import android.widget.Toast;

public class TagReceiver extends BroadcastReceiver {

	private NotificationManager notificationManager;
	
	
	@Override
	public void onReceive(Context ctx, Intent intent) {

		// token stuff
		SharedPreferences prefs = ctx.getSharedPreferences("oauth", Context.MODE_PRIVATE);
	
		if (!prefs.contains("access_token")){
			Log.d("demo", "No access token, please authorize...");
			Toast.makeText(ctx, "Please link to your Foursquare Account!",
					Toast.LENGTH_LONG);
			ctx.startActivity(new Intent(ctx, DashboardActivity.class));
		}
		else if (intent.hasExtra(NfcAdapter.EXTRA_TAG))
		{
			//start intent service, as it might take longer due to network connectivity
			Venue venue = null;
			
			Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);

			Ndef ndef = Ndef.get(tag);
			NdefMessage message = ndef.getCachedNdefMessage();
			StringBuilder b = new StringBuilder();
			
			for (NdefRecord record : message.getRecords()) {
				if (record.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
					venue = new Venue(new String(record.getPayload()));
				}
				else if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.getType(), NdefRecord.RTD_URI))
				{
					String url = "http://" + new String(record.getPayload(), 1, record.getPayload().length-1);
					try {
						venue = new Venue(new URL(url).getPath().substring(1)); //removes initial / of path
					} catch (MalformedURLException e) {
						Log.e("demo", "Unable to parse URL...", e);
					}
					Log.d("demo", "RTD WELL KNOWN / URI: " + url);
					Log.d("demo", "path/venueid: " + venue.getId());
					
				}
			}
			
			notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			int icon = R.drawable.notification_icon;
			CharSequence tickerText = "Processing checkin...";
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);

			CharSequence contentTitle = "NFC Checkin";
			
			CharSequence contentText = "Currently checking into " + venue.getTitle();
			
							
			//Intent notificationIntent = new Intent(this, MyClass.class);
			//PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);				
			
			notification.setLatestEventInfo(ctx, contentTitle, contentText, null);
			notificationManager.notify(CheckinService.CHECKIN_NOTIFICATION, notification);
			
			//we got a venue, start intent service
			Intent serviceIntent = new Intent(ctx, CheckinService.class);
			serviceIntent.putExtra("venue", venue);
			ctx.startService(serviceIntent);
		}
		
	}

}
