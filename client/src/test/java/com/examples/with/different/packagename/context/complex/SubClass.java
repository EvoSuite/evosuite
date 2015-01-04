package com.examples.with.different.packagename.context.complex;

public class SubClass extends ISubClass {

	ISubSubClass subsubclass;

	public SubClass() {
		subsubclass = new SubSubClass();
	}

	public boolean checkFiftneen(int i) {
		boolean bol = bla(i);
		if (bol)
			return true;
		return bol;
	}

	private boolean bla(int i) {
		boolean bol = false;
		if (i * 2 == 6) {
			System.out.println("covered BLA");
			bol = true;
		}
		bol = subsubclass.innermethod(i) || bol;
		return bol;
	}

}
