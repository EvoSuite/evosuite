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
package org.evosuite.ma;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ma.gui.SourceCodeGUI;
import org.evosuite.ma.gui.StnTestEditorGUI;
import org.evosuite.ma.gui.TestEditorGUI;
import org.evosuite.ma.gui.WideTestEditorGUI;
import org.evosuite.ma.parser.ParserConnector;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.SearchStatistics;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteMinimizer;
import org.evosuite.utils.HtmlAnalyzer;
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The <code>Editor</code> is the main class of the manual editor. It creates
 * {@link Transaction} instance, {@link TestEditorGUI}, {@link SourceCodeGUI}
 * and {@link ParserConnector}.
 * 
 * @author Yury Pavlov
 */
public class Editor implements UserFeedback {

	private static Logger logger = LoggerFactory.getLogger(Editor.class);

	// The dummy object for a synchronization of the manual editor with EvoSuite
	public final Object lock = new Object();

	// To retrieve coverage from EvoSuite
	private final SearchStatistics statistics = SearchStatistics.getInstance();

	// The current test suite of the manual editor
	private List<TCTuple> tcTuples = new ArrayList<TCTuple>();

	// The test suite of EvoSuite and co.
	private final TestSuiteChromosome testSuiteChr;

	// To get and insert a test suite from/into EvoSuite
	private final GeneticAlgorithm gaInstance;

	// A source code of the class under test
	private Iterable<String> sourceCode;

	public final TestEditorGUI sguiTE;

	public final SourceCodeGUI sguiSC = new SourceCodeGUI();

	// The current test case to deal
	private TCTuple currTCTuple;

	private final ParserConnector sep = new ParserConnector(this, true);

	private final Transactions transactions;

	// To check if a new coverage is smaller
	private int prevSuiteCoverage;

	// Uncovered branches
	private Set<TestFitnessFunction> uncGoals = new HashSet<TestFitnessFunction>();

	// To get a new coverage of the test suite after changes
	private final TestCaseExecutor executor = TestCaseExecutor.getInstance();

	/**
	 * Create an instance of the manual editor, fill all GUIs and set fields.
	 * Lock the EvoSuite's thread until the manual editor is not finished.
	 * 
	 * @param ga
	 *            - {@link GeneticAlgorithm}
	 */
	public Editor(GeneticAlgorithm ga) {
		gaInstance = ga;
		ga.pauseGlobalTimeStoppingCondition();
		testSuiteChr = (TestSuiteChromosome) gaInstance.getBestIndividual().clone();
		double originalFitness = testSuiteChr.getFitness();

		TestSuiteMinimizer minimizer = new TestSuiteMinimizer(
		        TestSuiteGenerator.getFitnessFactory());
		minimizer.minimize(testSuiteChr);

		List<TestCase> tests = testSuiteChr.getTests();
		HtmlAnalyzer html_analyzer = new HtmlAnalyzer();
		sourceCode = html_analyzer.getClassContent(Properties.TARGET_CLASS);

		Set<Integer> testCaseCoverega;
		for (TestCase testCase : tests) {
			testCaseCoverega = retrieveCoverage(testCase);
			tcTuples.add(new TCTuple(testCase, testCaseCoverega));
		}
		nextTCT();

		if (Properties.MA_WIDE_GUI) {
			sguiTE = new WideTestEditorGUI();
		} else {
			sguiTE = new StnTestEditorGUI();
		}
		sguiTE.createMainWindow(this);
		// See message from html_analyzer.getClassContent(...) to check this
		if (sourceCode.toString().equals("[No source found for "
		                                         + Properties.TARGET_CLASS + "]")) {
			File srcFile = chooseTargetFile(Properties.TARGET_CLASS);
			if (srcFile != null) {
				sourceCode = Utils.readFile(srcFile);
			}
		}
		sguiSC.createWindow(this);
		transactions = new Transactions(tcTuples);
		if (Properties.MA_BRANCHES_CALC) {
			retrieveAllUncovGoals();
		}
		printUncGoals();

		// Here is GUI active
		synchronized (lock) {
			while (sguiTE.getMainFrame().isVisible())
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		// Resuming part
		// Insert result into population
		ga.getFitnessFunction().getFitness(testSuiteChr);

		// Fitness might decrease, because we only keep what is _covered_ during
		// Minimization
		if (testSuiteChr.getFitness() > originalFitness) {
			logger.debug("Fitness has increased from " + originalFitness + " to "
			        + testSuiteChr.getFitness());
			double lastFitness = testSuiteChr.getFitness();

			TestSuiteChromosome original = (TestSuiteChromosome) ga.getBestIndividual();
			for (TestChromosome test : original.getTestChromosomes()) {
				testSuiteChr.addTest(test);
				ga.getFitnessFunction().getFitness(testSuiteChr);
				if (testSuiteChr.getFitness() < lastFitness) {
					lastFitness = testSuiteChr.getFitness();
				} else {
					testSuiteChr.deleteTest(test.getTestCase());
				}
			}
		}
		ga.getPopulation().set(0, testSuiteChr);
		logger.info("Resulting individual: " + ga.getBestIndividual().toString());

		// When work is done reset time
		ga.resumeGlobalTimeStoppingCondition();
	}

	/**
	 * Retrieve all branches, which are covered by this TestSuite. It's very
	 * expensive operation.
	 */
	public void retrieveAllUncovGoals() {
		// Use it only after minimize
		if (Properties.MA_BRANCHES_CALC) {
			List<TestFitnessFunction> goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals();
			Set<TestFitnessFunction> res = new HashSet<TestFitnessFunction>(
			        TestSuiteGenerator.getFitnessFactory().getCoverageGoals());
			List<TestCase> testSuite = getTests();
			for (TestFitnessFunction goal : goals) {
				for (TestCase testCase : testSuite) {
					if (goal.isCovered(testCase)) {
						res.remove(goal);
						break;
					}
				}
			}
			uncGoals = res;
		}
	}

	/**
	 * @return uncGoals Set<{@link TestFitnessFunction}>
	 */
	public Set<TestFitnessFunction> getUncGoals() {
		return uncGoals;
	}

	/**
	 * Parse a testCase from Editor to EvoSuite's instructions and insert in
	 * EvoSuite's population. Create coverage for the new TestCase.
	 * 
	 * @param testSource
	 * @throws IOException
	 */
	public boolean parseTest(String testCode) {
		try {
			TestCase newTestCase;
			newTestCase = sep.parseTest(testCode);
			if (newTestCase != null) {
				testSuiteChr.deleteTest(currTCTuple.getTestCase());
				tcTuples.remove(currTCTuple);
				addTestCase(testCode, newTestCase);
				writeRecord();
				printUncGoals();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			showParseException(e.getMessage());
		}
		return false;
	}

	private void printUncGoals() {
		if (Properties.MA_BRANCHES_CALC) {
			System.out.println("=========== BREANCHES ===========");
			for (TestFitnessFunction tff : uncGoals) {
				System.out.println(tff);
			}
			System.out.println("=========== " + uncGoals.size() + " ===========");
		}
	}

	/**
	 * @param testCode
	 * @param currentTestCase
	 * @param newTestCase
	 */
	private void addTestCase(String testCode, TestCase newTestCase) {
		// EvoSuite stuff
		executor.execute(newTestCase);
		testSuiteChr.addTest(newTestCase);

		// MA stuff
		Set<Integer> testCaseCoverega = retrieveCoverage(newTestCase);
		TCTuple newTestCaseTuple = new TCTuple(newTestCase, testCaseCoverega, testCode);
		currTCTuple = newTestCaseTuple;
		tcTuples.add(newTestCaseTuple);

		// Branches calc.
		retrieveAllUncovGoals();
	}

	/**
	 * Return the number of TestCases in suite.
	 * 
	 * @return int
	 */
	public int getNumOfTestCases() {
		return tcTuples.size();
	}

	/**
	 * Return displacement currentTestCase in Suite.
	 * 
	 * @return int
	 */
	public int getNumOfCurrTest() {
		return tcTuples.indexOf(currTCTuple);
	}

	/**
	 * Return current testCase in editor (and GUI).
	 * 
	 * @return TestCaseTupel
	 */
	public TCTuple getCurrTCTup() {
		return currTCTuple;
	}

	/**
	 * Return the source code of test case in form EvoSuite
	 */
	public String getCurrESTCCode() {
		return currTCTuple.getTestCase().toCode();
	}

	/**
	 * Returns a source code of the current test case.
	 */
	public String getCurrOrigTCCode() {
		return currTCTuple.getOrigSourceCode();
	}

	/**
	 * Set {@code currentTestCaseTuple} to the next.
	 */
	public void nextTCT() {
		if (currTCTuple == null && tcTuples.size() > 0) {
			currTCTuple = tcTuples.get(0);
		} else if (currTCTuple != null && tcTuples.size() > 0) {

			int j = 0;
			for (int i = 0; i < tcTuples.size(); i++) {
				if (currTCTuple == tcTuples.get(i)) {
					if (i == tcTuples.size() - 1) {
						j = 0;
					} else {
						j = i + 1;
					}
				}
			}
			currTCTuple = tcTuples.get(j);
		} else {
			createNewTCT();
		}
	}

	/**
	 * Set {@code currentTestCaseTuple} to previous.
	 */
	public void prevTCT() {
		if (currTCTuple == null && tcTuples.size() > 0) {
			currTCTuple = tcTuples.get(0);
		} else if (currTCTuple != null && tcTuples.size() > 0) {

			int j = 0;
			for (int i = 0; i < tcTuples.size(); i++) {
				if (currTCTuple == tcTuples.get(i)) {
					if (i == 0) {
						j = tcTuples.size() - 1;
					} else {
						j = i - 1;
					}
				}
			}
			currTCTuple = tcTuples.get(j);
		} else {
			createNewTCT();
		}
	}

	/**
	 * Returns the source code of the class.
	 * 
	 * @return <code>Iterable<{@code String}></code>
	 */
	public Iterable<String> getSourceCode() {
		return sourceCode;
	}

	/**
	 * Creates new TestCase which can be inserted in population.
	 * 
	 */
	public void createNewTCT() {
		TCTuple newTestCaseTuple = new TCTuple(new DefaultTestCase(),
		        new HashSet<Integer>());
		currTCTuple = newTestCaseTuple;
	}

	/**
	 * Deletes the {@code currentTestCase} from the {@code testSuiteChromosome}.
	 */
	public void delCurrTCT() {
		TestCase testCaseForDeleting = currTCTuple.getTestCase();
		testSuiteChr.deleteTest(testCaseForDeleting);
		tcTuples.remove(currTCTuple);
		nextTCT();
		writeRecord();
		if (Properties.MA_BRANCHES_CALC) {
			retrieveAllUncovGoals();
		}
	}

	/**
	 * Retrieves covered lines of the {@link TestCase} from EvoSuite
	 * (expensive).
	 * 
	 * @param testCase
	 *            {@link TestCase}
	 * @return <code>Set<{@code Integer}></code>
	 */
	private Set<Integer> retrieveCoverage(TestCase testCase) {
		ExecutionResult executionResult = statistics.executeTest(testCase,
		                                                         Properties.TARGET_CLASS);
		Set<Integer> result = statistics.getCoveredLines(executionResult.getTrace(),
		                                                 Properties.TARGET_CLASS);

		return result;
	}

	/**
	 * Gets covered lines of the current test case.
	 * 
	 * @return <code>Set<{@code Integer}></code>
	 */
	public Set<Integer> getCurrCoverage() {
		return currTCTuple.getCoverage();
	}

	/**
	 * Returns covered lines of the test suite.
	 * 
	 * @return <code>Set<{@code Integer}></code>
	 */
	public Set<Integer> getSuiteCoveredLines() {
		Set<Integer> res = new HashSet<Integer>();
		for (TCTuple tct : tcTuples) {
			res.addAll(tct.getCoverage());
		}
		return res;
	}

	/**
	 * Returns absolute suit's coverage value.
	 * 
	 * @return <code>int</code>
	 */
	public int getSuiteCoveratgeVal() {
		gaInstance.getFitnessFunction().getFitness(testSuiteChr);
		int newValue = (int) (testSuiteChr.getCoverage() * 100);
		if (newValue < prevSuiteCoverage) {
			prevSuiteCoverage = newValue;
			showWarning("New coverage is smaller!.");
		} else {
			prevSuiteCoverage = newValue;
		}
		return newValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ma.UserFeedback#showParseException(java.lang.
	 * String)
	 */
	@Override
	public void showParseException(String message) {
		JOptionPane.showMessageDialog(sguiTE.getMainFrame(), message, "Parsing error",
		                              JOptionPane.ERROR_MESSAGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ma.UserFeedback#chooseTargetFile(java.lang.String
	 * )
	 */
	@Override
	public File chooseTargetFile(String fileName) {
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Where is: " + fileName);
		int returnVal = fc.showOpenDialog(sguiTE.getMainFrame());

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}

		return null;
	}

	/**
	 * The dialog to enter a full path of a class.
	 * 
	 * @param className
	 *            {@code String}
	 * @return {@code String} a class path from a user
	 */
	public static String enterClassName(String className) {
		return JOptionPane.showInputDialog(null, "Where is class " + className + "?",
		                                   "Please enter full name",
		                                   JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * The Dialog to choose right class from many.
	 * 
	 * @param choices
	 *            {@code String[]} possible choice
	 * @param className
	 *            {@code String}
	 * @return {@code String} choice of a user
	 */
	public static String chooseClassName(String[] choices, String className) {
		return (String) JOptionPane.showInputDialog(null, "Choose now... " + className,
		                                            "The Choice of a Lifetime",
		                                            JOptionPane.QUESTION_MESSAGE, null,
		                                            choices, choices[0]);
	}

	/**
	 * To show some warning message.
	 * 
	 * @param message
	 *            {@code String}
	 */
	public static void showWarning(String message) {
		JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Write a new {@link Record} in the {@link Transactions}.
	 */
	private void writeRecord() {
		transactions.push(tcTuples);
	}

	/**
	 * Sets the manual editor back to the last record.
	 */
	public void unDo() {
		updateAfterTransaction(transactions.prev());
	}

	/**
	 * Reverts the last {@code unDo}.
	 */
	public void reDo() {
		updateAfterTransaction(transactions.next());
	}

	/**
	 * Back to the first {@link Record}.
	 */
	public void reset() {
		updateAfterTransaction(transactions.reset());
	}

	/**
	 * Updates the editor state after {@link Transactions} operation.
	 * 
	 * @param trns
	 *            <code>List<{@link TCTuple}></code> - to exchange current test
	 *            suite with a deep copy
	 */
	private void updateAfterTransaction(List<TCTuple> trns) {
		tcTuples = trns;
		nextTCT();
		testSuiteChr.restoreTests(getTests());
	}

	/**
	 * Returns a new instance <code>ArrayList<{@link TestCase}></code> of the
	 * test suite.
	 * 
	 * @return <code>ArrayList<{@link TestCase}></code>
	 */
	private ArrayList<TestCase> getTests() {
		ArrayList<TestCase> res = new ArrayList<TestCase>();
		for (TCTuple tcTupel : tcTuples) {
			res.add(tcTupel.getTestCase());
		}
		return res;
	}

	/**
	 * To load test cases from a file.
	 */
	public void readFromFile() {
		for (TestCase tc : sep.parseFile(chooseTargetFile("Choose file..."), "test\\d*")) {
			addTestCase(tc.toCode(), tc);
		}
		writeRecord();
		printUncGoals();
	}

}
