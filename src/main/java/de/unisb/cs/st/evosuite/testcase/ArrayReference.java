package de.unisb.cs.st.evosuite.testcase;

public class ArrayReference extends VariableReferenceImpl {

	private static final long serialVersionUID = -1473965684542348550L;

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
		VariableReference newRef = newTestCase.getStatement(getStPosition()).getReturnValue();
		if (newRef instanceof ArrayReference) {
			ArrayReference otherArray = (ArrayReference) newRef;
			otherArray.setArrayLength(array_length);
			return otherArray;
		} else {
			if (newRef.getComponentType() != null) {
				ArrayReference otherArray = new ArrayReference(newTestCase, type,
				        array_length);
				otherArray.setArrayLength(array_length);
				return otherArray;
			} else {
				// This may happen when cloning a method statement which returns an Object that in fact is an array
				// We'll just create a new ArrayReference in this case.
				ArrayReference otherArray = new ArrayReference(newTestCase, type,
				        array_length);
				otherArray.setArrayLength(array_length);
				return otherArray;
				//				throw new RuntimeException("After cloning the array disappeared: "
				//				        + getName() + "/" + newRef.getName() + " in test "
				//				        + newTestCase.toCode() + " / old test: " + testCase.toCode());
			}
		}
	}
}
