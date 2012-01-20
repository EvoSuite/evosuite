package de.unisb.cs.st.evosuite.symbolic.expr;

import java.util.logging.Logger;

import gov.nasa.jpf.JPF;

public class IntToStringCast extends StringExpression implements Cast<Long>{
	
	private static final long serialVersionUID = 2414222998301630838L;

	static Logger log = JPF.getLogger((IntToStringCast.class).toString());
	
	protected Expression<Long> intVar;

	public IntToStringCast(Expression<Long> expr) {
		this.intVar = expr;
	}
	
	@Override
	public String execute() {
		return Long.toString((Long)intVar.execute());
	}

	@Override
	public String getConcreteValue() {
		return Long.toString((Long)intVar.getConcreteValue());
	}
	
	@Override
	public String toString() {
		return intVar.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntToStringCast) {
			IntToStringCast other = (IntToStringCast) obj;
			return this.intVar.equals(other.intVar);
		}

		return false;
	}

	@Override
	public Expression<Long> getConcreteObject() {
		return intVar;
	}
}
