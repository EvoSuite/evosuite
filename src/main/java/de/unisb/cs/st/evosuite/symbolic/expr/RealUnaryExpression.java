package de.unisb.cs.st.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

public class RealUnaryExpression extends RealExpression implements
		UnaryExpression<Double> {

	private static final long serialVersionUID = 9086637495150131445L;

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.RealUnaryExpression");
	
	protected Double concretValue;
	
	protected Operator op;
	
	protected Expression<Double> expr;

	public RealUnaryExpression(Expression<Double> e, Operator op2, Double con) {
		this.expr=e;
		this.op=op2;
		this.concretValue=con;
	}

	@Override
	public Double getConcreteValue() {
		return concretValue;
	}

	@Override
	public Expression<Double> getOperand() {
		return expr;
	}

	@Override
	public Operator getOperator() {
		return op;
	}
	
	@Override
	public String toString() {
		return "("+op.toString()+"("+expr+"))";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RealUnaryExpression)
		{
			RealUnaryExpression v=(RealUnaryExpression) obj;
			return this.op.equals(v.op) 
//						&& this.getSize()==v.getSize() 
						&& this.expr.equals(v.expr);
		}
		return false;
	}
	
//	protected int size=0;
//	@Override
//	public int getSize() {
//		if(size == 0)
//		{
//			size=1+ getOperand().getSize();
//		}
//		return size;
//	}

	@Override
	public Double execute() {
		double leftVal = ExpressionHelper.getDoubleResult(expr);
		
		switch (op) {
		
		case NEG:
			return -leftVal;
		case ABS:
			return Math.abs(leftVal);
		default:
			log.warning("IntegerUnaryExpression: unimplemented operator!");
			return null;
		}
	}

}
