package com.examples.with.different.packagename.continuous;

public class Simple {

	private int counter = 0;
	
	public boolean reached(){
		if(counter==16){
			return true;
		} else {
			return false;
		}
	}
	
	public void incr(){
		counter++;
	}
	
	public void decr(){
		counter--;
	}
}
