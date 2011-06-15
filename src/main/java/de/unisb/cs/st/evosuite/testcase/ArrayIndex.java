package de.unisb.cs.st.evosuite.testcase;

import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * This class defines an reference to an array element. E.g. foo[3]
 * 
 * @author Sebastian Steenbuck
 * 
 */
public class ArrayIndex extends VariableReferenceImpl {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ArrayIndex.class);

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
}
