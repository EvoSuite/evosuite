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
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveExpression;
import org.evosuite.testcase.statements.PrimitiveExpression.Operator;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * o1.equals(o2) => o1.hashCode() == o2.hashCode()
 *
 * @author Gordon Fraser
 */
public class EqualsHashcodeContract extends Contract {

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
            if (pair.object1 == pair.object2)
                continue;

            Object object1 = scope.getObject(pair.object1);
            Object object2 = scope.getObject(pair.object2);
            if (object1 == null || object2 == null)
                continue;

            // We do not want to call hashcode if it is the default implementation
            Class<?>[] parameters = {Object.class};
            try {
                Method equalsMethod = object1.getClass().getMethod("equals", parameters);
                Method hashCodeMethod = object1.getClass().getMethod("hashCode"
                );
                if (equalsMethod.getDeclaringClass().equals(Object.class)
                        || hashCodeMethod.getDeclaringClass().equals(Object.class))
                    continue;

            } catch (SecurityException e1) {
                continue;
            } catch (NoSuchMethodException e1) {
                continue;
            }

            if (object1.equals(object2)) {
                if (object1.hashCode() != object2.hashCode()) {
                    return new ContractViolation(this, statement, exception,
                            pair.object1, pair.object2);
                }
            } else {
                if (object1.hashCode() == object2.hashCode()) {
                    return new ContractViolation(this, statement, exception,
                            pair.object1, pair.object2);
                }
            }
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
            Method hashCodeMethod = a.getGenericClass().getRawClass().getMethod("hashCode"
            );

            GenericMethod genericEqualsMethod = new GenericMethod(equalsMethod,
                    a.getGenericClass());
            GenericMethod genericHashCodeMethod = new GenericMethod(hashCodeMethod,
                    a.getGenericClass());

            // Create x = a.equals(b)
            Statement st1 = new MethodStatement(test, genericEqualsMethod, a,
                    Arrays.asList(b));
            VariableReference x = test.addStatement(st1, statement.getPosition() + 1);

            // Create y = a.hashCode();
            Statement st2 = new MethodStatement(test, genericHashCodeMethod, a,
                    Arrays.asList(new VariableReference[]{}));
            VariableReference y = test.addStatement(st2, statement.getPosition() + 2);

            // Create z = b.hashCode();
            Statement st3 = new MethodStatement(test, genericHashCodeMethod, b,
                    Arrays.asList(new VariableReference[]{}));
            VariableReference z = test.addStatement(st3, statement.getPosition() + 3);

            // Create w = z == z
            VariableReference w = new VariableReferenceImpl(test, boolean.class);
            PrimitiveExpression exp = new PrimitiveExpression(test, w, y,
                    Operator.EQUALS, z);
            w = test.addStatement(exp, statement.getPosition() + 4);

            Statement newStatement = test.getStatement(w.getStPosition());

            // Create assertEquals(x, w)
            EqualsAssertion assertion = new EqualsAssertion();
            assertion.setStatement(newStatement);
            assertion.setSource(x);
            assertion.setDest(w);
            assertion.setValue(true);
            newStatement.addAssertion(assertion);
            newStatement.addComment("Violates contract equals - hashcode");

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
        return "Equals hashcode check";
    }

}
