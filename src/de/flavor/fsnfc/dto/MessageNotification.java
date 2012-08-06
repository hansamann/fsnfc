package de.flavor.fsnfc.dto;

public class MessageNotification extends Notification {

	private static final long serialVersionUID = 1L;
	
	public String message;
	
	public MessageNotification(String message) {
		super(Type.MESSAGE);
		this.message = message;
	}


	
}
