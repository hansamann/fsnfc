package de.flavor.fsnfc.dto;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.test.AndroidTestCase;
import android.util.Log;

import com.google.gson.Gson;

import de.flavor.fsnfc.dto.GsonCheckinResponse.LeaderBoard;
import de.flavor.fsnfc.dto.GsonCheckinResponse.Notification;
import de.flavor.fsnfc.dto.GsonCheckinResponse.Response;


public class ResponseTests extends AndroidTestCase {
	
	private Gson gson; 
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		gson = new Gson();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		gson = null;
	}
	
   public void testCheckinResponse1() throws Exception{
	   String json = loadFromAssets("json/response.json");
	   
	   GsonCheckinResponse r = gson.fromJson(json, GsonCheckinResponse.class);
	   
	   assertNotNull(r);
	   assertEquals(200, r.meta.code.intValue());
	   assertEquals(5, r.notifications.size());
	   
	   //all notifications have type and item
	   for (Notification n : r.notifications)
	   {
		   assertNotNull(n.type);
		   assertNotNull(n.item);
	   }
	   
	   Notification message = r.notifications.get(0);
	   assertNotNull(message);
	   assertEquals("message", message.type);
	   assertEquals("OK! We've got you @ san francisco coffee company. You've been here 2 times.", message.item.message);
	   
	   
	   Notification mayorship = r.notifications.get(1);
	   assertNotNull(mayorship);
	   assertEquals("mayorship", mayorship.type);
	   assertEquals("nochange", mayorship.item.type);
	   assertEquals(2, mayorship.item.checkins.intValue());
	   assertEquals(1, mayorship.item.daysBehind.intValue());
	   assertEquals("Wolpertinger is the Mayor of san francisco coffee company.", mayorship.item.message);
	   assertEquals("https://playfoursquare.s3.amazonaws.com/userpix_thumbs/CGXK1T24ZSLUOPHU.jpg", mayorship.item.image.toString());
	   //user
	   assertNotNull(mayorship.item.user);
	   assertEquals("1030852", mayorship.item.user.id);
	   assertEquals("Wolpertinger", mayorship.item.user.firstName);
	   assertEquals("https://playfoursquare.s3.amazonaws.com/userpix_thumbs/CGXK1T24ZSLUOPHU.jpg", mayorship.item.user.photo.toString());
	   assertEquals("male", mayorship.item.user.gender);
	   assertEquals("Rosenheim, Deutschland", mayorship.item.user.homeCity);
	   
	   
	   Notification leaderboard = r.notifications.get(2);
	   assertNotNull(leaderboard);
	   assertEquals("leaderboard", leaderboard.type);
	   assertEquals(3, leaderboard.item.leaderboard.size());
	   for (LeaderBoard board : leaderboard.item.leaderboard)
	   {
		   assertNotNull(board.user);
		   assertNotNull(board.scores);
		   assertNotNull(board.rank);
		   
		   assertNotNull(board.user.id);
		   assertNotNull(board.user.firstName);
		   assertNotNull(board.user.lastName);
		   assertNotNull(board.user.photo);
		   assertNotNull(board.user.gender);
		   assertNotNull(board.user.homeCity);
		   assertNotNull(board.user.relationship);
		   
		   assertNotNull(board.scores.recent);
		   assertNotNull(board.scores.max);
		   assertNotNull(board.scores.checkinsCount);
	   }
	   
	   
	   Notification tip = r.notifications.get(3);
	   assertNotNull(tip);
	   assertEquals("tip", tip.type);
	   assertEquals("4cdec5eff8a4a1438b80d9bc", tip.item.tip.id);
	   assertEquals(1289668079, tip.item.tip.createdAt.longValue());
	   //Log.d("demo", "!!!" + tip.item.tip.text);
	   assertEquals("Super Laden mit free WiFi. Gerade einen ganzen Nachmittag in ruhiger Atmosphäre gearbeitet.", tip.item.tip.text);
	   
	   assertNotNull(tip.item.tip.user);
	   assertNotNull(tip.item.tip.done);
	   assertNotNull(tip.item.tip.todo);
	   assertEquals("Popular tip", tip.item.name);
	   
	   
	   Notification score = r.notifications.get(4);
	   assertNotNull(score);
	   assertEquals("score", score.type);
	   
	   
	   Response res = r.response;
	   assertNotNull(res);
	   assertNotNull(res.checkin);
	   assertEquals("4ddd415452b177ff2e7f899f", res.checkin.id);
	   assertEquals(1306345812, res.checkin.createdAt.longValue());
	   assertEquals("checkin", res.checkin.type);
	   assertEquals("NFC Checkin - http://nfc-checkin.appspot.com/", res.checkin.shout);
	   assertEquals("Europe/Berlin", res.checkin.timeZone);
	   assertNotNull(res.checkin.venue);
	   assertNotNull(res.checkin.venue.location);
	   assertNotNull(res.checkin.venue.categories);
	   assertEquals(Boolean.FALSE, res.checkin.venue.verified);
	   assertNotNull(res.checkin.venue.stats);
	   assertNotNull(res.checkin.venue.todos);
	   
	   
   }
   
   public void testCheckinResponse2() throws Exception{
	   String json = loadFromAssets("json/response2.json");
	   
	   GsonCheckinResponse r = gson.fromJson(json, GsonCheckinResponse.class);
	   
	   assertNotNull(r);
	   assertEquals(200, r.meta.code.intValue());
	   assertEquals(5, r.notifications.size());
	   
	   Notification score = r.notifications.get(4);
	   assertEquals("score", score.type);
	   assertEquals(1, score.item.scores.size());
	   assertEquals(2, score.item.scores.get(0).points.intValue());
	   assertEquals("/img/points/mayor.png", score.item.scores.get(0).icon);
	   assertEquals("The Mayor is in the house!", score.item.scores.get(0).message);
	   
   }
   
   public void testCheckinResponse3() throws Exception{
	   String json = loadFromAssets("json/response3.json");
	   
	   GsonCheckinResponse r = gson.fromJson(json, GsonCheckinResponse.class);
	   
	   assertNotNull(r);
	   assertEquals(200, r.meta.code.intValue());
	   assertEquals(4, r.notifications.size());
	   
	   Notification mayorship = r.notifications.get(1);
	   assertEquals("mayorship", mayorship.type);
	   assertEquals("You're still the Mayor of Spielplatz Ottostraße! (16 check-ins in the past two months)", mayorship.item.message);
   }   
   
   private String loadFromAssets(String path) throws IOException
   {
	   	InputStream inputStream = getContext().getAssets().open(path);
	   	
	   	BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 4096);
	   	String line;
	   	StringBuilder sb =  new StringBuilder();
	   	while ((line = rd.readLine()) != null) {
	   			sb.append(line);
	   	}
	   	rd.close();
	   	return sb.toString();	
   }
}
