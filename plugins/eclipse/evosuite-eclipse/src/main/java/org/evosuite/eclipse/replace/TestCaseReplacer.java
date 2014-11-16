/**
 * 
 */
package org.evosuite.eclipse.replace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Strategy;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
// import org.evosuite.junit.JUnitTestReader;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.RandomLengthTestFactory;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseMinimizer;
import org.evosuite.testcase.TestChromosome;

/**
 * This class is the main entry point for test case replacement
 * 
 */
public class TestCaseReplacer {

	/**
	 * Given a test, create a GA and look for a replacement test
	 * 
	 * @param test
	 */
	public TestCase replaceTest(String targetClass, List<TestCase> otherTests,
	        TestCase test) {
		// Various environmental setup necessary for EvoSuite
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.STRATEGY = Strategy.ONEBRANCH;
		ExecutionTracer.enableTraceCalls();

		// Run for 10 generations - adapt as necessary
		// Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
		// Properties.SEARCH_BUDGET = 20;
		// Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
		// Properties.SEARCH_BUDGET = 20;

		GeneticAlgorithm ga = TestSuiteGenerator.getGeneticAlgorithm(new RandomLengthTestFactory());

		// Set up fitness function for the parsed test case
		DifferenceFitnessFunction fitness = new DifferenceFitnessFunction(test,
		        otherTests, (TestFitnessFactory) TestSuiteGenerator.getFitnessFactory());
		//ga.setFitnessFunction(fitness);
		ga.addFitnessFunction(fitness);
		// Perform calculation
		ga.generateSolution();

		// The best individual at the end of the search contains our candidate solution
		TestChromosome testChromosome = (TestChromosome) ga.getBestIndividual();
		TestCaseMinimizer minimizer = new TestCaseMinimizer(fitness.getOriginalGoals());
		minimizer.minimize(testChromosome);

		System.out.println("Best individual has fitness: "
		        + fitness.getFitness(testChromosome));
		return testChromosome.getTestCase();
	}

	/**
	 * Try to parse test case from a file, then start replacement
	 * 
	 * @param fileName
	 *            Name of the file containing the unit tests
	 * 
	 * @param testName
	 *            Method name of the test=
	 */
	public TestCase replaceTest(String targetClass, String fileName, String testName,
	        String[] classPath) {
		Map<String, TestCase> tests = getTestCases(fileName, classPath);
		TestCase target = tests.get(testName);
		System.out.println("Found target test: " + target.toCode());
		tests.remove(testName); //remove "test5" method name
		return replaceTest(targetClass, new ArrayList<TestCase>(tests.values()), target);
	}

	/**
	 * Parse all the tests in the given file
	 * 
	 * @param fileName
	 * @return
	 */
	private Map<String, TestCase> getTestCases(String fileName, String[] classPath) {
		// JUnitTestReader parser = new JUnitTestReader(classPath, new String[0]);
		Map<String, TestCase> tests = new HashMap<String, TestCase>();
		// TODO:
		// tests.putAll(parser.readTests(fileName));
		if (tests.isEmpty()) {
			System.err.println("Found no parsable test cases in file " + fileName);
			System.exit(1);
		}
		return tests;
	}

	/**
	 * Main method when class is invoked. Parses a file, selects a test, and
	 * calculates a replacement. The replacement is simply output to the console
	 * 
	 * @param args
	 *            arguments: filename and number of test
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: TestCaseReplacer <targetClass> <filename> <methodname>");
			System.exit(1);
		}
		Properties.TARGET_CLASS = args[0];

		TestCaseReplacer replacer = new TestCaseReplacer();
		TestCase result = replacer.replaceTest(args[0],
		                                       args[1],
		                                       args[2],
		                                       System.getProperty("java.class.path").split(":"));

		System.out.println("Resulting test case:");
		System.out.println(result.toCode());
		System.exit(0);
	}

}
