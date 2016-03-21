package com.examples.with.different.packagename.epa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class MyBoundedStack {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface EpaState {
		public String name();
	}

	/*
	 * ======================================================
	 * Original Code
	 * ======================================================
	 */

	private final static int DEFAULT_SIZE = 10;

	private final Object[] elements = new Object[DEFAULT_SIZE];

	private int index = -1;

	private boolean initialState = true;

	public MyBoundedStack() {
		initialState = false;
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
	
	@EpaState(name = "S0")
	private boolean queryForS0() {
		return initialState;
	}
	
	@EpaState(name = "S1")
	private boolean queryForS1() {
		return isPushEnabled() && !isPopEnabled();
	}

	@EpaState(name = "S2")
	private boolean queryForS2() {
		return isPushEnabled() && isPopEnabled();
	}

	@EpaState(name = "S3")
	private boolean queryForS3() {
		return !isPushEnabled() && isPopEnabled();
	}

}
