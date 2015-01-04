package com.examples.with.different.packagename.context.complex;

public class EntryPointsClass {
	ISubClass sub;

	public EntryPointsClass() {
		sub = new SubClass();
	}

	public void dosmt(int i, String string, double d) {
		boolean b = sub.checkFiftneen(i);
		if (b) {
			System.out.println("ciao");
		}
	}

	public void doObj(AParameterObject o) {
		if(o.isEnabled())
			System.out.println("covered");
		System.out.println("not covered");
	}
}
