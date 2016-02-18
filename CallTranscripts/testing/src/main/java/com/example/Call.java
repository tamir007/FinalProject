package com.example;

public class Call {
	

	private String theCall;
	private double lat;
	private double longe;
	private int clock;
	private int day;
	private char callID;


	public Call(String theCall, double lat, double longe, int clock, int day, char callID) {
		super();
		this.theCall = theCall;
		this.lat = lat;
		this.longe = longe;
		this.clock = clock;
		this.day = day;
		this.callID = callID;
	}
	public Call(String theCall, String lat, String longe, String clock, String day, String callID) {
		super();
		this.theCall = theCall;
		this.lat = Double.parseDouble(lat);
		this.longe = Double.parseDouble(longe);
		this.clock = Integer.parseInt(clock);
		this.day = Integer.parseInt(day);
		this.callID = callID.charAt(0);
	}

	
	
	public char getCallID() {
		return callID;
	}

	public void setCallID(char callID) {
		this.callID = callID;
	}
	
	public String getTheCall() {
		return theCall;
	}
	public void setTheCall(String theCall) {
		this.theCall = theCall;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLonge() {
		return longe;
	}
	public void setLonge(double longe) {
		this.longe = longe;
	}
	public int getClock() {
		return clock;
	}
	public void setClock(int clock) {
		this.clock = clock;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}


}
