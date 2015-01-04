package com.examples.with.different.packagename.mock.java.lang;

public class HookWithBranch {

	public void foo(boolean b){
		final boolean FLAG = b;
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override public void run(){
				if(FLAG){
					System.out.println("Flag: ON");
				} else {
					System.out.println("Flag: OFF");
					throw new IllegalStateException();
				}
			}
		});
	}
}
