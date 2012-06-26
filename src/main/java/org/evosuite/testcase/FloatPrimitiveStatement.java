/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.testcase;

import org.evosuite.Properties;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.commons.GeneratorAdapter;


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
	 * @see org.evosuite.testcase.PrimitiveStatement#zero()
	 */
	@Override
	public void zero() {
		value = new Float(0.0);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#pushBytecode(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void pushBytecode(GeneratorAdapter mg) {
		mg.push((value).floatValue());
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#delta()
	 */
	@Override
	public void delta() {
		int delta = Randomness.nextInt(2 * Properties.MAX_DELTA) - Properties.MAX_DELTA;
		value = new Float((value.floatValue() + delta + Randomness.nextFloat()));
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	@Override
	public void increment(long delta) {
		value = value + delta;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	@Override
	public void increment(double delta) {
		value = value + (float) delta;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	@Override
	public void randomize() {
		if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL)
			value = (Randomness.nextInt(2 * Properties.MAX_INT) - Properties.MAX_INT) + Randomness.nextFloat();
		else
			value = primitive_pool.getRandomFloat();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment()
	 */
	@Override
	public void increment() {
		increment(1.0F);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void setMid(Float min, Float max) {
		value = (float) (min + ((max - min) / 2));
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#decrement()
	 */
	@Override
	public void decrement() {
		increment(-1);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#isPositive()
	 */
	@Override
	public boolean isPositive() {
		return value > 0;
	}

	@Override
	public void negate() {
		value = -value;
	}
}
