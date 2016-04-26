/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.testcase.statements.numeric;

import java.lang.reflect.Type;

import org.evosuite.Properties;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * <p>
 * BytePrimitiveStatement class.
 * </p>
 * 
 * @author fraser
 */
public class BytePrimitiveStatement extends NumericalPrimitiveStatement<Byte> {

	/**
	 * <p>
	 * Constructor for BytePrimitiveStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param value
	 *            a {@link java.lang.Byte} object.
	 */
	public BytePrimitiveStatement(TestCase tc, Byte value) {
		super(tc, byte.class, value);
	}

	/**
	 * <p>
	 * Constructor for BytePrimitiveStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public BytePrimitiveStatement(TestCase tc) {
		super(tc, byte.class, (byte) 0);
	}

	/**
	 * <p>
	 * Constructor for BytePrimitiveStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 */
	public BytePrimitiveStatement(TestCase tc, Type type) {
		super(tc, type, (byte) 0);
	}

	private static final long serialVersionUID = -8123457944460041347L;

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#zero()
	 */
	/** {@inheritDoc} */
	@Override
	public void zero() {
		value = (byte) 0;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#delta()
	 */
	/** {@inheritDoc} */
	@Override
	public void delta() {
		byte delta = (byte)Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
		value = (byte) (value.byteValue() + delta);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void increment(long delta) {
		value = (byte) (value + delta);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	/** {@inheritDoc} */
	@Override
	public void randomize() {
		if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL)
			value = (byte) (Randomness.nextInt(256) - 128);
		else {
			ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();
			value = (byte) constantPool.getRandomInt();
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment()
	 */
	/** {@inheritDoc} */
	@Override
	public void increment() {
		increment((byte) 1);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment()
	 */
	/** {@inheritDoc} */
	@Override
	public void decrement() {
		increment((byte) -1);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void setMid(Byte min, Byte max) {
		value = (byte) (min + ((max - min) / 2));
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#isPositive()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPositive() {
		// TODO Auto-generated method stub
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void negate() {
		value = (byte) -value;
	}
}
