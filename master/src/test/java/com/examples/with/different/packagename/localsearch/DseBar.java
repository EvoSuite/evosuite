package com.examples.with.different.packagename.localsearch;

public class DseBar {

	private String x;

	public DseBar(String x) {
		this.x = x;
	}

	public void coverMe(DseFoo f) {
		String y = x + f.getX();
		if (y.equals("baz5")) {
			System.out.println("TARGET");
		}
	}

}
