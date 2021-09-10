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

import org.evosuite.testcase.variable.VariableReference;

import java.util.HashSet;
import java.util.Set;

public class ArrayLengthTraceEntry implements OutputTraceEntry {

    protected VariableReference var;

    protected int length;

    public ArrayLengthTraceEntry(VariableReference var, Object[] value) {
        this.var = var;
        this.length = value.length;
    }

    public ArrayLengthTraceEntry(VariableReference var, int length) {
        this.var = var;
        this.length = length;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#differs(org.evosuite.assertion.OutputTraceEntry)
     */
    @Override
    public boolean differs(OutputTraceEntry other) {
        if (other instanceof ArrayLengthTraceEntry) {
            ArrayLengthTraceEntry otherEntry = (ArrayLengthTraceEntry) other;
            return length != otherEntry.length;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#getAssertions(org.evosuite.assertion.OutputTraceEntry)
     */
    @Override
    public Set<Assertion> getAssertions(OutputTraceEntry other) {
        Set<Assertion> assertions = new HashSet<>();
        if (other instanceof ArrayLengthTraceEntry) {
            ArrayLengthTraceEntry otherEntry = (ArrayLengthTraceEntry) other;
            if (length != otherEntry.length) {
                ArrayLengthAssertion assertion = new ArrayLengthAssertion();
                assertion.length = length;
                assertion.source = var;
                assertion.value = length;
                assertions.add(assertion);
                assert (assertion.isValid());
            }
        }
        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#getAssertions()
     */
    @Override
    public Set<Assertion> getAssertions() {
        Set<Assertion> assertions = new HashSet<>();
        ArrayLengthAssertion assertion = new ArrayLengthAssertion();
        assertion.source = var;
        assertion.length = length;
        assertion.value = length;
        assertions.add(assertion);
        assert (assertion.isValid());

        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#isDetectedBy(org.evosuite.assertion.Assertion)
     */
    @Override
    public boolean isDetectedBy(Assertion assertion) {
        if (assertion instanceof ArrayLengthAssertion) {
            ArrayLengthAssertion ass = (ArrayLengthAssertion) assertion;
            if (var.equals(ass.source)) {
                return length != ass.length;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#cloneEntry()
     */
    @Override
    public OutputTraceEntry cloneEntry() {
        return new ArrayLengthTraceEntry(var, length);
    }

}
