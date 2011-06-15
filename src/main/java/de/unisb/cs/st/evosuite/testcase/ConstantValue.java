/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author Gordon Fraser
 * 
 */
public class ConstantValue extends VariableReferenceImpl {

	/**
	 * @param testCase
	 * @param type
	 */
	public ConstantValue(TestCase testCase, GenericClass type) {
		super(testCase, type);
	}

	public ConstantValue(TestCase testCase, Type type) {
		this(testCase, new GenericClass(type));
	}

	public ConstantValue clone(TestCase testCase) {
		ConstantValue ret = new ConstantValue(testCase, type);
		ret.setValue(value);
		return ret;
	}

	private Object value;

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * The position of the statement, defining this VariableReference, in the
	 * testcase.
	 * 
	 * @return
	 */
	@Override
	public int getStPosition() {
		for (int i = 0; i < testCase.size(); i++) {
			if (testCase.getStatement(i).references(this)) {
				return i;
			}
		}

		throw new AssertionError(
		        "A ConstantValue position is only defined if the VariableReference is defined by a statement");
	}

	/**
	 * Return name for source code representation
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		if (value.getClass().equals(char.class)
		        || value.getClass().equals(Character.class))
			return "'" + StringEscapeUtils.escapeJava(value.toString()) + "';";
		else if (value.getClass().equals(String.class)) {
			return "\"" + StringEscapeUtils.escapeJava((String) value) + "\"";
		} else if (value.getClass().equals(float.class)
		        || value.getClass().equals(Float.class)) {
			return value + "F;";
		} else if (value.getClass().equals(long.class)
		        || value.getClass().equals(Long.class)) {
			return value + "L;";
		} else
			return "" + value;
	}
}
