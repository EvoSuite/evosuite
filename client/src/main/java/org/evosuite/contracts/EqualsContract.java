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

package org.evosuite.contracts;

import org.evosuite.assertion.EqualsAssertion;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;


/**
 * An object always has to equal itself
 *
 * @author Gordon Fraser
 */
public class EqualsContract extends Contract {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(Contract.class);

    /* (non-Javadoc)
     * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.TestCase, org.evosuite.testcase.Statement, org.evosuite.testcase.Scope, java.lang.Throwable)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public ContractViolation check(Statement statement, Scope scope, Throwable exception) {
        for (VariableReference var : getAllVariables(scope)) {
            Object object = scope.getObject(var);

            if (object == null)
                continue;

            // We do not want to call equals if it is the default implementation
            Class<?>[] parameters = {Object.class};
            try {
                Method equalsMethod = object.getClass().getMethod("equals", parameters);
                if (equalsMethod.getDeclaringClass().equals(Object.class))
                    continue;

            } catch (SecurityException e1) {
                continue;
            } catch (NoSuchMethodException e1) {
                continue;
            }

            try {
                // An object always has to equal itself
                if (!object.equals(object))
                    return new ContractViolation(this, statement, exception, var);

            } catch (NullPointerException e) {
                // No nullpointer exceptions may be thrown if the parameter was not null
                // TODO: Use UndeclaredExceptionContract instead?
                // return new ContractViolation(this, statement, e, var);
                // Returning this contract violation is definitely wrong as it would look like equals returned false


            } catch (Throwable ignored) {
                // ignored
            }
        }

        return null;
    }

    @Override
    public void addAssertionAndComments(Statement statement,
                                        List<VariableReference> variables, Throwable exception) {
        EqualsAssertion assertion = new EqualsAssertion();
        assertion.setStatement(statement);
        assertion.setSource(variables.get(0));
        assertion.setDest(variables.get(0));
        assertion.setValue(true);
        statement.addAssertion(assertion);
        statement.addComment("Violates contract a.equals(a)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Equality check";
    }

}
