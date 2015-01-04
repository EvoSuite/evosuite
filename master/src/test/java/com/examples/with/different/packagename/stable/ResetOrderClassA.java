package com.examples.with.different.packagename.stable;

public class ResetOrderClassA {

	public final static ResetOrderClassB object;
	
	static {
		System.out.println("enter A.<clinit>()");
		object = ResetOrderClassB.OBJECT;
		System.out.println("exit A.<clinit>()");
	}
	
	public boolean checkObjectsAreEqual() {
		if (object==ResetOrderClassB.OBJECT) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean checkObjectsAreNotEqual() {
		if (object!=ResetOrderClassB.OBJECT) {
			return true;
		} else {
			return false;
		}
	}
}
