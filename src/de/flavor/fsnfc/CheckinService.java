package de.flavor.fsnfc;

import de.flavor.fsnfc.dto.CheckinResponse;
import de.flavor.fsnfc.dto.Venue;
import de.flavor.fsnfc.util.FoursquareClient;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class CheckinService extends IntentService {

	public static int CHECKIN_NOTIFICATION = 10;
	
	private FoursquareClient client;
	private NotificationManager notificationManager;
	
	public CheckinService()
	{
		super("CheckinService");
	}
	
	public CheckinService(String name) {
		super(name);
	}
	
	

	@Override
	public void onCreate() {
		super.onCreate();
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// token stuff
		SharedPreferences prefs = getSharedPreferences("oauth", MODE_PRIVATE);
		if (prefs != null && prefs.contains("access_token")) {
			String token = prefs.getString("access_token", "NOVALUE");
			Log.d("demo", "Got an access_token: " + token);
			client = new FoursquareClient(token, this);
		} else {
			Log.d("demo", "No access token, please authorize...");
			Toast.makeText(this, "Please link to your Foursquare Account!",
					Toast.LENGTH_LONG);
			startActivity(new Intent(this, DashboardActivity.class));
		}		
	}



	@Override
	protected void onHandleIntent(Intent intent) {
		
		if (intent.hasExtra("venue"))
		{
			Venue venue = (Venue) intent.getSerializableExtra("venue");

			notificationManager.cancel(CHECKIN_NOTIFICATION);
			beepNotification(venue);
			
			CheckinResponse response = null;
			try {
				response = client.checkin(venue);
			} catch (Exception e) {
				Log.e("demo", e.getMessage(), e);
			}
			
			if (venue == null || response == null)
			{
				Toast.makeText(this.getApplicationContext(), "Canoot check you in - is there a working internet connection?", Toast.LENGTH_LONG).show();
				return;
			}
			else
			{
				
				String msg = response.getMessageNotification().message;
				CharSequence contentTitle = null;
				CharSequence contentText = null;
				
				if (msg != null && msg.contains(".")) {
					contentTitle = response.venue.getTitle();
					contentText = msg.substring(msg.indexOf(".")+1).trim();
				}
				else
				{
					contentTitle = "NFC Checkin";
					contentText = response.getMessageNotification().message;
				}
							
				Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
				notificationIntent.setData(Uri.parse("https://foursquare.com/v/place/" + venue.getId()));
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);				
				
				Notification noti = new Notification.Builder(getApplicationContext())
					.setContentTitle(contentTitle)
					.setContentText(contentText)
					.setSmallIcon(R.drawable.notification_icon)
					.setTicker(response.venue.getTitle())
					.setContentIntent(contentIntent)
					.setWhen(System.currentTimeMillis())
					.setAutoCancel(true)
					.getNotification();
				
				notificationManager.notify(CheckinService.CHECKIN_NOTIFICATION, noti);
			}
		}
	}

	private void beepNotification(Venue venue) {
		
		Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
		notificationIntent.setData(Uri.parse("https://foursquare.com/v/" + venue.getId()));
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);				
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useSound = prefs.getBoolean("use_sound", false);
				
		Notification noti = null;
		if (useSound)
		{
		
			noti = new Notification.Builder(getApplicationContext())
				.setContentTitle(getString(R.string.checkin_notification_title))
				.setContentText(getString(R.string.checkin_notification_text))
				.setSmallIcon(R.drawable.notification_icon)
				.setTicker(getString(R.string.checkin_notification_tickertext))
				.setSound(Uri.parse("android.resource://de.flavor.fsnfc/" +R.raw.beep))
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis())
				.getNotification();
		}
		else
		{
			noti = new Notification.Builder(getApplicationContext())
				.setContentTitle(getString(R.string.checkin_notification_title))
				.setContentText(getString(R.string.checkin_notification_text))
				.setSmallIcon(R.drawable.notification_icon)
				.setTicker(getString(R.string.checkin_notification_tickertext))
				.setContentIntent(contentIntent)
				.setWhen(System.currentTimeMillis())
				.getNotification();
		}
		
		notificationManager.notify(CheckinService.CHECKIN_NOTIFICATION, noti);
		
	}

}
