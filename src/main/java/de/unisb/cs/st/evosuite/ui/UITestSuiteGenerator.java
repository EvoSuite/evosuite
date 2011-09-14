package de.unisb.cs.st.evosuite.ui;

import java.io.*;
import java.lang.reflect.Method;
import java.security.Permission;

import javax.swing.UIManager;

import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;

import sun.awt.AWTAutoShutdown;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.TestSuiteGenerator;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testsuite.AbstractTestSuiteChromosome;
import de.unisb.cs.st.evosuite.ui.genetics.*;
import de.unisb.cs.st.evosuite.ui.model.states.UIStateGraph;
import de.unisb.cs.st.evosuite.ui.run.RandomWalkUIController;
import de.unisb.cs.st.evosuite.ui.run.UIRunner;
import de.unisb.cs.st.evosuite.utils.SimpleCondition;

public class UITestSuiteGenerator {
	private static final int TIME_LIMIT_SECONDS = 15 * 60;

	public static void main(String[] args) {
		System.setProperty("uispec4j.test.library", "junit");

		try {
			UIManager.setLookAndFeel(
			        UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		// Really, really important, otherwise event loops get killed randomly!
		AWTAutoShutdown.getInstance().notifyThreadBusy(Thread.currentThread());
		// Also important to do this very early (here), otherwise we might end up
		// with multiple event loops from loading classes
		UISpec4J.init();		

        writeCoverage();
		
		// 4 UITestSuites
		Properties.POPULATION = 4;
		// With (up to) 5 tests
		Properties.NUM_TESTS = 5;
		// With up to 15 actions
		Properties.CHROMOSOME_LENGTH = 15;

		Properties.GENERATIONS = Integer.MAX_VALUE;

		// No timeout
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.CPU_TIMEOUT = false;

		// String replacement as of 13-Jul-2011 / revision 500:3d19d48a9098
		// is severely broken, randomly replacing if conditions with garbage
		// and potentially even causing bytecode verification errors. -- fgross
		Properties.STRING_REPLACEMENT = false;
		
		Properties.OUTPUT_DIR = "/Users/flgr/svn/evosuite/evosuite-files/";
		
		System.setSecurityManager(new SecurityManager() {
 			@Override
			public void checkExit(int status) {
				throw new SecurityException();
			}

			@Override
			public void checkPermission(Permission perm) {
				/* Allowed */
			}
		});
		
        Properties.SANDBOX = false;
        Properties.MOCKS = false;
                
        long startTime = System.currentTimeMillis();

		UITestSuiteGenerator generator = new UITestSuiteGenerator(new Trigger() {
			@Override
			public void run() throws Exception {
				ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

				try {
					Class<?> cls;

					//cls = Class.forName("samples.calculator.CalculatorPanel");
					//cls = Class.forName("samples.addressbook.main.Main");
					//cls = Class.forName("terpword.Ekit");
					//cls = Class.forName("org.tss.TerpSpreadSheet");
					//cls = Class.forName("TerpPresent");
					cls = Class.forName("org.gjt.sp.jedit.jEdit");
					
					// Class.forName("org.gjt.sp.jedit.Macros");

					// Thread.currentThread().setContextClassLoader(TestCluster.classLoader);
					
					//cls = TestCluster.classLoader.loadClass("terpword.Ekit");
					// cls = TestCluster.classLoader.loadClass("org.gjt.sp.jedit.jEdit");
					// cls = TestCluster.classLoader.loadClass("samples.addressbook.main.Main");

					cls.getMethod("main", new Class<?>[] { String[].class }).invoke(null,
							new Object[] { new String[] {} });
				} catch (Exception e) {
					System.out.println("Got exception on invoking main method:");
					e.printStackTrace();
				} finally {
					Thread.currentThread().setContextClassLoader(oldClassLoader);
				}
			}
		});

		generator.generateTestSuite();

        long endTime = System.currentTimeMillis();
        
        writeCoverage();

        System.out.println();
        System.out.println(String.format("Generating test suite took %.2f seconds", (endTime - startTime) / 1000.0f));
        System.out.println(String.format("Generated %d tests, of which %d are failing tests",
        		UITestChromosome.getExecutedChromosomes().size(), UITestChromosome.getFailingChromosomes().size()));
        
		AWTAutoShutdown.getInstance().notifyThreadFree(Thread.currentThread());
	}

	public static void writeCoverage() {
		try {
        	Class<?> emmaRT = Class.forName("com.vladium.emma.rt.RT");
        	Method m = emmaRT.getMethod("dumpCoverageData", new Class<?>[] { File.class, boolean.class, boolean.class }); 
        	m.invoke(null, new File("coverage.ec"), false, false);
        } catch (Throwable t) { t.printStackTrace(); }
	}

	private TestSuiteGenerator base;
	private Trigger mainMethodTrigger;
	private UIStateGraph stateGraph;

	public UITestSuiteGenerator(Trigger mainMethodTrigger) {
		this.base = new TestSuiteGenerator();
		this.mainMethodTrigger = mainMethodTrigger;
		this.stateGraph = new UIStateGraph();
		this.doInitializationRandomWalk();
	}

	private void doInitializationRandomWalk() {
		final SimpleCondition cond = new SimpleCondition();
		
		try {
			UIRunner.run(this.stateGraph, new RandomWalkUIController(50) {
				@Override
				public void finished(UIRunner uiRunner) {
					super.finished(uiRunner);
					cond.signal();
				}
			}, this.mainMethodTrigger);

			cond.awaitUninterruptibly();
		} catch (Exception e) {
			System.out.println("Got exception in initialization random walk:");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void generateTestSuite() {
/*		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						UITestSuiteGenerator.this.writeStateGraph();
					} catch (Exception e) {
						System.err.println("Error writing state graph!");
						e.printStackTrace();
					}
					
					try {
						Thread.sleep(5 * 60 * 1000);
					} catch (InterruptedException e) { }
				}
			}
		}, "State Graph Writer");
		
		t.setDaemon(true);
		t.start();*/
		
		try {
			ChromosomeFactory<UITestChromosome> testFactory = new UITestChromosomeFactory(stateGraph, this.mainMethodTrigger);
			ChromosomeFactory<UITestSuiteChromosome> testSuiteFactory = new UITestSuiteChromosomeFactory(testFactory);

			GeneticAlgorithm ga = this.base.getGeneticAlgorithm(testSuiteFactory);

			ga.setStoppingCondition(getStoppingCondition());
			
			FitnessFunction fitnessFunction = new SizeRelativeTestSuiteFitnessFunction(base.getFitnessFunction());
			ga.setFitnessFunction(fitnessFunction);

			SelectionFunction selectionFunction = TestSuiteGenerator.getSelectionFunction();
			selectionFunction.setMaximize(false);
			ga.setSelectionFunction(selectionFunction);

			ga.generateSolution();

			AbstractTestSuiteChromosome<ExecutableChromosome> best = (AbstractTestSuiteChromosome<ExecutableChromosome>) ga.getBestIndividual();
			System.out.println(best);

			System.out.println("* Resulting TestSuite's coverage: " + best.getCoverage());
		} finally {
			this.writeStateGraph();
		}
	}

	private static StoppingCondition getStoppingCondition() {
		StoppingCondition stoppingCondition = new MaxTimeStoppingCondition();
		stoppingCondition.setLimit(TIME_LIMIT_SECONDS);
		return stoppingCondition;
	}

	private void writeStateGraph() {
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(
					"state.dot")), "UTF-8"));
			pw.println(this.stateGraph.toGraphViz());
			pw.close();
			
			FileOutputStream s = new FileOutputStream("state.graphml");
			this.stateGraph.addToYWorksEnvironment().writeGraphML(s);
			s.close();
		} catch (Exception e) {
			System.out.println("Exception on writing state graph:");
			e.printStackTrace();
		}
	}
}
