package com.example;

import java.util.ArrayList;


public class DB {
	
	private ArrayList<Call> theCalls;

	public DB(){
		this.theCalls = new ArrayList<>();
	}
	public DB(ArrayList<Call> theCalls){
		this.theCalls = theCalls;
	}

	public ArrayList<Call> getTheCalls() {
		return theCalls;
	}

	public void setTheCalls(ArrayList<Call> theCalls) {
		this.theCalls = theCalls;
	}
	
	public void addCall(Call aCall){
		theCalls.add(aCall);
	}




}
