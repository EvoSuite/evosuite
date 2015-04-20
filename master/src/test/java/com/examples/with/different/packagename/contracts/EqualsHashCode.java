package com.examples.with.different.packagename.contracts;

public class EqualsHashCode {

	private int x = 0;
	
	public void setX(int x) {
		this.x = x;
	}
	
	@Override
	public int hashCode() {
		return x;
	}
	
	public boolean equals(Object other) {
		if(other == null)
			return false;
		
		if(other == this)
			return true;
		
		if(x == 42)
			return true;
		
		if(other instanceof EqualsHashCode) {
			return ((EqualsHashCode)other).x == x;
		} else {
			return other.equals(this);
		}
	}
}
