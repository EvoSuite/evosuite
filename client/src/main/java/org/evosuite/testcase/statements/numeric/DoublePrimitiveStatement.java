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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.evosuite.Properties;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.Randomness;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * <p>
 * DoublePrimitiveStatement class.
 * </p>
 * 
 * @author fraser
 */
public class DoublePrimitiveStatement extends NumericalPrimitiveStatement<Double> {

	private static final long serialVersionUID = 6229514439946892566L;

	/**
	 * <p>
	 * Constructor for DoublePrimitiveStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param value
	 *            a {@link java.lang.Double} object.
	 */
	public DoublePrimitiveStatement(TestCase tc, Double value) {
		super(tc, double.class, value);
	}

	/**
	 * <p>
	 * Constructor for DoublePrimitiveStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public DoublePrimitiveStatement(TestCase tc) {
		super(tc, double.class, 0.0);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#zero()
	 */
	/** {@inheritDoc} */
	@Override
	public void zero() {
		value = new Double(0.0);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#delta()
	 */
	/** {@inheritDoc} */
	@Override
	public void delta() {
		double P = Randomness.nextDouble();
		if(P < 1d/3d) {
			value += Randomness.nextGaussian() * Properties.MAX_DELTA;
		} else if(P < 2d/3d) {
			value += Randomness.nextGaussian();
		} else {
			int precision = Randomness.nextInt(15);
			chopPrecision(precision);
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void increment(long delta) {
		value = value + delta;
	}
	
	private void chopPrecision(int precision) {
		if(value.isNaN() || value.isInfinite())
			return;
		
		BigDecimal bd = new BigDecimal(value).setScale(precision, RoundingMode.HALF_EVEN);
		this.value = bd.doubleValue();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void increment(double delta) {
		value = value + delta;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#randomize()
	 */
	/** {@inheritDoc} */
	@Override
	public void randomize() {
		if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
			value = Randomness.nextGaussian() * Properties.MAX_INT;
			int precision = Randomness.nextInt(15);
			chopPrecision(precision);
		}
		else {
			ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();
			value = constantPool.getRandomDouble();
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.PrimitiveStatement#increment()
	 */
	/** {@inheritDoc} */
	@Override
	public void increment() {
		increment(1.0);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public void setMid(Double min, Double max) {
		value = (double) (min + ((max - min) / 2));
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#decrement()
	 */
	/** {@inheritDoc} */
	@Override
	public void decrement() {
		increment(-1.0);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.NumericalPrimitiveStatement#isPositive()
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPositive() {
		return value >= 0;
	}

	/** {@inheritDoc} */
	@Override
	public void negate() {
		value = -value;
	}
}
