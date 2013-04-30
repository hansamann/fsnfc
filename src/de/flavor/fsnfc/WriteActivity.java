package de.flavor.fsnfc;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import de.flavor.fsnfc.util.NFCForegroundUtil;
import de.flavor.fsnfc.util.NFCUtil;

public class WriteActivity extends Activity {

	public static final int RESULT_FAILED = 2;
	public static final int RESULT_OK_LOCKED = 3;

	private final static int DIALOG_TAG_WRITTEN = 100;
	private final static int DIALOG_TAG_FAILURE_IO = 101;
	private final static int DIALOG_TAG_FAILURE_READONLY_SIZE = 102;
	private final static int DIALOG_TAG_FAILURE_FORMAT = 103;
	private final static int DIALOG_TAG_FAILURE_NOT_NDEF = 104;

	public static final String NDEF_MESSAGE = "NDEF_MESSAGE";
	private NdefMessage msg;
	private NFCForegroundUtil nfcForegroundUtil = null;
	private boolean makeReadonly = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write);

		// acquire tag sent to this activity using intent extra
		Intent i = getIntent();
		if (!i.hasExtra(NDEF_MESSAGE)) {

			// throw new
			// IllegalArgumentException("Expecting NDEF_MESSAGE intent extra!");
			Toast.makeText(
					this,
					"No NDEF Message for Tag writing was sent, please try again!",
					Toast.LENGTH_LONG);
			finish();
		}

		nfcForegroundUtil = new NFCForegroundUtil(this);
		msg = (NdefMessage) i.getParcelableExtra(NDEF_MESSAGE);
		
	}
	
	private SharedPreferences getSharedPreferences()
	{
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		// default failed
		setResult(RESULT_FAILED);

		if (NFCUtil.supportsNdef(tag)) {
			Ndef ndef = Ndef.get(tag);

			try {
				if (ndef.isWritable()
						&& ndef.getMaxSize() > this.msg.toByteArray().length) {
					ndef.connect();
					ndef.writeNdefMessage(this.msg);
					
					if (makeReadonly)
					{
						if (ndef.canMakeReadOnly())
						{
							boolean success = ndef.makeReadOnly();
							if (!success)
								Toast.makeText(this, "Unable to make tag readonly!", Toast.LENGTH_LONG).show();
							else
								Toast.makeText(this, "Tag is now readonly!", Toast.LENGTH_LONG).show();
						}
						else
						{
							Toast.makeText(this, "This tag cannot be made readonly!", Toast.LENGTH_LONG).show();
						}
					}
					

						
						
					setResult(RESULT_OK);
					showDialog(DIALOG_TAG_WRITTEN);
				} else {
					showDialog(DIALOG_TAG_FAILURE_READONLY_SIZE);
				}
			} catch (IOException e) {
				Log.e("demo", "Unable to write to tag.", e);
				showDialog(DIALOG_TAG_FAILURE_IO);

			} catch (FormatException e) {
				Log.e("demo", "Unable to write to tag due to FormatException.",
						e);
				showDialog(DIALOG_TAG_FAILURE_FORMAT);
			} finally {
				try {
					ndef.close();
				} catch (IOException e) {
				}
			}

		} else if (NFCUtil.supportsNdefFormatable(tag)) {
			NdefFormatable ndefFormatable = NdefFormatable.get(tag);

			try {
				ndefFormatable.connect();
				
				if (makeReadonly)
				{
					ndefFormatable.formatReadOnly(this.msg);
				}
				else
				{
					ndefFormatable.format(this.msg);
				}
				
				setResult(RESULT_OK);
				showDialog(DIALOG_TAG_WRITTEN);
			} catch (IOException e) {
				Log.e("demo", "Unable to write to tag. ", e);
				showDialog(DIALOG_TAG_FAILURE_IO);
			} catch (FormatException e) {
				Log.e("demo", "Unable to write to tag due to FormatException.",
						e);
				showDialog(DIALOG_TAG_FAILURE_FORMAT);
			} finally {
				try {
					ndefFormatable.close();
				} catch (IOException e) {
				}
			}

		} else {
			showDialog(DIALOG_TAG_FAILURE_NOT_NDEF);

		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (id) {
		case DIALOG_TAG_WRITTEN:

			builder.setMessage(
					"Your NFC tag has been written. To use it, just tap your phone against it and enjoy zero-click checkins!")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
									finish();
								}
							});
			d = builder.create();

			break;

		case DIALOG_TAG_FAILURE_FORMAT:
		case DIALOG_TAG_FAILURE_IO:
		case DIALOG_TAG_FAILURE_NOT_NDEF:
		case DIALOG_TAG_FAILURE_READONLY_SIZE:			
			builder.setMessage("") // changed in onPrepareDialog
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
									finish();
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
		AlertDialog alert = (AlertDialog) dialog; //we always got errorDialog
		
		switch (id) {
		case DIALOG_TAG_FAILURE_FORMAT:
			alert.setMessage("Unable to write message due to formatting error. Make sure you use NDEF or NDEFFormatable tags!");
			break;
		case DIALOG_TAG_FAILURE_IO:
			alert.setMessage("Unable to write message due to i/o exception. Please make sure you do not move your phone or tag during the write process!");
			break;
		case DIALOG_TAG_FAILURE_NOT_NDEF:
			alert.setMessage("Messages can only be written to NDEF or NDEFFormatable tags.");
			break;
		case DIALOG_TAG_FAILURE_READONLY_SIZE:
			alert.setMessage("Unable to write mesage as the tag is readonly or the capacity is not large enough.");
			break;
			
		default:
			super.onPrepareDialog(id, dialog);
		}

	}	

	@Override
	protected void onPause() {
		super.onPause();
		nfcForegroundUtil.disableForeground();
	}

	@Override
	protected void onResume() {
		super.onResume();
		nfcForegroundUtil.enableForeground();
		
		SharedPreferences prefs = getSharedPreferences();
		if (prefs.contains(Preferences.PREF_MAKE_READONLY))
			makeReadonly = prefs.getBoolean(Preferences.PREF_MAKE_READONLY, false);
		
		if (makeReadonly)
			Toast.makeText(this, "Make ReadOnly: true", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "Make ReadOnly: false", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_write, menu);
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
	
	public void openTagAge(View v)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.tagageshop)));
		startActivity(intent);
	}

}
