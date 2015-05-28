package com.examples.with.different.packagename.mutation;

public class MutationPropagation {

	private int x;
	
	public MutationPropagation(int y) {
		x = y;
	}
	
	public void inc() {
		x++;
	}
	
	public boolean isFoo() {
		return x == 5;
	}
	
}
