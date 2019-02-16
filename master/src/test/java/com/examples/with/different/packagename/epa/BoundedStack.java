package com.examples.with.different.packagename.epa;

import org.evosuite.epa.EpaAction;
import org.evosuite.epa.EpaActionPrecondition;

public class BoundedStack {

	private final int[] elements;

	private int index = -1;

	@EpaAction(name = "new")
	public BoundedStack(int length) {
		if (length < 0) {
			throw new IllegalArgumentException();
		}
		elements = new int[length];
	}

	@EpaAction(name = "push")
	public void push(int value) {
		try {
			push0(value);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private void push0(int value) {
		if (!isPushEnabled()) {
			throw new IllegalStateException();
		}
		elements[++index] = value;
	}

	@EpaAction(name = "pop")
	public int pop() {
		try {
			return pop0();
		} catch (Exception ex) {
			throw ex;
		}
	}

	private int pop0() {
		if (!isPopEnabled()) {
			throw new IllegalStateException();
		}
		int ret_val = elements[index--];
		return ret_val;
	}

	// ======================================================
	// Boolean Queries
	// ======================================================

	@EpaActionPrecondition(name = "push")
	private boolean isPushEnabled() {
		return index != elements.length - 1;
	}

	@EpaActionPrecondition(name = "pop")
	private boolean isPopEnabled() {
		return index != -1;
	}

}
