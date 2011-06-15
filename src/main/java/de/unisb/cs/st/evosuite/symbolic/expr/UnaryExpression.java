package de.unisb.cs.st.evosuite.symbolic.expr;

public interface UnaryExpression<T extends Number> extends Expression<T> {
	
	public Expression<T> getOperand();
	
	public Operator getOperator();

}
