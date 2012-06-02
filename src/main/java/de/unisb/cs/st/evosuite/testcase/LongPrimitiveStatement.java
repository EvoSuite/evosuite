/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
public class LongPrimitiveStatement extends NumericalPrimitiveStatement<Long> {

	private static final long serialVersionUID = 6902273233816031053L;

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public LongPrimitiveStatement(TestCase tc, Long value) {
		super(tc, long.class, value);
	}

	/**
	 * @param tc
	 * @param type
	 * @param value
	 */
	public LongPrimitiveStatement(TestCase tc) {
		super(tc, long.class, (long) 0);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#zero()
	 */
	@Override
	public void zero() {
		value = new Long(0);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#pushBytecode(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void pushBytecode(GeneratorAdapter mg) {
		mg.push((value).longValue());
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#delta()
	 */
	@Override
	public void delta() {
		int delta = Randomness.nextInt(2 * Properties.MAX_DELTA) - Properties.MAX_DELTA;
		value = new Long((value.longValue() + delta));
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	@Override
	public void increment(long delta) {
		value = value + delta;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	@Override
	public void randomize() {
		if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL)
			value = (long) (Randomness.nextInt(2 * Properties.MAX_INT) - Properties.MAX_INT);
		else
			value = primitive_pool.getRandomLong();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.PrimitiveStatement#increment()
	 */
	@Override
	public void increment() {
		increment(1);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setMid(Long min, Long max) {
		value = (long) (min + ((max - min) / 2));
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
		value = -value;
	}
}
