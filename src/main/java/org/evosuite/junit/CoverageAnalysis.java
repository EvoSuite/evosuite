/**
 * 
 */
package org.evosuite.junit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.result.Failure;
import org.evosuite.rmi.ClientServices;
import org.evosuite.sandbox.Sandbox;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.utils.ClassPathHandler;
import org.evosuite.utils.ExternalProcessUtilities;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.ResourceList;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runners.Suite;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * CoverageAnalysis class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class CoverageAnalysis {

	private final ExternalProcessUtilities util = new ExternalProcessUtilities();

	private final static Logger logger = LoggerFactory.getLogger(CoverageAnalysis.class);

	/**
	 * Identify all JUnit tests starting with the given name prefix, instrument
	 * and run tests
	 */
	public static void analyzeCoverage() {
		Sandbox.goingToExecuteSUTCode();
		Sandbox.goingToExecuteUnsafeCodeOnSameThread();
		try {
			String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
			DependencyAnalysis.analyze(Properties.TARGET_CLASS,
			                           Arrays.asList(cp.split(File.pathSeparator)));
			LoggingUtils.getEvoLogger().info("* Finished analyzing classpath");
		} catch (Throwable e) {
			LoggingUtils.getEvoLogger().error("* Error while initializing target class: "
			                                          + (e.getMessage() != null ? e.getMessage()
			                                                  : e.toString()));
			logger.error("Problem for " + Properties.TARGET_CLASS + ". Full stack:", e);
			return;
		} finally {
			Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
			Sandbox.doneWithExecutingSUTCode();
		}
		// TestCluster.getInstance();

		List<Class<?>> junitTests = getClasses();
		LoggingUtils.getEvoLogger().info("* Found " + junitTests.size() + " unit test classes");
		if (junitTests.isEmpty())
			return;

		 
		Class<?>[] classes =junitTests.toArray(new Class<?>[junitTests.size()]);
		LoggingUtils.getEvoLogger().info("* Executing tests");
		if(Properties.CRITERION == Criterion.MUTATION || Properties.CRITERION == Criterion.STRONGMUTATION) {
			junitMutationAnalysis(classes);
		} else {
			long startTime = System.currentTimeMillis();
			ExecutionTraceRunListener listener = new ExecutionTraceRunListener();
			List<Result> results = executeTests(listener, classes);
			printReport(listener.getExecutionTraces(), results, junitTests, startTime);
		}
	}

	public static void junitMutationAnalysis(Class<?>[] classes) {
		long startTime = System.currentTimeMillis();
		Set<Mutation> killed = executeTestsForMutationAnalysis(classes);
		List<Mutation> mutants = MutationPool.getMutants();
		if(Properties.NEW_STATISTICS) {
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, mutants.size());
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Covered_Goals, killed.size());
			if(mutants.isEmpty()) {
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BranchCoverage, 0.0); // TODO
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Coverage, 1.0);
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.MutationScore, 1.0);
			}
			else {
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BranchCoverage, 0.0); // TODO
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Coverage, (double)killed.size() / (double)mutants.size());
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.MutationScore, (double)killed.size() / (double)mutants.size());
			}
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Time, System.currentTimeMillis() - startTime);

			
			// FIXXME: Need to give some time for transmission before client is killed
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static double getCoverage() {
		TestCluster.getInstance();

		List<Class<?>> junitTests = getClasses();
		LoggingUtils.getEvoLogger().info("* Found " + junitTests.size()
		                                         + " unit test classes");
		if (junitTests.isEmpty())
			return 0.0;

		Class<?>[] classes = new Class<?>[junitTests.size()];
		junitTests.toArray(classes);
		LoggingUtils.getEvoLogger().info("* Executing tests");
		ExecutionTraceRunListener listener = new ExecutionTraceRunListener();
		List<Result> result = executeTests(listener, classes);
		return getCoverage(listener.getExecutionTraces(), result);
	}
	
	public static List<TestFitnessFunction> getCoveredGoals(Class<?> testClass, List<TestFitnessFunction> allGoals) {
		List<TestFitnessFunction> coveredGoals = new ArrayList<TestFitnessFunction>();
		ExecutionTraceRunListener listener = new ExecutionTraceRunListener();

		executeTests(listener, testClass);
		Set<ExecutionTrace> traces = listener.getExecutionTraces();
		for(ExecutionTrace trace : traces) {
			TestChromosome dummy = new TestChromosome();
			ExecutionResult executionResult = new ExecutionResult(dummy.getTestCase());
			executionResult.setTrace(trace);
			dummy.setLastExecutionResult(executionResult);
			dummy.setChanged(false);

			for(TestFitnessFunction goal : allGoals) {
				if (goal.isCovered(dummy))
					coveredGoals.add(goal);
			}
		}
		return coveredGoals;
	}

	private static List<Class<?>> getClassesFromClasspath() {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for(String prefix : Properties.JUNIT_PREFIX.split(":")) {
			Pattern pattern = Pattern.compile(prefix + ".*.class");
			Collection<String> resources = ResourceList.getResources(pattern);
			LoggingUtils.getEvoLogger().info("* Found " + resources.size() + " classes with prefix " + prefix);
			if (!resources.isEmpty()) {
				for (String resource : resources) {
					try {
						Class<?> clazz = Class.forName(
								resource.replaceAll(".class", "").replaceAll("/","."),
								true,
								TestGenerationContext.getInstance().getClassLoaderForSUT());
						if (isTest(clazz)) {
							classes.add(clazz);
						}
					} catch (ClassNotFoundException e2) {
						// Ignore?
						logger.info("Could not find class "+resource);
					} catch(Throwable t) {
						logger.info("Error while initialising class "+resource);
					}
				}

			}
		}
		return classes;
	}

	private static List<Class<?>> getClasses() {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		
		logger.debug("JUNIT_PREFIX: "+Properties.JUNIT_PREFIX);
		
		for(String prefix : Properties.JUNIT_PREFIX.split(":")) {
			
			LoggingUtils.getEvoLogger().info("* Analyzing entry: "+prefix);
			
			// If the target name is a path analyze it
			File path = new File(prefix);
			if (path.exists()) {
				if (Properties.JUNIT_PREFIX.endsWith(".jar"))
					classes.addAll(getClassesJar(path));
				else
					classes.addAll(getClasses(path));
			} else {

				try {
					Class<?> junitClass = Class.forName(prefix,
							true,
							TestGenerationContext.getInstance().getClassLoaderForSUT());
					classes.add(junitClass);
				} catch (ClassNotFoundException e) {
					// Second, try if the target name is a package name
					classes.addAll(getClassesFromClasspath());
				}
			}
		}
		return classes;
	}

	/**
	 * Analyze all classes that can be found in a given directory
	 * 
	 * @param directory
	 *            a {@link java.io.File} object.
	 * @throws ClassNotFoundException
	 *             if any.
	 * @return a {@link java.util.List} object.
	 */
	public static List<Class<?>> getClasses(File directory) {
		if (directory.getName().endsWith(".class")) {
			List<Class<?>> classes = new ArrayList<Class<?>>();
			LoggingUtils.muteCurrentOutAndErrStream();

			try {
				File file = new File(directory.getPath());
				byte[] array = new byte[(int) file.length()];
				ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
				InputStream in = new FileInputStream(file);
				try {
					int length = in.read(array);
					while (length > 0) {
						out.write(array, 0, length);
						length = in.read(array);
					}
				} finally {
					in.close();
				}
				ClassReader reader = new ClassReader(array);
				String className = reader.getClassName();

				// Use default classLoader
				Class<?> clazz = Class.forName(className.replace("/", "."), true,
				                               TestGenerationContext.getInstance().getClassLoaderForSUT());
				LoggingUtils.restorePreviousOutAndErrStream();

				//clazz = Class.forName(clazz.getName());
				classes.add(clazz);

			} catch (IllegalAccessError e) {
				LoggingUtils.restorePreviousOutAndErrStream();

				System.out.println("  Cannot access class "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6)
				        + ": " + e);
			} catch (NoClassDefFoundError e) {
				LoggingUtils.restorePreviousOutAndErrStream();

				System.out.println("  Error while loading "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6)
				        + ": Cannot find " + e.getMessage());
				//e.printStackTrace();
			} catch (ExceptionInInitializerError e) {
				LoggingUtils.restorePreviousOutAndErrStream();

				System.out.println("  Exception in initializer of "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6));
			} catch (ClassNotFoundException e) {
				LoggingUtils.restorePreviousOutAndErrStream();

				System.out.println("  Class not found in classpath: "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6)
				        + ": " + e);
			} catch (Throwable e) {
				LoggingUtils.restorePreviousOutAndErrStream();

				System.out.println("  Unexpected error: "
				        + directory.getName().substring(0,
				                                        directory.getName().length() - 6)
				        + ": " + e);
			}
			return classes;
		} else if (directory.isDirectory()) {
			List<Class<?>> classes = new ArrayList<Class<?>>();
			for (File file : directory.listFiles()) {
				classes.addAll(getClasses(file));
			}
			return classes;
		} else {
			return new ArrayList<Class<?>>();
		}
	}

	/**
	 * <p>
	 * getClassesJar
	 * </p>
	 * 
	 * @param file
	 *            a {@link java.io.File} object.
	 * @return a {@link java.util.List} object.
	 */
	public static List<Class<?>> getClassesJar(File file) {

		List<Class<?>> classes = new ArrayList<Class<?>>();

		ZipFile zf;
		try {
			zf = new ZipFile(file);
		} catch (final ZipException e) {
			throw new Error(e);
		} catch (final IOException e) {
			throw new Error(e);
		}

		final Enumeration<?> e = zf.entries();
		while (e.hasMoreElements()) {
			final ZipEntry ze = (ZipEntry) e.nextElement();
			final String fileName = ze.getName();
			if (!fileName.endsWith(".class"))
				continue;

			PrintStream old_out = System.out;
			PrintStream old_err = System.err;
			//System.setOut(outStream);
			//System.setErr(outStream);

			try {
				Class<?> clazz = Class.forName(fileName.replace(".class", "").replace("/",
				                                                                      "."),
				                               true,
				                               TestGenerationContext.getInstance().getClassLoaderForSUT());
				classes.add(clazz);
			} catch (IllegalAccessError ex) {
				System.setOut(old_out);
				System.setErr(old_err);
				System.out.println("Cannot access class "
				        + file.getName().substring(0, file.getName().length() - 6));
			} catch (NoClassDefFoundError ex) {
				System.setOut(old_out);
				System.setErr(old_err);
				System.out.println("Cannot find dependent class " + ex);
			} catch (ExceptionInInitializerError ex) {
				System.setOut(old_out);
				System.setErr(old_err);
				System.out.println("Exception in initializer of "
				        + file.getName().substring(0, file.getName().length() - 6));
			} catch (ClassNotFoundException ex) {
				System.setOut(old_out);
				System.setErr(old_err);
				System.out.println("Cannot find class "
				        + file.getName().substring(0, file.getName().length() - 6) + ": "
				        + ex);
			} catch (Throwable t) {
				System.setOut(old_out);
				System.setErr(old_err);

				System.out.println("  Unexpected error: "
				        + file.getName().substring(0, file.getName().length() - 6) + ": "
				        + t);
			} finally {
				System.setOut(old_out);
				System.setErr(old_err);
			}
		}
		try {
			zf.close();
		} catch (final IOException e1) {
			throw new Error(e1);
		}
		return classes;

	}

	private static double getCoverage(Set<ExecutionTrace> traces, List<Result> results) {
		int runCount = 0;
		for(Result result : results) {
			runCount += result.getRunCount();
		}
		LoggingUtils.getEvoLogger().info("* Executed " + runCount + " tests");
		int covered = 0;
		List<? extends TestFitnessFunction> goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals();
		
		for(ExecutionTrace trace : traces) {

			TestChromosome dummy = new TestChromosome();
			ExecutionResult executionResult = new ExecutionResult(dummy.getTestCase());
			executionResult.setTrace(trace);
			dummy.setLastExecutionResult(executionResult);
			dummy.setChanged(false);

			for (TestFitnessFunction goal : goals) {
				if (goal.isCovered(dummy))
					covered++;
			}
		}

		return (double) covered / (double) goals.size();
	}
	
	private static void printReport(Set<ExecutionTrace> traces, List<Result> results, List<Class<?>> classes, long startTime) {

		int runCount = 0;
		int numTests = 0;
		for(Result result : results) {
			runCount += result.getRunCount();
		}
		LoggingUtils.getEvoLogger().info("* Executed " + runCount + " tests");

		List<? extends TestFitnessFunction> goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals();
		Set<TestFitnessFunction> coveredGoals = new HashSet<TestFitnessFunction>();
		
		Set<Integer> coveredLines = new HashSet<Integer>();
		int explicitExceptions = 0;
		Set<String> explicitExceptionNames = new HashSet<String>();
		for(ExecutionTrace trace : traces) {

			TestChromosome dummy = new TestChromosome();
			ExecutionResult executionResult = new ExecutionResult(dummy.getTestCase());
			executionResult.setTrace(trace);
			dummy.setLastExecutionResult(executionResult);
			dummy.setChanged(false);
			coveredLines.addAll(trace.getCoveredLines());
			if(trace.getExplicitException() != null) {
				explicitExceptions++;
				explicitExceptionNames.add(trace.getExplicitException().getClass().getName());
			}

			boolean hasCoverage = false;
			for (TestFitnessFunction goal : goals) {
				if (goal.isCovered(dummy)) {
					hasCoverage = true;
					coveredGoals.add(goal);
				}
			}
			if(hasCoverage) {
				numTests++;
			}
		}
		LoggingUtils.getEvoLogger().info("* Covered "
		                                         + coveredGoals.size()
		                                         + "/"
		                                         + goals.size()
		                                         + " coverage goals: "
		                                         + NumberFormat.getPercentInstance().format((double) coveredGoals.size()

		                                                                                            / (double) goals.size()));

		JUnitReportGenerator reportGenerator = new JUnitReportGenerator(coveredGoals.size(),
		        goals.size(),
		        coveredLines,
		        classes, startTime, runCount);
		if(Properties.OLD_STATISTICS) {
			reportGenerator.writeCSV();
		} 
		if(Properties.NEW_STATISTICS) {
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Minimized_Size, numTests);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Result_Size, runCount);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Covered_Goals, coveredGoals.size());
			if(goals.isEmpty()) {
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BranchCoverage, 1.0);
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Coverage, 1.0);
			}
			else {
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BranchCoverage, (double)coveredGoals.size()/(double)goals.size());
				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Coverage, (double)coveredGoals.size()/(double)goals.size());
			}
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Time, System.currentTimeMillis() - startTime);

			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Explicit_TypeExceptions, explicitExceptionNames.size());
			
			// FIXXME: Need to give some time for transmission before client is killed
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		reportGenerator.writeReport();

	}

	private static List<Result> executeTests(ExecutionTraceRunListener listener, Class<?>... junitClasses) {
		ExecutionTracer.enable();
		ExecutionTracer.enableTraceCalls();
		ExecutionTracer.setCheckCallerThread(false);
		
		/*
		 * sort them in a deterministic way, in case there are 
		 * static state dependencies
		 */
		Arrays.sort(junitClasses,new Comparator<Class>(){
			@Override
			public int compare(Class o1, Class o2) {				
				return o1.getName().compareTo(o2.getName());
			}});
		
		List<Result> results = new ArrayList<Result>();
		final JUnitCore runner = new JUnitCore();
		runner.addListener(listener);

		for(Class<?> clazz : junitClasses) {
			try {
				logger.info("Running test "+clazz.getSimpleName());
				Result result = runner.run(clazz);
				results.add(result);
			} catch(Throwable t) {
				logger.warn("Error during test execution: "+t);
			}
		}
		ExecutionTracer.disable();
		return results;
	}
	
	private static Set<Mutation> executeTestsForMutationAnalysis(Class<?>... junitClasses) {
		ExecutionTracer.enable();
		ExecutionTracer.setCheckCallerThread(false);
		
		/*
		 * sort them in a deterministic way, in case there are 
		 * static state dependencies
		 */
		Arrays.sort(junitClasses,new Comparator<Class>(){
			@Override
			public int compare(Class o1, Class o2) {				
				return o1.getName().compareTo(o2.getName());
			}});
		
		List<Class<?>> passingClasses = new ArrayList<Class<?>>();
		for(Class<?> clazz : junitClasses) {
			try {
				logger.info("Running test "+clazz.getSimpleName());
				Result result = JUnitCore.runClasses(clazz);
				if(result.wasSuccessful())
					passingClasses.add(clazz);
			} catch(Throwable t) {
				logger.warn("Error during test execution: "+t);
			}
		}

		final JUnitCore runner = new JUnitCore();
		List<Mutation> mutants = MutationPool.getMutants();
		Set<Mutation> killed = new HashSet<Mutation>();
		for(Mutation mutation : mutants) {
			logger.info("Current mutant: "+mutation.getId());

			MutationObserver.activateMutation(mutation.getId());

			for(Class<?> clazz : passingClasses) {
				try {
					logger.info("Running test "+clazz.getSimpleName());
					Result result = runner.run(clazz);
					if(!result.wasSuccessful()) {
						// killed!
						killed.add(mutation);
						break;
					}
				} catch(Throwable t) {
					logger.warn("Error during test execution: "+t);
				}
			}
		}
		ExecutionTracer.disable();
		return killed;
	}


	/**
	 * Determine if this class contains JUnit tests
	 * 
	 * @param exceptionClassName
	 * @return
	 */
	public static boolean isTest(Class<?> clazz) {
		Class<?> superClazz = clazz.getSuperclass();
		while (superClazz != null && !superClazz.equals(Object.class)
		        && !superClazz.equals(clazz)) {
		    if (superClazz.equals(Suite.class))
                return true;
            if (superClazz.equals(TestSuite.class))
                return true;
            if (superClazz.equals(Test.class))
                return true;
            if (superClazz.equals(TestCase.class))
                return true;

			if (superClazz.equals(clazz.getSuperclass()))
				break;

			superClazz = clazz.getSuperclass();
		}
		for (Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(Test.class)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * run
	 * </p>
	 */
	public void run() {

		LoggingUtils.getEvoLogger().info("* Connecting to master process on port "
		                                         + Properties.PROCESS_COMMUNICATION_PORT);
		if (!util.connectToMainProcess()) {
			throw new RuntimeException("Could not connect to master process on port "
			        + Properties.PROCESS_COMMUNICATION_PORT);
		}

		analyzeCoverage();
		/*
		 * for now, we ignore the instruction (originally was meant to support several client in parallel and
		 * restarts, but that will be done in RMI)
		 */

		util.informSearchIsFinished(null);
	}

	/**
	 * <p>
	 * main
	 * </p>
	 * 
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	@Deprecated
	public static void main(String[] args) {
		LoggingUtils.getEvoLogger().error("Cannot start CoverageAnalysis directly");
		return;
		/*
		try {
			LoggingUtils.getEvoLogger().info("* Starting client");
			CoverageAnalysis process = new CoverageAnalysis();
			process.run();
			if (!Properties.CLIENT_ON_THREAD) {
				System.exit(0);
			}
		} catch (Throwable t) {
			LoggingUtils.getEvoLogger().error("Error when analyzing coveragetests for: "
			                                          + Properties.TARGET_CLASS
			                                          + " with seed "
			                                          + Randomness.getSeed(), t);

			//sleep 1 sec to be more sure that the above log is recorded
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			if (!Properties.CLIENT_ON_THREAD) {
				System.exit(1);
			}
		}
		*/
	}

}
