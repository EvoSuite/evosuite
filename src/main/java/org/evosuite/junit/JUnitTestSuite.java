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
package org.evosuite.junit;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Gordon Fraser
 * 
 */
public class JUnitTestSuite {

	private static Logger logger = LoggerFactory.getLogger(JUnitTestSuite.class);

	private Set<String> coveredMethods;

	private Set<Integer> coveredBranchesTrue;

	private Set<Integer> coveredBranchesFalse;

	private final TestCaseExecutor executor = TestCaseExecutor.getInstance();

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

	public Set<String> getCoveredMethods() {
		return coveredMethods;
	}

	public Set<Integer> getTrueCoveredBranches() {
		return coveredBranchesTrue;
	}

	public Set<Integer> getFalseCoveredBranches() {
		return coveredBranchesFalse;
	}

	public ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			logger.debug("Executing test");
			result = executor.execute(test);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			//result.touched.addAll(HOMObserver.getTouched());

		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			logger.error("TG: Exception caught: ", e);
			System.exit(1);
		}

		return result;
	}

}
