/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.apache.commons.lang.StringEscapeUtils;
import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author fraser
 * 
 */
public class CharPrimitiveStatement extends NumericalPrimitiveStatement<Character> {

	private static final long serialVersionUID = -1960567565801078784L;

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public CharPrimitiveStatement(TestCase tc, Character value) {
		super(tc, char.class, value);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public CharPrimitiveStatement(TestCase tc) {
		super(tc, char.class, (char) 0);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#zero()
	 */
	@Override
	public void zero() {
		value = (char) 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#pushBytecode(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void pushBytecode(GeneratorAdapter mg) {
		mg.push((value).charValue());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#delta()
	 */
	@Override
	public void delta() {
		int delta = Randomness.nextInt(2 * Properties.MAX_DELTA) - Properties.MAX_DELTA;
		value = (char) (value.charValue() + delta);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	@Override
	public void increment(long delta) {
		value = (char) (value + delta);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	@Override
	public void randomize() {
		value = (char) (Randomness.nextChar());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#getCode(java.lang.Throwable)
	 */
	@Override
	public String getCode(Throwable exception) {
		return ((Class<?>) retval.getType()).getSimpleName() + " " + retval.getName()
		        + " = '" + StringEscapeUtils.escapeJava(value.toString()) + "';";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment()
	 */
	@Override
	public void increment() {
		increment((char) 1);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setMid(Character min, Character max) {
		value = (char) (min + ((max - min) / 2));
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.NumericalPrimitiveStatement#decrement()
	 */
	@Override
	public void decrement() {
		increment(-1);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.NumericalPrimitiveStatement#isPositive()
	 */
	@Override
	public boolean isPositive() {
		return value >= 0;
	}
}
