package de.unisb.cs.st.evosuite.symbolic.expr;

import java.io.Serializable;

public abstract interface Expression<T extends Object> extends Serializable{
	
	public Expression<?> getParent();
	
	public void setParent(Expression<?> expr);
	
	public Object execute();
	
	public Object getConcreteValue();
	
	//public int getSize();

}
