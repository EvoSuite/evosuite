package de.unisb.cs.st.evosuite.ma;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.TestSuiteGenerator;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ma.gui.SimpleGUI;
import de.unisb.cs.st.evosuite.ma.parser.TestParser;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
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
	private GeneticAlgorithm gaInstance;
	private List<TestCase> tests;
	private TestCase currentTestCase;
	private Iterable<String> sourceCode;
	private final SearchStatistics statistics = SearchStatistics.getInstance();
	private Set<Integer> coverage = new HashSet<Integer>();
	private Set<Integer> currentCovarage = new HashSet<Integer>();
	private Class<?> clazz;
	private TestSuiteChromosome testSuiteChr;

	/**
	 * Create instance of Editor for manual edition of test individuals with:
	 * 
	 * @param sa
	 *            - SearchAlgorihm as parameter
	 */
	public Editor(GeneticAlgorithm ga) {
		gaInstance = ga;
		testSuiteChr = (TestSuiteChromosome) gaInstance.getBestIndividual();

		TestSuiteMinimizer minimizer = new TestSuiteMinimizer(TestSuiteGenerator.getFitnessFactory());
		minimizer.minimize(testSuiteChr);

		tests = testSuiteChr.getTests();
		nextTest();
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

		try {
			clazz = Class.forName(Properties.TARGET_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		for (TestCase test : tests) {
			ExecutionTrace trace = statistics.executeTest(test,
					Properties.TARGET_CLASS);

			coverage.addAll(statistics.getCoveredLines(trace,
					Properties.TARGET_CLASS));
		}

		SimpleGUI sgui = new SimpleGUI();
		sgui.createWindow(this);
	}

	/**
	 * @return the clazz
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * @param testSource
	 */
	public void parseTest(String testCode) {
		try {
			TestCase newTestCase = TestParser.parsTest(testCode,
					currentTestCase, clazz);
			testSuiteChr.setChanged(true);
			TestCaseExecutor executor = TestCaseExecutor.getInstance();
			ExecutionResult result = executor.execute(newTestCase);
			testSuiteChr.addTest(newTestCase);
			tests = testSuiteChr.getTests();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the coverage
	 */
	public Set<Integer> getCoverage() {
		return coverage;
	}

	/**
	 * @return the currentTestCase
	 */
	public TestCase getCurrentTestCase() {
		return currentTestCase;
	}

	/**
	 * @return the tests
	 */
	public List<TestCase> getTests() {
		return tests;
	}

	/**
	 * Set currentTestCase to the next TestCase.
	 * 
	 * @return next TestCase from current
	 */
	public void nextTest() {
		if (currentTestCase == null && tests.size() > 0) {
			currentTestCase = tests.get(0);
		} else if (currentTestCase != null && tests.size() > 0) {

			int j = 0;
			for (int i = 0; i < tests.size(); i++) {
				if (currentTestCase == tests.get(i)) {
					if (i == tests.size() - 1) {
						j = 0;
					} else {
						j = i + 1;
					}
				}
			}
			currentTestCase = tests.get(j);
		}
		updateCurrentCovarage();
	}

	/**
	 * @return the currentCovarage
	 */
	public Set<Integer> getCurrentCovarage() {
		return currentCovarage;
	}

	/**
	 * Set currentTestCase to previous TestCase.
	 * 
	 * @return previous TestCase from current
	 */
	public void prevTest() {
		if (currentTestCase == null && tests.size() > 0) {
			currentTestCase = tests.get(0);
		} else if (currentTestCase != null && tests.size() > 0) {

			int j = 0;
			for (int i = 0; i < tests.size(); i++) {
				if (currentTestCase == tests.get(i)) {
					if (i == 0) {
						j = tests.size() - 1;
					} else {
						j = i - 1;
					}
				}
			}
			currentTestCase = tests.get(j);
		}
		updateCurrentCovarage();
	}

	/**
	 * 
	 */
	private void updateCurrentCovarage() {
		currentCovarage.clear();

		ExecutionTrace trace = statistics.executeTest(currentTestCase,
				Properties.TARGET_CLASS);

		currentCovarage.addAll(statistics.getCoveredLines(trace,
				Properties.TARGET_CLASS));
	}

	/**
	 * Return source code of class in form of Iterable<String>.
	 * 
	 * @return Iterable<String> of SourceCode
	 */
	public Iterable<String> getSourceCode() {
		return sourceCode;
	}

	/**
	 * Create full new TestCase which can be inserted in population.
	 * 
	 * @return new TestCase
	 */
	public TestCase createTestCase() {
		TestCase res = new DefaultTestCase();
		return res;
	}

}
