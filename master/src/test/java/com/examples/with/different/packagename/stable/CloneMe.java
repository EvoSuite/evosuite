package com.examples.with.different.packagename.stable;

public class CloneMe {

	private final int value;
	
	public CloneMe(int value) {
		this.value = value;
	}
	
	private boolean cloned = false;
	
	public Object cloneMe() {
		cloned = true;
		CloneMe clone = new CloneMe(this.value);
		return clone;
	}
	
	public boolean throwMe() throws IllegalStateException {
		if (cloned)
			throw new IllegalStateException();
		return false;
	}
	
}
