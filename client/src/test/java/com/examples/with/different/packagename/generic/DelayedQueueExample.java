/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Gordon Fraser
 * 
 */
public class DelayedQueueExample<T> {

	public static class DummyDelayed implements Delayed {

		@Override
		public int compareTo(Delayed o) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	public boolean foo(BlockingQueue<T> bar) {
		if (bar instanceof DelayQueue)
			return true;
		else
			return false;
	}
}
