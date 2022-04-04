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
import org.evosuite.testcase.variable.VariableReference;

import java.util.Collection;
import java.util.Set;

public class ContainsAssertion extends Assertion {

    private static final long serialVersionUID = -86374077651820640L;

    /**
     * Variable on which the assertion is made
     */
    protected VariableReference containedVariable;

    public VariableReference getContainedVariable() {
        return containedVariable;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Assertion copy(TestCase newTestCase, int offset) {
        ContainsAssertion s = new ContainsAssertion();
        s.source = source.copy(newTestCase, offset);
        s.value = value;
        s.containedVariable = containedVariable.copy(newTestCase, offset);
        s.comment = comment;
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return "assertTrue(" + source.getName() + ".contains(" + containedVariable.getName() + "));";
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#evaluate(org.evosuite.testcase.Scope)
     */
    @Override
    public boolean evaluate(Scope scope) {
        try {
            if (source.getObject(scope) == null)
                return value == null;
            else {
                Object container = source.getObject(scope);
                Object object = containedVariable.getObject(scope);
                if (container instanceof Collection) {
                    return ((Collection<?>) container).contains(object);
                } else {
                    return false; // Is this possible?
                }
            }
        } catch (CodeUnderTestException e) {
            throw new UnsupportedOperationException();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.assertion.Assertion#getReferencedVariables()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<VariableReference> getReferencedVariables() {
        Set<VariableReference> vars = super.getReferencedVariables();
        vars.add(source);
        vars.add(containedVariable);
        return vars;
    }
}
