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
package org.evosuite.testsuite;

import org.evosuite.Properties;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.utils.LoggingUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RegressionTestSuiteSerialization {

	private static String regressionFile = Properties.SEED_DIR + File.separator + Properties.TARGET_CLASS + ".regression";

	public RegressionTestSuiteSerialization() {
		// empty
	}

	public static void performRegressionAnalysis(TestSuiteChromosome testSuite) {

		// load previous regression test suite
		List<TestChromosome> previousSuite = TestSuiteSerialization.loadTests(regressionFile);
		LoggingUtils.getEvoLogger().info("* previousSuite.size(): " + previousSuite.size());

		// execute previous regression test chromosome

		// Store this value; if this option is true then the JUnit check
        // would not succeed, as the JUnit classloader wouldn't find the class
        boolean junitSeparateClassLoader = Properties.USE_SEPARATE_CLASSLOADER;
        Properties.USE_SEPARATE_CLASSLOADER = false;

		Iterator<TestChromosome> iter = previousSuite.iterator();
		while (iter.hasNext()) {
			TestCase tc = iter.next().getTestCase();

			List<TestCase> l = new ArrayList<TestCase>();
			l.add(tc);

			JUnitAnalyzer.removeTestsThatDoNotCompile(l);
			if (l.isEmpty()) {
				// if TesCase 'tc' does not compile, just remove it
				iter.remove();
				continue ;
			}
		}

		Properties.USE_SEPARATE_CLASSLOADER = junitSeparateClassLoader;

		// write some statistics, e.g., number of failing test cases
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.NumRegressionTestCases, previousSuite.size());

		// join previous regression test suite with new test suite
		testSuite.addTests(previousSuite);

		// serialize
		TestSuiteSerialization.saveTests(testSuite, new File(regressionFile));
	}
}
