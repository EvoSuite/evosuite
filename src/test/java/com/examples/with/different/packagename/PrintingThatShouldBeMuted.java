package com.examples.with.different.packagename;

public class PrintingThatShouldBeMuted{

	public void foo(int x){
		
		if(x > 0){
			System.out.println("Greater");
		} else {
			System.out.println("Lower");
		}
		
	}
	
	/*
	public void doPrint() { //throws RuntimeException{
		System.err.println("This should not be printed.");
		
		Thread t  = new Thread(){
			@Override
			public void run(){
				//throw new RuntimeException("Also this one should not be printed");
			}
		};
		t.start();
		try {
			t.join(50);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Thread k = new Thread(){
			@Override
			public void run(){
				System.err.println("Going to sleep for a bit. EvoSuite should throw a timeout");
				System.err.println("We enter in infinite loop, even if EvoSuite try to interrupt");
				while(true){
					try {
						long time = 200;
						System.err.println("Going to sleep for "+time+" ms");
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		k.start();
		try {
			k.join(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} */
}
