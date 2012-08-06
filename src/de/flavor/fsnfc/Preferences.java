package de.flavor.fsnfc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	public static String PREF_MAKE_READONLY = "make_readonly";
	public static String PREF_CHECKIN_MESSAGE = "checkin_message";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		getListView().setCacheColorHint(0);

		Preference buyTagsPreference = findPreference("buy_tags");
		Preference feedbackPreference = findPreference("feedback");
		Preference sharePreference = findPreference("share");
		Preference ratePreference = findPreference("rate");

		feedbackPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						final Intent emailIntent = new Intent(
								android.content.Intent.ACTION_SEND);

						emailIntent.setType("plain/text");

						emailIntent.putExtra(
								android.content.Intent.EXTRA_EMAIL,
								new String[] { getString(R.string.dev_email) });

						emailIntent.putExtra(
								android.content.Intent.EXTRA_SUBJECT,
								"NFC Checkin Feedback");

						startActivity(Intent.createChooser(emailIntent,
								"Send mail..."));

						return true;
					}

				});

		sharePreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						Intent shareIntent = new Intent(
								android.content.Intent.ACTION_SEND);
						shareIntent.setType("text/plain");
						shareIntent.putExtra(
								android.content.Intent.EXTRA_SUBJECT,
								"Check out NFC Chekin!");
						shareIntent
								.putExtra(
										android.content.Intent.EXTRA_TEXT,
										"Check out NFC Checkin from the Android Market to checkin to Foursquare using NFC: http://bit.ly/koEBS5");

						startActivity(Intent.createChooser(shareIntent,
								"Share..."));

						return true;
					}
				});

		ratePreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						Intent rateIntent = new Intent(
								android.content.Intent.ACTION_VIEW);
						rateIntent.setData(Uri
								.parse("https://market.android.com/details?id=de.flavor.fsnfc"));
						startActivity(rateIntent);

						return true;
					}
				});

		buyTagsPreference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						Intent rateIntent = new Intent(
								android.content.Intent.ACTION_VIEW);
						rateIntent.setData(Uri
								.parse("http://www.tagage.net/tagage-shop/"));
						startActivity(rateIntent);

						return true;
					}
				});
	}

}
