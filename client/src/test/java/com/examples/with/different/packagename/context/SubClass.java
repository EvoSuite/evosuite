package com.examples.with.different.packagename.context;

public class SubClass extends ISubClass {

	ISubSubClass subsubclass;

	public SubClass() {
		subsubclass = new SubSubClass();
	}

	public boolean checkFiftneen(int i) {
		boolean bol = bla(i);
		if (bol)
			return true;
		return false;
	}

	private boolean bla(int i) {
		boolean bol = false;
		if (i * 2 == 6) {
			bol = true;
		}
		bol = subsubclass.innermethod(i) || bol;
		return bol;
	}

}
