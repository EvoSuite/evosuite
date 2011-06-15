/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;

/**
 * @author Gordon Fraser
 * 
 */
public class ConstantValue extends VariableReferenceImpl {

	private Object value;

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

	/**
	 * Return name for source code representation
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return "" + value;
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

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
