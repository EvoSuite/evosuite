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
		reportState(); // Instrumentation Call
	}
	
	public void push(Object object) {
		if (index==elements.length-1) {
			throw new IllegalStateException();
		}
		elements[++index] = object;
		
		reportState(); // Instrumentation Call
	}
	
	public Object pop() {
		if (index==-1) {
			throw new IllegalStateException();
		}
		Object ret_val = elements[index--];

		reportState(); // Instrumentation Call
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
	
	private boolean isState1() {
		return isPushEnabled() && !isPopEnabled();
	}
	
	private boolean isState2() {
		return isPushEnabled() && isPopEnabled();
	}

	private boolean isState3() {
		return !isPushEnabled() && isPopEnabled();
	}

	private void reportState() {
		if (isState1()) {
			reportState1();
		} else if (isState2()) {
			reportState2();
		} else if (isState3()) {
			reportState3();
		}
		
	}

	private void reportState1() {
		// dummy method
	}

	private void reportState2() {
		// dummy method
		
	}

	private void reportState3() {
		// dummy method
	}

}
