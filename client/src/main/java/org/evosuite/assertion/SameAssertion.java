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

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.variable.VariableReference;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>SameAssertion class.</p>
 *
 * @author Gordon Fraser
 */
public class SameAssertion extends Assertion {

    private static final long serialVersionUID = -8575378209167070678L;

    protected VariableReference dest;

    /**
     * <p>Getter for the field <code>dest</code>.</p>
     *
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public VariableReference getDest() {
        return dest;
    }

    /**
     * <p>Setter for the field <code>dest</code>.</p>
     *
     * @param dest a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public void setDest(VariableReference dest) {
        this.dest = dest;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#getCode()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        if ((Boolean) value)
            return "assertSame(" + source.getName() + ", " + dest.getName() + ");";
        else
            return "assertNotSame(" + source.getName() + ", " + dest.getName() + ");";
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#copy(org.evosuite.testcase.TestCase, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Assertion copy(TestCase newTestCase, int offset) {
        SameAssertion s = new SameAssertion();
        s.source = source.copy(newTestCase, offset);
        s.dest = dest.copy(newTestCase, offset);
        s.value = value;
        s.comment = comment;
        s.killedMutants.addAll(killedMutants);
        return s;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#evaluate(org.evosuite.testcase.Scope)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(Scope scope) {
        try {
            if ((Boolean) value) {
                if (source.getObject(scope) == null)
                    return dest.getObject(scope) == null;
                else
                    return source.getObject(scope) == dest.getObject(scope);
            } else {
                if (source.getObject(scope) == null)
                    return dest.getObject(scope) != null;
                else
                    return source.getObject(scope) != dest.getObject(scope);
            }
        } catch (CodeUnderTestException e) {
            throw new UnsupportedOperationException();
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dest == null) ? 0 : dest.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SameAssertion other = (SameAssertion) obj;
        if (dest == null) {
            return other.dest == null;
        } else return dest.equals(other.dest);
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
        Set<VariableReference> vars = new HashSet<>();
        vars.add(source);
        vars.add(dest);
        return vars;
    }
}
