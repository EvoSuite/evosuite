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
 * Assertion on comparison value of two objects
 *
 * @author Gordon Fraser
 */
public class CompareAssertion extends Assertion {

    private static final long serialVersionUID = 7415863202662602633L;

    protected VariableReference dest;

    /**
     * <p>
     * Getter for the field <code>dest</code>.
     * </p>
     *
     * @return a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public VariableReference getDest() {
        return dest;
    }

    /**
     * <p>
     * Setter for the field <code>dest</code>.
     * </p>
     *
     * @param dest a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public void setDest(VariableReference dest) {
        this.dest = dest;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Create a copy of the compare assertion
     */
    @Override
    public Assertion copy(TestCase newTestCase, int offset) {
        CompareAssertion s = new CompareAssertion();
        s.source = newTestCase.getStatement(source.getStPosition() + offset).getReturnValue();
        s.dest = newTestCase.getStatement(dest.getStPosition() + offset).getReturnValue();
        s.value = value;
        s.comment = comment;
        s.killedMutants.addAll(killedMutants);
        return s;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method returns the Java Code
     */
    @Override
    public String getCode() {
        if (source.getType().equals(Integer.class)) {
            if ((Integer) value == 0)
                return "assertTrue(" + source.getName() + " == " + dest.getName() + ");";
            else if ((Integer) value < 0)
                return "assertTrue(" + source.getName() + " < " + dest.getName() + ");";
            else
                return "assertTrue(" + source.getName() + " > " + dest.getName() + ");";

        } else {
            return "assertEquals(" + source.getName() + ".compareTo(" + dest.getName()
                    + "), " + value + ");";
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Determine if assertion holds in current scope
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean evaluate(Scope scope) {
        try {
            Comparable<Object> comparable = (Comparable<Object>) source.getObject(scope);
            if (comparable == null)
                if ((Integer) value == 0)
                    return dest.getObject(scope) == null;
                else
                    return true; // TODO - true or false?
            else {
                try {
                    return comparable.compareTo(dest.getObject(scope)) == (Integer) value;
                } catch (Exception e) {
                    return true; // TODO - true or false?
                }
            }
        } catch (CodeUnderTestException e) {
            throw new UnsupportedOperationException();
        }
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompareAssertion other = (CompareAssertion) obj;
        if (dest == null) {
            if (other.dest != null)
                return false;
        } else if (!dest.equals(other.dest))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (value == null) {
            return other.value == null;
        } else if ((Integer) value > 0) {
            return (Integer) other.value > 0;
        } else if ((Integer) value < 0) {
            return (Integer) other.value < 0;
        } else if ((Integer) value == 0) {
            return (Integer) other.value == 0;
        }
        return true;
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

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#isValid()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return super.isValid() && dest != null;
    }

}
