package de.unisb.cs.st.evosuite.symbolic.expr;

public interface BinaryExpression<T extends Number> extends Expression<T> {

	public Expression<T> getLeftOperand();

	public Operator getOperator();

	public Expression<T> getRightOperand();

}
