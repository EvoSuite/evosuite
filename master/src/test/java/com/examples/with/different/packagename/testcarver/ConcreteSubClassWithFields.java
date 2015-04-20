package com.examples.with.different.packagename.testcarver;

public class ConcreteSubClassWithFields extends AbstractSubClassWithFields {

	public ConcreteSubClassWithFields(int x) {
		super(x);
	}

	public ConcreteSubClassWithFields(int x, int y) {
		super(x, y);
	}

	@Override
	public boolean testMe(int y) {
		return x == y;
	}
	
}
