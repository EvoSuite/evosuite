/**
 * 
 */
package com.examples.with.different.packagename.generic;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

/**
 * @author Gordon Fraser
 * 
 */
public class DelayedQueueExample<T> {

	public boolean foo(BlockingQueue<T> bar) {
		if (bar instanceof DelayQueue)
			return true;
		else
			return false;
	}
}
