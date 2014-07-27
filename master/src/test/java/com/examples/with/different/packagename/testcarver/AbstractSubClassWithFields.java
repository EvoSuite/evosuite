package com.examples.with.different.packagename.testcarver;

public abstract class AbstractSubClassWithFields extends AbstractSuperClassWithFields {

	protected int y = 0;
	
	public AbstractSubClassWithFields(int x) {
		super(x);
		setSeed(0);
	}

	public AbstractSubClassWithFields(int x, int y) {
		super(x);
		setSeed(y);
	}

	@Override
	public abstract boolean testMe(int y);

	
	public void setSeed(int y) {
		this.y = y * 2;
	}
}
