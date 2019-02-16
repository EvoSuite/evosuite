package com.examples.with.different.packagename.epa;

import org.evosuite.epa.EpaAction;
import org.evosuite.epa.EpaState;

public class MiniBoundedStack {

	private final static int DEFAULT_SIZE = 1;

	private final int[] elements = new int[DEFAULT_SIZE];

	private int index = -1;

	@EpaAction(name = "MyBoundedStack()")
	public MiniBoundedStack() {
	}

	@EpaAction(name = "push()")
	public void push(int value) {
		try {
			if (index == elements.length - 1) {
				throw new IllegalStateException();
			}
			elements[++index] = value;
		} catch (Exception ex) {
			throw ex;
		}
	}

	@EpaAction(name = "pop()")
	public int pop() {
		try {
			if (index == -1) {
				throw new IllegalStateException();
			}
			int ret_val = elements[index--];
			return ret_val;
		} catch (Exception ex) {
			throw ex;
		}
	}

	// ======================================================
	// Boolean Queries
	// ======================================================
	private boolean isPushEnabled() {
		return index != elements.length - 1;
	}

	private boolean isPopEnabled() {
		return index != -1;
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
