package de.flavor.fsnfc;

import java.util.Date;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.flavor.fsnfc.dto.Venue;
import de.flavor.fsnfc.util.ConnectionUtil;
import de.flavor.fsnfc.util.FoursquareClient;

public class SearchVenuesActivity extends Activity implements LocationListener {

	private final static int DIALOG_CHECKIN_DIRECTLY = 1;


	private LocationManager locationManager;
	private FoursquareClient client;
	private ArrayAdapter<Venue> adapter;
	private Location currentLocation = null;

	private ListView list;
	private TextView address;
	private RelativeLayout wrapper;
	private ProgressBar progress;

	private SearchTask task = null;
	private boolean updateVenues = true;

	private Venue currentlySelectedVenue = null;

	public static final int CODE_TAG_WRITTEN = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		wrapper = (RelativeLayout) findViewById(R.id.wrapper);
		address = (TextView) findViewById(R.id.address);

		list = (ListView) findViewById(R.id.list);

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Venue venue = adapter.getItem(position);
				// Toast.makeText(SearchVenuesActivity.this, venue.getTitle(),
				// Toast.LENGTH_SHORT).show();
				writeToTag(venue);
			}

		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentlySelectedVenue = adapter.getItem(position);
				showDialog(DIALOG_CHECKIN_DIRECTLY);
				return true;

			}
		});
		progress = (ProgressBar) findViewById(R.id.progress);

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		

		SharedPreferences prefs = getSharedPreferences("oauth", MODE_PRIVATE);
		if (prefs.contains("access_token")) {
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
	protected Dialog onCreateDialog(int id) {
		Dialog d = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (id) {
		case DIALOG_CHECKIN_DIRECTLY:

			builder.setMessage("")
					// changed in onPrepareDialog
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									if (currentlySelectedVenue != null) {
										Intent checkinIntent = new Intent(
												SearchVenuesActivity.this,
												CheckinActivity.class);
										checkinIntent.putExtra("manualCheckin", true);
										checkinIntent.putExtra("venue",
												currentlySelectedVenue);
										startActivity(checkinIntent);
									}
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			d = builder.create();

			break;

		default:
			d = super.onCreateDialog(id);

		}

		return d;

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_CHECKIN_DIRECTLY:
			if (currentlySelectedVenue != null) {
				AlertDialog alert = (AlertDialog) dialog;
				alert.setMessage("Would you like to checkin directly to "
						+ currentlySelectedVenue.getTitle() + "?");
			}
			break;

		default:
			super.onPrepareDialog(id, dialog);
		}

	}

	private void writeToTag(Venue venue) {

		NdefRecord packageRecord = NdefRecord.createApplicationRecord("de.flavor.fsnfc");
		NdefRecord uriRecord = NdefRecord.createUri("http://flavor.de/" + venue.getId());
		NdefMessage msg = new NdefMessage(new NdefRecord[] {uriRecord, packageRecord});
		//NdefMessage msg = NFCUtil.getNdefWellKnownURI("flavor.de/" + venue.getId());
		
		Intent i = new Intent(this, WriteActivity.class);
		i.putExtra(WriteActivity.NDEF_MESSAGE, msg);
		startActivityForResult(i, CODE_TAG_WRITTEN);

	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);

		if (task != null)
			task.cancel(true);

		this.updateVenues = true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (ConnectionUtil.isAirplaneModeOn(this.getApplicationContext()))
		{
			Toast.makeText(this.getApplicationContext(), "Airplane Mode is on, please turn it off to use this app!", Toast.LENGTH_LONG).show();
			return;
		}
		
		if (!this.updateVenues) {
			Log.d("demo", "Not updating venues, user just wrote a tag...");
			return;
		}

		requestLocationAndSearch();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_TAG_WRITTEN
				&& resultCode == Activity.RESULT_OK) {
			this.updateVenues = false;
		} else
			this.updateVenues = true;

	}

	public void startVenueSearch(Location loc) {
		Log.d("demo",
				"Starting venue search with this location: " + loc.toString());

		this.currentLocation = loc;
		task = new SearchTask();
		task.execute(loc);
	}

	private void updateListView(List<Venue> venues, Location loc) {
		progress.setVisibility(View.GONE);
		adapter = new ArrayAdapter<Venue>(this, android.R.layout.simple_list_item_1, venues);
		list.setAdapter(adapter);
		address.setText(getString(R.string.near, loc.getLatitude() + ", " + loc.getLongitude()));
		wrapper.setVisibility(View.VISIBLE);

	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			requestLocationAndSearch();
			return true;
		case R.id.map:
			Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.geo,
							Double.toString(currentLocation.getLatitude()),
							Double.toString(currentLocation.getLongitude()))));
			startActivity(mapIntent);
			return true;
		case R.id.preferences:
			Intent prefActivity = new Intent(this, Preferences.class);
			startActivity(prefActivity);
		return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (this.currentLocation != null)
			menu.getItem(1).setEnabled(true);
		else
			menu.getItem(1).setEnabled(false);
		return true;
	}

	private void requestLocationAndSearch() {
	
		if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Toast.makeText(this, "PLease enable network location provider!",
					Toast.LENGTH_LONG).show();
			startActivity(new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			return;
		}


		Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		showSearchUI();
		if (loc.getTime() > (new Date().getTime() - 1000*2*60)) //2Minutes back
		{
			Log.d("demo", "LastKnownLocation is good enough, using this location!");
			startVenueSearch(loc);
		}
		else
		{
			Log.d("demo", "Requesting network location updates...");
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
		




	}

	private void showSearchUI() {
		progress.setVisibility(View.VISIBLE);
		wrapper.setVisibility(View.GONE);
	}



	private class SearchTask extends
			AsyncTask<Location, Void, List<Venue>> {

		private Location loc = null;

		@Override
		protected List<Venue> doInBackground(Location... params) {
			loc = params[0];
			return SearchVenuesActivity.this.client.searchVenues(loc, 50);
		}

		@Override
		protected void onPostExecute(List<Venue> result) {
			SearchVenuesActivity.this.updateListView(result, loc);
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		locationManager.removeUpdates(this);
		Log.d("demo", "Got fresh location from "+location.getProvider()+", beginning... " + location.getLatitude() + " / " + location.getLongitude());
		//fake hybris
		//location.setLatitude(48.150098d);
		//location.setLongitude(11.546026d);
		
		startVenueSearch(location);
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
