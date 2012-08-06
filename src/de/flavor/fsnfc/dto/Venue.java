package de.flavor.fsnfc.dto;

import java.io.Serializable;

public class Venue implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String title;
	private String ll;
	
	
	
	public Venue(String id) {
		super();
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLl() {
		return ll;
	}
	public void setLl(String ll) {
		this.ll = ll;
	}
	
	public String toString()
	{
		return title;
	}
	
	
}
