package com.examples.with.different.packagename.mock.java.lang;

public class MemoryCheck {

	@Override
	public String toString(){
		return ""+Runtime.getRuntime().maxMemory();
	}
}
