package de.flavor.fsnfc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import de.flavor.fsnfc.App;
import de.flavor.fsnfc.Preferences;
import de.flavor.fsnfc.dto.CheckinResponse;
import de.flavor.fsnfc.dto.MessageNotification;
import de.flavor.fsnfc.dto.Notification.Type;
import de.flavor.fsnfc.dto.Venue;

public class FoursquareClient {

	private String token;
	private Context context;
	private String DATEVERIFIED = "20120101";

	public FoursquareClient(String token, Context context) {
		this.context = context;
		this.token = token;
	}

	public CheckinResponse checkin(Venue venue) {
		CheckinResponse res = new CheckinResponse();

		String url = "https://api.foursquare.com/v2/checkins/add";

		HttpPost post = new HttpPost(url);

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("v", DATEVERIFIED));
		qparams.add(new BasicNameValuePair("venueId", venue.getId()));
		qparams.add(new BasicNameValuePair("broadcast", "public"));
		qparams.add(new BasicNameValuePair("oauth_token", this.token));

		String message = getSharedPreferences().getString(
				Preferences.PREF_CHECKIN_MESSAGE, "");
		if (message.length() > 0)
			qparams.add(new BasicNameValuePair("shout", message));

		JSONObject fullResponse = null;
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(qparams);
			post.setEntity(entity);
			fullResponse = execute(post);

			JSONArray notificationsArray = fullResponse
					.getJSONArray("notifications");
			if (notificationsArray != null) {
				for (int i = 0; i < notificationsArray.length(); i++) {
					JSONObject notificationObj = notificationsArray
							.getJSONObject(i);
					Type type = null;

					type = Type.valueOf(notificationObj.getString("type")
							.toUpperCase());

					switch (type) {
					case MESSAGE:
						res.notifications.add(new MessageNotification(
								notificationObj.getJSONObject("item")
										.getString("message")));
						break;

					case MAYORSHIP:
						break;
					case LEADERBOARD:
						break;
					case SCORE:
						break;
					}
				}
			}
			JSONObject response = fullResponse.getJSONObject("response");
			JSONObject checkinObj = response.getJSONObject("checkin");
			JSONObject venueObj = checkinObj.getJSONObject("venue");
			venue.setTitle(venueObj.getString("name"));
			res.venue = venue;
		} catch (UnsupportedEncodingException e) {
			Log.e("demo", "Unable to encode", e);
			return null;
		} catch (ClientProtocolException e) {
			Log.e("demo", "ClientProtocolException", e);
			return null;
		} catch (IOException e) {
			Log.e("demo", "IO", e);
			return null;
		} catch (JSONException e) {
			Log.e("demo", "JSON issue", e);
			return null;
		}

		return res;
	}

	public List<Venue> searchVenues(Location loc, int limit) {
		List<Venue> venues = new ArrayList<Venue>();

		String ll = Double.toString(loc.getLatitude()) + ","
				+ Double.toString(loc.getLongitude());

		// ll="48.150874,11.551455";
		String url = "https://api.foursquare.com/v2/venues/search?v="
				+ DATEVERIFIED + "&oauth_token=" + this.token + "&ll=" + ll;
		if (limit > -1)
			url += "&limit=" + limit;

		HttpGet get = new HttpGet(url);

		try {
			JSONObject response = executeForResponse(get);

			JSONArray venuesObj = response.getJSONArray("venues");
			

			for (int j = 0; j < venuesObj.length(); j++) {
				JSONObject obj = venuesObj.getJSONObject(j);
				Venue venue = new Venue(obj.getString("id"));
				venue.setTitle(obj.getString("name"));

				JSONObject location = obj.getJSONObject("location");
				venue.setLl(Double.toString(location.getDouble("lat")) + ","
						+ Double.toString(location.getDouble("lng")));

				Log.d("demo", "New venue: " + venue.toString() + " id: "
						+ venue.getId());
				venues.add(venue);

			}

		} catch (JSONException e) {
			Log.e("demo", "Unable to parse JSON", e);
		} catch (ClientProtocolException e) {
			Log.e("demo", "ClientProtocolEx", e);
		} catch (IOException e) {
			Log.e("demo", "IOEx", e);
		}

		return venues;
	}

	private JSONObject executeForResponse(HttpRequestBase request)
			throws ClientProtocolException, IOException, JSONException {

		HttpResponse response = getHttp().execute(request);

		String page = readStream(response.getEntity().getContent());

		Log.d("demo", page);

		JSONObject json = new JSONObject(page);

		int code = json.getJSONObject("meta").getInt("code");
		if (code == 500) {
			String message = json.getJSONObject("meta")
					.getString("errorDetail");
			throw new HttpResponseException(code, message);
		}

		return json.getJSONObject("response");

	}

	private JSONObject execute(HttpRequestBase request)
			throws ClientProtocolException, IOException, JSONException {

		HttpResponse response = getHttp().execute(request);

		String page = readStream(response.getEntity().getContent());

		Log.d("demo", page);

		JSONObject json = new JSONObject(page);

		int code = json.getJSONObject("meta").getInt("code");
		if (code != 200) {
			throw new HttpResponseException(code, "Unexpected status code");
		}

		return json;

	}

	private String readStream(InputStream input) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		StringBuffer sb = new StringBuffer();
		String line = "";
		String NL = System.getProperty("line.separator");
		while ((line = in.readLine()) != null) {
			sb.append(line + NL);
		}
		in.close();
		String page = sb.toString();
		return page;
	}

	private void addAccessToken(HttpRequestBase request) {
		request.getParams().setParameter("oauth_token", this.token);
	}

	private HttpClient getHttp() {
		return ((App) context.getApplicationContext()).getHttp();

	}

	private SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

}
