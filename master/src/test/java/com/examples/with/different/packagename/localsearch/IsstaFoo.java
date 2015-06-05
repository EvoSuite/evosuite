package com.examples.with.different.packagename.localsearch;

public class IsstaFoo {

	private int x = 0;
	private String str;
	private String str2 = "b" + "a" + "r";

	public IsstaFoo(String str) {
		this.str = str;
	}

	public void inc() {
		x++;
	}

	public boolean coverMe() {
		if (x == 5) {
			if (!str.equals(str2)) {
				if (str.equalsIgnoreCase(str2)) {
					return true;
				}
			}
		}
		return false;
	}
}
