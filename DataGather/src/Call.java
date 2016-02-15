
public class Call {
	

	private String theCall;
	private double lat;
	private double longe;
	private int clock;
	private int day;
	
	public Call(String theCall, double lat, double longe, int clock, int day) {
		super();
		this.theCall = theCall;
		this.lat = lat;
		this.longe = longe;
		this.clock = clock;
		this.day = day;
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
