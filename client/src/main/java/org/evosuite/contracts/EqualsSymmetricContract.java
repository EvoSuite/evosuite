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
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * EqualsSymmetricContract class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class EqualsSymmetricContract extends Contract {

    /* (non-Javadoc)
     * @see org.evosuite.contracts.Contract#check(org.evosuite.testcase.Statement, org.evosuite.testcase.Scope, java.lang.Throwable)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public ContractViolation check(Statement statement, Scope scope,
                                   Throwable exception) {
        for (Pair<VariableReference> pair : getAllVariablePairs(scope)) {
            // Equals self is covered by EqualsContract
            if (pair.object1 == pair.object2)
                continue;

            Object object1 = scope.getObject(pair.object1);
            Object object2 = scope.getObject(pair.object2);
            if (object1 == null || object2 == null)
                continue;

            // We do not want to call equals if it is the default implementation
            Class<?>[] parameters = {Object.class};
            try {
                Method equalsMethod = object1.getClass().getMethod("equals", parameters);
                if (equalsMethod.getDeclaringClass().equals(Object.class))
                    continue;

            } catch (SecurityException e1) {
                continue;
            } catch (NoSuchMethodException e1) {
                continue;
            }
            ExecutionTracer.disable();
            if (object1.equals(object2)) {
                if (!object2.equals(object1)) {
                    ExecutionTracer.enable();
                    return new ContractViolation(this, statement, exception,
                            pair.object1, pair.object2);
                }
            } else {
                if (object2.equals(object1)) {
                    ExecutionTracer.enable();
                    return new ContractViolation(this, statement, exception,
                            pair.object1, pair.object2);
                }
            }
            ExecutionTracer.enable();
        }
        return null;
    }

    @Override
    public void addAssertionAndComments(Statement statement,
                                        List<VariableReference> variables, Throwable exception) {
        TestCase test = statement.getTestCase();

        VariableReference a = variables.get(0);
        VariableReference b = variables.get(1);

        try {
            Method equalsMethod = a.getGenericClass().getRawClass().getMethod("equals",
                    Object.class);

            GenericMethod method = new GenericMethod(equalsMethod, a.getGenericClass());

            // Create x = a.equals(b)
            Statement st1 = new MethodStatement(test, method, a,
                    Arrays.asList(b));
            VariableReference x = test.addStatement(st1, statement.getPosition() + 1);

            // Create y = b.equals(a);
            Statement st2 = new MethodStatement(test, method, b,
                    Arrays.asList(a));
            VariableReference y = test.addStatement(st2, statement.getPosition() + 2);

            Statement newStatement = test.getStatement(y.getStPosition());

            // Create assertEquals(x, y)
            EqualsAssertion assertion = new EqualsAssertion();
            assertion.setStatement(newStatement);
            assertion.setSource(x);
            assertion.setDest(y);
            assertion.setValue(true);
            newStatement.addAssertion(assertion);
            newStatement.addComment("Violates contract a.equals(b) <-> b.equals(a)");

        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Equals symmetric check";
    }
}
