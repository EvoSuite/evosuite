/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.coverage.io.input;

import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Jose Miguel Rojas
 */
public class InputObserver extends ExecutionObserver {

    private Map<Integer, Set<InputCoverageGoal>> inputCoverage = new LinkedHashMap<>();

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
        if (statement instanceof EntityWithParametersStatement) {
            EntityWithParametersStatement parameterisedStatement = (EntityWithParametersStatement)statement;
            List<VariableReference> parRefs = parameterisedStatement.getParameterReferences();

            List<Object> argObjects = new ArrayList<>(parRefs.size());
            for (VariableReference parRef : parRefs) {
                Object parObject = null;
                try {
                    if (parRef instanceof ArrayIndex || parRef instanceof FieldReference) {
                        parObject = parRef.getObject(scope);
                    } else if (parRef instanceof ConstantValue) {
                        parObject = ((ConstantValue) parRef).getValue();
                    } else {
                        parObject = parRef.getObject(scope);
                    }
                } catch (CodeUnderTestException e) {
                    e.printStackTrace();
                }
                argObjects.add(parObject);
            }
            assert parRefs.size() == argObjects.size();
            String className  = parameterisedStatement.getDeclaringClassName();
            String methodDesc = parameterisedStatement.getDescriptor();
            String methodName = parameterisedStatement.getMethodName();

            inputCoverage.put(statement.getPosition(), InputCoverageGoal.createCoveredGoalsFromParameters(className, methodName, methodDesc, argObjects));
            // argumentsValues.put((EntityWithParametersStatement) statement, argObjects);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#testExecutionFinished(org.evosuite.testcase.ExecutionResult)
     */
    @Override
    public void testExecutionFinished(ExecutionResult r, Scope s) {
        logger.info("Attaching argumentsValues map to ExecutionResult");
        r.setInputGoals(inputCoverage);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.ExecutionObserver#clear()
     */
    @Override
    public void clear() {
        logger.info("Clearing InputObserver data");
        inputCoverage = new LinkedHashMap<>();
    }

}
