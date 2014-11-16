package com.examples.with.different.packagename.agent;

public class SumRuntime {

	public static long getSum(){
		return Runtime.getRuntime().availableProcessors() +
				Runtime.getRuntime().freeMemory() +
				Runtime.getRuntime().maxMemory() + 
				Runtime.getRuntime().totalMemory();
	}
}
