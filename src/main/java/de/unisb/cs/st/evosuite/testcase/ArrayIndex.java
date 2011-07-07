package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Array;
import java.util.Map;

import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This class defines an reference to an array element. E.g. foo[3]
 * 
 * @author Sebastian Steenbuck
 * 
 */
public class ArrayIndex extends VariableReferenceImpl {

	/**
	 * Index in the array
	 */
	protected int array_index = 0;

	/**
	 * If this variable is contained in an array, this is the reference to the
	 * array
	 */
	protected ArrayReference array = null;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            The type (class) of the variable
	 * @param position
	 *            The statement in the test case that declares this variable
	 */
	public ArrayIndex(TestCase testCase, ArrayReference array, int index) {
		super(testCase, new GenericClass(array.getComponentType()));
		this.array = array;
		this.array_index = index;
	}

	public ArrayReference getArray() {
		return array;
	}

	public void setArray(ArrayReference r) {
		array = r;
	}

	/**
	 * Return true if variable is an array
	 */
	public boolean isArrayIndex() {
		return true;
	}

	public int getArrayIndex() {
		return array_index;
	}

	public void setArrayIndex(int index) {
		array_index = index;
	}

	@Override
	public int getStPosition() {
		assert (array != null);
		for (int i = 0; i < testCase.size(); i++) {
			if (testCase.getStatement(i).getReturnValue().equals(this)) {
				return i;
			}
		}

		//notice that this case is only reached if no AssignmentStatement was used to assign to the array index (as in that case the for loop would have found something)
		//Therefore the array must have been assigned in some method and we can return the method call

		//throw new AssertionError(
		//        "A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");

		return array.getStPosition();

		//throw new AssertionError("A VariableReferences position is only defined if the VariableReference is defined by a statement in the testCase");
	}

	/**
	 * Return name for source code representation
	 * 
	 * @return
	 */
	@Override
	public String getName() {
		return array.getName() + "[" + array_index + "]";
	}

	@Override
	public void loadBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
		array.loadBytecode(mg, locals);
		mg.push(array_index);
		mg.arrayLoad(org.objectweb.asm.Type.getType(type.getRawClass()));

	}

	@Override
	public void storeBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals) {
		array.loadBytecode(mg, locals);
		mg.push(array_index);
		mg.arrayStore(org.objectweb.asm.Type.getType(type.getRawClass()));
	}

	@Override
	public boolean same(VariableReference r) {
		if (r == null)
			return false;

		if (!(r instanceof ArrayIndex))
			return false;

		ArrayIndex other = (ArrayIndex) r;
		if (this.getStPosition() != r.getStPosition())
			return false;

		if (!this.array.same(other.getArray()))
			return false;

		if (this.array_index != other.getArrayIndex())
			return false;

		if (this.type.equals(r.getGenericClass()))
			;

		return true;
	}

	/**
	 * Return the actual object represented by this variable for a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 */
	@Override
	public Object getObject(Scope scope) throws CodeUnderTestException {
		Object arrayObject = array.getObject(scope);
		if (arrayObject != null) {
			return Array.get(arrayObject, array_index);
		} else {
			return null;
		}
	}

	/**
	 * Set the actual object represented by this variable in a given scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 * @param value
	 *            The value to be assigned
	 */
	@Override
	public void setObject(Scope scope, Object value) throws CodeUnderTestException{
		Object arrayObject = array.getObject(scope);
		Array.set(arrayObject, array_index, value);
	}

	/**
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference clone(TestCase newTestCase) {
		ArrayReference otherArray = (ArrayReference) newTestCase.getStatement(array.getStPosition()).getReturnValue(); //must be set as we only use this to clone whole testcases
		return new ArrayIndex(newTestCase, otherArray, array_index);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#getAdditionalVariableReference()
	 */
	@Override
	public VariableReference getAdditionalVariableReference() {
		if (array.getAdditionalVariableReference() == null)
			return array;
		else
			return array.getAdditionalVariableReference();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#setAdditionalVariableReference(de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void setAdditionalVariableReference(VariableReference var) {
		assert (var instanceof ArrayReference);
		array = (ArrayReference) var;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.VariableReference#replaceAdditionalVariableReference(de.unisb.cs.st.evosuite.testcase.VariableReference, de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replaceAdditionalVariableReference(VariableReference var1,
	        VariableReference var2) {
		if (array.equals(var1)) {
			if (var2 instanceof ArrayReference) {
				array = (ArrayReference) var2;
			}
			// EvoSuite might try to replace this with a field reference
			// but for this we have FieldStatements, which would give us
			// ArrayReferences.
			// Such a replacement should only happen as part of a graceful delete
		} else
			array.replaceAdditionalVariableReference(var1, var2);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((array == null) ? 0 : array.hashCode());
		result = prime * result + array_index;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayIndex other = (ArrayIndex) obj;
		if (array == null) {
			if (other.array != null)
				return false;
		} else if (!array.equals(other.array))
			return false;
		if (array_index != other.array_index)
			return false;
		return true;
	}

}
