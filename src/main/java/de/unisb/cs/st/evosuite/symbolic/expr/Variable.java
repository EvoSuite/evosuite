package de.unisb.cs.st.evosuite.symbolic.expr;

public interface Variable<T extends Number> extends Expression<T> {

	public T getMaxValue();

	public T getMinValue();

	public String getName();
}
