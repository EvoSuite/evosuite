package de.unisb.cs.st.evosuite.symbolic.expr;

//TODO <maybe wrong> changed UnaryExpression<T extends Number> to the following
public interface UnaryExpression<T extends Object> extends Expression<T> {
	
	public Expression<T> getOperand();
	
	public Operator getOperator();

}
