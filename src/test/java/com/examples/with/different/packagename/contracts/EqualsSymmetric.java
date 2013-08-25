package com.examples.with.different.packagename.contracts;

public class EqualsSymmetric {

	private int x;
	
	public EqualsSymmetric(int x) {
		this.x = x;
	}
	
	public boolean equals(Object other) {
		if(other instanceof EqualsSymmetric) {
			EqualsSymmetric otherEquals = (EqualsSymmetric)other;
			if(x > 0 && otherEquals.x <= 0)
				return true;
			else
				return false;
		}
		return false;
	}
}
