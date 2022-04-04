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

import org.evosuite.Properties;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.testcase.ConstantInliner;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * ContractViolation class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class ContractViolation {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(ContractViolation.class);

    private final Contract contract;

    private TestCase test;

    private Statement statement;

    /**
     * If the statement execution leads to a contract violation with an
     * undeclared exception this is stored here
     */
    private final Throwable exception;

    /**
     * List of all variables involved in the contract violation
     */
    private final List<VariableReference> variables = new ArrayList<>();

    private boolean isMinimized = false;

    /**
     * <p>
     * Constructor for ContractViolation.
     * </p>
     *
     * @param contract  a {@link org.evosuite.contracts.Contract} object.
     * @param statement a {@link org.evosuite.testcase.statements.Statement} object.
     * @param exception a {@link java.lang.Throwable} object.
     */
    public ContractViolation(Contract contract, Statement statement,
                             Throwable exception, VariableReference... variables) {
        this.contract = contract;
        this.test = statement.getTestCase().clone();
        this.test.chop(statement.getPosition() + 1);
        ((DefaultTestCase) this.test).setFailing(true);
        this.statement = this.test.getStatement(statement.getPosition());
        for (VariableReference var : variables) {
            this.variables.add(var.clone(this.test));
        }
        this.exception = exception;
    }

    protected VariableReference getVariable(int num) {
        return variables.get(num).clone(this.test);
    }

    /**
     * Getter for test case
     *
     * @return a {@link org.evosuite.testcase.TestCase} object.
     */
    public TestCase getTestCase() {
        return test;
    }

    /**
     * Getter for contract that was violated
     *
     * @return a {@link org.evosuite.contracts.Contract} object.
     */
    public Contract getContract() {
        return contract;
    }

    public int getPosition() {
        return statement.getPosition();
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isExceptionOfType(Class<?> throwableClass) {
        if (exception == null)
            return false;

        if (MockList.isAMockClass(exception.getClass().getName())) {
            return throwableClass.equals(exception.getClass().getSuperclass());
        } else {
            return throwableClass.equals(exception.getClass());
        }
    }

    public boolean resultsFromMethod(String methodName) {
        if (statement instanceof MethodStatement) {
            MethodStatement ms = (MethodStatement) statement;
            String target = ms.getMethodName() + ms.getDescriptor();
            return target.equals(methodName);
        } else if (statement instanceof ConstructorStatement) {
            return methodName.startsWith("<init>");
        } else {
            return false;
        }
    }

    public Statement getStatement() {
        return statement;
    }

    /**
     * Remove all statements that do not contribute to the contract violation
     */
    public void minimizeTest() {
        if (isMinimized)
            return;

        /** Factory method that handles statement deletion */
        TestFactory testFactory = TestFactory.getInstance();

        if (Properties.INLINE) {
            ConstantInliner inliner = new ConstantInliner();
            inliner.inline(test);
        }
        TestCase origTest = test.clone();

        List<Integer> positions = new ArrayList<>();

        for (VariableReference var : variables)
            positions.add(var.getStPosition());

        int oldLength = test.size();
        boolean changed = true;
        while (changed) {
            changed = false;

            for (int i = test.size() - 1; i >= 0; i--) {
                // TODO - why??
                if (i >= test.size())
                    continue;
                if (positions.contains(i))
                    continue;

                boolean deleted = testFactory.deleteStatement(test, i);
                if (!deleted) {
                    continue;
                }

                if (!contract.fails(test)) {
                    test = origTest.clone();
                } else {
                    changed = true;
                    for (int j = 0; j < positions.size(); j++) {
                        if (positions.get(j) > i) {
                            positions.set(j,
                                    positions.get(j)
                                            - (oldLength - test.size()));
                        }
                    }
                    origTest = test.clone();
                    oldLength = test.size();
                }
            }
        }

        statement = test.getStatement(test.size() - 1);
        for (int i = 0; i < variables.size(); i++) {
            variables.set(i, test.getStatement(positions.get(i)).getReturnValue());
        }

        contract.addAssertionAndComments(statement, variables, exception);
        isMinimized = true;
    }

    /**
     * Determine if we have already seen an instance of this violation
     *
     * @param other a {@link org.evosuite.contracts.ContractViolation} object.
     * @return a boolean.
     */
    public boolean same(ContractViolation other) {

        // Same contract?
        if (!contract.getClass().equals(other.contract.getClass()))
            return false;

        // Same type of statement?
        if (!statement.getClass().equals(other.statement.getClass()))
            return false;

        // Same exception type?
        if (exception != null && other.exception != null) {
            if (!exception.getClass().equals(other.exception.getClass()))
                return false;
        }

        // Same method call / constructor?
        if (statement instanceof MethodStatement) {
            MethodStatement ms1 = (MethodStatement) statement;
            MethodStatement ms2 = (MethodStatement) other.statement;
            return ms1.getMethod().getMethod().equals(ms2.getMethod().getMethod());
        } else if (statement instanceof ConstructorStatement) {
            ConstructorStatement ms1 = (ConstructorStatement) statement;
            ConstructorStatement ms2 = (ConstructorStatement) other.statement;
            return ms1.getConstructor().getConstructor().equals(ms2.getConstructor().getConstructor());
        } else if (statement instanceof AssignmentStatement) {
            VariableReference var1 = statement.getReturnValue();
            VariableReference var2 = other.statement.getReturnValue();
            if (var1 instanceof FieldReference && var2 instanceof FieldReference) {
                return ((FieldReference) var1).getField().getField().equals(((FieldReference) var2).getField().getField());
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Violated contract: " + contract + " in statement " + statement
                + " with exception " + exception;
    }

    public void changeClassLoader(ClassLoader classLoader) {
        ((DefaultTestCase) test).changeClassLoader(classLoader);
        contract.changeClassLoader(classLoader);
        this.statement = this.test.getStatement(statement.getPosition());
        for (int i = 0; i < variables.size(); i++) {
            variables.set(i, variables.get(i).clone(test));
        }
    }

}
