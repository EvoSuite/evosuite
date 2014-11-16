package com.examples.with.different.packagename;

public abstract class AbstractSuperClass {

	public void coverMe(int x) {
		if(x == 256) {
			System.out.println("OK");
		}
	}
	
	public abstract void overrideMe();
	
}
