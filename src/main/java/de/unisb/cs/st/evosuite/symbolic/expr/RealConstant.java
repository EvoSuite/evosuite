package de.unisb.cs.st.evosuite.symbolic.expr;

public class RealConstant extends RealExpression{
	private static final long serialVersionUID = 1L;
	
	protected double value;

	public RealConstant(double doubleValue) {
		this.value=doubleValue;
	}

	@Override
	public Double getConcreteValue() {
		return value;
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RealConstant)
		{
			RealConstant v=(RealConstant) obj;
			return this.value==v.value;
		}
		return false;
	}

//	@Override
//	public int getSize() {
//		return 1;
//	}

	@Override
	public Double execute() {
		// TODO Auto-generated method stub
		return value;
	}
}
