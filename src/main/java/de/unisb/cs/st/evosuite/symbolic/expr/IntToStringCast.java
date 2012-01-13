package de.unisb.cs.st.evosuite.symbolic.expr;

import java.util.logging.Logger;

import gov.nasa.jpf.JPF;

public class IntToStringCast extends StringExpression implements Cast<Long>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7845864837597413613L;
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.StringUnaryExpression");
	
	protected IntegerVariable intVar;

	public IntToStringCast(IntegerVariable _intVar) {
		this.intVar = _intVar;
	}
	
	@Override
	public String execute() {
		return Long.toString(intVar.execute());
	}

	@Override
	public String getConcreteValue() {
		return Long.toString(intVar.getConcreteValue());
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
//					 && this.getSize() == other.getSize();
		}

		return false;
	}
	
//	@Override
//	public int getSize() {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public IntegerVariable getConcreteObject() {
		return intVar;
	}
}
