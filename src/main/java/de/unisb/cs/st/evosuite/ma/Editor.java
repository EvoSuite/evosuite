package de.unisb.cs.st.evosuite.ma;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.TestSuiteGenerator;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ma.gui.SourceCodeGUI;
import de.unisb.cs.st.evosuite.ma.gui.TestEditorGUI;
import de.unisb.cs.st.evosuite.ma.parser.TestParser;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteMinimizer;
import de.unisb.cs.st.evosuite.utils.HtmlAnalyzer;
import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * @author Yury Pavlov
 * 
 */
public class Editor {

	public final Object lock = new Object();

	private final SearchStatistics statistics = SearchStatistics.getInstance();

	private final Set<Integer> suiteCoveredLines = new HashSet<Integer>();

	private final List<TestCaseTuple> testCases = new ArrayList<TestCaseTuple>();

	private final TestSuiteChromosome testSuiteChr;

	private final GeneticAlgorithm gaInstance;

	private Iterable<String> sourceCode;

	public final TestEditorGUI sguiTE;

	public final SourceCodeGUI sguiSC;

	private TestParser testParser;

	private TestCaseTuple currTCTuple;

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
			suiteCoveredLines.addAll(testCaseCoverega);
		}

		nextTest();
		sguiSC = new SourceCodeGUI();
		sguiTE = new TestEditorGUI();
		sguiSC.createWindow(this);
		sguiTE.createMainWindow(this);
		testParser = new TestParser(this);
		
		// see message from html_analyzer.getClassContent(...) to check this
		if (sourceCode.toString().equals(
				"[No source found for " + Properties.TARGET_CLASS + "]")) {
			File srcFile = showChooseFileMenu(Properties.TARGET_CLASS)
					.getSelectedFile();
			sourceCode = Utils.readFile(srcFile);
		}


		synchronized (lock) {
			while (sguiTE.mainFrame.isVisible())
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
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
		TestCase currentTestCase = currTCTuple.getTestCase();
		try {
			TestCase newTestCase = testParser.parsTest(testCode);

			if (newTestCase != null) {
				// EvoSuite stuff
				testSuiteChr.setChanged(true);
				TestCaseExecutor executor = TestCaseExecutor.getInstance();
				executor.execute(newTestCase);

				// If we change already existed testCase, remove old version
				testSuiteChr.deleteTest(currentTestCase);
				testCases.remove(currTCTuple);
				testSuiteChr.addTest(newTestCase);

				// MA stuff
				Set<Integer> testCaseCoverega = retrieveCoverage(newTestCase);
				suiteCoveredLines.addAll(testCaseCoverega);
				TestCaseTuple newTestCaseTuple = new TestCaseTuple(newTestCase,
						testCaseCoverega);
				currTCTuple = newTestCaseTuple;
				testParser = new TestParser(this);
				testCases.add(newTestCaseTuple);
				updateCoverage();

				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			showParseException(e.getMessage());
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
	public int getNumOfCurrTest() {
		return testCases.indexOf(currTCTuple);
	}

	/**
	 * Return current testCase in editor (and GUI).
	 * 
	 * @return TestCaseTupel
	 */
	public TestCaseTuple getCurrTCTup() {
		return currTCTuple;
	}

	/**
	 * 
	 */
	public String getCurrTCCode() {
		return currTCTuple.getTestCase().toCode();
	}

	/**
	 * Set currentTestCase to the next testCase.
	 */
	public void nextTest() {
		if (currTCTuple == null && testCases.size() > 0) {
			currTCTuple = testCases.get(0);
		} else if (currTCTuple != null && testCases.size() > 0) {

			int j = 0;
			for (int i = 0; i < testCases.size(); i++) {
				if (currTCTuple == testCases.get(i)) {
					if (i == testCases.size() - 1) {
						j = 0;
					} else {
						j = i + 1;
					}
				}
			}
			currTCTuple = testCases.get(j);
		} else {
			createNewTestCase();
		}
	}

	/**
	 * Set currentTestCase to previous testCase.
	 */
	public void prevTest() {
		if (currTCTuple == null && testCases.size() > 0) {
			currTCTuple = testCases.get(0);
		} else if (currTCTuple != null && testCases.size() > 0) {

			int j = 0;
			for (int i = 0; i < testCases.size(); i++) {
				if (currTCTuple == testCases.get(i)) {
					if (i == 0) {
						j = testCases.size() - 1;
					} else {
						j = i - 1;
					}
				}
			}
			currTCTuple = testCases.get(j);
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
		currTCTuple = newTestCaseTuple;
	}

	/**
	 * Delete from testSuiteChromosome currentTestCase. Set current TestCase to
	 * the next.
	 */
	public void delCurrTC() {
		TestCase testCaseForDeleting = currTCTuple.getTestCase();
		testSuiteChr.deleteTest(testCaseForDeleting);
		testCases.remove(currTCTuple);
		updateCoverage();
		nextTest();
	}

	/**
	 * Rebuild set of covered lines.
	 */
	private void updateCoverage() {
		suiteCoveredLines.clear();
		for (TestCaseTuple tct : testCases) {
			suiteCoveredLines.addAll(tct.getCoverage());
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
	public Set<Integer> getCurrCoverage() {
		return currTCTuple.getCoverage();
	}

	/**
	 * Return covered lines of the whole TestSuite.
	 * 
	 * @return Set of Integers
	 */
	public Set<Integer> getSuiteCoveredLines() {
		return suiteCoveredLines;
	}

	/**
	 * Return testSuit's coverage value.
	 * 
	 * @return int
	 */
	public int getSuiteCoveratgeVal() {
		gaInstance.getFitnessFunction().getFitness(testSuiteChr);
		return (int) (testSuiteChr.getCoverage() * 100);
	}

	public void showParseException(String message) {
		JOptionPane.showMessageDialog(sguiTE.mainFrame, message,
				"Parsing error", JOptionPane.ERROR_MESSAGE);
	}

	public JFileChooser showChooseFileMenu(String className) {
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Where is class: " + className);
		int returnVal = fc.showOpenDialog(sguiTE.mainFrame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc;
		}

		return null;
	}
}
