package de.flavor.fsnfc.adapter;

import de.flavor.fsnfc.dto.Venue;

public class ElementWrapper {

	private Venue venue = null;
	private String header = null;
	
	public ElementWrapper(String header) {
		super();
		this.header = header;
	}	
	
	public ElementWrapper(Venue venue) {
		super();
		this.venue = venue;
	}	
	
	public boolean isVenue()
	{
		return (venue != null);
	}
	
	public boolean isHeader() {
		return (header != null);
	}

	public Venue getVenue() {
		return venue;
	}

	public String getHeader() {
		return header;
	}
	


	
}
