package de.unisb.cs.st.evosuite.symbolic.expr;

public class IntegerVariable extends IntegerExpression implements Variable<Long> {

	private static final long serialVersionUID = 6302073364874210525L;

	protected String name;

	protected long concreteValue;
	
	protected long minValue;

	protected long maxValue;

	public IntegerVariable(String name, long conV, long minValue, long maxValue) {
		super();
		this.name = name;
		this.concreteValue = conV;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public Long getConcreteValue() {
		return concreteValue;
	}
	
	public void setConcreteValue(Long con) {
		concreteValue = con;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Long getMinValue() {
		return minValue;
	}

	@Override
	public Long getMaxValue() {
		return maxValue;
	}

	@Override
	public String toString() {
		return this.name + "(" + concreteValue + ")";
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntegerVariable) {
			Variable<Integer> v = (Variable<Integer>) obj;
			return this.getName().equals(v.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = this.name.hashCode();
		}
		return hash;
	}

//	@Override
//	public int getSize() {
//		return 1;
//	}

	@Override
	public Long execute() {
		return concreteValue;
	}

}
