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
package org.evosuite.coverage.io.output;

import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Jose Miguel Rojas
 */
public class OutputObserver extends ExecutionObserver {

    private Map<Integer, Set<OutputCoverageGoal>> outputCoverage = new LinkedHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(OutputObserver.class);

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
     */
    @Override
    public void output(int position, String output) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#beforeStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope)
     */
    @Override
    public void beforeStatement(Statement statement, Scope scope) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#afterStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
     */
    @Override
    public void afterStatement(Statement statement, Scope scope,
                               Throwable exception) {
        if (statement instanceof MethodStatement) {
            MethodStatement methodStmt = (MethodStatement) statement;
            VariableReference varRef = methodStmt.getReturnValue();

            try {
                Object returnObject = varRef.getObject(scope);
                if (exception == null && !methodStmt.getReturnType().equals(Void.TYPE)) {
                    // we don't save anything if there was an exception
                    // we are only interested in methods whose return type != void

                    String className = methodStmt.getDeclaringClassName();
                    String methodDesc = methodStmt.getDescriptor();
                    String methodName = methodStmt.getMethodName();

                    outputCoverage.put(statement.getPosition(), OutputCoverageGoal.createGoalsFromObject(className, methodName, methodDesc, returnObject));
                }
            } catch (CodeUnderTestException e) {
                // ignore?
            }
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#testExecutionFinished(org.evosuite.testcase.ExecutionResult)
     */
    @Override
    public void testExecutionFinished(ExecutionResult r, Scope s) {
        logger.debug("Adding returnValues map to ExecutionResult");
        r.setOutputGoals(outputCoverage);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#clear()
     */
    @Override
    public void clear() {
        outputCoverage = new LinkedHashMap<>();
    }

}
