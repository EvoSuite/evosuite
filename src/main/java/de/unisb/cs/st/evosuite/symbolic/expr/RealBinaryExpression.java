package de.unisb.cs.st.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

public class RealBinaryExpression extends RealExpression implements
        BinaryExpression<Double> {

	private static final long serialVersionUID = 3095108718393239244L;

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.IntegerBinaryExpression");
	
	protected Double concretValue;

	protected Operator op;

	protected Expression<Double> left;
	protected Expression<Double> right;

	public RealBinaryExpression(Expression<Double> left2, Operator op2,
	        Expression<Double> right2, Double con) {
		this.concretValue = con;
		this.left = left2;
		this.right = right2;
		this.op = op2;
	}

	@Override
	public Double getConcreteValue() {
		return concretValue;
	}

	@Override
	public Operator getOperator() {
		return op;
	}

	@Override
	public Expression<Double> getLeftOperand() {
		return left;
	}

	@Override
	public Expression<Double> getRightOperand() {
		return right;
	}

	@Override
	public String toString() {
		return "(" + left + op.toString() + right + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RealBinaryExpression) {
			RealBinaryExpression other = (RealBinaryExpression) obj;
			return this.op.equals(other.op) 
//					&& this.getSize() == other.getSize()
			        && this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	protected int size = 0;

//	@Override
//	public int getSize() {
//		if (size == 0) {
//			size = 1 + getLeftOperand().getSize() + getRightOperand().getSize();
//		}
//		return size;
//	}

	@Override
	public Object execute() {
		
		double leftVal = ExpressionHelper.getDoubleResult(left);
		double rightVal = ExpressionHelper.getDoubleResult(right);
		
		switch (op) {
		
		case DIV:
			return leftVal / rightVal;
		case MUL:
			return leftVal * rightVal;
		case MINUS:
			return leftVal - rightVal;
		case PLUS: 
			return leftVal + rightVal;
		case REM: 
			return leftVal % rightVal;	
		default:
			log.warning("IntegerBinaryExpression: unimplemented operator!");
			return null;
		}

	}

}
