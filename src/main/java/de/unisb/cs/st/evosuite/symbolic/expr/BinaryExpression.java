package de.unisb.cs.st.evosuite.symbolic.expr;

//TODO <maybe wrong> changed BinaryExpressio<T extends Number> to the following
public interface BinaryExpression<T extends Object> extends Expression<T> {
	
	public Operator getOperator();
	
	public Expression<T> getLeftOperand();
	public Expression<?> getRightOperand();

}
