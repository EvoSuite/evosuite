package de.unisb.cs.st.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

public class RealBinaryExpression extends RealExpression implements
        BinaryExpression<Double> {

	private static final long serialVersionUID = 3095108718393239244L;

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.IntegerBinaryExpression");

	protected Double concretValue;

	protected Operator op;

	protected Expression<Double> left;
	protected Expression<?> right;

	public RealBinaryExpression(Expression<Double> left2, Operator op2,
	        Expression<?> right2, Double con) {
		this.concretValue = con;
		this.left = left2;
		this.right = right2;
		this.op = op2;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
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
	public Expression<?> getRightOperand() {
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
			return this.op.equals(other.op) && this.getSize() == other.getSize()
			        && this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + left.getSize() + right.getSize();
		}
		return size;
	}

	@Override
	public Object execute() {

		double leftVal = (Double) left.execute();
		double rightVal = (Double) right.execute();

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
		case ATAN2:
			return Math.atan2(leftVal, rightVal);
		case COPYSIGN:
			return Math.copySign(leftVal, rightVal);
		case HYPOT:
			return Math.hypot(leftVal, rightVal);
		case IEEEREMAINDER:
			return Math.IEEEremainder(leftVal, rightVal);
		case MAX:
			return Math.max(leftVal, rightVal);
		case MIN:
			return Math.min(leftVal, rightVal);
		case NEXTAFTER:
			return Math.nextAfter(leftVal, rightVal);
		case POW:
			return Math.pow(leftVal, rightVal);
		case SCALB:
			return Math.scalb(leftVal, (int) rightVal);

		default:
			log.warning("IntegerBinaryExpression: unimplemented operator!");
			return null;
		}

	}

}
