package com.examples.with.different.packagename;

public class InfiniteLoops {

	public void easyLoop() throws InterruptedException{	
		Thread t = new Thread(){
			@Override
			public void run(){
				System.err.println("ERROR This should not be printed");
				while(true){
					try {
						System.err.println("In the loop going to sleep. Thread "+Thread.currentThread().getId());
						Thread.sleep(10);
					} catch (InterruptedException e) {
						System.err.println("ERROR This should not be printed: "+e);
						return;
					}
				}
			}
		};
		t.start();
		// with following line, no problem in muting the output
		//t.join(1000);
	}

	/*
	public void ignoreIterrupt(){
		Thread t = new Thread(){
			@Override
			public void run(){
				System.err.println("ERROR This should not be printed");
				while(true){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						System.err.println("ERROR This should not be printed: "+e);
					}
				}
			}
		};
		t.start();
	}


	public void hardToKill(){
		Thread t = new Thread(){
			@Override
			public void run(){
				System.err.println("ERROR This should not be printed");
				while(true){
					try{
						Thread.sleep(10);
					} catch (Exception e) {
						System.err.println("ERROR This should not be printed: "+e);
					}
				}
			}
		};
		t.start();
	}
	*/
}
