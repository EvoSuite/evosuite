package com.examples.with.different.packagename.context;

public class EntryPointsClass {
	ISubClass sub;

	public EntryPointsClass() {
		sub = new SubClass();
	}

	public void dosmt(int i) {
		boolean b = sub.checkFiftneen(i);
		if (b) {
			System.out.println("ciao");
		}
	}

}
