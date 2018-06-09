package com.examples.with.different.packagename.epa;

import org.evosuite.epa.EpaAction;
import org.evosuite.epa.EpaState;

public class BoundedStackWithInvalidadState {

	private final static int DEFAULT_SIZE = 10;

	private int[] elements = new int[DEFAULT_SIZE];

	private int index = -1;

	@EpaAction(name = "MyBoundedStack()")
	public BoundedStackWithInvalidadState() {

	}

	@EpaAction(name = "push()")
	public void push(int value) {
		try {
			if (index == elements.length - 1) {
				throw new IllegalStateException();
			}
			elements[++index] = value;

			this.breakStack();
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
	private boolean isStateS1() {
		return isPushEnabled() && !isPopEnabled();
	}

	@EpaState(name = "S2")
	private boolean isStateS2() {
		return isPushEnabled() && isPopEnabled();
	}

	@EpaState(name = "S3")
	private boolean isStateS3() {
		return !isPushEnabled() && isPopEnabled();
	}

	private void breakStack() {
		this.elements = null;
	}

}
