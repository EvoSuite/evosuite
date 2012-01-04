package de.unisb.cs.st.evosuite.symbolic.expr;

import java.io.Serializable;

//TODO <maybe wrong> changed Expression<T extends Number> to the following
public abstract interface Expression<T extends Object> extends Serializable{
	
	public Expression<?> getParent();
	
	public void setParent(Expression<?> expr);
	
//	public void execute();
	
	//TODO <maybe wrong> changed public T getConcreteValue(); to this
	public Object getConcreteValue();
	
	public int getSize();

}
