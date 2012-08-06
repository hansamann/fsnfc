package de.flavor.fsnfc.dto;

import java.net.URL;
import java.util.List;

public class GsonCheckinResponse {

	public GsonCheckinResponse() {
		super();
	}

	public Meta meta;
	public List<Notification> notifications;
	public Response response;

	static class Meta {
		public Meta() {
			super();
		}

		Integer code;

	}

	static class Notification {
		String type;
		NotificationItem item;

		public Notification() {
			super();

		}

	}

	static class NotificationItem {

		public NotificationItem() {
			super();

		}

		String type;
		String message;
		Integer checkins;
		Integer daysBehind;
		User user;
		URL image;
		List<LeaderBoard> leaderboard;
		Integer total; // this is for scores, total scores earned
		Tip tip;
		String name;
		List<Badge> badge;
		List<Score> scores;

	}

	static class Score {
		public Score() {
			super();

		}

		Integer points;
		String icon;
		String message;
	}

	static class Badge {

		public Badge() {
			super();

		}

		String id;
		String badgeId;
		String name;
		String description;
		URL image;
		// unlocks here, but unclear?

	}

	static class User {

		public User() {
			super();

		}

		String id;
		String firstName;
		String lastName;
		URL photo;
		String gender;
		String homeCity;
		String relationship;
	}

	static class LeaderBoard {

		public LeaderBoard() {
			super();

		}

		User user;
		Scores scores;
		Integer rank;
	}

	static class Scores {

		public Scores() {
			super();

		}

		Integer recent;
		Integer max;
		Integer checkinsCount;
	}

	static class Tip {

		public Tip() {
			super();

		}

		String id;
		Long createdAt;
		String text;
		Todo todo;
		Done done;
		User user;
	}

	static class Todo {

		public Todo() {
			super();

		}

		Integer count;
	}

	static class Done {

		public Done() {
			super();

		}

		Integer count;
	}

	static class Response {

		public Response() {
			super();

		}

		Checkin checkin;
	}

	static class Checkin {

		public Checkin() {
			super();

		}

		String id;
		Long createdAt;
		String type;
		String shout;
		String timeZone;
		Venue venue;

	}

	static class Venue {

		public Venue() {
			super();

		}

		String id;
		String name;
		Contact contact;
		Location location;
		List<Category> categories;
		Boolean verified;
		Stats stats;
		Todos todos;

	}

	static class Contact {

		public Contact() {
			super();

		}

	}

	static class Location {

		public Location() {
			super();

		}

		String address;
		String city;
		String state;
		String postalCode;
		Double lat;
		Double lng;
	}

	static class Category {

		public Category() {
			super();

		}

		String id;
		String name;
		String pluralName;
		URL icon;
		List<String> parents;
		Boolean primary;
	}

	static class Stats {

		public Stats() {
			super();

		}

		Integer checkinsCount;
		Integer usersCount;
	}

	static class Todos {

		public Todos() {
			super();

		}

		Integer count;
	}

}
