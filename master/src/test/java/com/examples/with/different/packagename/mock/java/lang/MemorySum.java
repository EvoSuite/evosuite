package com.examples.with.different.packagename.mock.java.lang;

public class MemorySum {

	public long sum(){
		java.lang.Runtime runtime = Runtime.getRuntime();
		return 
				runtime.freeMemory() + 
				runtime.totalMemory() + 
				runtime.maxMemory() + 
				runtime.availableProcessors();
	}
}
