package de.unisb.cs.st.evosuite.ma;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.TestSuiteGenerator;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ma.gui.SimpleGUITestEditor;
import de.unisb.cs.st.evosuite.ma.parser.TestParser;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteMinimizer;
import de.unisb.cs.st.evosuite.utils.HtmlAnalyzer;

/**
 * @author Yury Pavlov
 * 
 */
public class Editor {

	private final SearchStatistics statistics = SearchStatistics.getInstance();

	private final Set<Integer> suiteCoverage = new HashSet<Integer>();

	private final List<TestCaseTuple> testCases = new ArrayList<TestCaseTuple>();

	private final TestSuiteChromosome testSuiteChr;

	private final GeneticAlgorithm gaInstance;

	private final Iterable<String> sourceCode;

	private final SimpleGUITestEditor sgui;

	private double suiteCoverageValProLine;

	private TestParser testParser;

	private TestCaseTuple currentTestCaseTuple;

	/**
	 * Create instance of manual editor.
	 * 
	 * @param sa
	 *            - SearchAlgorihm as parameter
	 */
	public Editor(GeneticAlgorithm ga) {
		gaInstance = ga;
		ga.pauseGlobalTimeStoppingCondition();
		testSuiteChr = (TestSuiteChromosome) gaInstance.getBestIndividual();

		TestSuiteMinimizer minimizer = new TestSuiteMinimizer(
				TestSuiteGenerator.getFitnessFactory());
		minimizer.minimize(testSuiteChr);

		List<TestCase> tests = testSuiteChr.getTests();
		HtmlAnalyzer html_analyzer = new HtmlAnalyzer();
		sourceCode = html_analyzer.getClassContent(Properties.TARGET_CLASS);

		Set<Integer> testCaseCoverega;
		for (TestCase testCase : tests) {
			testCaseCoverega = retrieveCoverage(testCase);
			testCases.add(new TestCaseTuple(testCase, testCaseCoverega));
			suiteCoverage.addAll(testCaseCoverega);
		}
		suiteCoverageValProLine = testSuiteChr.getCoverage()
				/ suiteCoverage.size();
		System.out.println("!@#!@#!ddfj " + suiteCoverageValProLine);

		// set currentTestCaseTuple to proper. value
		nextTest();

		sgui = new SimpleGUITestEditor();
		testParser = new TestParser(sgui);
		sgui.createMainWindow(this);

		// when work is done reset time
		ga.resumeGlobalTimeStoppingCondition();
	}

	/**
	 * Pars a testCase from Editor to EvoSuite's instructions and insert in
	 * EvoSuite's population. Create coverage for the new TestCase.
	 * 
	 * @param testSource
	 */
	public boolean saveTest(String testCode) {
		TestCase currentTestCase = currentTestCaseTuple.getTestCase();
		try {
			TestCase newTestCase = testParser.parsTest(testCode);

			if (newTestCase != null) {
				// EvoSuite stuff
				testSuiteChr.setChanged(true);
				TestCaseExecutor executor = TestCaseExecutor.getInstance();
				executor.execute(newTestCase);

				// If we change already existed testCase, remove old version
				testSuiteChr.deleteTest(currentTestCase);
				testCases.remove(currentTestCaseTuple);
				testSuiteChr.addTest(newTestCase);

				// MA stuff
				Set<Integer> testCaseCoverega = retrieveCoverage(newTestCase);
				suiteCoverage.addAll(testCaseCoverega);
				TestCaseTuple newTestCaseTuple = new TestCaseTuple(newTestCase,
						testCaseCoverega);
				currentTestCaseTuple = newTestCaseTuple;
				testParser = new TestParser(sgui);
				testCases.add(newTestCaseTuple);
				updateCoverage();

				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Return the number of TestCases in suite.
	 * 
	 * @return int
	 */
	public int getNumOfTestCases() {
		return testCases.size();
	}

	/**
	 * Return displacement currentTestCase in Suite.
	 * 
	 * @return int
	 */
	public int getNumOfCurrntTest() {
		return testCases.indexOf(currentTestCaseTuple);
	}

	/**
	 * Return current testCase in editor (and GUI).
	 * 
	 * @return TestCaseTupel
	 */
	public TestCaseTuple getCurrentTestCase() {
		return currentTestCaseTuple;
	}

	/**
	 * Set currentTestCase to the next testCase.
	 */
	public void nextTest() {
		if (currentTestCaseTuple == null && testCases.size() > 0) {
			currentTestCaseTuple = testCases.get(0);
		} else if (currentTestCaseTuple != null && testCases.size() > 0) {

			int j = 0;
			for (int i = 0; i < testCases.size(); i++) {
				if (currentTestCaseTuple == testCases.get(i)) {
					if (i == testCases.size() - 1) {
						j = 0;
					} else {
						j = i + 1;
					}
				}
			}
			currentTestCaseTuple = testCases.get(j);
		} else {
			createNewTestCase();
		}
	}

	/**
	 * Set currentTestCase to previous testCase.
	 */
	public void prevTest() {
		if (currentTestCaseTuple == null && testCases.size() > 0) {
			currentTestCaseTuple = testCases.get(0);
		} else if (currentTestCaseTuple != null && testCases.size() > 0) {

			int j = 0;
			for (int i = 0; i < testCases.size(); i++) {
				if (currentTestCaseTuple == testCases.get(i)) {
					if (i == 0) {
						j = testCases.size() - 1;
					} else {
						j = i - 1;
					}
				}
			}
			currentTestCaseTuple = testCases.get(j);
		} else {
			createNewTestCase();
		}
	}

	/**
	 * Return source code of class.
	 * 
	 * @return Iterable of String
	 */
	public Iterable<String> getSourceCode() {
		return sourceCode;
	}

	/**
	 * Create new TestCase that can be insert in population. Without coverage
	 * information. Set current TestCase to this.
	 * 
	 */
	public void createNewTestCase() {
		TestCaseTuple newTestCaseTuple = new TestCaseTuple(
				new DefaultTestCase(), new HashSet<Integer>());
		currentTestCaseTuple = newTestCaseTuple;
	}

	/**
	 * Delete from testSuiteChromosome currentTestCase. Set current TestCase to
	 * the next.
	 */
	public void deleteCurrentTestCase() {
		TestCase testCaseForDeleting = currentTestCaseTuple.getTestCase();
		testSuiteChr.deleteTest(testCaseForDeleting);
		testCases.remove(currentTestCaseTuple);
		updateCoverage();
		nextTest();
	}

	/**
	 * Rebuild set of covered lines.
	 */
	private void updateCoverage() {
		suiteCoverage.clear();
		for (TestCaseTuple tct : testCases) {
			suiteCoverage.addAll(tct.getCoverage());
		}

	}

	/**
	 * Retrieve the covered lines from EvoSuite (slow). Executed only 1 time at
	 * init.
	 * 
	 * @param testCase
	 * @return Set of Integers
	 */
	private Set<Integer> retrieveCoverage(TestCase testCase) {
		ExecutionTrace trace = statistics.executeTest(testCase,
				Properties.TARGET_CLASS);
		Set<Integer> result = statistics.getCoveredLines(trace,
				Properties.TARGET_CLASS);

		return result;
	}

	/**
	 * Return the coverage of the current TestCase.
	 * 
	 * @return Set of Integers
	 */
	public Set<Integer> getCurrentCoverage() {
		return currentTestCaseTuple.getCoverage();
	}

	/**
	 * Return covered lines of the whole TestSuite.
	 * 
	 * @return Set of Integers
	 */
	public Set<Integer> getSuiteCoverage() {
		return suiteCoverage;
	}

	/**
	 * Return testSuit's coverage value.
	 * 
	 * @return int
	 */
	public int getSuiteCoveratgeVal() {
		System.out.println("asdsdaasdqweqweqwe "
				+ (int) (suiteCoverageValProLine * suiteCoverage.size() * 100));
		System.out.println("number of lines " + suiteCoverage.size());
		return (int) (suiteCoverageValProLine * suiteCoverage.size() * 100);
	}
}
