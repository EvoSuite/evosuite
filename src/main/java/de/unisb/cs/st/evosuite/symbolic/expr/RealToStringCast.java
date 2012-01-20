package de.unisb.cs.st.evosuite.symbolic.expr;

import java.util.logging.Logger;

import gov.nasa.jpf.JPF;

public class RealToStringCast extends StringExpression implements Cast<Double>{

	private static final long serialVersionUID = -5322228289539145088L;

	static Logger log = JPF.getLogger((RealToStringCast.class).toString());
	
	protected Expression<Double> expr;

	public RealToStringCast(Expression<Double> _expr) {
		this.expr = _expr;
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
			return this.expr.equals(other.expr);
		}

		return false;
	}

	@Override
	public Expression<Double> getConcreteObject() {
		return expr;
	}
}
