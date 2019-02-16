package com.examples.with.different.packagename.epa;

import org.evosuite.epa.EpaAction;
import org.evosuite.epa.EpaActionPrecondition;

public class BoundedStackSize3ForMining {

	private final static int DEFAULT_SIZE = 3;

	private final int[] elements = new int[DEFAULT_SIZE];

	private int index = -1;

	@EpaAction(name = "new")
	public BoundedStackSize3ForMining() {

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
