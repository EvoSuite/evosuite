/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author fraser
 * 
 */
public class ShortPrimitiveStatement extends NumericalPrimitiveStatement<Short> {

	private static final long serialVersionUID = -1041008456902695964L;

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public ShortPrimitiveStatement(TestCase tc, Short value) {
		super(tc, short.class, value);
	}

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public ShortPrimitiveStatement(TestCase tc) {
		super(tc, short.class, (short) 0);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#zero()
	 */
	@Override
	public void zero() {
		value = (short) 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#pushBytecode(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void pushBytecode(GeneratorAdapter mg) {
		mg.push((value).shortValue());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#delta()
	 */
	@Override
	public void delta() {
		int delta = Randomness.nextInt(2 * Properties.MAX_DELTA) - Properties.MAX_DELTA;
		value = (short) (value.shortValue() + delta);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	@Override
	public void increment(long delta) {
		value = (short) (value + (short) delta);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	@Override
	public void randomize() {
		int max = Math.min(Properties.MAX_INT, 32767);
		if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL)
			value = (short) (Randomness.nextInt(2 * max) - max);
		else
			value = (short) primitive_pool.getRandomInt();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment()
	 */
	@Override
	public void increment() {
		increment((short) 1);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setMid(Short min, Short max) {
		value = (short) (min + ((max - min) / 2));
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
	
	@Override
	public void negate() {
		value = (short) -value;
	}
}
