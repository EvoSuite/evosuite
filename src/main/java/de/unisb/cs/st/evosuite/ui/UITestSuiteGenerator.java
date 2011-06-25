package de.unisb.cs.st.evosuite.ui;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;

import sun.awt.AWTAutoShutdown;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.TestSuiteGenerator;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testsuite.AbstractTestSuiteChromosome;
import de.unisb.cs.st.evosuite.ui.genetics.UITestChromosome;
import de.unisb.cs.st.evosuite.ui.genetics.UITestChromosomeFactory;
import de.unisb.cs.st.evosuite.ui.genetics.UITestSuiteChromosome;
import de.unisb.cs.st.evosuite.ui.genetics.UITestSuiteChromosomeFactory;
import de.unisb.cs.st.evosuite.ui.model.UIStateGraph;

public class UITestSuiteGenerator {
	public static void main(String[] args) {
		System.setProperty("uispec4j.test.library", "testng");
		AWTAutoShutdown.getInstance().notifyThreadBusy(Thread.currentThread()); // Really, really important, otherwise event loops get killed randomly!
		UISpec4J.init(); // Also important to do this very early (here), otherwise we might end up with multiple event loops from loading classes

		// 20 UITestSuites
		Properties.POPULATION = 20;
		// With (up to) 5 tests
		Properties.NUM_TESTS = 5;
		// With up to 10 actions
		Properties.CHROMOSOME_LENGTH = 10;
		
		// 3 generations
		Properties.GENERATIONS = 3;

		// No timeout
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.CPU_TIMEOUT = false;
		
		UITestSuiteGenerator generator = new UITestSuiteGenerator(new Trigger() {
			@Override
			public void run() throws Exception {
				//samples.calculator.CalculatorPanel.main(new String[] {});
				samples.addressbook.main.Main.main(new String[] {});
			}
		});
		
		generator.generateTestSuite();
		
		AWTAutoShutdown.getInstance().notifyThreadFree(Thread.currentThread());
	}

	private TestSuiteGenerator base;
	private Trigger mainMethodTrigger;
	private UIStateGraph stateGraph;
	
	public UITestSuiteGenerator(Trigger mainMethodTrigger) {
		this.base = new TestSuiteGenerator();
		this.mainMethodTrigger = mainMethodTrigger;
		this.stateGraph = new UIStateGraph();
	}

	@SuppressWarnings("unchecked")
	private void generateTestSuite() {
		ChromosomeFactory<UITestChromosome> testFactory = new UITestChromosomeFactory(stateGraph, this.mainMethodTrigger);
		ChromosomeFactory<UITestSuiteChromosome> testSuiteFactory = new UITestSuiteChromosomeFactory(testFactory);

		GeneticAlgorithm ga = this.base.getGeneticAlgorithm(testSuiteFactory);

		FitnessFunction fitnessFunction = new SizeRelativeTestSuiteFitnessFunction(base.getFitnessFunction());
		ga.setFitnessFunction(fitnessFunction);

		SelectionFunction selectionFunction = this.base.getSelectionFunction();
		selectionFunction.setMaximize(false);
		ga.setSelectionFunction(selectionFunction);
		
		ga.generateSolution();

		AbstractTestSuiteChromosome<ExecutableChromosome> best = (AbstractTestSuiteChromosome<ExecutableChromosome>) ga.getBestIndividual();
		System.out.println(best);
		
		System.out.println("* Resulting TestSuite's coverage: " + best.getCoverage());

		this.writeStateGraph();
	}
	
	private void writeStateGraph() {
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("state.dot")), "UTF-8"));
			pw.println(this.stateGraph.toGraphViz());
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
