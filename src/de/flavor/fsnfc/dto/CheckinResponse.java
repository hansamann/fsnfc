package de.flavor.fsnfc.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CheckinResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	public List<Notification> notifications = new ArrayList<Notification>();
	public Venue venue;
	
	public boolean hasMessage()
	{
		for (Notification n : notifications)
		{
			if (n instanceof MessageNotification)
				return true;
		}
		
		return false;
	}
	
	public MessageNotification getMessageNotification()
	{
		for (Notification n : notifications)
		{
			if (n instanceof MessageNotification)
				return (MessageNotification)n;
		}
		
		return null;
	}
	
}
