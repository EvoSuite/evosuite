/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

package org.evosuite.testcase.statements.numeric;

import org.evosuite.Properties;
import org.evosuite.seeding.ConstantPool;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.Randomness;

/**
 * <p>
 * IntPrimitiveStatement class.
 * </p>
 *
 * @author fraser
 */
public class IntPrimitiveStatement extends NumericalPrimitiveStatement<Integer> {

    private static final long serialVersionUID = -8616399657291345433L;

    /**
     * <p>
     * Constructor for IntPrimitiveStatement.
     * </p>
     *
     * @param tc    a {@link org.evosuite.testcase.TestCase} object.
     * @param value a {@link java.lang.Integer} object.
     */
    public IntPrimitiveStatement(TestCase tc, Integer value) {
        super(tc, int.class, value);
    }

    /**
     * <p>
     * Constructor for IntPrimitiveStatement.
     * </p>
     *
     * @param tc a {@link org.evosuite.testcase.TestCase} object.
     */
    public IntPrimitiveStatement(TestCase tc) {
        super(tc, int.class, 0);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#zero()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void zero() {
        value = 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#delta()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void delta() {
        int delta = (int) Math.floor(Randomness.nextGaussian() * Properties.MAX_DELTA);
        value = value + delta;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment(long delta) {
        value = value + (int) delta;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#randomize()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void randomize() {
        if (Randomness.nextDouble() >= Properties.PRIMITIVE_POOL) {
            value = (int) (Randomness.nextGaussian() * Properties.MAX_INT);
        } else {
            ConstantPool constantPool = ConstantPoolManager.getInstance().getConstantPool();
            value = constantPool.getRandomInt();
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment() {
        increment(1);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMid(Integer min, Integer max) {
        value = min + ((max - min) / 2);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.NumericalPrimitiveStatement#decrement()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrement() {
        increment(-1);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.NumericalPrimitiveStatement#isPositive()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPositive() {
        return value >= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void negate() {
        value = -value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getValue() {
        return value;
    }
}
