/**
 * 
 */
package com.examples.with.different.packagename.generic;

import com.examples.with.different.packagename.generic.concurrent.BlockingQueue;
import com.examples.with.different.packagename.generic.concurrent.DelayQueue;


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
