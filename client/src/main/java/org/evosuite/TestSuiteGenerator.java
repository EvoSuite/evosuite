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
package org.evosuite;

 
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.TestFactory;
import org.evosuite.assertion.AssertionGenerator;
import org.evosuite.assertion.CompleteAssertionGenerator;
import org.evosuite.assertion.SimpleMutationAssertionGenerator;
import org.evosuite.assertion.UnitAssertionGenerator;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.contracts.ContractChecker;
import org.evosuite.contracts.FailingTestSet;
import org.evosuite.coverage.CoverageAnalysis;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import org.evosuite.idNaming.TestNameGenerator;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.runtime.sandbox.PermissionStatistics;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.StatisticsSender;
import org.evosuite.strategy.FixedNumRandomTestStrategy;
import org.evosuite.strategy.IndividualTestStrategy;
import org.evosuite.strategy.RandomTestStrategy;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.strategy.WholeTestSuiteStrategy;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.testcase.ConstantInliner;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTraceImpl;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.ValueMinimizer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.testsuite.TestSuiteMinimizer;
import org.evosuite.testsuite.TestSuiteSerialization;
import org.evosuite.testsuite.factories.SerializationSuiteChromosomeFactory;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main entry point.
 * Does all the static analysis, invokes a test generation strategy,
 * and then applies postprocessing.
 * 
 * @author Gordon Fraser
 */
public class TestSuiteGenerator {

	private static Logger logger = LoggerFactory.getLogger(TestSuiteGenerator.class);

	/**
	 * Generate a test suite for the target class
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public TestGenerationResult generateTestSuite() {

		LoggingUtils.getEvoLogger().info("* Analyzing classpath: ");

		ClientServices.getInstance().getClientNode().changeState(ClientState.INITIALIZATION);

		TestCaseExecutor.initExecutor();
		Sandbox.goingToExecuteSUTCode();
        TestGenerationContext.getInstance().goingToExecuteSUTCode();
		Sandbox.goingToExecuteUnsafeCodeOnSameThread();
		try {
			String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
			DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS,
			                           Arrays.asList(cp.split(File.pathSeparator)));
			LoggingUtils.getEvoLogger().info("* Finished analyzing classpath");
		} catch (Throwable e) {
			LoggingUtils.getEvoLogger().error("* Error while initializing target class: "
			                                          + (e.getMessage() != null ? e.getMessage()
			                                                  : e.toString()));
			logger.error("Problem for " + Properties.TARGET_CLASS + ". Full stack:", e);
			return TestGenerationResultBuilder.buildErrorResult(
			          e.getMessage() != null ? e.getMessage() : e.toString());
		} finally {
			Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
			Sandbox.doneWithExecutingSUTCode();
            TestGenerationContext.getInstance().doneWithExecuteingSUTCode();
		}
		

        /*
         * Initialises the object pool with objects carved from SELECTED_JUNIT classes
         */
		// TODO: Do parts of this need to be wrapped into sandbox statements?
		ObjectPoolManager.getInstance();


		LoggingUtils.getEvoLogger().info("* Generating tests for class "
		                                         + Properties.TARGET_CLASS);
		printTestCriterion();

		if (Properties.getTargetClass() == null)
		    return TestGenerationResultBuilder.buildErrorResult("Could not load target class");

		TestSuiteChromosome testCases = generateTests();
		postProcessTests(testCases);
        ClientServices.getInstance().getClientNode().publishPermissionStatistics();
		PermissionStatistics.getInstance().printStatistics(LoggingUtils.getEvoLogger());
		
		// progressMonitor.setCurrentPhase("Writing JUnit test cases");
		TestGenerationResult result = writeJUnitTestsAndCreateResult(testCases);

		TestCaseExecutor.pullDown();
		/*
		 * TODO: when we will have several processes running in parallel, we ll
		 * need to handle the gathering of the statistics.
		 */
		ClientServices.getInstance().getClientNode().changeState(ClientState.WRITING_STATISTICS);
		
		LoggingUtils.getEvoLogger().info("* Done!");
		LoggingUtils.getEvoLogger().info("");

		return result;
	}

	/**
	 * Apply any readability optimizations and other techniques
	 * that should use or modify the generated tests
	 *  
	 * @param testSuite
	 */
	protected void postProcessTests(TestSuiteChromosome testSuite) {
		
        if (Properties.TEST_FACTORY == TestFactory.SERIALIZATION) {
            SerializationSuiteChromosomeFactory.saveTests(testSuite);
        }
        
        if(Properties.CTG_SEEDS_FILE_OUT != null){
                TestSuiteSerialization.saveTests(testSuite, new File(Properties.CTG_SEEDS_FILE_OUT));
        }
        
		if (Properties.MINIMIZE_VALUES && 
		                Properties.CRITERION.length == 1) {
		    double fitness = testSuite.getFitness();

			ClientServices.getInstance().getClientNode().changeState(ClientState.MINIMIZING_VALUES);
			LoggingUtils.getEvoLogger().info("* Minimizing values");
			ValueMinimizer minimizer = new ValueMinimizer();
			minimizer.minimize(testSuite, (TestSuiteFitnessFunction)testSuite.getFitnessValues().keySet().iterator().next());
//			minimizer.minimizeUnsafeType(testSuite);
			assert (fitness >= testSuite.getFitness());
		}

		if (Properties.INLINE) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.INLINING);
			ConstantInliner inliner = new ConstantInliner();
			// progressMonitor.setCurrentPhase("Inlining constants");

			//Map<FitnessFunction<? extends TestSuite<?>>, Double> fitnesses = testSuite.getFitnesses();

			inliner.inline(testSuite);
		}

		if (Properties.MINIMIZE) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.MINIMIZATION);
			// progressMonitor.setCurrentPhase("Minimizing test cases");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactories());
			//if (Properties.CRITERION.length == 1) {
				LoggingUtils.getEvoLogger().info("* Minimizing test suite");
			    minimizer.minimize(testSuite, true);
			//}
//			else {
//				LoggingUtils.getEvoLogger().info("* Minimizing test suites");
//				minimizer.minimize(testSuite, false);
//			}
		} else {
		    ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Result_Size, testSuite.size());
		    ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Minimized_Size, testSuite.size());
		    ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Result_Length, testSuite.totalLengthOfTestCases());
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Minimized_Length, testSuite.totalLengthOfTestCases());
		}

		if (Properties.COVERAGE) {
		    for (Properties.Criterion pc : Properties.CRITERION) {
		    	LoggingUtils.getEvoLogger().info("* Coverage analysis for criterion " + pc);
		        CoverageAnalysis.analyzeCoverage(testSuite, pc);
		    }
		}

        double coverage = testSuite.getCoverage();

        if (ArrayUtil.contains(Properties.CRITERION, Criterion.MUTATION)
                || ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION)) {
		    //SearchStatistics.getInstance().mutationScore(coverage);
		}

		StatisticsSender.executedAndThenSendIndividualToMaster(testSuite);
		LoggingUtils.getEvoLogger().info("* Generated " + testSuite.size()
		                                         + " tests with total length "
		                                         + testSuite.totalLengthOfTestCases());

		// TODO: In the end we will only need one analysis technique
		if (!Properties.ANALYSIS_CRITERIA.isEmpty()) {
		    //SearchStatistics.getInstance().addCoverage(Properties.CRITERION.toString(), coverage);
		    CoverageAnalysis.analyzeCriteria(testSuite, Properties.ANALYSIS_CRITERIA); // FIXME: can we send all bestSuites?
		}
        if (Properties.CRITERION.length > 1)
            LoggingUtils.getEvoLogger().info("* Resulting test suite's coverage: "
                    + NumberFormat.getPercentInstance().format(coverage) + " (average coverage for all fitness functions)");
        else
            LoggingUtils.getEvoLogger().info("* Resulting test suite's coverage: "
                    + NumberFormat.getPercentInstance().format(coverage));

		// printBudget(ga); // TODO - need to move this somewhere else
		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
		        && Properties.ANALYSIS_CRITERIA.isEmpty())
			DefUseCoverageSuiteFitness.printCoverage();

        DSEStats.trackConstraintTypes();

        DSEStats.trackSolverStatistics();
        
        if (Properties.DSE_PROBABILITY > 0.0 && Properties.LOCAL_SEARCH_RATE > 0 && Properties.LOCAL_SEARCH_PROBABILITY > 0.0) {
                DSEStats.logStatistics();
        }
        
		if (Properties.FILTER_SANDBOX_TESTS) {
			for (TestChromosome test : testSuite.getTestChromosomes()) {
				// delete all statements leading to security exceptions
				ExecutionResult result = test.getLastExecutionResult();
				if (result == null) {
					result = TestCaseExecutor.runTest(test.getTestCase());
				}
				if (result.hasSecurityException()) {
					int position = result.getFirstPositionOfThrownException();
					if (position > 0) {
						test.getTestCase().chop(position);
						result = TestCaseExecutor.runTest(test.getTestCase());
						test.setLastExecutionResult(result);
					}
				}
		    }
		}
		
		if (Properties.ASSERTIONS) {
			LoggingUtils.getEvoLogger().info("* Generating assertions");
			// progressMonitor.setCurrentPhase("Generating assertions");
			ClientServices.getInstance().getClientNode().changeState(ClientState.ASSERTION_GENERATION);
			addAssertions(testSuite);
			StatisticsSender.sendIndividualToMaster(testSuite); // FIXME: can we pass the list of testsuitechromosomes?
		}

		if (Properties.CHECK_CONTRACTS) {
			for (TestCase failing_test : FailingTestSet.getFailingTests()) {
				testSuite.addTest(failing_test);    			
		    }
			FailingTestSet.sendStatistics();
		}

		if (Properties.JUNIT_TESTS && Properties.JUNIT_CHECK) {
			compileAndCheckTests(testSuite);
		}
		
		
	}
	
	 /**
     * Compile and run the given tests. Remove from input list all tests that do not compile, and handle the
     * cases of instability (either remove tests or comment out failing assertions)
     *
     * @param chromosomeList
     */
    private void compileAndCheckTests(TestSuiteChromosome chromosome) {
        LoggingUtils.getEvoLogger().info("* Compiling and checking tests");

        if(!JUnitAnalyzer.isJavaCompilerAvailable()) {
            String msg = "No Java compiler is available. Are you running with the JDK?";
            logger.error(msg);
            throw new RuntimeException(msg);
        }

        ClientServices.getInstance().getClientNode().changeState(ClientState.JUNIT_CHECK);

        // Store this value; if this option is true then the JUnit check
        // would not succeed, as the JUnit classloader wouldn't find the class
        boolean junitSeparateClassLoader = Properties.USE_SEPARATE_CLASSLOADER;
        Properties.USE_SEPARATE_CLASSLOADER = false;

        int numUnstable = 0;

        //note: compiling and running JUnit tests can be very time consuming
        if(!TimeController.getInstance().isThereStillTimeInThisPhase()) {
        	return;
        }

        List<TestCase> testCases = chromosome.getTests(); // make copy of current tests

        //first, let's just get rid of all the tests that do not compile
        JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);

        //compile and run each test one at a time. and keep track of total time
        long start = java.lang.System.currentTimeMillis();
        Iterator<TestCase> iter = testCases.iterator();
        while(iter.hasNext()){
        	if(!TimeController.getInstance().hasTimeToExecuteATestCase()) {
        		break;
        	}
        	TestCase tc = iter.next();
        	List<TestCase> list = new ArrayList<>();
        	list.add(tc);
        	numUnstable += JUnitAnalyzer.handleTestsThatAreUnstable(list);
        	if(list.isEmpty()){
        		//if the test was unstable and deleted, need to remove it from final testSuite
        		iter.remove();
        	}
        }
        /*
                compiling and running each single test individually will take more than
                compiling/running everything in on single suite. so it can be used as an
                upper bound
         */
        long delta = java.lang.System.currentTimeMillis() - start;

        numUnstable += checkAllTestsIfTime(testCases, delta);

        //second passage on reverse order, this is to spot dependencies among tests
        if (testCases.size() > 1) {
        	Collections.reverse(testCases);
        	numUnstable += checkAllTestsIfTime(testCases, delta);
        }

        chromosome.clearTests(); //remove all tests
        for (TestCase testCase : testCases) {
        	chromosome.addTest(testCase); //add back the filtered tests
        }

        boolean unstable = (numUnstable > 0);

        if(!TimeController.getInstance().isThereStillTimeInThisPhase()){
        	logger.warn("JUnit checking timed out");
        }

        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.HadUnstableTests, unstable);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.NumUnstableTests, numUnstable);
        Properties.USE_SEPARATE_CLASSLOADER = junitSeparateClassLoader;

    }

    private int checkAllTestsIfTime(List<TestCase> testCases, long delta) {
    	if(TimeController.getInstance().hasTimeToExecuteATestCase() &&
    			TimeController.getInstance().isThereStillTimeInThisPhase(delta)) {
    		return JUnitAnalyzer.handleTestsThatAreUnstable(testCases);
    	}
        return 0;
    }
    
	
	
	
	private int getBytecodeCount(RuntimeVariable v, Map<RuntimeVariable,Set<Integer>> m){
		Set<Integer> branchSet = m.get(v);
		return (branchSet==null) ? 0 : branchSet.size();
	}

	private TestSuiteChromosome generateTests() {
		// Make sure target class is loaded at this point
		TestCluster.getInstance();

		if (TestCluster.getInstance().getNumTestCalls() == 0) {
			LoggingUtils.getEvoLogger().info("* Found no testable methods in the target class "
			                                         + Properties.TARGET_CLASS);
			return new TestSuiteChromosome();
		}

		ContractChecker checker = null;
		if (Properties.CHECK_CONTRACTS) {
			checker = new ContractChecker();
			TestCaseExecutor.getInstance().addObserver(checker);
		}

		TestGenerationStrategy strategy = getTestGenerationStrategy();
		TestSuiteChromosome testSuite = strategy.generateTests();
		
		if (Properties.CHECK_CONTRACTS) {
			TestCaseExecutor.getInstance().removeObserver(checker);
		}
		
		StatisticsSender.executedAndThenSendIndividualToMaster(testSuite);
		getBytecodeStatistics();

        ClientServices.getInstance().getClientNode().publishPermissionStatistics();

        writeObjectPool(testSuite);

		/*
		 * PUTGeneralizer generalizer = new PUTGeneralizer(); for (TestCase test
		 * : tests) { generalizer.generalize(test); // ParameterizedTestCase put
		 * = new ParameterizedTestCase(test); }
		 */

		return testSuite;
	}
	
	private TestGenerationStrategy getTestGenerationStrategy() {
		switch(Properties.STRATEGY) {
		case EVOSUITE:
			return new WholeTestSuiteStrategy();
		case RANDOM:
			return new RandomTestStrategy();
		case RANDOM_FIXED:
			return new FixedNumRandomTestStrategy();
		case ONEBRANCH:
			return new IndividualTestStrategy();
		default:
			throw new RuntimeException("Unsupported strategy: "+Properties.STRATEGY);
		}
	}

	/**
	 * <p>
	 * If Properties.JUNIT_TESTS is set, this method writes the given test cases
	 * to the default directory Properties.TEST_DIR.
	 * 
	 * <p>
	 * The name of the test will be equal to the SUT followed by the given
	 * suffix
	 * 
	 * @param tests
	 *            a {@link java.util.List} object.
	 */
	public static TestGenerationResult writeJUnitTestsAndCreateResult(List<TestCase> tests, String suffix) {
		if (Properties.JUNIT_TESTS) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.WRITING_TESTS);

			TestSuiteWriter suite = new TestSuiteWriter();
			if (Properties.ASSERTION_STRATEGY == AssertionStrategy.STRUCTURED)
				suite.insertAllTests(tests);
			else
				suite.insertTests(tests);

			if (Properties.CHECK_CONTRACTS) {
				LoggingUtils.getEvoLogger().info("* Writing failing test cases");
				// suite.insertAllTests(FailingTestSet.getFailingTests());
				FailingTestSet.writeJUnitTestSuite(suite);
			}

			String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
			String testDir = Properties.TEST_DIR;

			LoggingUtils.getEvoLogger().info("* Writing JUnit test case '" + (name + suffix) + "' to " + testDir);
			suite.writeTestSuite(name + suffix, testDir, true);
		}
	
		return TestGenerationResultBuilder.buildSuccessResult();
	}

	/**
	 * 
	 * @param tests
	 *            the test cases which should be written to file
	 */
	public static TestGenerationResult writeJUnitTestsAndCreateResult(TestSuiteChromosome testSuite) {
		return writeJUnitTestsAndCreateResult(testSuite.getTests(), Properties.JUNIT_SUFFIX);	    
	}

	private void addAssertions(TestSuiteChromosome tests) {
		AssertionGenerator asserter;
		ContractChecker.setActive(false);

		if (Properties.ASSERTION_STRATEGY == AssertionStrategy.MUTATION) {
			asserter = new SimpleMutationAssertionGenerator();
		} else if (Properties.ASSERTION_STRATEGY == AssertionStrategy.ALL) {
			asserter = new CompleteAssertionGenerator();
		} else
			asserter = new UnitAssertionGenerator();

		asserter.addAssertions(tests);

		if (Properties.FILTER_ASSERTIONS)
			asserter.filterFailingAssertions(tests);
	}

	private void writeObjectPool(TestSuiteChromosome suite) {
		if (!Properties.WRITE_POOL.isEmpty()) {
			LoggingUtils.getEvoLogger().info("* Writing sequences to pool");
			ObjectPool pool = ObjectPool.getPoolFromTestSuite(suite);
			pool.writePool(Properties.WRITE_POOL);
		}
	}

	private void getBytecodeStatistics() {
		if(Properties.TRACK_BOOLEAN_BRANCHES){
			int gradientBranchCount = ExecutionTraceImpl.gradientBranches.size() * 2;
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Gradient_Branches, gradientBranchCount);
		}
		if (Properties.TRACK_COVERED_GRADIENT_BRANCHES) {
			int coveredGradientBranchCount = ExecutionTraceImpl.gradientBranchesCoveredTrue.size()
					+ ExecutionTraceImpl.gradientBranchesCoveredFalse.size();
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Gradient_Branches_Covered, coveredGradientBranchCount);
		}
		if(Properties.BRANCH_COMPARISON_TYPES){
			int cmp_intzero=0, cmp_intint=0, cmp_refref=0, cmp_refnull=0;
			int bc_lcmp=0, bc_fcmpl=0, bc_fcmpg=0, bc_dcmpl=0, bc_dcmpg=0;
			for(Branch b:BranchPool.getAllBranches()){
				int branchOpCode = b.getInstruction().getASMNode().getOpcode();
				int previousOpcode = -2;
				if(b.getInstruction().getASMNode().getPrevious() != null)
					previousOpcode = b.getInstruction().getASMNode().getPrevious().getOpcode();
				switch(previousOpcode){
					case Opcodes.LCMP:
						bc_lcmp++;
						break;
					case Opcodes.FCMPL:
						bc_fcmpl++;
						break;
					case Opcodes.FCMPG:
						bc_fcmpg++;
						break;
					case Opcodes.DCMPL:
						bc_dcmpl++;
						break;
					case Opcodes.DCMPG:
						bc_dcmpg++;
						break;
				}
				switch(branchOpCode){
					// copmpare int with zero
					case Opcodes.IFEQ:
					case Opcodes.IFNE:
					case Opcodes.IFLT:
					case Opcodes.IFGE:
					case Opcodes.IFGT:
					case Opcodes.IFLE:
						cmp_intzero++;
					break;
					// copmpare int with int
					case Opcodes.IF_ICMPEQ:
					case Opcodes.IF_ICMPNE:
					case Opcodes.IF_ICMPLT:
					case Opcodes.IF_ICMPGE:
					case Opcodes.IF_ICMPGT:
					case Opcodes.IF_ICMPLE:
						cmp_intint++;
					break;
					// copmpare reference with reference
					case Opcodes.IF_ACMPEQ:
					case Opcodes.IF_ACMPNE:
						cmp_refref++;
					break;
					// compare reference with null
					case Opcodes.IFNULL:
					case Opcodes.IFNONNULL:
						cmp_refnull++;
					break;
					
				}
			}
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Cmp_IntZero, cmp_intzero);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Cmp_IntInt, cmp_intint);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Cmp_RefRef, cmp_refref);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Cmp_RefNull, cmp_refnull);

			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BC_lcmp, bc_lcmp);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BC_fcmpl, bc_fcmpl);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BC_fcmpg, bc_fcmpg);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BC_dcmpl, bc_dcmpl);
			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.BC_dcmpg, bc_dcmpg);
			
			
			
			RuntimeVariable[] bytecodeVarsCovered = new RuntimeVariable[] {
					RuntimeVariable.Covered_lcmp, 
					RuntimeVariable.Covered_fcmpl, 
					RuntimeVariable.Covered_fcmpg,
					RuntimeVariable.Covered_dcmpl,
					RuntimeVariable.Covered_dcmpg,
					RuntimeVariable.Covered_IntInt,
					RuntimeVariable.Covered_IntInt,
					RuntimeVariable.Covered_IntZero,
					RuntimeVariable.Covered_RefRef,
					RuntimeVariable.Covered_RefNull };
			
			for(RuntimeVariable bcvar:bytecodeVarsCovered){
				ClientServices.getInstance().getClientNode().trackOutputVariable(bcvar, 
						getBytecodeCount(bcvar, ExecutionTraceImpl.bytecodeInstructionCoveredFalse) 
						+ getBytecodeCount(bcvar, ExecutionTraceImpl.bytecodeInstructionCoveredTrue)
						);
			}

			RuntimeVariable[] bytecodeVarsReached = new RuntimeVariable[] {
					RuntimeVariable.Reached_lcmp, 
					RuntimeVariable.Reached_fcmpl, 
					RuntimeVariable.Reached_fcmpg,
					RuntimeVariable.Reached_dcmpl,
					RuntimeVariable.Reached_dcmpg,
					RuntimeVariable.Reached_IntInt,
					RuntimeVariable.Reached_IntInt,
					RuntimeVariable.Reached_IntZero,
					RuntimeVariable.Reached_RefRef,
					RuntimeVariable.Reached_RefNull };
			
			for(RuntimeVariable bcvar:bytecodeVarsReached){
				ClientServices.getInstance().getClientNode().trackOutputVariable(bcvar, 
						getBytecodeCount(bcvar, ExecutionTraceImpl.bytecodeInstructionReached) * 2);
			}
			
		}

	}

	private void printTestCriterion() {
		if (Properties.CRITERION.length > 1)
			LoggingUtils.getEvoLogger().info("* Test criteria:");
		else
			LoggingUtils.getEvoLogger().info("* Test criterion:");
	    for (int i = 0; i < Properties.CRITERION.length; i++)
	        printTestCriterion(Properties.CRITERION[i]);
	}

	private void printTestCriterion(Criterion criterion) {
		switch (criterion) {
		case WEAKMUTATION:
			LoggingUtils.getEvoLogger().info("  - Mutation testing (weak)");
			break;
        case ONLYMUTATION:
            LoggingUtils.getEvoLogger().info("  - Only Mutation testing (weak)");
            break;
		case STRONGMUTATION:
		case MUTATION:
			LoggingUtils.getEvoLogger().info("  - Mutation testing (strong)");
			break;
		case DEFUSE:
			LoggingUtils.getEvoLogger().info("  - All DU Pairs");
			break;
		case STATEMENT:
			LoggingUtils.getEvoLogger().info("  - Statement Coverage");
			break;
		case RHO:
			LoggingUtils.getEvoLogger().info("  - Rho Coverage");
			break;
		case AMBIGUITY:
			LoggingUtils.getEvoLogger().info("  - Ambiguity Coverage");
			break;
		case ALLDEFS:
			LoggingUtils.getEvoLogger().info("  - All Definitions");
			break;
		case EXCEPTION:
			LoggingUtils.getEvoLogger().info("  - Exception");
			break;
		case ONLYBRANCH:
			LoggingUtils.getEvoLogger().info("  - Only-Branch Coverage");
			break;
		case METHODTRACE:
			LoggingUtils.getEvoLogger().info("  - Method Coverage");
			break;
		case METHOD:
			LoggingUtils.getEvoLogger().info("  - Top-Level Method Coverage");
			break;
		case METHODNOEXCEPTION:
			LoggingUtils.getEvoLogger().info("  - No-Exception Top-Level Method Coverage");
			break;
		case LINE:
			LoggingUtils.getEvoLogger().info("  - Line Coverage");
			break;
		case OUTPUT:
			LoggingUtils.getEvoLogger().info("  - Method-Output Coverage");
			break;
		default:
			LoggingUtils.getEvoLogger().info("  - Branch Coverage");
		}
	}

	/**
	 * <p>
	 * getFitnessFunction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testsuite.TestSuiteFitnessFunction} object.
	 */
	public static List<TestSuiteFitnessFunction> getFitnessFunction() {
	    List<TestSuiteFitnessFunction> ffs = new ArrayList<TestSuiteFitnessFunction>();
	    for (int i = 0; i < Properties.CRITERION.length; i++) {
	        ffs.add(FitnessFunctions.getFitnessFunction(Properties.CRITERION[i]));
	    }

		return ffs;
	}


	/**
	 * Prints out all information regarding this GAs stopping conditions
	 * 
	 * So far only used for testing purposes in TestSuiteGenerator
	 */
	public void printBudget(GeneticAlgorithm<?> algorithm) {
		LoggingUtils.getEvoLogger().info("* Search Budget:");
		for (StoppingCondition sc : algorithm.getStoppingConditions())
			LoggingUtils.getEvoLogger().info("\t- " + sc.toString());
	}

	/**
	 * <p>
	 * getBudgetString
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getBudgetString(GeneticAlgorithm<?> algorithm) {
		String r = "";
		for (StoppingCondition sc : algorithm.getStoppingConditions())
			r += sc.toString() + " ";

		return r;
	}

	/**
	 * <p>
	 * getFitnessFactory
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.TestFitnessFactory} object.
	 */
	public static List<TestFitnessFactory<? extends TestFitnessFunction>> getFitnessFactories() {
	    List<TestFitnessFactory<? extends TestFitnessFunction>> goalsFactory = new ArrayList<TestFitnessFactory<? extends TestFitnessFunction>>();
	    for (int i = 0; i < Properties.CRITERION.length; i++) {
	        goalsFactory.add(FitnessFunctions.getFitnessFactory(Properties.CRITERION[i]));
	    }

		return goalsFactory;
	}

	/**
	 * <p>
	 * main
	 * </p>
	 * 
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		TestSuiteGenerator generator = new TestSuiteGenerator();
		generator.generateTestSuite();
		System.exit(0);
	}

}
