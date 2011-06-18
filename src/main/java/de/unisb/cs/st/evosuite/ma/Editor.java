package de.unisb.cs.st.evosuite.ma;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Yury Pavlov
 * 
 */
public class Editor {
	private GeneticAlgorithm gaInstance;
	private List<TestCase> tests;
	private List<String> sourceCode;

	/**
	 * Create instance of Editor for manual edition of test individuals with:
	 * 
	 * @param sa
	 *            - SearchAlgorihm as parameter
	 */
	public Editor(GeneticAlgorithm ga) {
		gaInstance = ga;
		setUp();
	}

	private void setUp() {
		TestSuiteChromosome testSuiteChr = (TestSuiteChromosome) gaInstance
				.getBestIndividual();

		tests = testSuiteChr.getTests();
		for (String test : tests.toString()) {
			System.out.println(test + "\n\n");
		}
		sourceCode = getSourceCode();

		SimpleGUI sgui = new SimpleGUI();
		sgui.createWindow();
	}

	/**
	 * @return Source code of tested class
	 */
	private List<String> getSourceCode() {
		try {
			// FileReader sourceFile = new FileReader();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

}
