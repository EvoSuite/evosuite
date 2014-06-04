package com.examples.with.different.packagename.contracts;

public class EqualsSymmetric {

	private int x = 0;
	
	public void setX(int x) {
		this.x = x;
	}
	
	public boolean equals(Object other) {
		if(other == null)
			return false;
		if(other == this)
			return true;
		if(other instanceof EqualsSymmetric) {
			EqualsSymmetric otherEquals = (EqualsSymmetric)other;
			if(x > 0 && otherEquals.x < 0)
				return true;
			else
				return x > 0 == otherEquals.x > 0;
		}
		return false;
	}
}
