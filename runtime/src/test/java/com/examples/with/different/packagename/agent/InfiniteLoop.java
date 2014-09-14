package com.examples.with.different.packagename.agent;

public class InfiniteLoop {

	public Thread getInfiniteLoop(){
		
		return new Thread(){
			@Override
			public void run(){
				while(true){
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						//keep going
					}
				}
			}
		};
		
	}
	
}
