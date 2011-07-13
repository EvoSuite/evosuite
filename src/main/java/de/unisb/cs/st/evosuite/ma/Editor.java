package de.unisb.cs.st.evosuite.ma;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
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
	private Iterable<String> sourceCode;
	private HtmlAnalyzer html_analyzer = new HtmlAnalyzer();

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

	
	/**
	 * Setup editor for work.
	 */
	private void setUp() {
		TestSuiteChromosome testSuiteChr = (TestSuiteChromosome) gaInstance
				.getBestIndividual();

		tests = testSuiteChr.getTests();
//		for (TestCase test : tests) {
//			System.out.println(test.toCode() + "\n\n");
//		}
		sourceCode = getSourceCode();

		SimpleGUI sgui = new SimpleGUI();
		sgui.createWindow();
	}

	/**
	 * @return Source code of tested class
	 */
	private Iterable<String> getSourceCode() {
		Iterable<String> res = html_analyzer.getClassContent(Properties.TARGET_CLASS);
		return res;
	}

}
