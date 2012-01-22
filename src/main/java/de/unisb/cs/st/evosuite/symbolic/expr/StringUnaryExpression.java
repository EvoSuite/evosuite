/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

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

	protected Expression<String> left;

	public StringUnaryExpression(Expression<String> left2, Operator op2, String con) {
		this.concretValue = con;
		this.left = left2;
		this.op = op2;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
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
		return left;
	}

	@Override
	public String toString() {
		return left + "." + op.toString().trim() + "(" + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringUnaryExpression) {
			StringUnaryExpression other = (StringUnaryExpression) obj;
			return this.op.equals(other.op) 
			        && this.left.equals(other.left);
		}

		return false;
	}

	protected int size=0;
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1 + left.getSize();
		}
		return size;
	}
	
	@Override
	public String execute() {
		String exOn = (String)left.execute();
		
		switch (op) {
		
		case TOLOWERCASE:
			return exOn.toLowerCase();
		case TOUPPERCASE:
			return exOn.toUpperCase();
		case TRIM:
			return exOn.trim();
		case LENGTH:
			return Integer.toString(exOn.length());
		default:
			log.warning("StringUnaryExpression: unimplemented operator!");
			return null;
		}		
	}

}
