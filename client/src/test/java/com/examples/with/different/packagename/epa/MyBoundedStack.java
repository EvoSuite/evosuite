package com.examples.with.different.packagename.epa;

public class MyBoundedStack {

	/*
	 * ======================================================
	 * Original Code
	 * ======================================================
	 */

	private final static int DEFAULT_SIZE = 10;
	
	private final Object[] elements = new Object[DEFAULT_SIZE];

	private int index = -1;
	
	public MyBoundedStack() {
	}
	
	public void push(Object object) {
		if (index==elements.length-1) {
			throw new IllegalStateException();
		}
		elements[++index] = object;
	}
	
	public Object pop() {
		if (index==-1) {
			throw new IllegalStateException();
		}
		Object ret_val = elements[index--];
		return ret_val;
	}


	/*
	 * ======================================================
	 * Instrumentation
	 * ======================================================
	 */
	private boolean isPushEnabled() {
		return index!=elements.length-1;
	}
	
	private boolean isPopEnabled() {
		return index!=-1;
	}
	
	private boolean isStateS0() {
		return false;
	}
	
	private boolean isStateS1() {
		return isPushEnabled() && !isPopEnabled();
	}
	
	private boolean isStateS2() {
		return isPushEnabled() && isPopEnabled();
	}

	private boolean isStateS3() {
		return !isPushEnabled() && isPopEnabled();
	}

}
