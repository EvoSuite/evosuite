package de.unisb.cs.st.evosuite.testcase;

public class ArrayReference extends VariableReferenceImpl {
	protected int array_length;

	public ArrayReference(TestCase tc, GenericClass clazz, int array_length) {
		super(tc, clazz);
		assert (array_length >= 0);
		this.array_length = array_length;
	}

	public int getArrayLength() {
		return array_length;
	}

	public void setArrayLength(int l) {
		assert (l >= 0);
		array_length = l;
	}

	/**
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference clone(TestCase newTestCase) {
		ArrayReference otherArray = (ArrayReference) newTestCase.getStatement(getStPosition()).getReturnValue();
		otherArray.setArrayLength(array_length);
		return otherArray;
	}
}
