package de.unisb.cs.st.evosuite.symbolic.expr;

//TODO <maybe wrong> changed Variable<T extends Number> to the following
public  interface Variable<T extends Object> extends Expression<T> {

	public String getName();
	
	//TODO get rid of this since not all variables have them e.g. String, 
	//Array or whatever object
	public T getMinValue();
	public T getMaxValue();
}
