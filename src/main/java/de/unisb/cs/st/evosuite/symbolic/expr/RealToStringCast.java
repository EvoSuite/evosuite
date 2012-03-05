package de.unisb.cs.st.evosuite.symbolic.expr;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

import gov.nasa.jpf.JPF;

public class RealToStringCast extends StringExpression implements Cast<Double>{

	private static final long serialVersionUID = -5322228289539145088L;

	static Logger log = JPF.getLogger((RealToStringCast.class).toString());
	
	protected Expression<Double> expr;

	public RealToStringCast(Expression<Double> _expr) {
		this.expr = _expr;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}
	
	@Override
	public String execute() {
		return Double.toString((Double)expr.execute());
	}

	@Override
	public String getConcreteValue() {
		return Double.toString((Double)expr.getConcreteValue());
	}
	
	@Override
	public String toString() {
		return "(String)" + expr.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RealToStringCast) {
			RealToStringCast other = (RealToStringCast) obj;
			return this.expr.equals(other.expr)
				&& this.getSize() == other.getSize();
		}

		return false;
	}
	
	protected int size=0;
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1 + expr.getSize();
		}
		return size;
	}

	@Override
	public Expression<Double> getConcreteObject() {
		return expr;
	}
}
