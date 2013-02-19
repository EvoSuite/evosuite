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
/**
 * 
 */
package org.evosuite.testsuite;

import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.AbstractStatement;
import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.Scope;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.VariableReference;
import org.evosuite.testcase.VariableReferenceImpl;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * <p>
 * TestCallStatement class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class TestCallStatement extends AbstractStatement {

	private static final long serialVersionUID = -7886618899521718039L;


	private final TestCallObject testCall;

	/**
	 * <p>
	 * Constructor for TestCallStatement.
	 * </p>
	 * 
	 * @param tc
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param call
	 *            a {@link org.evosuite.testsuite.TestCallObject} object.
	 * @param type
	 *            a {@link java.lang.reflect.Type} object.
	 */
	public TestCallStatement(TestCase tc, TestCallObject call, Type type) {
		super(tc, new VariableReferenceImpl(tc, type));
		this.testCall = call;
	}

	/**
	 * Execute a test case
	 * 
	 * @param test
	 *            The test case to execute
	 * @return Result of the execution
	 */
	public Object runTest(TestCase test) {

		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		try {
			Scope scope = new Scope();
			// logger.info("Starting test call " + test.toCode());
			// logger.info("Original test was: " + testCall.testCase.toCode());
			executor.execute(test, scope, Properties.TIMEOUT);

			// TODO: Count as 1 or length?
			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			// logger.info("Finished test call. Getting variables of type: "
			// + testCall.getReturnType());
			List<VariableReference> variables = scope.getElements(testCall.getReturnType());

			// logger.info("Got " + variables.size());
			if (!variables.isEmpty()) {
				Collections.sort(variables, Collections.reverseOrder());
				// logger.info("Return value is good: "
				// + scope.get(variables.get(0)).getClass());
				return variables.get(0).getObject(scope);
			}

		} catch (Exception e) {
			logger.error("TestCallStatement: Exception caught",e);
			throw new Error(e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.Statement#execute(org.evosuite
	 * .testcase.Scope, java.io.PrintStream)
	 */
	/** {@inheritDoc} */
	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {

		try {
			TestCase test = testCall.getTest();
			if (test != null && !test.hasCalls()) {
				Object value = runTest(test);
				assert (retval.getVariableClass().isAssignableFrom(value.getClass())) : "we want an "
				        + retval.getVariableClass() + " but got an " + value.getClass();
				retval.setObject(scope, value);
			} else {
				retval.setObject(scope, null);
			}
		} catch (CodeUnderTestException e) {
			return e.getCause();
		}

		return null; // TODO: Pass on any of the exceptions?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.Statement#getCode(java.lang.Throwable)
	 */
	// TODO!
	/*
	@Override
	public String getCode(Throwable exception) {

		TestCase test = testCall.getTest();
		if (test == null || test.hasCalls()) {
			return retval.getSimpleClassName() + " " + retval.getName()
			        + " = call to null";
		}
		int num = 0;
		for (TestCase other : testCall.getSuite().getTests()) {
			if (test.equals(other))
				return retval.getSimpleClassName() + " " + retval.getName()
				        + " = Call to test case: " + num + "...\n" + test.toCode()
				        + "...\n";
			num++;
		}

		return retval.getSimpleClassName() + " " + retval.getName()
		        + " = Call to test case (null) ";
	}
	*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.Statement#getBytecode(org.objectweb.
	 * asm.commons.GeneratorAdapter, java.util.Map, java.lang.Throwable)
	 */
	/** {@inheritDoc} */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        Throwable exception) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.Statement#getVariableReferences()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(retval);
		return vars;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#replace(org.evosuite.testcase.VariableReference, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void replace(VariableReference var1, VariableReference var2) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.testcase.Statement#clone()
	 */
	/** {@inheritDoc} */
	@Override
	public StatementInterface copy(TestCase newTestCase, int offset) {
		TestCallStatement statement = new TestCallStatement(newTestCase, testCall,
		        retval.getType());
		return statement;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testCall == null) ? 0 : testCall.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;
		TestCallStatement other = (TestCallStatement) s;
		if (testCall == null) {
			if (other.testCall != null)
				return false;
		} else if (!testCall.equals(other.testCall))
			return false;
		return true;
	}

	/**
	 * <p>
	 * getTest
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.TestCase} object.
	 */
	public TestCase getTest() {
		return testCall.getTest();
	}

	/**
	 * <p>
	 * getTestNum
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getTestNum() {
		return testCall.getNum();
	}

	/**
	 * <p>
	 * setTestNum
	 * </p>
	 * 
	 * @param num
	 *            a int.
	 */
	public void setTestNum(int num) {
		testCall.setNum(num);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	/** {@inheritDoc} */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return new ArrayList<VariableReference>(getVariableReferences());
	}

	/** {@inheritDoc} */
	@Override
	public boolean same(StatementInterface s) {
		return equals(s);
	}

	/** {@inheritDoc} */
	@Override
	public AccessibleObject getAccessibleObject() {
		assert (false); //not supposed to be called
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssignmentStatement() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.StatementInterface#changeClassLoader(java.lang.ClassLoader)
	 */
	/** {@inheritDoc} */
	@Override
	public void changeClassLoader(ClassLoader loader) {
		// No-op
	}
}
