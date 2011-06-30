package de.unisb.cs.st.evosuite.utils;

import java.util.concurrent.locks.*;

public class SimpleCondition {
	private Lock lock;
	private Condition condition;
	private boolean wasSignaled = false;
	
	public SimpleCondition() {
		this.lock = new ReentrantLock();
		this.condition = this.lock.newCondition();
		this.lock.lock();
	}
	
	public void awaitUninterruptibly() {
		this.condition.awaitUninterruptibly();
		this.lock.unlock();
	}
	
	public synchronized void signal() {
		this.lock.lock();

		try {
			this.condition.signal();
			this.wasSignaled = true;
		} finally {
			this.lock.unlock();
		}
	}

	public synchronized boolean wasSignaled() {
		return this.wasSignaled;
	}
}
