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

package org.evosuite.assertion;

import org.evosuite.testcase.variable.VariableReference;

import java.util.HashSet;
import java.util.Set;


/**
 * <p>NullTraceEntry class.</p>
 *
 * @author fraser
 */
public class NullTraceEntry implements OutputTraceEntry {

    private final boolean isNull;

    private final VariableReference var;

    /**
     * <p>Constructor for NullTraceEntry.</p>
     *
     * @param var    a {@link org.evosuite.testcase.variable.VariableReference} object.
     * @param result a boolean.
     */
    public NullTraceEntry(VariableReference var, boolean result) {
        this.var = var;
        this.isNull = result;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#differs(org.evosuite.assertion.OutputTraceEntry)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean differs(OutputTraceEntry other) {
        if (other instanceof NullTraceEntry) {
            NullTraceEntry otherEntry = (NullTraceEntry) other;
            return var.equals(otherEntry.var) && otherEntry.isNull != isNull;

        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#getAssertions(org.evosuite.assertion.OutputTraceEntry)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Assertion> getAssertions(OutputTraceEntry other) {
        Set<Assertion> assertions = new HashSet<>();

        if (other instanceof NullTraceEntry) {
            NullTraceEntry otherEntry = (NullTraceEntry) other;
            if (var.equals(otherEntry.var) && otherEntry.isNull != isNull) {
                NullAssertion assertion = new NullAssertion();
                assertion.value = isNull;
                assertion.source = var;
                assertions.add(assertion);
                assert (assertion.isValid());
            }

        }
        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#getAssertions()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Assertion> getAssertions() {
        Set<Assertion> assertions = new HashSet<>();
        NullAssertion assertion = new NullAssertion();
        assertion.value = isNull;
        assertion.source = var;
        assertions.add(assertion);
        assert (assertion.value != null);
        assert (assertion.isValid());
        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#isDetectedBy(org.evosuite.assertion.Assertion)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDetectedBy(Assertion assertion) {
        if (assertion instanceof NullAssertion) {
            NullAssertion ass = (NullAssertion) assertion;
            if (var.equals(ass.source))
                return (Boolean) ass.value != isNull;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#cloneEntry()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputTraceEntry cloneEntry() {
        return new NullTraceEntry(var, isNull);
    }

}
