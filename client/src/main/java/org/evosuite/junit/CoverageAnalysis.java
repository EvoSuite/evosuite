/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.junit;

import junit.framework.TestCase;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.annotations.EvoSuiteTest;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.classpath.ResourceList;
import org.evosuite.coverage.CoverageCriteriaAnalyzer;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationObserver;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.rmi.ClientServices;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.StatisticsSender;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ExternalProcessUtilities;
import org.evosuite.utils.LoggingUtils;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Modifier;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * <p>
 * CoverageAnalysis class.
 * </p>
 * 
 * @author Gordon Fraser
 * @author José Campos
 */
public class CoverageAnalysis {

	/**
	 * FIXME
	 * 
	 * OUTPUT
	 * METHOD
	 * METHODNOEXCEPTION
	 * 
	 * relies on Observers. to have coverage of these criteria, JUnit test cases
	 * must have to be converted to some format that EvoSuite can understand
	 */

	private final static Logger logger = LoggerFactory.getLogger(CoverageAnalysis.class);

	private static int totalGoals = 0;
	private static int totalCoveredGoals = 0;
	private static Set<String> targetClasses = new LinkedHashSet<String>();

	/**
	 * Identify all JUnit tests starting with the given name prefix, instrument
	 * and run tests
	 */
	public static void analyzeCoverage() {
		Sandbox.goingToExecuteSUTCode();
        TestGenerationContext.getInstance().goingToExecuteSUTCode();
		Sandbox.goingToExecuteUnsafeCodeOnSameThread();
		ExecutionTracer.setCheckCallerThread(false);
		try {
			String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();

			if (Properties.TARGET_CLASS.endsWith(".jar")
					|| Properties.TARGET_CLASS.contains(File.separator)) {
				targetClasses = DependencyAnalysis.analyzeTarget(Properties.TARGET_CLASS,
                        Arrays.asList(cp.split(File.pathSeparator)));
			}
			else {
				targetClasses.add(Properties.TARGET_CLASS);
				DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS,
                        Arrays.asList(cp.split(File.pathSeparator)));
			}

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
            TestGenerationContext.getInstance().doneWithExecutingSUTCode();
		}
		// TestCluster.getInstance();

		List<Class<?>> testClasses = getTestClasses();
		LoggingUtils.getEvoLogger().info("* Found " + testClasses.size() + " test class(es)");
		if (testClasses.isEmpty())
			return;

		/*
         * sort them in a deterministic way, in case there are 
         * static state dependencies
         */
		sortTestClasses(testClasses);

		Class<?>[] tests = testClasses.toArray(new Class<?>[testClasses.size()]);
		LoggingUtils.getEvoLogger().info("* Executing test(s)");
		if (Properties.SELECTED_JUNIT == null) {
			boolean origUseAgent = EvoRunner.useAgent;
			boolean origUseClassLoader = EvoRunner.useClassLoader;
			try {
				EvoRunner.useAgent = false; //avoid double instrumentation
				EvoRunner.useClassLoader = false; //avoid double instrumentation

				List<JUnitResult> results = executeTests(tests);
				printReport(results);
			} finally {
				EvoRunner.useAgent = origUseAgent;
				EvoRunner.useClassLoader = origUseClassLoader;
			}
		} else {
			// instead of just running junit tests, carve them
			JUnitTestCarvedChromosomeFactory carvedFactory = new JUnitTestCarvedChromosomeFactory(null);
			TestSuiteChromosome testSuite = carvedFactory.getCarvedTestSuite();

			int goals = 0;
			for (Properties.Criterion pc : Properties.CRITERION) {
				LoggingUtils.getEvoLogger().info("* Coverage analysis for criterion " + pc);

				TestFitnessFactory ffactory = FitnessFunctions.getFitnessFactory(pc);
				goals += ffactory.getCoverageGoals().size();

				FitnessFunction ffunction = FitnessFunctions.getFitnessFunction(pc);
				ffunction.getFitness(testSuite);

				CoverageCriteriaAnalyzer.analyzeCoverage(testSuite, pc);
			}

			// Generate test suite
			TestSuiteGenerator.writeJUnitTestsAndCreateResult(testSuite);

			StatisticsSender.executedAndThenSendIndividualToMaster(testSuite);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals);
			if (Properties.COVERAGE_MATRIX)
				throw new IllegalArgumentException("Coverage matrix not yet available when measuring coverage of a carved test suite");
		}
	}

	/**
	 * Return the number of covered goals
	 * 
	 * @param testClass
	 * @param allGoals
	 * @return
	 */
	public static Set<TestFitnessFunction> getCoveredGoals(Class<?> testClass, List<TestFitnessFunction> allGoals) {

	    // A dummy Chromosome
	    TestChromosome dummy = new TestChromosome();
        dummy.setChanged(false);

        // Execution result of a dummy Test Case
        ExecutionResult executionResult = new ExecutionResult(dummy.getTestCase());

		Set<TestFitnessFunction> coveredGoals = new HashSet<TestFitnessFunction>();

		List<JUnitResult> results = executeTests(testClass);
		for (JUnitResult testResult : results) {
		    executionResult.setTrace(testResult.getExecutionTrace());
            dummy.setLastExecutionResult(executionResult);

            for(TestFitnessFunction goal : allGoals) {
            	if(coveredGoals.contains(goal))
            		continue;
            	else if (goal.isCovered(dummy))
                    coveredGoals.add(goal);
            }
		}

		return coveredGoals;
	}

	private static List<Class<?>> getTestClassesFromClasspath() {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for(String prefix : Properties.JUNIT.split(":")) {
			
			Set<String> suts = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(
					ClassPathHandler.getInstance().getTargetProjectClasspath(), prefix, false);
			
			LoggingUtils.getEvoLogger().info("* Found " + suts.size() + " classes with prefix '" + prefix + "'");
			if (!suts.isEmpty()) {
				for (String sut : suts) {
					if (targetClasses.contains(sut)) {
						continue ;
					}

					try {
						Class<?> clazz = Class.forName(
								sut,true,TestGenerationContext.getInstance().getClassLoaderForSUT());
						
						if (isTest(clazz)) {
							classes.add(clazz);
						}
					} catch (ClassNotFoundException e2) {
						logger.info("Could not find class "+sut);
					} catch(Throwable t) {
						logger.info("Error while initialising class "+sut);
					}
				}

			}
		}
		return classes;
	}

	private static List<Class<?>> getTestClasses() {
		List<Class<?>> testClasses = new ArrayList<Class<?>>();
		
		logger.debug("JUNIT: "+Properties.JUNIT);
		
		for(String prefix : Properties.JUNIT.split(":")) {
			
			LoggingUtils.getEvoLogger().info("* Analyzing entry: "+prefix);
			
			// If the target name is a path analyze it
			File path = new File(prefix);
			if (path.exists()) {
				if (Properties.JUNIT.endsWith(".jar"))
					testClasses.addAll(getTestClassesJar(path));
				else
					testClasses.addAll(getTestClasses(path));
			} else {

				try {
					Class<?> clazz = Class.forName(prefix,
							true,
							TestGenerationContext.getInstance().getClassLoaderForSUT());
					testClasses.add(clazz);
				} catch (ClassNotFoundException e) {
					// Second, try if the target name is a package name
					testClasses.addAll(getTestClassesFromClasspath());
				}
			}
		}
		return testClasses;
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
	private static List<Class<?>> getTestClasses(File directory) {

		List<Class<?>> testClasses = new ArrayList<Class<?>>();

		if (directory.getName().endsWith(".class")) {			
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
				Class<?> clazz = Class.forName(className.replace('/', '.'), true,
				                               TestGenerationContext.getInstance().getClassLoaderForSUT());
				LoggingUtils.restorePreviousOutAndErrStream();

				//clazz = Class.forName(clazz.getName());
				if (isTest(clazz))
					testClasses.add(clazz);

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
		} else if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				testClasses.addAll(getTestClasses(file));
			}
		}

		return testClasses;
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
	private static List<Class<?>> getTestClassesJar(File file) {

		List<Class<?>> testClasses = new ArrayList<Class<?>>();

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
			/*if (fileName.contains("$"))
                continue;*/

			PrintStream old_out = System.out;
			PrintStream old_err = System.err;
			//System.setOut(outStream);
			//System.setErr(outStream);

			try {
				Class<?> clazz = Class.forName(fileName.replace(".class", "").replace("/",
				                                                                      "."),
				                               true,
				                               TestGenerationContext.getInstance().getClassLoaderForSUT());

				if (isTest(clazz))
					testClasses.add(clazz);
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

		return testClasses;
	}

	private static void analyzeCoverageCriterion(List<JUnitResult> results, Properties.Criterion criterion) {

		logger.info("analysing coverage of " + criterion);

		// Factory
		TestFitnessFactory<? extends TestFitnessFunction> factory = FitnessFunctions.getFitnessFactory(criterion);

		// Goals
		List<?> goals = null;

		if (criterion == Criterion.MUTATION
				|| criterion == Criterion.STRONGMUTATION) {
			goals = MutationPool.getMutants();
		} else {
			goals = factory.getCoverageGoals();
		}
		totalGoals += goals.size();

		// A dummy Chromosome
        TestChromosome dummy = new TestChromosome();
        dummy.setChanged(false);

        // Execution result of a dummy Test Case
        ExecutionResult executionResult = new ExecutionResult(dummy.getTestCase());

        // coverage matrix (each row represents the coverage of each test case
        // and each column represents the coverage of each component (e.g., line)
        // this coverage matrix is useful for Rho fitness
    	boolean[][] coverage_matrix = new boolean[results.size()][goals.size() + 1]; // +1 because we also want to include the test result
    	BitSet covered = new BitSet(goals.size());

        for (int index_test = 0; index_test < results.size(); index_test++) {
        	JUnitResult tR = results.get(index_test);

        	ExecutionTrace trace = tR.getExecutionTrace();
            executionResult.setTrace(trace);
            dummy.getTestCase().clearCoveredGoals();
            dummy.setLastExecutionResult(executionResult);

            if (criterion == Criterion.MUTATION
            		|| criterion ==  Criterion.STRONGMUTATION) {
            	for (Integer mutationID : trace.getTouchedMutants()) {
            		Mutation mutation = MutationPool.getMutant(mutationID);

            		if (goals.contains(mutation)) {
            			MutationObserver.activateMutation(mutationID);
            			List<JUnitResult> mutationResults = executeTests(tR.getJUnitClass());
            			MutationObserver.deactivateMutation();

            			for (JUnitResult mR : mutationResults) {
            				if (mR.getFailureCount() != tR.getFailureCount()) {
            					logger.info("Mutation killed: " + mutationID);
            					covered.set(mutation.getId());
                                coverage_matrix[index_test][mutationID.intValue()] = true;
                                break;
            				}
            			}
            		}
            	}
            } else {
	            for (int index_component = 0; index_component < goals.size(); index_component++) {
	            	TestFitnessFunction goal = (TestFitnessFunction) goals.get(index_component);

	                if (goal.isCovered(dummy)) {
	                	covered.set(index_component);
	                	coverage_matrix[index_test][index_component] = true;
	                }
	                else {
	                	coverage_matrix[index_test][index_component] = false;
	                }
	            }
            }

            coverage_matrix[index_test][goals.size()] = tR.wasSuccessful();
        }
        totalCoveredGoals += covered.cardinality();

        if (Properties.COVERAGE_MATRIX) {
		    CoverageReportGenerator.writeCoverage(coverage_matrix, criterion);
        }

        StringBuilder str = new StringBuilder();
        for (int index_component = 0; index_component < goals.size(); index_component++) {
        	str.append(covered.get(index_component) ? "1" : "0");
        }
        logger.info("* CoverageBitString " + str.toString());

        RuntimeVariable bitStringVariable = CoverageCriteriaAnalyzer.getBitStringVariable(criterion);
        if (goals.isEmpty()) {
			LoggingUtils.getEvoLogger().info("* Coverage of criterion " + criterion + ": 100% (no goals)");
			ClientServices.getInstance().getClientNode().trackOutputVariable(CoverageCriteriaAnalyzer.getCoverageVariable(criterion), 1.0);
			if (bitStringVariable != null) {
				ClientServices.getInstance().getClientNode().trackOutputVariable(bitStringVariable, "1");
			}
		} 
        else {
        	double coverage = ((double) covered.cardinality()) / ((double) goals.size());
        	LoggingUtils.getEvoLogger().info("* Coverage of criterion " + criterion + ": " + NumberFormat.getPercentInstance().format(coverage));
			LoggingUtils.getEvoLogger().info("* Number of covered goals: " + covered.cardinality() + " / " + goals.size());

			ClientServices.getInstance().getClientNode().trackOutputVariable(CoverageCriteriaAnalyzer.getCoverageVariable(criterion), coverage);
			if (bitStringVariable != null) {
				ClientServices.getInstance().getClientNode().trackOutputVariable(bitStringVariable, str.toString());
			}
        }
	}

	private static void printReport(List<JUnitResult> results) {

		Iterator<String> it = targetClasses.iterator();
		Criterion[] criterion = Properties.CRITERION;

		while (it.hasNext()) {
			String targetClass = it.next();

			// restart variables
			totalGoals = 0;
			totalCoveredGoals = 0;

			Properties.TARGET_CLASS = targetClass;
			LoggingUtils.getEvoLogger().info("* Target class " + Properties.TARGET_CLASS);
			ClientServices.getInstance().getClientNode().updateProperty("TARGET_CLASS", Properties.TARGET_CLASS);

			for (int criterion_index = 0; criterion_index < criterion.length; criterion_index++) {
				Properties.Criterion c = criterion[criterion_index];
				Properties.CRITERION = new Criterion[] { c };

				analyzeCoverageCriterion(results, c);
			}

			// restore
			Properties.CRITERION = criterion;

			LoggingUtils.getEvoLogger().info("* Total number of covered goals: " + totalCoveredGoals + " / " + totalGoals);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, totalGoals);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Covered_Goals, totalCoveredGoals);

			double coverage = totalGoals == 0 ? 1.0 : ((double) totalCoveredGoals) / ((double) totalGoals);
			LoggingUtils.getEvoLogger().info("* Total coverage: " + NumberFormat.getPercentInstance().format(coverage));
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Coverage, coverage);

			// need to give some time for transmission before client is killed
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// last element will be flush by master process
			if (it.hasNext()) {
				ClientServices.getInstance().getClientNode().flushStatisticsForClassChange();
			}
		}
	}

	private static List<JUnitResult> executeTests(Class<?>... testClasses) {

		ExecutionTracer.enable();
		ExecutionTracer.setCheckCallerThread(false);
		ExecutionTracer.getExecutionTracer().clear();

		List<JUnitResult> results = new ArrayList<JUnitResult>();
        for (Class<?> testClass : testClasses) {
        	LoggingUtils.getEvoLogger().info("  Executing " + testClass.getSimpleName());
        	// Set the context classloader in case the SUT requests it
    		Thread.currentThread().setContextClassLoader(testClass.getClassLoader());
            JUnitRunner jR = new JUnitRunner(testClass);
            jR.run();
            results.addAll(jR.getTestResults());
        }

		ExecutionTracer.disable();

        LoggingUtils.getEvoLogger().info("* Executed " + results.size() + " unit test(s)");
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Tests_Executed, results.size());

        return results;
	}

	/**
	 * Determine if a class contains JUnit tests
	 * 
	 * @param cls
	 * @return
	 */
	public static boolean isTest(Class<?> cls) {
		if (Modifier.isAbstract(cls.getModifiers())) {
			return false;
		}

		TestClass tc;

		try {
			tc = new TestClass(cls);
		} catch (IllegalArgumentException e) {
			return false;
		} catch (RuntimeException e){
			//this can happen if class has Annotations that are not available on classpath
			throw new RuntimeException("Failed to analyze class "+cls.getName()+ " due to: " + e.toString());
		}

		// JUnit 4
		try {
			List<FrameworkMethod> methods = new ArrayList<>();
			methods.addAll(tc.getAnnotatedMethods(Test.class));
			methods.addAll(tc.getAnnotatedMethods(EvoSuiteTest.class));
			for (FrameworkMethod method : methods) {
				List<Throwable> errors = new ArrayList<Throwable>();
				method.validatePublicVoidNoArg(false, errors);
				if (errors.isEmpty()) {
					return true;
				}
			}
		} catch (IllegalArgumentException e) {
			return false;
		}

		// JUnit 3
		Class<?> superClass = cls; 
		while ((superClass = superClass.getSuperclass()) != null) {
			if (superClass.getCanonicalName().equals(Object.class.getCanonicalName())) {
				break ;
			}
			else if (superClass.getCanonicalName().equals(TestCase.class.getCanonicalName())) {
				return true;
			}
		}

		// TODO add support for other frameworks, e.g., TestNG ?

		return false;
	}

	/**
     * re-order test classes
     * 
     * @param tests
     */
    private static void sortTestClasses(List<Class<?>> tests) {
        Collections.sort(tests, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> t0, Class<?> t1) {
                return Integer.compare(t1.getName().length(), t0.getName().length());
            }
        });
    }

	/**
	 * <p>
	 * run
	 * </p>
	 */
	public void run() {

		LoggingUtils.getEvoLogger().info("* Connecting to master process on port "
		                                         + Properties.PROCESS_COMMUNICATION_PORT);

		ExternalProcessUtilities util = new ExternalProcessUtilities();
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

	// just for testing
	protected static void reset() {
		totalGoals = 0;
		totalCoveredGoals = 0;
		targetClasses.clear();
	}
}
