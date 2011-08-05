/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * @author Gordon Fraser
 * 
 */
public class FloatPrimitiveStatement extends NumericalPrimitiveStatement<Float> {

	private static final long serialVersionUID = 708022695544843828L;

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public FloatPrimitiveStatement(TestCase tc, Float value) {
		super(tc, float.class, value);
	}

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public FloatPrimitiveStatement(TestCase tc) {
		super(tc, float.class, 0.0F);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#zero()
	 */
	@Override
	public void zero() {
		value = new Float(0.0);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#pushBytecode(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void pushBytecode(GeneratorAdapter mg) {
		mg.push((value).floatValue());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#delta()
	 */
	@Override
	public void delta() {
		int delta = Randomness.nextInt(2 * Properties.MAX_DELTA) - Properties.MAX_DELTA;
		value = new Float((value.floatValue() + delta + Randomness.nextFloat()));
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	@Override
	public void increment(long delta) {
		value = value + delta;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	@Override
	public void increment(double delta) {
		value = value + (float) delta;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	@Override
	public void randomize() {
		if (Randomness.nextDouble() >= P_pool)
			value = (Randomness.nextInt(2 * MAX_INT) - MAX_INT) + Randomness.nextFloat();
		else
			value = primitive_pool.getRandomFloat();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#getCode(java.lang.Throwable)
	 */
	@Override
	public String getCode(Throwable exception) {
		return ((Class<?>) retval.getType()).getSimpleName() + " " + retval.getName()
		        + " = " + value + "F;";
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment()
	 */
	@Override
	public void increment() {
		increment(1.0F);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setMid(Float min, Float max) {
		value = (float) (min + ((max - min) / 2));
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
		return value > 0;
	}
}
