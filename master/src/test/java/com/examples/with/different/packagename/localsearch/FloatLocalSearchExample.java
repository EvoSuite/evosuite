package com.examples.with.different.packagename.localsearch;

public class FloatLocalSearchExample {

	public boolean testMe(float x, float y) {
		if(Math.abs((x * Math.sqrt(Math.abs(y))) - 20F * Math.PI) < 0.01F)
			return true;
		else
			return false;
	}
}
