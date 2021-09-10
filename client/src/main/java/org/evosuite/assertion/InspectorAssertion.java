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
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.NumberFormatter;

public class InspectorAssertion extends Assertion {

    private static final long serialVersionUID = -4080051661226820222L;

    // VariableReference value;
    protected Inspector inspector;

    public InspectorAssertion() {

    }

    public InspectorAssertion(Inspector inspector, Statement statement, VariableReference source, Object value) {
        this.inspector = inspector;
        this.source = source;
        this.statement = statement;
        this.value = value;
    }

    /**
     * <p>Getter for the field <code>inspector</code>.</p>
     *
     * @return a {@link org.evosuite.assertion.Inspector} object.
     */
    public Inspector getInspector() {
        return inspector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assertion copy(TestCase newTestCase, int offset) {
        InspectorAssertion s = new InspectorAssertion();
        s.source = newTestCase.getStatement(source.getStPosition() + offset).getReturnValue();
        s.inspector = inspector;
        s.value = value;
        s.comment = comment;
        s.killedMutants.addAll(killedMutants);
        return s;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        if (value == null) {
            return "assertNull(" + source.getName() + "." + inspector.getMethodCall()
                    + "());";
        } else if (value.getClass().equals(Long.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().equals(Float.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + inspector.getMethodCall() + "(), 0.01F);";
        } else if (value.getClass().equals(Double.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + inspector.getMethodCall() + "(), 0.01D);";
        } else if (value.getClass().equals(Character.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().equals(String.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + inspector.getMethodCall() + "());";
        } else if (value.getClass().isEnum()) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + inspector.getMethodCall() + "());";

        } else
            return "assertEquals(" + value + ", " + source.getName() + "."
                    + inspector.getMethodCall() + "());";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(Scope scope) {
        try {
            if (source.getObject(scope) == null)
                return true; // TODO - true or false?
            else {
                try {
                    Object val = inspector.getValue(source.getObject(scope));
                    if (val == null)
                        return value == null;
                    else
                        return val.equals(value);
                } catch (Exception e) {
                    logger.error("* Exception during call to inspector", e);
                    return true;
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
        result = prime * result + ((inspector == null) ? 0 : inspector.hashCode());
        return result;
    }

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
        InspectorAssertion other = (InspectorAssertion) obj;
        if (inspector == null) {
            return other.inspector == null;
        } else return inspector.equals(other.inspector);
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.Assertion#isValid()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return source != null;
    }

    @Override
    public void changeClassLoader(ClassLoader loader) {
        super.changeClassLoader(loader);
        inspector.changeClassLoader(loader);
    }
}
