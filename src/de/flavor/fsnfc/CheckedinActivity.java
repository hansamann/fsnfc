package de.flavor.fsnfc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import de.flavor.fsnfc.dto.CheckinResponse;

public class CheckedinActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkedin);
		
		TextView msg = (TextView)findViewById(R.id.msg);
		
		AdView adView = (AdView)this.findViewById(R.id.adView);
	    adView.loadAd(new AdRequest());		
		
		if (getIntent().hasExtra("checkinResponse"))
		{
			CheckinResponse response = (CheckinResponse)getIntent().getSerializableExtra("checkinResponse");
			if (response.hasMessage())
				msg.setText(response.getMessageNotification().message);
			else
				msg.setText("Cool! We got you at " + response.venue.getTitle() + "!");
		}
		else
		{
			startActivity(new Intent(this, DashboardActivity.class));
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	        startActivity(new Intent(this, SearchVenuesActivity.class));
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_checkedin, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.preferences:
			Intent prefActivity = new Intent(this, Preferences.class);
			startActivity(prefActivity);
		return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}	

}
