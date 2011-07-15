package de.unisb.cs.st.evosuite.ma;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ma.gui.SimpleGUI;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.javalanche.mutation.analyze.html.HtmlAnalyzer;

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

	/**
	 * Create instance of Editor for manual edition of test individuals with:
	 * 
	 * @param sa
	 *            - SearchAlgorihm as parameter
	 */
	public Editor(GeneticAlgorithm ga) {
		gaInstance = ga;
		setUp();
		currentTestCase = getNextTest();
	}

	/**
	 * Setup editor for work.
	 */
	private void setUp() {
		TestSuiteChromosome testSuiteChr = (TestSuiteChromosome) gaInstance
				.getBestIndividual();

		tests = testSuiteChr.getTests();

		HtmlAnalyzer html_analyzer = new HtmlAnalyzer();
		sourceCode = html_analyzer.getClassContent(Properties.TARGET_CLASS);

		statistics.minimized(testSuiteChr);

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
	 * Return next TestCase from current.
	 * 
	 * @return next TestCase from current
	 */
	public TestCase getNextTest() {
		if (currentTestCase == null && tests.size() > 0) {
			currentTestCase = tests.get(0);
			return tests.get(0);
		}

		for (int i = 0; i < tests.size(); i++) {
			if (currentTestCase == tests.get(i)) {
				if (i == tests.size() - 1) {
					currentTestCase = tests.get(0);
					return tests.get(0);
				} else {
					currentTestCase = tests.get(i + 1);
					return tests.get(i + 1);
				}
			}
		}

		return null;
	}

	/**
	 * Return previous TestCase from current.
	 * 
	 * @return previous TestCase from current
	 */
	public TestCase getPrevTest() {
		if (currentTestCase == null && tests.size() > 0) {
			currentTestCase = tests.get(0);
			return tests.get(0);
		}

		for (int i = 0; i < tests.size(); i++) {
			if (currentTestCase == tests.get(i)) {
				if (i == 0) {
					currentTestCase = tests.get(tests.size() - 1);
					return tests.get(tests.size() - 1);
				} else {
					currentTestCase = tests.get(i - 1);
					return tests.get(i - 1);
				}
			}
		}

		return null;
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
