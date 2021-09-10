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

import org.evosuite.Properties;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

public class PrimitiveTraceObserver extends AssertionTraceObserver<PrimitiveTraceEntry> {

    private static final Pattern addressPattern = Pattern.compile(".*[\\w+\\.]+@[abcdef\\d]+.*", Pattern.MULTILINE);

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void afterStatement(Statement statement, Scope scope,
                                            Throwable exception) {
        // By default, no assertions are created for statements that threw exceptions
        if (exception != null)
            return;

        // No assertions are created for mock statements
        if (statement instanceof FunctionalMockStatement)
            return;

        visitReturnValue(statement, scope);
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.AssertionTraceObserver#visit(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visit(Statement statement, Scope scope, VariableReference var) {
        if (statement.isAssignmentStatement())
            return;

        logger.debug("Checking primitive " + var);
        try {
            // Need only legal values
            if (var == null)
                return;

            // We don't need assertions on constant values
            if (statement instanceof PrimitiveStatement<?>)
                return;

            if (statement instanceof MethodStatement) {
                if (((MethodStatement) statement).getMethod().getName().equals("hashCode"))
                    return;
            }

            Object object = var.getObject(scope);

            // We don't need to compare to null
            if (object == null)
                return;

            // We can't check private member enums
            if (object.getClass().isEnum()
                    && !Modifier.isPublic(object.getClass().getModifiers()))
                return;

            if (object.getClass().isPrimitive() || object.getClass().isEnum()
                    || isWrapperType(object.getClass()) || object instanceof String) {
                if (object instanceof String) {
                    int length = ((String) object).length();
                    // Maximum length of strings we look at
                    if (length > Properties.MAX_STRING) {
                        return;
                    }
                    // String literals may not be longer than 32767
                    if (length >= 32767) {
                        return;
                    }
                    // Avoid asserting anything on values referring to mockito proxy objects
                    if (((String) object).toLowerCase().contains("EnhancerByMockito")) {
                        return;
                    }
                    // The word "hashCode" is also suspicious
                    if (((String) object).toLowerCase().contains("hashcode")) {
                        return;
                    }
                    // Check if there is a reference that would make the test fail
                    if (addressPattern.matcher((String) object).find()) {
                        return;
                    }

                }
                logger.debug("Observed value " + object + " for statement "
                        + statement.getCode());
                trace.addEntry(statement.getPosition(), var, new PrimitiveTraceEntry(var,
                        object));

            }
        } catch (CodeUnderTestException e) {
            logger.debug("", e);
        }
    }

    @Override
    public void testExecutionFinished(ExecutionResult r, Scope s) {
        // do nothing
    }
}