package de.unisb.cs.st.evosuite.symbolic.expr;

public interface BinaryExpression<T extends Number> extends Expression<T> {
	
	public Operator getOperator();
	
	public Expression<T> getLeftOperand();
	public Expression<T> getRightOperand();

}
