package org.exsyst.ui;

import java.io.*;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.SelectionFunction;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.codegen.PostProcessor;
import org.evosuite.testcarver.testcase.TestCarvingExecutionObserver;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestCluster;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.SimpleCondition;
import org.slf4j.Logger;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;

import sun.awt.AWTAutoShutdown;
import org.exsyst.ui.genetics.*;
import org.exsyst.ui.model.states.UIStateGraph;
import org.exsyst.ui.run.RandomWalkUIController;
import org.exsyst.ui.run.UIRunner;
import org.exsyst.ui.util.ReplayUITestHelper;


public class UITestSuiteGenerator {
	private static final int TIME_LIMIT_SECONDS = Integer.valueOf(System.getProperty("timelimit", "" + Properties.GLOBAL_TIMEOUT));
	
	public static final class MainTrigger implements Trigger, Serializable {
		private static final long serialVersionUID = 1L;
		private String mainClass;
		private Class<?> mainClassClass;

		public MainTrigger(String mainClass) {
			this.mainClass = mainClass;
		}

		public Class<?> getMainClass()
		{
			return this.mainClassClass;
		}
		
		@Override
		public void run() throws Exception {
			ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			final ClassLoader[] oldEventThreadClassLoader = new ClassLoader[1];

			try {
				final ClassLoader classLoader = TestCluster.classLoader;
				Thread.currentThread().setContextClassLoader(classLoader);

				mainClassClass = classLoader.loadClass(this.mainClass);
				
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						oldEventThreadClassLoader[0] = Thread.currentThread().getContextClassLoader();
						Thread.currentThread().setContextClassLoader(classLoader);
					}
				});

				mainClassClass.getMethod("main", new Class<?>[] { String[].class }).invoke(null, new Object[] { new String[] {} });
			} catch (Exception e) {
				System.out.println("Got exception on invoking main method:");
				e.printStackTrace();
			} finally {
				Thread.currentThread().setContextClassLoader(oldClassLoader);

				if (oldEventThreadClassLoader[0] != null) {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							Thread.currentThread().setContextClassLoader(oldEventThreadClassLoader[0]);
						}
					});
				}
			}
		}
	}

	public static void main(String[] args) {
		System.out.println(System.out);
		
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

			
			Properties.INSTRUMENTATION_SKIP_DEBUG = true;
			
			Properties.MAX_SIZE = 1000;
			Properties.SEARCH_BUDGET = Integer.MAX_VALUE;

			// Timeout of 2 * 60 seconds per test.
			Properties.TIMEOUT = 2 * 60 * 1000;
			Properties.CPU_TIMEOUT = false;

			// String replacement as of 13-Jul-2011 / revision 500:3d19d48a9098
			// is severely broken, randomly replacing if conditions with garbage
			// and potentially even causing bytecode verification errors. --
			// fgross
			
			// String replacement still has (another) bug
			// with jEdit...
			Properties.STRING_REPLACEMENT = false;

			System.setSecurityManager(new SecurityManager() {
				@Override
				public void checkExit(int status) {
					StackTraceElement[] trace = new Exception().fillInStackTrace().getStackTrace();
					boolean afterExit = false;
					boolean isOK = false;

					for (StackTraceElement elem : trace) {
						String methodName = elem.getClassName() + "." + elem.getMethodName();
						String mainMethod = "org.exsyst.ui.UITestSuiteGenerator.main";

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
			
			// We don't actually care about the result...
			// Call this here so later calls to it won't call TestCluster.reset()
			// (which would otherwise create a new InstrumentingClassLoader and cause severe trouble)
			Properties.getTargetClass();

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
	
	public static synchronized void writeCoverage() {
		try {
			Class<?> emmaRT = Class.forName("com.vladium.emma.rt.RT");
			Method m = emmaRT.getMethod("dumpCoverageData", new Class<?>[] { File.class, boolean.class, boolean.class });
			m.invoke(null, new File("coverage.ec"), false, false);
		} catch (Throwable t) {
			/* No worries */
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
			UIRunner.run(this.stateGraph, new RandomWalkUIController(0) {
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
//		Thread t = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (true) {
//					try {
//						Thread.sleep(5 * 60 * 1000);
//					} catch (InterruptedException e) {
//					}
//
//					try {
//						UITestSuiteGenerator.this.writeStateGraph();
//					} catch (Exception e) {
//						System.err.println("Error writing state graph!");
//						e.printStackTrace();
//					}
//				}
//			}
//		}, "State Graph Writer");
//
//		t.setDaemon(true);
//		t.start();

		this.writeStateGraph();

		try {
			FitnessFunction fitnessFunction = TestSuiteGenerator.getFitnessFunction();

			ChromosomeFactory<UITestChromosome> testFactory = new UITestChromosomeFactory(stateGraph, this.mainMethodTrigger);
			ChromosomeFactory<UITestSuiteChromosome> testSuiteFactory = new UITestSuiteChromosomeFactory(testFactory, fitnessFunction);

			GeneticAlgorithm ga = TestSuiteGenerator.getGeneticAlgorithm(testSuiteFactory);
			TestSuiteGenerator.getSecondaryObjectives(ga);

			ga.setStoppingCondition(getStoppingCondition());


			ga.setFitnessFunction(fitnessFunction);

			SelectionFunction selectionFunction = TestSuiteGenerator.getSelectionFunction();
			selectionFunction.setMaximize(false);
			ga.setSelectionFunction(selectionFunction);

//			Thread thread = new Thread(new Runnable() {	
//				private int coverageIdx = 1;
//				private long delay = Properties.UI_BACKGROUND_COVERAGE_DELAY;
//				
//				@Override
//				public void run() {				
//					if (delay < 0)
//						return;
//
//					long currentTime = System.currentTimeMillis();
//					long nextTime = currentTime;
//					
//					while (true) {
//						currentTime = System.currentTimeMillis();
//
//						if (currentTime >= nextTime) {
//							try {
//								Class<?> emmaRT = Class.forName("com.vladium.emma.rt.RT");
//								Method m = emmaRT.getMethod("dumpCoverageData", new Class<?>[] { File.class, boolean.class, boolean.class });
//								String filename = String.format("coverage-%d.ec", coverageIdx);
//								m.invoke(null, new File(filename), false, false);
//								nextTime = System.currentTimeMillis() + delay;
//								
//								delay *= 1.1;
//								
//								coverageIdx++;
//							} catch (Throwable t) {
//								t.printStackTrace();
//							}
//						} else {
//							try {
//								Thread.sleep(nextTime - currentTime);
//							} catch (InterruptedException e) {
//								/* OK */
//							}
//						}
//					}
//				}
//			}, "Coverage writer thread");
//
//			thread.setDaemon(true);
//			thread.start();
			
			ga.generateSolution();

			AbstractTestSuiteChromosome<ExecutableChromosome> best = (AbstractTestSuiteChromosome<ExecutableChromosome>) ga.getBestIndividual();
		 
			if(Properties.TEST_CARVING)
			{
				final List<ExecutableChromosome>  chromosomes    = best.getTestChromosomes();
				final int                         numChromosomes = chromosomes.size();
				final ArrayList<UITestChromosome> testCases      = new ArrayList<UITestChromosome>(numChromosomes);
				
				for(int i = 0; i < numChromosomes; i++)
				{
					testCases.add((UITestChromosome) chromosomes.get(i));
				}
				
				this.carveTests(testCases);
			}
			
			return best;
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally {
			this.writeStateGraph();
		}
	}

	
	
	
	private List<CaptureLog> executeAndCapture(List<UITestChromosome> testsToBeCarved)
	{
		System.err.println("NUM TESTS TO BE CARVED: " + testsToBeCarved.size());

		// variables needed in loop
		for(UITestChromosome t : testsToBeCarved)
		{
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>> start c");
			ReplayUITestHelper.waitForEmptyAWTEventQueue();
			
			
			
			// start capture before genetic algorithm is applied so that all interactions can be captured
			Capturer.startCapture();
			
			// execute test case
			ReplayUITestHelper.run(t);
			
			// stop capture after best individual has been determined and obtain corresponding capture log
			Capturer.stopCapture();
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> end c");
		}

		final List<CaptureLog> logs = Capturer.getCaptureLogs();
		
		// clear Capturer content to save memory
		Capturer.clear();
		
		return logs;
	}
	
	
	private void carveTests(List<UITestChromosome> testsToBeCarved)
	{
		
		long s = System.currentTimeMillis();
		final List<CaptureLog> logs   = this.executeAndCapture(testsToBeCarved);
		final Logger           logger = LoggingUtils.getEvoLogger();
		
		final HashSet<Class<?>>     allAccessedClasses = new HashSet<Class<?>>();
		final ArrayList<String>     packages           = new ArrayList<String>();
		final ArrayList<Class<?>[]> observedClasses    = new ArrayList<Class<?>[]>();
		final ArrayList<CaptureLog> logsToBeDeleted    = new ArrayList<CaptureLog>();
		
		CaptureLog log;
		
		final int numLogs = logs.size();
		for(int i = 0; i < numLogs; i++)
		{
			log = logs.get(i);
			
			for(String className : log.oidClassNames)
			{
				if(className.startsWith(Properties.TARGET_CLASS_PREFIX) && ! className.contains("$"))
				{
					try 
					{
						allAccessedClasses.add(Class.forName(className));
					} 
					catch (final ClassNotFoundException e) 
					{
						logger.warn("an error occurred while resolving target class {} -> ignored", className, e);
					}
				}
			}
			
			try 
			{
				allAccessedClasses.remove(Class.forName(((MainTrigger)this.mainMethodTrigger).getMainClass().getName()));
			} 
			catch (final ClassNotFoundException e) 
			{
				logger.warn("an error occurred while resolving main class", e);
				logsToBeDeleted.add(log);
				continue;
			}
				
			if(allAccessedClasses.isEmpty())
			{
				logger.warn("There are no classes which can be observed in test\n{}\n --> no test carving performed", testsToBeCarved.get(i));
				logsToBeDeleted.add(log);
				continue;
			}
			
			packages.add(allAccessedClasses.iterator().next().getPackage().getName());
			observedClasses.add(allAccessedClasses.toArray(new Class[allAccessedClasses.size()]));
		}
		
		
		logs.removeAll(logsToBeDeleted);
		
		try 
		{
			PostProcessor.init();
			PostProcessor.process(logs, packages, observedClasses);
		} 
		catch (final Exception e) 
		{
			e.printStackTrace();
			logger.error("an error occurred while postprocessing captured data", e);
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
