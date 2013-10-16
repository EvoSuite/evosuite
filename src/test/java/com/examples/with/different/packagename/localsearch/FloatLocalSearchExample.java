package com.examples.with.different.packagename.localsearch;

public class FloatLocalSearchExample {

	public boolean testMe(float x, float y) {
		if(Math.round((x * 2) * Math.sin(y)) == 20F)
			return true;
		else
			return false;
	}
}
