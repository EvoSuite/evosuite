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
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testsuite.AbstractTestSuiteChromosome;
import de.unisb.cs.st.evosuite.ui.genetics.*;
import de.unisb.cs.st.evosuite.ui.model.states.UIStateGraph;
import de.unisb.cs.st.evosuite.ui.run.RandomWalkUIController;
import de.unisb.cs.st.evosuite.ui.run.UIRunner;
import de.unisb.cs.st.evosuite.utils.SimpleCondition;

public class UITestSuiteGenerator {
	private static final int TIME_LIMIT_SECONDS = Integer.valueOf(System.getProperty("timelimit", "" + Properties.GLOBAL_TIMEOUT));
	
	private static final class MainTrigger implements Trigger, Serializable {
		private static final long serialVersionUID = 1L;
		private String mainClass;

		public MainTrigger(String mainClass) {
			this.mainClass = mainClass;
		}

		@Override
		public void run() throws Exception {
			ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

			try {
				ClassLoader classLoader = TestCluster.classLoader;
				Thread.currentThread().setContextClassLoader(classLoader);

				Class<?> cls = classLoader.loadClass(this.mainClass);

				// cls = Class.forName("samples.calculator.CalculatorPanel");
				// cls = Class.forName("samples.addressbook.main.Main");
				// cls = Class.forName("terpword.Ekit");
				// cls = classLoader.loadClass("org.tss.TerpSpreadSheet");
				// cls = Class.forName("terppresent.TerpPresent");
				// cls = Class.forName("org.gjt.sp.jedit.jEdit");

				cls.getMethod("main", new Class<?>[] { String[].class }).invoke(null, new Object[] { new String[] {} });
			} catch (Exception e) {
				System.out.println("Got exception on invoking main method:");
				e.printStackTrace();
			} finally {
				Thread.currentThread().setContextClassLoader(oldClassLoader);
			}
		}
	}
	
	static {
		Thread thread = new Thread(new Runnable() {	
			private int coverageIdx = 1;

			@Override
			public void run() {
				long delay = Properties.UI_BACKGROUND_COVERAGE_DELAY;
				
				if (delay < 0)
					return;

				long currentTime = System.currentTimeMillis();
				long nextTime = currentTime + delay;
				
				while (true) {
					currentTime = System.currentTimeMillis();

					if (currentTime >= nextTime) {
						try {
							Class<?> emmaRT = Class.forName("com.vladium.emma.rt.RT");
							Method m = emmaRT.getMethod("dumpCoverageData", new Class<?>[] { File.class, boolean.class, boolean.class });
							String filename = String.format("coverage-%d.ec", coverageIdx);
							m.invoke(null, new File(filename), false, false);
							nextTime = System.currentTimeMillis() + delay;
							coverageIdx++;
						} catch (Throwable t) {
							t.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(nextTime - currentTime);
						} catch (InterruptedException e) {
							/* OK */
						}
					}
				}
			}
		}, "Coverage writer thread");

		thread.setDaemon(true);
		thread.start();
	}

	public static void main(String[] args) {
		try {
			System.setProperty("uispec4j.test.library", "junit");

			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Really, really important, otherwise event loops get killed
			// randomly!
			AWTAutoShutdown.getInstance().notifyThreadBusy(Thread.currentThread());
			// Also important to do this very early (here), otherwise we might
			// end up
			// with multiple event loops from loading classes
			UISpec4J.init();

			writeCoverage();

			Properties.MAX_SIZE = 1000;
			Properties.GENERATIONS = Integer.MAX_VALUE;

			// Timeout of 60 seconds per test.
			Properties.TIMEOUT = 60 * 1000;
			Properties.CPU_TIMEOUT = false;

			// String replacement as of 13-Jul-2011 / revision 500:3d19d48a9098
			// is severely broken, randomly replacing if conditions with garbage
			// and potentially even causing bytecode verification errors. --
			// fgross
			
			// String replacement still has (another) bug
			// with jEdit...
			Properties.STRING_REPLACEMENT = false;

			Properties.OUTPUT_DIR = "/home/flgr/workspace/evosuite/evosuite-files/";

			System.setSecurityManager(new SecurityManager() {
				@Override
				public void checkExit(int status) {
					StackTraceElement[] trace = new Exception().fillInStackTrace().getStackTrace();
					boolean afterExit = false;
					boolean isOK = false;

					for (StackTraceElement elem : trace) {
						String methodName = elem.getClassName() + "." + elem.getMethodName();
						String mainMethod = "de.unisb.cs.st.evosuite.ui.UITestSuiteGenerator.main";

						if ((methodName.equals(mainMethod) && afterExit) ||
							(methodName.equals("javax.swing.JFrame.setDefaultCloseOperation"))) {
							isOK = true;
							break;
						}

						afterExit = methodName.equals("java.lang.System.exit");
					}

					if (!isOK) {
						throw new SecurityException();
					}
				}

				@Override
				public void checkPermission(Permission perm) {
					/* Allowed */
				}
			});

			Properties.SANDBOX = false;
			Properties.MOCKS = false;

			long startTime = System.currentTimeMillis();

			UITestSuiteGenerator generator = new UITestSuiteGenerator(new MainTrigger(args[0]));

			AbstractTestSuiteChromosome<ExecutableChromosome> solution = generator.generateTestSuite();

			long endTime = System.currentTimeMillis();

			writeCoverage();

			System.out.println("Found a solution:");
			System.out.println(solution);
			System.out.println("* Resulting TestSuite's coverage: " + solution.getCoverage());

			serializeObjectToFile(solution, "solution.obj");
			
			writeAllExecutedTests();
			
			// Logger logger = Logger.getLogger(UITestSuiteGenerator.class);

			PrintWriter log;
			try {
				log = new PrintWriter(System.out); // new PrintWriter(new
													// FileOutputStream("log.txt"));
				log.println("");
				log.println(String.format("Generating test suite took %.2f seconds", (endTime - startTime) / 1000.0f));
				log.println(String.format("Generated %d tests, of which %d are failing tests", UITestChromosome.getExecutedChromosomes().size(), UITestChromosome
						.getFailingChromosomes().size()));

				log.flush();
				log.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			AWTAutoShutdown.getInstance().notifyThreadFree(Thread.currentThread());
			System.exit(0);
		}
	}
	
	public static void writeCoverage() {
		try {
			Class<?> emmaRT = Class.forName("com.vladium.emma.rt.RT");
			Method m = emmaRT.getMethod("dumpCoverageData", new Class<?>[] { File.class, boolean.class, boolean.class });
			m.invoke(null, new File("coverage.ec"), false, false);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private Trigger mainMethodTrigger;
	private UIStateGraph stateGraph;

	public UITestSuiteGenerator(Trigger mainMethodTrigger) {
		new TestSuiteGenerator();
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
	private AbstractTestSuiteChromosome<ExecutableChromosome> generateTestSuite() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(5 * 60 * 1000);
					} catch (InterruptedException e) {
					}

					try {
						UITestSuiteGenerator.this.writeStateGraph();
					} catch (Exception e) {
						System.err.println("Error writing state graph!");
						e.printStackTrace();
					}
				}
			}
		}, "State Graph Writer");

		t.setDaemon(true);
		t.start();

		this.writeStateGraph();

		try {
			ChromosomeFactory<UITestChromosome> testFactory = new UITestChromosomeFactory(stateGraph, this.mainMethodTrigger);
			ChromosomeFactory<UITestSuiteChromosome> testSuiteFactory = new UITestSuiteChromosomeFactory(testFactory);

			GeneticAlgorithm ga = TestSuiteGenerator.getGeneticAlgorithm(testSuiteFactory);
			TestSuiteGenerator.getSecondaryObjectives(ga);

			ga.setStoppingCondition(getStoppingCondition());

			FitnessFunction fitnessFunction = TestSuiteGenerator.getFitnessFunction();

			ga.setFitnessFunction(fitnessFunction);

			SelectionFunction selectionFunction = TestSuiteGenerator.getSelectionFunction();
			selectionFunction.setMaximize(false);
			ga.setSelectionFunction(selectionFunction);

			ga.generateSolution();

			AbstractTestSuiteChromosome<ExecutableChromosome> best = (AbstractTestSuiteChromosome<ExecutableChromosome>) ga.getBestIndividual();
			return best;
		} finally {
			this.writeStateGraph();
		}
	}

	private static StoppingCondition getStoppingCondition() {
		StoppingCondition stoppingCondition = new MaxTimeStoppingCondition();
		stoppingCondition.setLimit(TIME_LIMIT_SECONDS);
		return stoppingCondition;
	}

	public static void serializeObjectToFile(Object object, String filename) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
			oos.writeObject(object);
			oos.close();
		} catch (Throwable t) {
			System.out.println("Exception on serializing object:");
			t.printStackTrace();
		}
	}

	private void writeStateGraph() {
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("state.dot")), "UTF-8"));
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
	
	public static void writeAllExecutedTests() {
		writeTests("tests.txt", UITestChromosome.getExecutedChromosomes());
	}
	
	public static void writeTests(String targetPath, Iterable<UITestChromosome> tests) {
		try {
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(targetPath)), "UTF-8"));
			
			for (UITestChromosome testChromosome : tests) {
				pw.println(testChromosome);
			}
			
			pw.close();
		} catch (Exception e) {
			System.out.println("Exception on writing executed tests:");
			e.printStackTrace();
		}
	}

}
