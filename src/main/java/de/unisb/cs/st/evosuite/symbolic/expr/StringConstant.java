/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

/**
 * @author krusev
 *
 */
public class StringConstant extends StringExpression {

	private static final long serialVersionUID = 6785078290753992374L;

	protected String value;

	public StringConstant(String StringValue) {
		this.value = StringValue;
	}

	@Override
	public String getConcreteValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringConstant) {
			StringConstant v = (StringConstant) obj;
			return this.value.equals(v.value);
		}
		return false;
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public String execute() {
		return value;
		
	}

}