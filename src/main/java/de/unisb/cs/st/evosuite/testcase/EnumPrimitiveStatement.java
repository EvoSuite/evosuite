/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class EnumPrimitiveStatement<T extends Enum<T>> extends PrimitiveStatement<T> {

	private static final long serialVersionUID = -7027695648061887082L;

	private final T[] constants;

	@SuppressWarnings("unchecked")
	public EnumPrimitiveStatement(TestCase tc, Class<T> clazz) {
		super(tc, clazz, clazz.getEnumConstants()[0]);
		constants = (T[]) value.getClass().getEnumConstants();
	}

	@SuppressWarnings("unchecked")
	public EnumPrimitiveStatement(TestCase tc, T value) {
		super(tc, value.getClass(), value);
		constants = (T[]) value.getClass().getEnumConstants();
		assert (constants.length > 0);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#delta()
	 */
	@Override
	public void delta() {
		int pos = 0;
		for (pos = 0; pos < constants.length; pos++) {
			if (constants[pos].equals(value)) {
				break;
			}
		}
		boolean delta = Randomness.nextBoolean();
		if (delta) {
			pos++;
		} else {
			pos--;
		}
		if (pos >= constants.length) {
			pos = 0;
		} else if (pos < 0) {
			pos = constants.length - 1;
		}
		value = constants[pos];
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#zero()
	 */
	@Override
	public void zero() {
		value = constants[0];
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#pushBytecode(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	protected void pushBytecode(GeneratorAdapter mg) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	@Override
	public void randomize() {
		int pos = Randomness.nextInt(constants.length);
		value = constants[pos];
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#getCode(java.lang.Throwable)
	 */
	@Override
	public String getCode(Throwable exception) {
		return ((Class<?>) retval.getType()).getSimpleName() + " " + retval.getName()
		        + " = " + value.getClass().getSimpleName() + "." + value + ";";
	}
}
