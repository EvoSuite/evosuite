/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.junit;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;

import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class JUnitTestSuite {

	private static Logger logger = Logger.getLogger(JUnitTestSuite.class);

	private Set<String> covered_methods;

	private Set<String> covered_branches_true;

	private Set<String> covered_branches_false;

	private final TestCaseExecutor executor = TestCaseExecutor.getInstance();

	public void runSuite(String name) {
		try {
			Class<?> forName = null;
			forName = Class.forName(name);
			logger.info("Running against JUnit test suite " + name);
			JUnitCore.runClasses(forName);
			ExecutionTrace trace = ExecutionTracer.getExecutionTracer().getTrace();

			covered_methods = new HashSet<String>();
			covered_branches_true = new HashSet<String>();
			covered_branches_false = new HashSet<String>();

			for (Entry<String, Integer> entry : trace.covered_methods.entrySet()) {
				if (!entry.getKey().contains("$"))
					covered_methods.add(entry.getKey());
			}

			for (Entry<String, Double> entry : trace.true_distances.entrySet()) {
				if (entry.getValue() == 0.0)
					if (!entry.getKey().contains("$"))
						covered_branches_true.add(entry.getKey());
			}

			for (Entry<String, Double> entry : trace.false_distances.entrySet()) {
				if (entry.getValue() == 0.0)
					if (!entry.getKey().contains("$"))
						covered_branches_false.add(entry.getKey());
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void runSuite(TestSuiteChromosome chromosome) {
		covered_methods = new HashSet<String>();
		covered_branches_true = new HashSet<String>();
		covered_branches_false = new HashSet<String>();

		for (TestCase test : chromosome.getTests()) {
			ExecutionResult result = runTest(test);
			for (Entry<String, Integer> entry : result.getTrace().covered_methods.entrySet()) {
				//if(!entry.getKey().contains("$"))
				covered_methods.add(entry.getKey());
			}

			for (Entry<String, Double> entry : result.getTrace().true_distances.entrySet()) {
				if (entry.getValue() == 0.0)
					//if(!entry.getKey().contains("$"))
					covered_branches_true.add(entry.getKey());
			}

			for (Entry<String, Double> entry : result.getTrace().false_distances.entrySet()) {
				if (entry.getValue() == 0.0)
					//if(!entry.getKey().contains("$"))
					covered_branches_false.add(entry.getKey());
			}
		}
	}

	public Set<String> getCoveredMethods() {
		return covered_methods;
	}

	public Set<String> getTrueCoveredBranches() {
		return covered_branches_true;
	}

	public Set<String> getFalseCoveredBranches() {
		return covered_branches_false;
	}

	public ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			result.exceptions = executor.run(test);
			executor.setLogging(true);
			result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			logger.fatal("TG: Exception caught: ", e);
			System.exit(1);
		}

		return result;
	}

}
