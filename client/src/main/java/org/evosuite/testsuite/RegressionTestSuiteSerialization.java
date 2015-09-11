package org.evosuite.testsuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.utils.LoggingUtils;

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

		int number_of_regression_test_case_failing = 0;

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

			// compile and run the test case
			JUnitAnalyzer.handleTestsThatAreUnstable(l);
			if (l.isEmpty() || l.get(0).isUnstable() || l.get(0).isFailing()) {
				// TesCase 'tc' fails!
				number_of_regression_test_case_failing++;
			}
		}

		Properties.USE_SEPARATE_CLASSLOADER = junitSeparateClassLoader;

		// write some statistics, e.g., number of failing test cases
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.NumRegressionTestCases, previousSuite.size());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.NumRegressionTestCasesFailing, number_of_regression_test_case_failing);

		// join previous regression test suite with new test suite
		TestSuiteChromosome tsc = new TestSuiteChromosome(new RandomLengthTestFactory());
		tsc.clearTests();

		previousSuite.addAll(testSuite.getTestChromosomes());
		tsc.addTests(previousSuite);

		// serialize
		TestSuiteSerialization.saveTests(tsc, new File(regressionFile));
	}
}
