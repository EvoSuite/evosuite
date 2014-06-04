package com.examples.with.different.packagename.purity;

public class SpecialInspector extends AbstractInspector {
	
	private int value;
	public SpecialInspector(int myValue) {
		this.value = myValue;
	}
	
	public boolean greaterThanZero() {
		return getValue()>0;
	}
	
	private int getValue() {
		return value;
	}

	@Override
	public boolean notPureGreaterThanZero() {
		return notPureGetValue()>0;
	}
	
	private int notPureGetValue() {
		value++;
		return value;
	}
	
	public boolean notPureCreationOfObject() {
		SpecialInspector other = new SpecialInspector(this.value);
		return other.greaterThanZero();
	}

	public boolean pureCreationOfObject() {
		EmptyBox box = new EmptyBox();
		return this.getValue()>0;
	}

	public boolean superPureCall() {
		return super.negateValue(this.value)>0;
	}
	
	public boolean superNotPureCall() {
		return super.impureNegateValue(this.value)>0;
	}
}
