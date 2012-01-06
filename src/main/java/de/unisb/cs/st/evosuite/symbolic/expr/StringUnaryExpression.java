/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

import java.util.logging.Logger;

import gov.nasa.jpf.JPF;

/**
 * @author krusev
 *
 */
public class StringUnaryExpression extends StringExpression implements
UnaryExpression<String>{

	private static final long serialVersionUID = -384874147850376188L;

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.StringUnaryExpression");
	
	protected String concretValue;

	//protected int conretIntValue;
	
	protected Operator op;

	protected Expression<String> val;

	public StringUnaryExpression(Expression<String> left2, Operator op2, String con) {
		this.concretValue = con;
		this.val = left2;
		this.op = op2;
	}

	@Override
	public String getConcreteValue() {
		return concretValue;
	}

	@Override
	public Operator getOperator() {
		return op;
	}

	@Override
	public Expression<String> getOperand() {
		return val;
	}

	@Override
	public String toString() {
		return val + "." + op.toString().trim() + "(" + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringUnaryExpression) {
			StringUnaryExpression other = (StringUnaryExpression) obj;
			return this.op.equals(other.op) && this.getSize() == other.getSize()
			        && this.val.equals(other.val);
		}

		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		//TODO fix this
		return -1;
//		if (size == 0) {
//			size = 1 + getLeftOperand().getSize() + getRightOperand().getSize();
//		}
//		return size;
	}

	@Override
	public Object execute() {
		String exOn = (String)val.execute();
		
		switch (op) {
		case TOLOWERCASE:
			return exOn.toLowerCase();
		case TRIM:
			return exOn.trim();
		case LENGTH:
			//TODO what to do here
			return Integer.toString(exOn.length());
		default:
			log.warning("StringUnaryExpression: unimplemented operator!");
			return null;
		}
		// TODO Auto-generated method stub
		
	}

}
