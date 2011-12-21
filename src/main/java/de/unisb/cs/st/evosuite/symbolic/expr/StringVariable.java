/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

/**
 * @author krusev
 *
 */
public class StringVariable extends StringExpression implements Variable<String> {

	private static final long serialVersionUID = 5925030390824261492L;

	protected String name;

	protected String minValue;

	protected String maxValue;

	public StringVariable(String name, String minValue, String maxValue) {
		super();
		this.name = name;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}	
	
	@Override
	public String getConcreteValue() {
		return null;
	}
	
	//TODO getMax and getMin are only here to ... the convention from variable
	//they should maybe be removed from there since it makes no sense having 
	//them for objects
	@Override
	public String getMaxValue() {
		return maxValue;
	}

	@Override
	public String getMinValue() {
		return minValue;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringVariable) {
			StringVariable v = (StringVariable) obj;
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
	
	@Override
	public int getSize() {
		return -1;
	}

}
