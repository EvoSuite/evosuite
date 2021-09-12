/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.assertion;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;

import java.lang.reflect.Array;

public class ArrayLengthAssertion extends Assertion {

    private static final long serialVersionUID = -3524688649310294677L;

    public int length = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public Assertion copy(TestCase newTestCase, int offset) {
        ArrayLengthAssertion s = new ArrayLengthAssertion();
        s.source = source.copy(newTestCase, offset);
        s.value = value;
        s.length = length;
        s.comment = comment;
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return "assertEquals(" + value + ", " + source.getName() + ".length);";
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#evaluate(org.evosuite.testcase.Scope)
     */
    @Override
    public boolean evaluate(Scope scope) {
        try {
            if (source.getObject(scope) == null)
                return value == null;
            else
                return Array.getLength(source.getObject(scope)) == length;
        } catch (CodeUnderTestException e) {
            throw new UnsupportedOperationException();
        }
    }
}
