/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.input;

import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jose Miguel Rojas
 */
public class InputObserver extends ExecutionObserver {

    private Map<MethodStatement, List<Object>> argumentsValues = new HashMap<MethodStatement, List<Object>>();

    private static final Logger logger = LoggerFactory.getLogger(InputObserver.class);

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
            List<VariableReference> parRefs = methodStmt.getParameterReferences();
            List<Object> argObjects = new ArrayList<>(parRefs.size());
            for (VariableReference parRef : parRefs) {
                Object parObject = null;
                if (parRef instanceof ArrayIndex) {
                    try {
                        parObject = ((ArrayIndex)parRef).getObject(scope);
                    } catch (CodeUnderTestException e) {
                        e.printStackTrace();
                    }
                } else if (parRef instanceof ConstantValue) {
                    parObject = ((ConstantValue) parRef).getValue();
                } else {
                    parObject = scope.getObject(parRef);
                }
                argObjects.add(parObject);
            }
            assert parRefs.size() == argObjects.size();
            argumentsValues.put(methodStmt, argObjects);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#testExecutionFinished(org.evosuite.testcase.ExecutionResult)
     */
    @Override
    public void testExecutionFinished(ExecutionResult r) {
        logger.info("Attaching argumentsValues map to ExecutionResult");
        r.setArgumentsValues(argumentsValues);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#clear()
     */
    @Override
    public void clear() {
        logger.info("Clearing InputObserver data");
        argumentsValues = new HashMap<MethodStatement, List<Object>>();
    }

}
