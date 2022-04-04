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
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.Randomness;


/**
 * <p>CharPrimitiveStatement class.</p>
 *
 * @author fraser
 */
public class CharPrimitiveStatement extends NumericalPrimitiveStatement<Character> {

    private static final long serialVersionUID = -1960567565801078784L;

    /**
     * <p>Constructor for CharPrimitiveStatement.</p>
     *
     * @param tc    a {@link org.evosuite.testcase.TestCase} object.
     * @param value a {@link java.lang.Character} object.
     */
    public CharPrimitiveStatement(TestCase tc, Character value) {
        super(tc, char.class, value);
        // TODO Auto-generated constructor stub
    }

    /**
     * <p>Constructor for CharPrimitiveStatement.</p>
     *
     * @param tc a {@link org.evosuite.testcase.TestCase} object.
     */
    public CharPrimitiveStatement(TestCase tc) {
        super(tc, char.class, (char) 0);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#zero()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void zero() {
        value = (char) 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#delta()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void delta() {
        int delta = Randomness.nextInt(2 * Properties.MAX_DELTA) - Properties.MAX_DELTA;
        value = (char) (value + delta);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment(java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment(long delta) {
        value = (char) (value + delta);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#randomize()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void randomize() {
        value = Randomness.nextChar();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.PrimitiveStatement#increment()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void increment() {
        increment((char) 1);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.NumericalPrimitiveStatement#setMid(java.lang.Object, java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMid(Character min, Character max) {
        value = (char) (min + ((max - min) / 2));
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
        // chars are always positive
        return true;
    }
}
