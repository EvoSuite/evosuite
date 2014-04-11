package com.examples.with.different.packagename.stable;

public class Overload {

	public Overload() {
	}

	public boolean execute(Overload str, Overload str2) {
		return true;
	}

	public boolean execute(Overload str, Object object) {
		return false;
	}

}
