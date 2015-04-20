package com.examples.with.different.packagename.contracts;

public class ToStringException {

	private int state = 0;
	
	public void setState(int state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		if(state == 42)
			throw new RuntimeException("Test!");
		else
			return ""+state;
	}
}
