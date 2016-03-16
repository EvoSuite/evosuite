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
/**
 * 
 */
package org.evosuite.contracts;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.experimental.theories.Theory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ContractChecker class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class ContractChecker extends ExecutionObserver {

	private static Logger logger = LoggerFactory.getLogger(ContractChecker.class);

	private final Set<Contract> contracts = new HashSet<Contract>();

	/*
	 * Maybe it was not a problem, but it all depends on when Properties.CHECK_CONTRACTS_END 
	 * is initialized. Maybe best to just call it directly
	 */
	//private static final boolean checkAtEnd = Properties.CHECK_CONTRACTS_END;

	private static Set<Contract> invalid = new HashSet<Contract>();

	//private static boolean valid = true;

	private static boolean active = true;

	/**
	 * <p>
	 * Constructor for ContractChecker.
	 * </p>
	 */
	public ContractChecker() {
		// Default from EvoSuite
		contracts.add(new UndeclaredExceptionContract());
		contracts.add(new JCrasherExceptionContract());

		// Defaults from Randoop paper
		contracts.add(new NullPointerExceptionContract());
		contracts.add(new AssertionErrorContract());
		contracts.add(new EqualsContract());
		contracts.add(new ToStringReturnsNormallyContract());
		contracts.add(new HashCodeReturnsNormallyContract());

		// Further Randoop contracts, not in paper
		contracts.add(new EqualsHashcodeContract());
		contracts.add(new EqualsNullContract());
		contracts.add(new EqualsSymmetricContract());

		loadJUnitTheories();
	}

	private void loadJUnitTheories() {
		if (Properties.JUNIT_THEORIES.isEmpty())
			return;

		for (String theoryName : Properties.JUNIT_THEORIES.split(":")) {
			try {
				Class<?> theory = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(theoryName);
				Constructor<?> constructor = theory.getConstructor();
				if (!Modifier.isPublic(constructor.getModifiers())) {
					logger.info("Theory class does not have public default constructor");
					continue;
				}
				for (Method method : theory.getDeclaredMethods()) {
					if (method.isAnnotationPresent(Theory.class)) {
						logger.info("Found theory method: " + method.getName());
						if (method.getParameterTypes().length != 1) {
							logger.info("Wrong number of arguments!");
							continue;
						}
						try {
							GenericMethod gm = new GenericMethod(method, theory);
							JUnitTheoryContract contract = new JUnitTheoryContract(gm);
							contracts.add(contract);
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			} catch (ClassNotFoundException e) {
				logger.warn("Could not load theory " + theoryName + ": " + e);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	/**
	 * <p>
	 * Setter for the field <code>active</code>.
	 * </p>
	 * 
	 * @param isActive
	 *            a boolean.
	 */
	public static void setActive(boolean isActive) {
		active = isActive;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#output(int, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	/**
	 * Set the current test case, on which we check oracles while it is executed
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 */
	public static void currentTest(TestCase test) {
		currentTest = test;
		//ContractChecker.valid = true;
		ContractChecker.invalid.clear();
		// TODO: Keep track of objects that raised an exception, and exclude them from contract checking
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#statement(int, org.evosuite.testcase.Scope, org.evosuite.testcase.VariableReference)
	 */
	/** {@inheritDoc} */
	@Override
	public void afterStatement(Statement statement, Scope scope,
	        Throwable exception) {

		//if (!ContractChecker.valid) {
		/*
		 * once we get a contract that is violated, no point in checking the following statements,
		 * because the internal state of the SUT is corrupted.
		 * 
		 * TODO: at this point, for the fitness function we still consider the coverage given by the 
		 * following statements. Maybe that should be changed? At the moment, we only stop if exceptions 
		 */
		//	logger.debug("Not checking contracts for invalid test");
		//	return;
		//}

		if (!ContractChecker.active) {
			return;
		}

		if (Properties.CHECK_CONTRACTS_END
		        && statement.getPosition() < (currentTest.size() - 1))
			return;

		for (Contract contract : contracts) {
			if (invalid.contains(contract))
				continue;

			try {
				logger.debug("Checking contract {}", contract);
				ContractViolation violation = contract.check(statement, scope, exception);
				if (violation != null) {
					logger.debug("Contract failed: {} {}", contract, statement.getCode());

					FailingTestSet.addFailingTest(violation);
					/*
					FailingTestSet.addFailingTest(currentTest, contract, statement,
					                              exception);
					                              */
					//ContractChecker.valid = false;
					invalid.add(contract);
					//break;
				}
			} catch (Throwable t) {
				logger.debug("Caught exception during contract checking: " + t);
				for (StackTraceElement e : t.getStackTrace())
					logger.info(e.toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#beforeStatement(org.evosuite.testcase.StatementInterface, org.evosuite.testcase.Scope)
	 */
	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// Do nothing
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionObserver#clear()
	 */
	/** {@inheritDoc} */
	@Override
	public void clear() {
		ContractChecker.invalid.clear();
		// ContractChecker.valid = true;
	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		// do nothing
	}
}
