/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.output;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.MethodStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jose Miguel Rojas
 */
public class OutputObserver extends ExecutionObserver {

    private Map<MethodStatement, Object> returnValues = new HashMap<MethodStatement, Object>();

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
            Object returnObject = scope.getObject(varRef);
            if (exception == null && !methodStmt.getReturnType().equals(Void.TYPE)) {
                // we don't save anything if there was an exception
                // we are only interested in methods whose return type != void
                returnValues.put(methodStmt, returnObject);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#testExecutionFinished(org.evosuite.testcase.ExecutionResult)
     */
    @Override
    public void testExecutionFinished(ExecutionResult r, Scope s) {
        logger.info("Adding returnValues map to ExecutionResult");
        r.setReturnValues(returnValues);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#clear()
     */
    @Override
    public void clear() {
        returnValues = new HashMap<MethodStatement, Object>();
    }

    public Map<MethodStatement, Object> getreturnValues() {
        return returnValues;
    }
}
