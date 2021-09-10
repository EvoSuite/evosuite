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

import org.evosuite.testcase.TestCase;
import org.evosuite.utils.Randomness;


/**
 * <p>BooleanPrimitiveStatement class.</p>
 *
 * @author fraser
 */
public class BooleanPrimitiveStatement extends NumericalPrimitiveStatement<Boolean> {

    /**
     * <p>Constructor for BooleanPrimitiveStatement.</p>
     *
     * @param tc    a {@link org.evosuite.testcase.TestCase} object.
     * @param value a {@link java.lang.Boolean} object.
     */
    public BooleanPrimitiveStatement(TestCase tc, Boolean value) {
        super(tc, boolean.class, value);
    }

    /**
     * <p>Constructor for BooleanPrimitiveStatement.</p>
     *
     * @param tc a {@link org.evosuite.testcase.TestCase} object.
     */
    public BooleanPrimitiveStatement(TestCase tc) {
        super(tc, boolean.class, false);
    }

    private static final long serialVersionUID = 2864789903354543815L;

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#zero()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void zero() {
        value = false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#delta()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void delta() {
        value = !value;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment(long delta) {
        delta();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#randomize()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void randomize() {
        value = Randomness.nextBoolean();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment() {
        delta();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void decrement() {
        delta();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMid(Boolean min, Boolean max) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.NumericalPrimitiveStatement#isPositive()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPositive() {
        return !value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void negate() {
        value = !value;
    }
}
