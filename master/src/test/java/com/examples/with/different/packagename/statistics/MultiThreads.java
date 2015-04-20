package com.examples.with.different.packagename.statistics;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiThreads {

	private static volatile AtomicBoolean FLAG = new AtomicBoolean(false);
	
	public void foo() throws InterruptedException{
		synchronized(FLAG){
			if(FLAG.get()){
				return;
			}
			Thread a = new Thread(new Foo());
			Thread b = new Thread(new Foo());
			a.start();
			b.start();
			b.join();
			a.join();
			FLAG.set(true);
		}
	}
	
	private static class Foo implements Runnable{
		@Override
		public void run(){
			new File(".").exists();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}
