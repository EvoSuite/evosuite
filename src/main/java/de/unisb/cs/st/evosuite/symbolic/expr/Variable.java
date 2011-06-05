package de.unisb.cs.st.evosuite.symbolic.expr;

public  interface Variable<T extends Number> extends Expression<T> {

	public String getName();
	public T getMinValue();
	public T getMaxValue();
}
