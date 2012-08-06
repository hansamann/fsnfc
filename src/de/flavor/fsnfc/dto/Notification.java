package de.flavor.fsnfc.dto;

import java.io.Serializable;

public abstract class Notification implements Serializable{

	private static final long serialVersionUID = 1L;

	public enum Type { MESSAGE, MAYORSHIP, LEADERBOARD, SCORE, TIP, BADGE, TIPALERT, NOTIFICATIONTRAY }
	
	public Type type;

	public Notification(Type type) {
		super();
		this.type = type;
	}

}
