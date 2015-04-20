package com.examples.with.different.packagename.stable;

public class RuntimeUser {
	
	private final Runtime runtime;
	
	public RuntimeUser() {
		runtime = Runtime.getRuntime();
	}
	
	public long freeMemory() {
		return runtime.freeMemory();
	}

	public long totalMemory() {
		return runtime.totalMemory();
	}

	public long maxMemory() {
		return runtime.maxMemory();
	}
	
	public int availableProcs() {
		return runtime.availableProcessors();
	}
	

}
