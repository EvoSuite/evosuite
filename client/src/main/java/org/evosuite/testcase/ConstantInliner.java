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

package org.evosuite.testcase;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.Properties;
import org.evosuite.testcase.execution.*;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Inline all primitive values and null references in the test case
 *
 * @author Gordon Fraser
 */
public class ConstantInliner extends ExecutionObserver {

    private TestCase test = null;

    private static final Logger logger = LoggerFactory.getLogger(ConstantInliner.class);

    /**
     * <p>
     * inline
     * </p>
     *
     * @param test a {@link org.evosuite.testcase.TestCase} object.
     */
    public void inline(TestCase test) {
        this.test = test;
        TestCaseExecutor executor = TestCaseExecutor.getInstance();
        executor.addObserver(this);
        executor.execute(test);
        executor.removeObserver(this);
        removeUnusedVariables(test);
        assert (test.isValid());

    }

    /**
     * <p>
     * inline
     * </p>
     *
     * @param test a {@link org.evosuite.testcase.TestChromosome} object.
     */
    public void inline(TestChromosome test) {
        inline(test.test);
    }

    /**
     * <p>
     * inline
     * </p>
     *
     * @param suite a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
     */
    public void inline(TestSuiteChromosome suite) {
        for (TestChromosome test : suite.getTestChromosomes()) {
            final int old_test_size = test.size();
            inline(test);
            final int new_test_size = test.size();
            final int removed_statements = old_test_size - new_test_size;
            if (removed_statements > 0) {
                ExecutionResult lastExecResult = test.getLastExecutionResult();
                if (lastExecResult != null) {
                    final int old_exec_statements = lastExecResult.getExecutedStatements();
                    final int new_exec_statements = old_exec_statements - removed_statements;
                    lastExecResult.setExecutedStatements(new_exec_statements);
                }
            }
        }
    }

    /**
     * Remove all unreferenced variables
     *
     * @param t The test case
     * @return True if something was deleted
     */
    public boolean removeUnusedVariables(TestCase t) {
        List<Integer> toDelete = new ArrayList<>();
        boolean hasDeleted = false;

        int num = 0;
        for (Statement s : t) {
            if (s instanceof PrimitiveStatement) {

                VariableReference var = s.getReturnValue();
                if (!t.hasReferences(var)) {
                    toDelete.add(num);
                    hasDeleted = true;
                }
            }
            num++;
        }
        toDelete.sort(Collections.reverseOrder());
        for (Integer position : toDelete) {
            t.remove(position);
        }

        return hasDeleted;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.ExecutionObserver#output(int,
     * java.lang.String)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void output(int position, String output) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.testcase.ExecutionObserver#statement(org.evosuite.testcase.
     * StatementInterface, org.evosuite.testcase.Scope, java.lang.Throwable)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterStatement(Statement statement, Scope scope, Throwable exception) {
        try {
            for (VariableReference var : statement.getVariableReferences()) {
                if (var.equals(statement.getReturnValue())
                        || var.equals(statement.getReturnValue().getAdditionalVariableReference()))
                    continue;
                Object object = var.getObject(scope);

                if (var.isPrimitive()) {
                    ConstantValue value = new ConstantValue(test, var.getGenericClass());
                    value.setValue(object);
                    // logger.info("Statement before inlining: " +
                    // statement.getCode());
                    statement.replace(var, value);
                    // logger.info("Statement after inlining: " +
                    // statement.getCode());
                } else if (var.isString() && object != null) {
                    ConstantValue value = new ConstantValue(test, var.getGenericClass());
                    try {
                        String val = StringEscapeUtils.unescapeJava(object.toString());
                        if (val.length() < Properties.MAX_STRING) {
                            value.setValue(val);
                            statement.replace(var, value);
                        }
                    } catch (IllegalArgumentException e) {
                        // Exceptions may happen if strings are not valid
                        // unicode
                        logger.info("Cannot escape invalid string: " + object);
                    }
                    // logger.info("Statement after inlining: " +
                    // statement.getCode());
                } else if (var.isArrayIndex()) {
                    // If this is an array index and there is an object outside
                    // the array
                    // then replace the array index with that object
                    for (VariableReference otherVar : scope.getElements(var.getType())) {
                        Object otherObject = otherVar.getObject(scope);
                        if (otherObject == object && !otherVar.isArrayIndex()
                                && otherVar.getStPosition() < statement.getPosition()) {
                            statement.replace(var, otherVar);
                            break;
                        }
                    }
                } else {
                    // TODO: Ignoring exceptions during getObject, but keeping
                    // the assertion for now
                    if (object == null) {
                        if (statement instanceof MethodStatement) {
                            MethodStatement ms = (MethodStatement) statement;
                            if (var.equals(ms.getCallee())) {
                                // Don't put null in callee's, the compiler will not accept it
                                continue;
                            }
                        } else if (statement instanceof FieldStatement) {
                            FieldStatement fs = (FieldStatement) statement;
                            if (var.equals(fs.getSource())) {
                                // Don't put null in source, the compiler will not accept it
                                continue;
                            }
                        }
                        ConstantValue value = new ConstantValue(test, var.getGenericClass());
                        value.setValue(null);
                        // logger.info("Statement before inlining: " +
                        // statement.getCode());
                        statement.replace(var, value);
                        // logger.info("Statement after inlining: " +
                        // statement.getCode());
                    }
                }
            }
        } catch (CodeUnderTestException e) {
            logger.warn("Not inlining test: " + e.getCause());
            // throw new AssertionError("This case isn't handled yet: " +
            // e.getCause()
            // + ", " + Arrays.asList(e.getStackTrace()));
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.testcase.ExecutionObserver#beforeStatement(org.evosuite.
     * testcase.StatementInterface, org.evosuite.testcase.Scope)
     */
    @Override
    public void beforeStatement(Statement statement, Scope scope) {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     *
     * @see org.evosuite.testcase.ExecutionObserver#clear()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        // TODO Auto-generated method stub
    }

    @Override
    public void testExecutionFinished(ExecutionResult r, Scope s) {
        // do nothing
    }
}
