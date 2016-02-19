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
package org.evosuite.junit;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>JUnitTestSuite class.</p>
 *
 * @author Gordon Fraser
 */
public class JUnitTestSuite {

	private static Logger logger = LoggerFactory.getLogger(JUnitTestSuite.class);

	private Set<String> coveredMethods;

	private Set<Integer> coveredBranchesTrue;

	private Set<Integer> coveredBranchesFalse;

	private final TestCaseExecutor executor = TestCaseExecutor.getInstance();

	/**
	 * <p>runSuite</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void runSuite(String name) {
		try {
			Class<?> forName = null;
			forName = Class.forName(name);
			logger.info("Running against JUnit test suite " + name);
			JUnitCore.runClasses(forName);
			ExecutionTrace trace = ExecutionTracer.getExecutionTracer().getTrace();

			coveredMethods = new HashSet<String>();
			coveredBranchesTrue = trace.getCoveredTrueBranches();
			coveredBranchesFalse = trace.getCoveredFalseBranches();

			for (String methodName : trace.getCoveredMethods()) {
				if (!methodName.contains("$"))
					coveredMethods.add(methodName);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>runSuite</p>
	 *
	 * @param chromosome a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 */
	public void runSuite(TestSuiteChromosome chromosome) {
		coveredMethods = new HashSet<String>();
		coveredBranchesTrue = new HashSet<Integer>();
		coveredBranchesFalse = new HashSet<Integer>();

		for (TestCase test : chromosome.getTests()) {
			ExecutionResult result = runTest(test);
			coveredMethods.addAll(result.getTrace().getCoveredMethods());
			coveredBranchesTrue.addAll(result.getTrace().getCoveredTrueBranches());
			coveredBranchesFalse.addAll(result.getTrace().getCoveredFalseBranches());
		}
	}

	/**
	 * <p>Getter for the field <code>coveredMethods</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getCoveredMethods() {
		return coveredMethods;
	}

	/**
	 * <p>getTrueCoveredBranches</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Integer> getTrueCoveredBranches() {
		return coveredBranchesTrue;
	}

	/**
	 * <p>getFalseCoveredBranches</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<Integer> getFalseCoveredBranches() {
		return coveredBranchesFalse;
	}

	/**
	 * <p>runTest</p>
	 *
	 * @param test a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 */
	public ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			logger.debug("Executing test");
			result = executor.execute(test);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			//result.touched.addAll(HOMObserver.getTouched());

		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}

}
