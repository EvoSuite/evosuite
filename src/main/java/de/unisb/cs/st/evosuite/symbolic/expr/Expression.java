package de.unisb.cs.st.evosuite.symbolic.expr;

import java.io.Serializable;

public abstract interface Expression<T extends Number> extends Serializable {

	public T getConcreteValue();

	public int getSize();

}
