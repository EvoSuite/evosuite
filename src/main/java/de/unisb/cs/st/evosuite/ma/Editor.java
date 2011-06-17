package de.unisb.cs.st.evosuite.ma;

import java.util.List;

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Yury Pavlov
 * 
 */
public class Editor {
	private GeneticAlgorithm gaInstance;
	private List<TestCase> tests;

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
//		Scanner scanner = new Scanner(System.in);
		TestSuiteChromosome testSuiteChr = (TestSuiteChromosome) gaInstance
				.getBestIndividual();
		
		tests = testSuiteChr.getTests();
		
		
		SimpleGUI sgui = new SimpleGUI();
		sgui.createWindow();
		

		
//		Debug.printDebugInformation("Enter in join");
//		try {
//			guiThread.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		Debug.printDebugInformation("Exit from join");

//		Debug.printDebugInformation("Print TSC:\n");
//		Debug.printDebugInformation(tsc.toString());
		
//		scanner.close();
	}

}
