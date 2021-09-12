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

import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.VariableReference;

import java.lang.reflect.Array;

public class ArrayLengthObserver extends AssertionTraceObserver<ArrayLengthTraceEntry> {
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
        visitDependencies(statement, scope);
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.AssertionTraceObserver#visit(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visit(Statement statement, Scope scope, VariableReference var) {
        logger.debug("Checking array " + var);
        try {
            // Need only legal values
            if (var == null)
                return;

            // We don't need assertions on constant values
            if (statement instanceof PrimitiveStatement<?>)
                return;

            // We don't need assertions on array assignments
            if (statement instanceof AssignmentStatement)
                return;

            // We don't need assertions on array declarations
            if (statement instanceof ArrayStatement)
                return;

            Object object = var.getObject(scope);

            // We don't need to compare to null
            if (object == null)
                return;

            // We are only interested in arrays
            if (!object.getClass().isArray())
                return;

            if (var.getComponentClass() == null)
                return;

            // TODO: Could also add array lengths of all public fields that are arrays?
            int arrlength = Array.getLength(object);
            logger.debug("Observed length {} for statement {}", arrlength, statement.getCode());
            trace.addEntry(statement.getPosition(), var, new ArrayLengthTraceEntry(var, arrlength));

        } catch (CodeUnderTestException e) {
            logger.debug("", e);
        }
    }

    @Override
    public void testExecutionFinished(ExecutionResult r, Scope s) {
        // do nothing
    }
}
