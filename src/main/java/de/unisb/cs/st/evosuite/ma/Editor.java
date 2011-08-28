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
	private final GeneticAlgorithm gaInstance;
	private TestCaseTuple currentTestCaseTuple;
	private final Iterable<String> sourceCode;
	private final SearchStatistics statistics = SearchStatistics.getInstance();
	private final Set<Integer> suiteCoverage = new HashSet<Integer>();
	private final TestSuiteChromosome testSuiteChr;
	private final List<TestCaseTuple> testCases = new ArrayList<TestCaseTuple>();
	private SimpleGUITestEditor sgui;

	/**
	 * Create instance of Editor for manual edition of tests.
	 * 
	 * @param sa
	 *            - SearchAlgorihm as parameter
	 */
	public Editor(GeneticAlgorithm ga) {
		gaInstance = ga;
		testSuiteChr = (TestSuiteChromosome) gaInstance.getBestIndividual();

		TestSuiteMinimizer minimizer = new TestSuiteMinimizer(
				TestSuiteGenerator.getFitnessFactory());
		minimizer.minimize(testSuiteChr);

		List<TestCase> tests = testSuiteChr.getTests();
		// for (int i = 0; i < tests.get(0).size(); i++) {
		// if (tests.get(0).getStatement(i) instanceof ArrayStatement) {
		// System.out.println(i + ": ArrayStatement");
		// }
		// if (tests.get(0).getStatement(i) instanceof AssignmentStatement) {
		// System.out.println(i + ": AssignmentStatement");
		// }
		// if (tests.get(0).getStatement(i) instanceof ConstructorStatement) {
		// System.out.println(i + ": ConstructorStatement");
		// }
		// if (tests.get(0).getStatement(i) instanceof NullStatement) {
		// System.out.println(i + ": NullStatement");
		// }
		// if (tests.get(0).getStatement(i) instanceof PrimitiveStatement) {
		// System.out.println(i + ": PrimitiveStatement");
		// }
		// if (tests.get(0).getStatement(i) instanceof MethodStatement) {
		// System.out.println(i + ": MethodStatement");
		// System.out.println("11: " +
		// tests.get(0).getStatement(i).getReturnValue());
		// }
		// }

		HtmlAnalyzer html_analyzer = new HtmlAnalyzer();
		sourceCode = html_analyzer.getClassContent(Properties.TARGET_CLASS);

		Set<Integer> testCaseCoverega;
		for (TestCase testCase : tests) {
			testCaseCoverega = retrieveCoverage(testCase);
			testCases.add(new TestCaseTuple(testCase, testCaseCoverega));
			suiteCoverage.addAll(testCaseCoverega);

		}

		// set currentTestCaseTuple to proper. value
		nextTest();

		sgui = new SimpleGUITestEditor();
		sgui.createMainWindow(this);

		// when work is done reset time
		ga.resetGlobalTimeStoppingCondition();
	}

	/**
	 * Retrieve the coverage information of the TestCase
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
	 * Pars a testCase from Editor to EvoSuite's instructions and insert in
	 * EvoSuite's population. Create coverage for the new TestCase.
	 * 
	 * @param testSource
	 */
	public boolean saveTest(String testCode) {
		TestCase currentTestCase = currentTestCaseTuple.getTestCase();
		try {
			TestCase newTestCase = TestParser.parsTest(testCode,
					currentTestCase, sgui);

			if (newTestCase != null) {
				// EvoSuite stuff
				testSuiteChr.setChanged(true);
				TestCaseExecutor executor = TestCaseExecutor.getInstance();
				executor.execute(newTestCase);

				// If we change already existed testCase, remove old version
				testSuiteChr.deleteTest(currentTestCase);
				testSuiteChr.addTest(newTestCase);

				// MA stuff
				Set<Integer> testCaseCoverega = retrieveCoverage(newTestCase);
				suiteCoverage.addAll(testCaseCoverega);
				TestCaseTuple newTestCaseTuple = new TestCaseTuple(newTestCase,
						testCaseCoverega);
				currentTestCaseTuple = newTestCaseTuple;
				testCases.add(newTestCaseTuple);
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
	 * Return the Coverage of whole suite.
	 * 
	 * @return Set of Integers
	 */
	public Set<Integer> getSuiteCoverage() {
		return suiteCoverage;
	}

	/**
	 * @return TestCaseTupel
	 */
	public TestCaseTuple getCurrentTestCase() {
		return currentTestCaseTuple;
	}

	/**
	 * Set currentTestCase to the next TestCase.
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
	 * Set currentTestCase to previous TestCase.
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
	 * The coverage of the current TestCase.
	 * 
	 * @return Set of Integers
	 */
	public Set<Integer> getCurrentCoverage() {
		return currentTestCaseTuple.getCoverage();
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
		nextTest();
	}

}
