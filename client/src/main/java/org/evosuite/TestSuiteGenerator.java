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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.DSEType;
import org.evosuite.Properties.Strategy;
import org.evosuite.Properties.TestFactory;
import org.evosuite.Properties.TheReplacementFunction;
import org.evosuite.assertion.AssertionGenerator;
import org.evosuite.assertion.CompleteAssertionGenerator;
import org.evosuite.assertion.SimpleMutationAssertionGenerator;
import org.evosuite.assertion.StructuredAssertionGenerator;
import org.evosuite.assertion.UnitAssertionGenerator;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.contracts.ContractChecker;
import org.evosuite.contracts.FailingTestSet;
import org.evosuite.coverage.CoverageAnalysis;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.FitnessLogger;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.archive.ArchiveTestChromosomeFactory;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.coverage.dataflow.DefUseFitnessCalculator;
import org.evosuite.coverage.mutation.MutationTestPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.coverage.mutation.StrongMutationSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.FitnessReplacementFunction;
import org.evosuite.ga.IBranchSecondaryObjective;
import org.evosuite.ga.MinimizeSizeSecondaryObjective;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.ga.TournamentChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SteadyStateGA;
import org.evosuite.ga.metaheuristics.NSGAII;
import org.evosuite.ga.metaheuristics.OnePlusOneEA;
import org.evosuite.ga.metaheuristics.RandomSearch;
import org.evosuite.ga.metaheuristics.StandardGA;
import org.evosuite.ga.metaheuristics.MonotonicGA;
import org.evosuite.ga.operators.crossover.CoverageCrossOver;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.crossover.SinglePointCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointFixedCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointRelativeCrossOver;
import org.evosuite.ga.operators.selection.BinaryTournamentSelectionCrowdedComparison;
import org.evosuite.ga.operators.selection.FitnessProportionateSelection;
import org.evosuite.ga.operators.selection.RankSelection;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.ga.operators.selection.TournamentSelection;
import org.evosuite.ga.populationlimit.IndividualPopulationLimit;
import org.evosuite.ga.populationlimit.PopulationLimit;
import org.evosuite.ga.populationlimit.SizePopulationLimit;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxFitnessEvaluationsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.RMIStoppingCondition;
import org.evosuite.ga.stoppingconditions.SocketStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.regression.RegressionTestChromosomeFactory;
import org.evosuite.regression.RegressionTestSuiteChromosomeFactory;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.runtime.sandbox.PermissionStatistics;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.seeding.TestCaseRecycler;
import org.evosuite.seeding.factories.BIAndRITestSuiteChromosomeFactory;
import org.evosuite.seeding.factories.BIMethodSeedingTestSuiteChromosomeFactory;
import org.evosuite.seeding.factories.BIMutatedMethodSeedingTestSuiteChromosomeFactory;
import org.evosuite.seeding.factories.BestIndividualTestSuiteChromosomeFactory;
import org.evosuite.seeding.factories.RandomIndividualTestSuiteChromosomeFactory;
import org.evosuite.seeding.factories.RandomMethodSeedingTestSuiteChromosomeFactory;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.StatisticsSender;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.codegen.CaptureLogAnalyzer;
import org.evosuite.testcarver.testcase.EvoTestCaseCodeGenerator;
import org.evosuite.testcarver.testcase.TestCarvingExecutionObserver;
import org.evosuite.testcase.ConstantInliner;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseMinimizer;
import org.evosuite.testcase.TestCaseReplacementFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.UncompilableCodeException;
import org.evosuite.testcase.ValueMinimizer;
import org.evosuite.testcase.VariableReference;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTraceImpl;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.factories.AllMethodsTestChromosomeFactory;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.evosuite.testsuite.FixedSizeTestSuiteChromosomeFactory;
import org.evosuite.testsuite.MinimizeAverageLengthSecondaryObjective;
import org.evosuite.testsuite.MinimizeExceptionsSecondaryObjective;
import org.evosuite.testsuite.MinimizeMaxLengthSecondaryObjective;
import org.evosuite.testsuite.MinimizeTotalLengthSecondaryObjective;
import org.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import org.evosuite.testsuite.SerializationSuiteChromosomeFactory;
import org.evosuite.testsuite.StatementsPopulationLimit;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosomeFactory;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.testsuite.TestSuiteMinimizer;
import org.evosuite.testsuite.TestSuiteReplacementFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.ResourceController;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;

//import org.evosuite.testsuite.SearchStatistics;

/**
 * Main entry point
 * 
 * @author Gordon Fraser
 */
public class TestSuiteGenerator {

	private static Logger logger = LoggerFactory.getLogger(TestSuiteGenerator.class);

	//@Deprecated
	//private final SearchStatistics statistics = SearchStatistics.getInstance();

	/** Constant <code>zero_fitness</code> */
	public final static ZeroFitnessStoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();

	/** Constant <code>global_time</code> */
	public static final GlobalTimeStoppingCondition global_time = new GlobalTimeStoppingCondition();

	/** Constant <code>stopping_condition</code> */
	public static StoppingCondition stopping_condition;

	private final ProgressMonitor progressMonitor = new ProgressMonitor();

	/*
	 * FIXME: a field is needed for "ga" to avoid a large re-factoring of the
	 * code. "ga" is given as input to many functions, but there are
	 * side-effects like "ga = setup()" that are not propagated to the "ga"
	 * reference of the top-function caller
	 */
	private GeneticAlgorithm ga;

	/**
	 * Generate a test suite for the target class
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public List<TestGenerationResult> generateTestSuite() {

		LoggingUtils.getEvoLogger().info("* Analyzing classpath: ");

		ClientServices.getInstance().getClientNode().changeState(ClientState.INITIALIZATION);

		TestCaseExecutor.initExecutor();
		Sandbox.goingToExecuteSUTCode();
        TestGenerationContext.getInstance().goingToExecuteSUTCode();
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
			return new ArrayList<TestGenerationResult>(Arrays.asList(TestGenerationResultBuilder.buildErrorResult(
			          e.getMessage() != null ? e.getMessage() : e.toString())));
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
		    return new ArrayList<TestGenerationResult>(Arrays.asList(TestGenerationResultBuilder.buildErrorResult("Could not load target class")));

		List<TestSuiteChromosome> testCases = generateTests();
        ClientServices.getInstance().getClientNode().publishPermissionStatistics();
		PermissionStatistics.getInstance().printStatistics(LoggingUtils.getEvoLogger());
		
		// progressMonitor.setCurrentPhase("Writing JUnit test cases");
		List<TestGenerationResult> results = writeJUnitTestsAndCreateResult(testCases);

		TestCaseExecutor.pullDown();
		/*
		 * TODO: when we will have several processes running in parallel, we ll
		 * need to handle the gathering of the statistics.
		 */
		ClientServices.getInstance().getClientNode().changeState(ClientState.WRITING_STATISTICS);
		/*
		if (Properties.OLD_STATISTICS) {
			statistics.writeReport();
			if (!Properties.NEW_STATISTICS)
				statistics.writeStatistics();
		}
		 */
		
		LoggingUtils.getEvoLogger().info("* Done!");
		LoggingUtils.getEvoLogger().info("");

		return results;
	}

	/*
	 * return reference of the GA used in the most recent generateTestSuite()
	 */
	/**
	 * <p>
	 * getEmployedGeneticAlgorithm
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.metaheuristics.GeneticAlgorithm} object.
	 */
	public GeneticAlgorithm getEmployedGeneticAlgorithm() {
		return ga;
	}
	
	private int getBytecodeCount(RuntimeVariable v, Map<RuntimeVariable,Set<Integer>> m){
		Set<Integer> branchSet = m.get(v);
		return (branchSet==null) ? 0 : branchSet.size();
	}

	private List<TestSuiteChromosome> generateTests() {
	    List<TestSuiteChromosome> tests = new ArrayList<TestSuiteChromosome>();
		// Make sure target class is loaded at this point
		TestCluster.getInstance();

		if (TestCluster.getInstance().getNumTestCalls() == 0) {
			LoggingUtils.getEvoLogger().info("* Found no testable methods in the target class "
			                                         + Properties.TARGET_CLASS);
			return new ArrayList<TestSuiteChromosome>();
		}

		ContractChecker checker = null;
		if (Properties.CHECK_CONTRACTS) {
			checker = new ContractChecker();
			TestCaseExecutor.getInstance().addObserver(checker);
		}

		if (Properties.STRATEGY == Strategy.EVOSUITE)
		    tests.addAll(generateWholeSuite());
		else if (Properties.STRATEGY == Strategy.RANDOM)
		    tests.add(generateRandomTests());
		else if (Properties.STRATEGY == Strategy.RANDOM_FIXED)
		    tests.add(generateFixedRandomTests());
		else
		    tests.add(generateIndividualTests());

		if (Properties.CHECK_CONTRACTS) {
			TestCaseExecutor.getInstance().removeObserver(checker);
		}
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

		StatisticsSender.executedAndThenSendIndividualToMaster(tests.get(0)); // FIXME: can we pass the list of testsuitechromosomes?
		
        ClientServices.getInstance().getClientNode().publishPermissionStatistics();
		
		LoggingUtils.getEvoLogger().info("* Time spent executing tests: "
		                                         + TestCaseExecutor.timeExecuted + "ms");

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)) {
			if (Properties.ENABLE_ALTERNATIVE_FITNESS_CALCULATION)
				LoggingUtils.getEvoLogger().info("* Time spent calculating alternative fitness: "
				                                         + DefUseFitnessCalculator.alternativeTime
				                                         + "ms");
			LoggingUtils.getEvoLogger().info("* Time spent calculating single fitnesses: "
			                                         + DefUseCoverageTestFitness.singleFitnessTime
			                                         + "ms");
		}

		if (Properties.ASSERTIONS) {
			LoggingUtils.getEvoLogger().info("* Generating assertions");
			// progressMonitor.setCurrentPhase("Generating assertions");
			ClientServices.getInstance().getClientNode().changeState(ClientState.ASSERTION_GENERATION);
			addAssertions(tests);
			StatisticsSender.sendIndividualToMaster(tests.get(0)); // FIXME: can we pass the list of testsuitechromosomes?
		}

		if (Properties.CHECK_CONTRACTS) {
			for (TestSuiteChromosome test : tests) {
    		    for (TestCase failing_test : FailingTestSet.getFailingTests()) {
    				test.addTest(failing_test);
    			}
		    }
			FailingTestSet.sendStatistics();
		}

		LoggingUtils.getEvoLogger().info("* Compiling and checking tests");
		int i = 0;
		for (TestSuiteChromosome test : tests) {
    		//List<TestCase> testCases = tests.getTests();
		    List<TestCase> testCases = test.getTests();

    		if (Properties.JUNIT_TESTS && Properties.JUNIT_CHECK) {
    			if (JUnitAnalyzer.isJavaCompilerAvailable()) {
    				if(tests.size() > 1)
    					LoggingUtils.getEvoLogger().info("  - Compiling and checking test " + i);

    				JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);

    				boolean unstable = false;
    				int numUnstable = 0;
    				numUnstable = JUnitAnalyzer.handleTestsThatAreUnstable(testCases); 
    				unstable = numUnstable > 0;

    				//second passage on reverse order, this is to spot dependencies among tests
    				if (testCases.size() > 1) {
    					Collections.reverse(testCases);
    					numUnstable += JUnitAnalyzer.handleTestsThatAreUnstable(testCases); 
    					unstable = (numUnstable > 0) || unstable;
    				}

    				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.HadUnstableTests,unstable);
    				ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.NumUnstableTests,numUnstable);
    			}
    			else {
    				logger.error("No Java compiler is available. Are you running with the JDK?");
    			}			
    		}

    		test.clearTests();
    		for (TestCase testCase : testCases)
    		    test.addTest(testCase);

    		i++;
		}

		writeObjectPool(tests);

		/*
		 * PUTGeneralizer generalizer = new PUTGeneralizer(); for (TestCase test
		 * : tests) { generalizer.generalize(test); // ParameterizedTestCase put
		 * = new ParameterizedTestCase(test); }
		 */

		return tests;
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
			suite.writeTestSuite(name + suffix, testDir);
		}
		return TestGenerationResultBuilder.buildSuccessResult();
	}

	/**
	 * 
	 * @param tests
	 *            the test cases which should be written to file
	 */
	public static List<TestGenerationResult> writeJUnitTestsAndCreateResult(List<TestSuiteChromosome> tests) {
	    List<TestGenerationResult> results = new ArrayList<TestGenerationResult>();
	    if(tests.size() > 1) {
	    	for (int i = 0; i < tests.size(); i++)
	    		results.add(writeJUnitTestsAndCreateResult(tests.get(i).getTests(), "_"+i+"_" + Properties.JUNIT_SUFFIX  ));
	    } else {
		    if (tests.size() == 1 && tests.get(0).getTests().size() > 0)
		    	results.add(writeJUnitTestsAndCreateResult(tests.get(0).getTests(), Properties.JUNIT_SUFFIX  ));
	    }
	    return results;
	}

	private void addAssertions(List<TestSuiteChromosome> l_tests) {
	    for (TestSuiteChromosome tests : l_tests)
	        addAssertions(tests);
	}

	private void addAssertions(TestSuiteChromosome tests) {
		AssertionGenerator asserter;
		ContractChecker.setActive(false);

		if (Properties.ASSERTION_STRATEGY == AssertionStrategy.MUTATION) {
			asserter = new SimpleMutationAssertionGenerator();
		} else if (Properties.ASSERTION_STRATEGY == AssertionStrategy.STRUCTURED) {
			asserter = new StructuredAssertionGenerator();
		} else if (Properties.ASSERTION_STRATEGY == AssertionStrategy.ALL) {
			asserter = new CompleteAssertionGenerator();
		} else
			asserter = new UnitAssertionGenerator();

		asserter.addAssertions(tests);

		if (Properties.FILTER_ASSERTIONS)
			asserter.filterFailingAssertions(tests);
	}

	private void writeObjectPool(List<TestSuiteChromosome> suites) {
	    for (TestSuiteChromosome suite : suites)
	        writeObjectPool(suite);
	}

	private void writeObjectPool(TestSuiteChromosome suite) {
		if (!Properties.WRITE_POOL.isEmpty()) {
			LoggingUtils.getEvoLogger().info("* Writing sequences to pool");
			ObjectPool pool = ObjectPool.getPoolFromTestSuite(suite);
			pool.writePool(Properties.WRITE_POOL);
		}
	}

	/**
	 * Executes all given test cases and carves their execution. Note that the
	 * accessed classes of a TestCase are considered as classes to be observed
	 * (if they do not represent primitive types).
	 * 
	 * @param testsToBeCarved
	 *            list of test cases
	 * @return list of test cases carved from the execution of the given test
	 *         cases
	 */
	private List<TestCase> carveTests(List<TestCase> testsToBeCarved) {
		final ArrayList<TestCase> result = new ArrayList<TestCase>(testsToBeCarved.size());
		final TestCaseExecutor executor = TestCaseExecutor.getInstance();
		final TestCarvingExecutionObserver execObserver = new TestCarvingExecutionObserver();
		executor.addObserver(execObserver);

		final HashSet<Class<?>> allAccessedClasses = new HashSet<Class<?>>();
		final Logger logger = LoggingUtils.getEvoLogger();

		// variables needed in loop
		CaptureLog log;
		TestCase carvedTestCase;

		final CaptureLogAnalyzer analyzer = new CaptureLogAnalyzer();
		final EvoTestCaseCodeGenerator codeGen = new EvoTestCaseCodeGenerator();

		for (TestCase t : testsToBeCarved) {
			// collect all accessed classes ( = classes to be observed)
			allAccessedClasses.addAll(t.getAccessedClasses());

			// start capture before genetic algorithm is applied so that all
			// interactions can be captured
			Capturer.startCapture();

			// execute test case
			executor.execute(t);

			// stop capture after best individual has been determined and obtain
			// corresponding capture log
			log = Capturer.stopCapture();

			// ----- filter accessed classes

			// remove all classes representing primitive types
			Class<?> c;
			final Iterator<Class<?>> iter = allAccessedClasses.iterator();
			while (iter.hasNext()) {
				c = iter.next();
				if (c.isPrimitive()) {
					iter.remove();
				}
			}

			if (allAccessedClasses.isEmpty()) {
				logger.warn("There are no classes which can be observed in test\n{}\n --> no test carving performed",
				            t);
				Capturer.clear();
				continue;
			}

			// ----- generate code out of capture log

			logger.debug("Evosuite Test:\n{}", t);

			// generate carved test with the currently captured log and
			// allAccessedlasses as classes to be observed

			analyzer.analyze(log,
			                 codeGen,
			                 allAccessedClasses.toArray(new Class[allAccessedClasses.size()]));
			carvedTestCase = codeGen.getCode();
			codeGen.clear();

			logger.info("Carved Test:\n{}", carvedTestCase);
			result.add(carvedTestCase);

			// reuse helper data structures
			allAccessedClasses.clear();

			// clear Capturer content to save memory
			Capturer.clear();
		}

		executor.removeObserver(execObserver);

		return result;
	}

	/**
	 * Use the EvoSuite approach (Whole test suite generation)
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<TestSuiteChromosome> generateWholeSuite() {
		// Set up search algorithm
		if (ga == null || ga.getAge() == 0) {
			LoggingUtils.getEvoLogger().info("* Setting up search algorithm for whole suite generation");
			ga = setup();
		} else {
			LoggingUtils.getEvoLogger().info("* Resuming search algorithm at generation "
			                                         + ga.getAge()
			                                         + " for whole suite generation");
		}
		if(Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD)
			TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(ga);
		
		long start_time = System.currentTimeMillis() / 1000;

		// What's the search target
		List<TestSuiteFitnessFunction> fitness_functions = getFitnessFunction();
		ga.addFitnessFunctions(fitness_functions);

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION)) {
		    for (FitnessFunction<?> fitness_function : fitness_functions)
		        ga.addListener((StrongMutationSuiteFitness) fitness_function);
		}

		//ga.setChromosomeFactory(getChromosomeFactory(fitness_function));
		ga.setChromosomeFactory(getChromosomeFactory(fitness_functions.get(0))); // FIXME: just one fitness function?
		// if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
		ga.addListener(progressMonitor); // FIXME progressMonitor may cause
		// client hang if EvoSuite is
		// executed with -prefix!

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
//		        || ArrayUtil.contains(Properties.CRITERION, Criterion.IBRANCH)
//		        || ArrayUtil.contains(Properties.CRITERION, Criterion.ARCHIVEIBRANCH)  
//		        || ArrayUtil.contains(Properties.CRITERION, Criterion.CBRANCH) 
		        || ArrayUtil.contains(Properties.CRITERION, Criterion.ALLDEFS)
		        || ArrayUtil.contains(Properties.CRITERION, Criterion.STATEMENT)
		        || ArrayUtil.contains(Properties.CRITERION, Criterion.RHO)
		        || ArrayUtil.contains(Properties.CRITERION, Criterion.AMBIGUITY))
			ExecutionTracer.enableTraceCalls();

		// TODO: why it was only if "analyzing"???
		// if (analyzing)
		ga.resetStoppingConditions();

		List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactory();
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
		if(goalFactories.size() == 1) {
			TestFitnessFactory<? extends TestFitnessFunction> factory = goalFactories.iterator().next();
			LoggingUtils.getEvoLogger().info("* Total number of test goals: {}", factory.getCoverageGoals().size());
			goals.addAll(factory.getCoverageGoals());
		} else {
			LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
			for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
				goals.addAll(goalFactory.getCoverageGoals());
				LoggingUtils.getEvoLogger().info("  - " + goalFactory.getClass().getSimpleName().replace("CoverageFactory", "")
						+ " " + goalFactory.getCoverageGoals().size());
			}
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals,
		                                                                 goals.size());
		List<TestSuiteChromosome> bestSuites = new ArrayList<TestSuiteChromosome>();
        /*
         * Proceed with search if CRITERION=EXCEPTION, even if goals is empty
         */
		if (!(Properties.STOP_ZERO && goals.isEmpty()) || ArrayUtil.contains(Properties.CRITERION, Criterion.EXCEPTION)) {
			// Perform search
			LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed() );
			LoggingUtils.getEvoLogger().info("* Starting evolution");
			ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

			ga.generateSolution();
			bestSuites = (List<TestSuiteChromosome>) ga.getBestIndividuals();
			if (bestSuites.isEmpty()) {
				LoggingUtils.getEvoLogger().warn("Could not find any suitable chromosome");
				return bestSuites;
			}
		} else {
			bestSuites.add(new TestSuiteChromosome());
			//statistics.searchStarted(ga);
			//statistics.searchFinished(ga);
			zero_fitness.setFinished();
			for (TestSuiteChromosome best : bestSuites) {
                for (FitnessFunction ff : best.getFitnesses().keySet()) {
                    best.setCoverage(ff, 1.0);
                }
            }

		}

		long end_time = System.currentTimeMillis() / 1000;

		// Newline after progress bar
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");
		String text = " statements, best individual has fitness: ";
		if(bestSuites.size() > 1) {
			text = " statements, best individuals have fitness: ";			
		}
		LoggingUtils.getEvoLogger().info("* Search finished after "
		                                         + (end_time - start_time)
		                                         + "s and "
		                                         + ga.getAge()
		                                         + " generations, "
		                                         + MaxStatementsStoppingCondition.getNumExecutedStatements()
		                                         + text
		                                         + bestSuites.get(0).getFitness());
        // Search is finished, send statistics
        sendExecutionStatistics();

		// TODO also consider time for test carving in end_time?
		if (Properties.TEST_CARVING) {
			/*
			 * If the SUT is class X,
			 * then we might get tests that call methods from Y which indirectly call X.
			 * A unit test that only calls Y is useless
			 * but one could use the test carver to produce a test on X out of it.
			 */

		    for (TestSuiteChromosome best : bestSuites) {
		        // execute all tests to carve them
		        final List<TestCase> carvedTests = this.carveTests(best.getTests());

		        // replace chromosome test cases with carved tests
		        best.clearTests();
		        for (TestCase t : carvedTests)
		            best.addTest(t);
		    }
		}

		if (Properties.TEST_FACTORY == TestFactory.SERIALIZATION) {
		    SerializationSuiteChromosomeFactory.saveTests(bestSuites);
        }

		if (Properties.MINIMIZE_VALUES && 
		                Properties.CRITERION.length == 1) {
		    double fitness = bestSuites.get(0).getFitness();

			ClientServices.getInstance().getClientNode().changeState(ClientState.MINIMIZING_VALUES);
			LoggingUtils.getEvoLogger().info("* Minimizing values");
			ValueMinimizer minimizer = new ValueMinimizer();
			minimizer.minimize(bestSuites.get(0), (TestSuiteFitnessFunction) fitness_functions.get(0));
			assert (fitness >= bestSuites.get(0).getFitness());
		}
		// progressMonitor.updateStatus(33);

		// progressMonitor.updateStatus(66);

		if (Properties.INLINE) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.INLINING);
			ConstantInliner inliner = new ConstantInliner();
			// progressMonitor.setCurrentPhase("Inlining constants");

			for (TestSuiteChromosome best : bestSuites) {
                Map<FitnessFunction<?>, Double> fitnesses = best.getFitnesses();

                inliner.inline(best);
                for (FitnessFunction<?> fitness : fitnesses.keySet())
                    assert (fitnesses.get(fitness) >= best.getFitness(fitness));
			}
		}

		if (Properties.MINIMIZE) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.MINIMIZATION);
			// progressMonitor.setCurrentPhase("Minimizing test cases");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(goalFactories);
			if (Properties.CRITERION.length == 1 || Properties.COMPOSITIONAL_FITNESS) {
				LoggingUtils.getEvoLogger().info("* Minimizing test suite");
			    minimizer.minimize(bestSuites.get(0), true);
			}
			else {
				LoggingUtils.getEvoLogger().info("* Minimizing test suites");
			    for (TestSuiteChromosome best : bestSuites)
			        minimizer.minimize(best, false);
			}
		} else {
		    ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Result_Size, bestSuites.get(0).size());
		    ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Minimized_Size, bestSuites.get(0).size());
		    ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Result_Length, bestSuites.get(0).totalLengthOfTestCases());
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Minimized_Length, bestSuites.get(0).totalLengthOfTestCases());
		}

		if (Properties.COVERAGE) {
		    for (Properties.Criterion pc : Properties.CRITERION)
		        CoverageAnalysis.analyzeCoverage(bestSuites.get(0), pc); // FIXME: can we send all bestSuites?
		}

		// progressMonitor.updateStatus(99);

		int number_of_test_cases = 0;
        int totalLengthOfTestCases = 0;
        double coverage = 0.0;

        for (TestSuiteChromosome tsc : bestSuites) {
            number_of_test_cases += tsc.size();
            totalLengthOfTestCases += tsc.totalLengthOfTestCases();
            coverage += tsc.getCoverage();
        }
        coverage = coverage / ((double)bestSuites.size());

        if (ArrayUtil.contains(Properties.CRITERION, Criterion.MUTATION)
                || ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION)) {
		    //SearchStatistics.getInstance().mutationScore(coverage);
		}

		StatisticsSender.executedAndThenSendIndividualToMaster(bestSuites.get(0)); // FIXME: can we send all bestSuites?
		//statistics.iteration(ga);
		//statistics.minimized(bestSuites.get(0)); // FIXME: can we send all bestSuites?
		LoggingUtils.getEvoLogger().info("* Generated " + number_of_test_cases
		                                         + " tests with total length "
		                                         + totalLengthOfTestCases);

		// TODO: In the end we will only need one analysis technique
		if (!Properties.ANALYSIS_CRITERIA.isEmpty()) {
		    //SearchStatistics.getInstance().addCoverage(Properties.CRITERION.toString(), coverage);
		    CoverageAnalysis.analyzeCriteria(bestSuites.get(0), Properties.ANALYSIS_CRITERIA); // FIXME: can we send all bestSuites?
		}
        if (Properties.COMPOSITIONAL_FITNESS)
            LoggingUtils.getEvoLogger().info("* Resulting test suite's coverage: "
                    + NumberFormat.getPercentInstance().format(coverage) + " (average coverage for all fitness functions)");
        else
            LoggingUtils.getEvoLogger().info("* Resulting test suite's coverage: "
                    + NumberFormat.getPercentInstance().format(coverage));

		ga.printBudget();
		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
		        && Properties.ANALYSIS_CRITERIA.isEmpty())
			DefUseCoverageSuiteFitness.printCoverage();

		if (Properties.LOCAL_SEARCH_DSE != DSEType.OFF) {
			DSEStats.printStatistics();
		}

		if (Properties.FILTER_SANDBOX_TESTS) {
		    for (TestSuiteChromosome best : bestSuites) {
    			for (TestChromosome test : best.getTestChromosomes()) {
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
		}

		return bestSuites;
	}

    private void sendExecutionStatistics() {
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Statements_Executed, MaxStatementsStoppingCondition.getNumExecutedStatements());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Tests_Executed, MaxTestsStoppingCondition.getNumExecutedTests());
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
	 * <p>
	 * getFitnessFactory
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.TestFitnessFactory} object.
	 */
	public static List<TestFitnessFactory<? extends TestFitnessFunction>> getFitnessFactory() {
	    List<TestFitnessFactory<? extends TestFitnessFunction>> goalsFactory = new ArrayList<TestFitnessFactory<? extends TestFitnessFunction>>();
	    for (int i = 0; i < Properties.CRITERION.length; i++) {
	        goalsFactory.add(FitnessFunctions.getFitnessFactory(Properties.CRITERION[i]));
	    }

		return goalsFactory;
	}



	/**
	 * Cover the easy targets first with a set of random tests, so that the
	 * actual search can focus on the non-trivial test goals
	 * 
	 * @return
	 */
	private TestSuiteChromosome bootstrapRandomSuite(FitnessFunction<?> fitness,
	        TestFitnessFactory<?> goals) {

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
	            || ArrayUtil.contains(Properties.CRITERION, Criterion.ALLDEFS)) {
			LoggingUtils.getEvoLogger().info("* Disabled random bootstraping for dataflow criterion");
			Properties.RANDOM_TESTS = 0;
		}

		if (Properties.RANDOM_TESTS > 0) {
			LoggingUtils.getEvoLogger().info("* Bootstrapping initial random test suite");
		} // else
		  // LoggingUtils.getEvoLogger().info("* Bootstrapping initial random test suite disabled!");

		FixedSizeTestSuiteChromosomeFactory factory = new FixedSizeTestSuiteChromosomeFactory(
		        Properties.RANDOM_TESTS);

		TestSuiteChromosome suite = factory.getChromosome();
		if (Properties.RANDOM_TESTS > 0) {
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(goals);
			minimizer.minimize(suite, true);
			LoggingUtils.getEvoLogger().info("* Initial test suite contains "
			                                         + suite.size() + " tests");
		}

		return suite;
	}

	private boolean isFinished(TestSuiteChromosome chromosome) {
		if (stopping_condition.isFinished())
			return true;

		if (Properties.STOP_ZERO) {
			if (chromosome.getFitness() == 0.0)
				return true;
		}

		if (!(stopping_condition instanceof MaxTimeStoppingCondition)) {
			if (global_time.isFinished())
				return true;
		}

		return false;
	}

	/**
	 * Generate one random test at a time and check if adding it improves
	 * fitness (1+1)RT
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public TestSuiteChromosome generateFixedRandomTests() {
		LoggingUtils.getEvoLogger().info("* Generating fixed number of random tests");
		RandomLengthTestFactory factory = new RandomLengthTestFactory();
		TestSuiteChromosome suite = new TestSuiteChromosome();
		// The GA is not actually used, except to provide the same statistics as
		// during search
		GeneticAlgorithm<TestSuiteChromosome> suiteGA = getGeneticAlgorithm(new TestSuiteChromosomeFactory());
		// GeneticAlgorithm suiteGA = setup();
		stopping_condition = getStoppingCondition();
		//statistics.searchStarted(suiteGA);

		for (int i = 0; i < Properties.NUM_RANDOM_TESTS; i++) {
			if (suiteGA.isFinished())
				break;
			logger.info("Current test: " + i + "/" + Properties.NUM_RANDOM_TESTS);
			TestChromosome test = factory.getChromosome();
			ExecutionResult result = TestCaseExecutor.runTest(test.getTestCase());
			Integer pos = result.getFirstPositionOfThrownException();
			if (pos != null) {
				if (result.getExceptionThrownAtPosition(pos) instanceof CodeUnderTestException
				        || result.getExceptionThrownAtPosition(pos) instanceof UncompilableCodeException
				        || result.getExceptionThrownAtPosition(pos) instanceof TestCaseExecutor.TimeoutExceeded) {
					continue;
					// test.getTestCase().chop(pos);
				} else {
					test.getTestCase().chop(pos + 1);
				}
				test.setChanged(true);
			} else {
				test.setLastExecutionResult(result);
			}
			suite.addTest(test);
		}
        // Search is finished, send statistics
        sendExecutionStatistics();

		suiteGA.getPopulation().add(suite);
		//statistics.searchFinished(suiteGA);
		suiteGA.printBudget();
		//statistics.minimized(suiteGA.getBestIndividual());

		return suite;
	}

	/**
	 * Generate one random test at a time and check if adding it improves
	 * fitness (1+1)RT
	 * 
	 * @return a {@link java.util.List} object.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TestSuiteChromosome generateRandomTests() {
		LoggingUtils.getEvoLogger().info("* Using random test generation");

		List<TestSuiteFitnessFunction> fitness_functions = getFitnessFunction();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		for (TestSuiteFitnessFunction fitness_function : fitness_functions)
            suite.addFitness(fitness_function);

		List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactory();
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
		LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
		for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
            goals.addAll(goalFactory.getCoverageGoals());
            LoggingUtils.getEvoLogger().info("  - " + goalFactory.getClass().getSimpleName().replace("CoverageFactory", "")
                    + " " + goalFactory.getCoverageGoals().size());
        }
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals,
		                                                                 goals.size());

		// The GA is not actually used, except to provide the same statistics as
		// during search
		GeneticAlgorithm suiteGA = getGeneticAlgorithm(new TestSuiteChromosomeFactory());
		// GeneticAlgorithm suiteGA = setup();
		suiteGA.addFitnessFunctions(fitness_functions);
		//statistics.searchStarted(suiteGA);

		ga = suiteGA;

		RandomLengthTestFactory factory = new RandomLengthTestFactory();

		// TODO: Shutdown hook?

		stopping_condition = getStoppingCondition();
		for (FitnessFunction<?> fitness_function : fitness_functions)
		    ((TestSuiteFitnessFunction)fitness_function).getFitness(suite);
		ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

		while (!isFinished(suite)) {
			TestChromosome test = factory.getChromosome();
			TestSuiteChromosome clone = suite.clone();
			clone.addTest(test);
			for (FitnessFunction<?> fitness_function : fitness_functions) {
                ((TestSuiteFitnessFunction)fitness_function).getFitness(clone);
                logger.debug("Old fitness: {}, new fitness: {}", suite.getFitness(),
			             clone.getFitness());
            }
			if (clone.compareTo(suite) < 0) {
				suite = clone;
				StatisticsSender.executedAndThenSendIndividualToMaster(clone);				
			}
		}
		suiteGA.getPopulation().add(suite);
		//statistics.searchFinished(suiteGA);
		suiteGA.printBudget();

		if (Properties.MINIMIZE && Properties.CRITERION.length == 1) {
			LoggingUtils.getEvoLogger().info("* Minimizing result");
			ClientServices.getInstance().getClientNode().changeState(ClientState.MINIMIZATION);
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(goalFactories);
			minimizer.minimize((TestSuiteChromosome) suiteGA.getBestIndividual(), true);
		}
		//statistics.minimized(suiteGA.getBestIndividual()); // FIXME: only best individual or ALL best individuals?

		// In the GA, these statistics are sent via the SearchListener when notified about the GA completing
        // Search is finished, send statistics
        sendExecutionStatistics();

        // TODO: Check this: Fitness_Evaluations = getNumExecutedTests?
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Fitness_Evaluations, MaxTestsStoppingCondition.getNumExecutedTests());

        if (Properties.COVERAGE) {
            for (Properties.Criterion pc : Properties.CRITERION)
                CoverageAnalysis.analyzeCoverage(suite, pc);
        }

		// TODO: In the end we will only need one analysis technique
		if (!Properties.ANALYSIS_CRITERIA.isEmpty()) {
			CoverageAnalysis.analyzeCriteria(suite, Properties.ANALYSIS_CRITERIA);
		}

		return suite;
	}

	/**
	 * Use the OneBranch approach: The budget for the search is split equally
	 * among all test goals, and then search is attempted for each goal. If a
	 * goal is covered, the remaining budget will be used in the next iteration.
	 * 
	 * @return a {@link java.util.List} object.
	 */
	@SuppressWarnings("unchecked")
	public TestSuiteChromosome generateIndividualTests() {
		// Set up search algorithm
		LoggingUtils.getEvoLogger().info("* Setting up search algorithm for individual test generation");
		ExecutionTracer.enableTraceCalls();
		if (ga == null)
			ga = setup();

		GeneticAlgorithm suiteGA = getGeneticAlgorithm(new TestSuiteChromosomeFactory());
		List<TestSuiteFitnessFunction> fitness_functions = getFitnessFunction();
		suiteGA.addFitnessFunctions(fitness_functions);

		long start_time = System.currentTimeMillis() / 1000;
		FitnessLogger fitnessLogger = new FitnessLogger();
		if (Properties.LOG_GOALS) {
			ga.addListener(fitnessLogger);
		}

		// Get list of goals
        List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactory();
		long goalComputationStart = System.currentTimeMillis();
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
		LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
        for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
            goals.addAll(goalFactory.getCoverageGoals());
            LoggingUtils.getEvoLogger().info("  - " + goalFactory.getClass().getSimpleName().replace("CoverageFactory", "")
                    + " " + goalFactory.getCoverageGoals().size());
        }

		if (AbstractFitnessFactory.goalComputationTime != 0l)
			AbstractFitnessFactory.goalComputationTime = System.currentTimeMillis()
			        - goalComputationStart;
		// Need to shuffle goals because the order may make a difference
		if (Properties.SHUFFLE_GOALS) {
			// LoggingUtils.getEvoLogger().info("* Shuffling goals");
			Randomness.shuffle(goals);
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals,
		                                                                 goals.size());

		LoggingUtils.getEvoLogger().info("* Total number of test goals: " + goals.size());

		// Bootstrap with random testing to cover easy goals
		//statistics.searchStarted(suiteGA);

		TestSuiteChromosome suite = bootstrapRandomSuite(fitness_functions.get(0), goalFactories.get(0)); // FIXME: just one fitness and one factory?!
		suiteGA.getPopulation().add(suite);
		Set<Integer> covered = new HashSet<Integer>();
		int covered_goals = 0;
		int num = 0;

		for (TestFitnessFunction fitness_function : goals) {
			if (fitness_function.isCovered(suite.getTests())) {
				covered.add(num);
				covered_goals++;
			}
			num++;
		}
		if (covered_goals > 0)
			LoggingUtils.getEvoLogger().info("* Random bootstrapping covered "
			                                         + covered_goals + " test goals");

		int total_goals = goals.size();
		if (covered_goals == total_goals)
			zero_fitness.setFinished();

		int current_budget = 0;

		long total_budget = Properties.SEARCH_BUDGET;
		LoggingUtils.getEvoLogger().info("* Budget: "
		                                         + NumberFormat.getIntegerInstance().format(total_budget));

		while (current_budget < total_budget && covered_goals < total_goals
		        && !global_time.isFinished() && !ShutdownTestWriter.isInterrupted()) {
			long budget = (total_budget - current_budget) / (total_goals - covered_goals);
			logger.info("Budget: " + budget + "/" + (total_budget - current_budget));
			logger.info("Statements: " + current_budget + "/" + total_budget);
			logger.info("Goals covered: " + covered_goals + "/" + total_goals);
			stopping_condition.setLimit(budget);

			num = 0;
			// int num_statements = 0;
			// //MaxStatementsStoppingCondition.getNumExecutedStatements();
			for (TestFitnessFunction fitnessFunction : goals) {

				if (covered.contains(num)) {
					num++;
					continue;
				}

				ga.resetStoppingConditions();
				ga.clearPopulation();
				ga.setChromosomeFactory(getChromosomeFactory(fitnessFunction));

				if (Properties.PRINT_CURRENT_GOALS)
					LoggingUtils.getEvoLogger().info("* Searching for goal " + num + ": "
					                                         + fitnessFunction.toString());
				logger.info("Goal " + num + "/" + (total_goals - covered_goals) + ": "
				        + fitnessFunction);

				if (ShutdownTestWriter.isInterrupted()) {
					num++;
					continue;
				}
				if (global_time.isFinished()) {
					LoggingUtils.getEvoLogger().info("Skipping goal because time is up");
					num++;
					continue;
				}

				// FitnessFunction fitness_function = new
				ga.addFitnessFunction(fitnessFunction);

				// Perform search
				logger.info("Starting evolution for goal " + fitnessFunction);
				ga.generateSolution();

				if (ga.getBestIndividual().getFitness() == 0.0) {
					if (Properties.PRINT_COVERED_GOALS)
						LoggingUtils.getEvoLogger().info("* Covered!"); // : " +
					// fitness_function.toString());
					logger.info("Found solution, adding to test suite at "
					        + MaxStatementsStoppingCondition.getNumExecutedStatements());
					TestChromosome best = (TestChromosome) ga.getBestIndividual();
					if (Properties.MINIMIZE && Properties.CRITERION.length == 1) {
						ClientServices.getInstance().getClientNode().changeState(ClientState.MINIMIZATION);
						TestCaseMinimizer minimizer = new TestCaseMinimizer(
						        fitnessFunction);
						minimizer.minimize(best);
					}
					best.getTestCase().addCoveredGoal(fitnessFunction);
					suite.addTest(best);
					suiteGA.getPopulation().set(0, suite);
					// Calculate and keep track of overall fitness
					for (TestSuiteFitnessFunction fitness_function : fitness_functions)
					    fitness_function.getFitness(suite);

					covered_goals++;
					covered.add(num);

					// experiment:
					if (Properties.SKIP_COVERED) {
						Set<Integer> additional_covered_nums = getAdditionallyCoveredGoals(goals,
						                                                                   covered,
						                                                                   best);
						// LoggingUtils.getEvoLogger().info("Additionally covered: "+additional_covered_nums.size());
						for (Integer covered_num : additional_covered_nums) {
							covered_goals++;
							covered.add(covered_num);
						}
					}

				} else {
					logger.info("Found no solution for " + fitnessFunction + " at "
					        + MaxStatementsStoppingCondition.getNumExecutedStatements());
				}

				//statistics.iteration(suiteGA);
				if (Properties.REUSE_BUDGET)
					current_budget += stopping_condition.getCurrentValue();
				else
					current_budget += budget + 1;

				// print console progress bar
				if (Properties.SHOW_PROGRESS
				        && !(Properties.PRINT_COVERED_GOALS || Properties.PRINT_CURRENT_GOALS)) {
					double percent = current_budget;
					percent = percent / total_budget * 100;

					double coverage = covered_goals;
					coverage = coverage / total_goals * 100;

					// ConsoleProgressBar.printProgressBar((int) percent, (int)
					// coverage);
				}

				if (current_budget > total_budget)
					break;
				num++;

				// break;
			}
		}
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");

		// for testing purposes
		if (global_time.isFinished())
			LoggingUtils.getEvoLogger().info("! Timeout reached");
		if (current_budget >= total_budget)
			LoggingUtils.getEvoLogger().info("! Budget exceeded");
		else
			LoggingUtils.getEvoLogger().info("* Remaining budget: "
			                                         + (total_budget - current_budget));

		stopping_condition.setLimit(Properties.SEARCH_BUDGET);
		stopping_condition.forceCurrentValue(current_budget);
		suiteGA.setStoppingCondition(stopping_condition);
		suiteGA.addStoppingCondition(global_time);
		suiteGA.printBudget();

		int c = 0;
		int uncovered_goals = total_goals - covered_goals;
		if (uncovered_goals < 10)
			for (TestFitnessFunction goal : goals) {
				if (!covered.contains(c)) {
					LoggingUtils.getEvoLogger().info("! Unable to cover goal " + c + " "
					                                         + goal.toString());
				}
				c++;
			}
		else
			LoggingUtils.getEvoLogger().info("! #Goals that were not covered: "
			                                         + uncovered_goals);

		//statistics.searchFinished(suiteGA);
		long end_time = System.currentTimeMillis() / 1000;
		LoggingUtils.getEvoLogger().info("* Search finished after "
		                                         + (end_time - start_time)
		                                         + "s, "
		                                         + current_budget
		                                         + " statements, best individual has fitness "
		                                         + suite.getFitness());
        // Search is finished, send statistics
        sendExecutionStatistics();
		// TODO: In the end we will only need one analysis technique
		if (!Properties.ANALYSIS_CRITERIA.isEmpty()) {
			CoverageAnalysis.analyzeCriteria(suite, Properties.ANALYSIS_CRITERIA);
		}

		LoggingUtils.getEvoLogger().info("* Covered " + covered_goals + "/"
		                                         + goals.size() + " goals");
		logger.info("Resulting test suite: " + suite.size() + " tests, length "
		        + suite.totalLengthOfTestCases());

		// Generate a test suite chromosome once all test cases are done?
		if (Properties.MINIMIZE && Properties.CRITERION.length == 1) {
			LoggingUtils.getEvoLogger().info("* Minimizing result");
			logger.info("Size before: " + suite.totalLengthOfTestCases());
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(goalFactories);
			minimizer.minimize(suite, true);
			logger.info("Size after: " + suite.totalLengthOfTestCases());
		}

		if (Properties.INLINE) {
			ConstantInliner inliner = new ConstantInliner();
			inliner.inline(suite);
		}

		if (Properties.COVERAGE) {
			for (Properties.Criterion pc : Properties.CRITERION)
				CoverageAnalysis.analyzeCoverage(suite, pc);
		}

		/*
		 * if(Properties.MINIMIZE) {
		 * LoggingUtils.getEvoLogger().info("* Minimizing result");
		 * TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
		 * minimizer.minimize(suite, suite_fitness); }
		 */
		// LoggingUtils.getEvoLogger().info("Resulting test suite has fitness "+suite.getFitness());
		LoggingUtils.getEvoLogger().info("* Resulting test suite: " + suite.size()
		                                         + " tests, length "
		                                         + suite.totalLengthOfTestCases());

		// Log some stats
		//statistics.iteration(suiteGA);
		//statistics.minimized(suite);

		return suite;
	}

	/**
	 * Returns a list containing all positions of goals in the given goalList
	 * that are covered by the given test but not already in the given
	 * coveredSet
	 * 
	 * Used to avoid unnecessary solutionGenerations in
	 * generateIndividualTests()
	 */
	private Set<Integer> getAdditionallyCoveredGoals(
	        List<? extends TestFitnessFunction> goals, Set<Integer> covered,
	        TestChromosome best) {

		Set<Integer> r = new HashSet<Integer>();
		ExecutionResult result = best.getLastExecutionResult();
		assert (result != null);
		// if (result == null) {
		// result = TestCaseExecutor.getInstance().execute(best.test);
		// }
		int num = -1;
		for (TestFitnessFunction goal : goals) {
			num++;
			if (covered.contains(num))
				continue;
			if (goal.isCovered(best, result)) {
				r.add(num);
				if (Properties.PRINT_COVERED_GOALS)
					LoggingUtils.getEvoLogger().info("* Additionally covered: "
					                                         + goal.toString());
			}
		}
		return r;
	}

	/*
	 * protected List<BranchCoverageGoal> getBranches() {
	 * List<BranchCoverageGoal> goals = new ArrayList<BranchCoverageGoal>();
	 * 
	 * // Branchless methods String class_name = Properties.TARGET_CLASS;
	 * logger.info("Getting branches for "+class_name); for(String method :
	 * CFGMethodAdapter.branchless_methods) { goals.add(new
	 * BranchCoverageGoal(class_name, method));
	 * logger.info("Adding new method goal for method "+method); }
	 * 
	 * // Branches for(String className : CFGMethodAdapter.branch_map.keySet())
	 * { for(String methodName :
	 * CFGMethodAdapter.branch_map.get(className).keySet()) { // Get CFG of
	 * method ControlFlowGraph cfg =
	 * ExecutionTracer.getExecutionTracer().getCFG(className, methodName);
	 * 
	 * for(Entry<Integer,Integer> entry :
	 * CFGMethodAdapter.branch_map.get(className).get(methodName).entrySet()) {
	 * // Identify vertex in CFG goals.add(new
	 * BranchCoverageGoal(entry.getValue(), entry.getKey(), true, cfg,
	 * className, methodName)); goals.add(new
	 * BranchCoverageGoal(entry.getValue(), entry.getKey(), false, cfg,
	 * className, methodName));
	 * logger.info("Adding new branch goals for method "+methodName); }
	 * 
	 * // Approach level is measured in terms of line coverage? Or possible in
	 * terms of branches... } }
	 * 
	 * return goals; }
	 */

	/**
	 * <p>
	 * getStoppingCondition
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
	 *         object.
	 */
	public static StoppingCondition getStoppingCondition() {
		logger.info("Setting stopping condition: " + Properties.STOPPING_CONDITION);
		switch (Properties.STOPPING_CONDITION) {
		case MAXGENERATIONS:
			return new MaxGenerationStoppingCondition();
		case MAXFITNESSEVALUATIONS:
			return new MaxFitnessEvaluationsStoppingCondition();
		case MAXTIME:
			return new MaxTimeStoppingCondition();
		case MAXTESTS:
			return new MaxTestsStoppingCondition();
		case MAXSTATEMENTS:
			return new MaxStatementsStoppingCondition();
		default:
			logger.warn("Unknown stopping condition: " + Properties.STOPPING_CONDITION);
			return new MaxGenerationStoppingCondition();
		}
	}

	/**
	 * <p>
	 * getCrossoverFunction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.operators.crossover.CrossOverFunction} object.
	 */
	public static CrossOverFunction getCrossoverFunction() {
		switch (Properties.CROSSOVER_FUNCTION) {
		case SINGLEPOINTFIXED:
			return new SinglePointFixedCrossOver();
		case SINGLEPOINTRELATIVE:
			return new SinglePointRelativeCrossOver();
		case SINGLEPOINT:
			return new SinglePointCrossOver();
		case COVERAGE:
			if (Properties.STRATEGY != Properties.Strategy.EVOSUITE)
				throw new RuntimeException(
				        "Coverage crossover function requires test suite mode");

			return new CoverageCrossOver();
		default:
			throw new RuntimeException("Unknown crossover function: "
			        + Properties.CROSSOVER_FUNCTION);
		}
	}

	/**
	 * <p>
	 * getSelectionFunction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.operators.selection.SelectionFunction} object.
	 */
	public static SelectionFunction getSelectionFunction() {
		switch (Properties.SELECTION_FUNCTION) {
		case ROULETTEWHEEL:
			return new FitnessProportionateSelection();
		case TOURNAMENT:
			return new TournamentSelection();
		case BINARY_TOURNAMENT:
		    return new BinaryTournamentSelectionCrowdedComparison();
		default:
			return new RankSelection();
		}
	}
	
	public static GeneticAlgorithm<TestSuiteChromosome> getLastGeneticAlgorithm(){
		try {
			FileInputStream fis = new FileInputStream(Properties.SEED_FILE);
			ObjectInputStream oo = new ObjectInputStream(fis);
			Object stored = oo.readObject();
			
			GeneticAlgorithm<?> lastGa = null;
			
			if (stored instanceof GeneticAlgorithm<?>){
				
				lastGa = (GeneticAlgorithm<?>) stored;
				
			} else if (stored instanceof TestGenerationResult){
				lastGa = ((TestGenerationResult) stored).getGeneticAlgorithm();
			}
			
			if (lastGa != null){
				if (lastGa.getBestIndividual() instanceof TestSuiteChromosome){
					return (GeneticAlgorithm<TestSuiteChromosome>) lastGa;
				}
			}
			LoggingUtils.getEvoLogger().error("* Could not load Genetic Algorithm from file " + Properties.SEED_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * <p>
	 * getChromosomeFactory
	 * </p>
	 * 
	 * @param fitness
	 *            a {@link org.evosuite.ga.FitnessFunction} object.
	 * @return a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	@SuppressWarnings("unchecked")
	protected static ChromosomeFactory<? extends Chromosome> getChromosomeFactory(
	        FitnessFunction<? extends Chromosome> fitness) {
		TestSuiteChromosomeFactory defaultSeedingFactory = new 
				TestSuiteChromosomeFactory(
						new RandomLengthTestFactory());
		switch (Properties.STRATEGY) {
		case EVOSUITE:
			switch (Properties.TEST_FACTORY) {
			case ALLMETHODS:
				logger.info("Using all methods chromosome factory");
				return new TestSuiteChromosomeFactory(
				        new AllMethodsTestChromosomeFactory());
			case RANDOM:
				logger.info("Using random chromosome factory");
				return new TestSuiteChromosomeFactory(new RandomLengthTestFactory());
			case ARCHIVE:
				logger.info("Using archive chromosome factory");
				return new TestSuiteChromosomeFactory(new ArchiveTestChromosomeFactory());
			case TOURNAMENT:
				logger.info("Using tournament chromosome factory");
				return new TournamentChromosomeFactory<TestSuiteChromosome>(
				        (FitnessFunction<TestSuiteChromosome>) fitness,
				        new TestSuiteChromosomeFactory());
			case JUNIT:
				logger.info("Using seeding chromosome factory");
				JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
				        new RandomLengthTestFactory());
				return new TestSuiteChromosomeFactory(factory);
            case SERIALIZATION:
                logger.info("Using serialization seeding chromosome factory");
                return new SerializationSuiteChromosomeFactory(
                        new RandomLengthTestFactory());
            case SEED_BEST_INDIVIDUAL:{
            	logger.info("Using Best Individual Seeding factory");
            	GeneticAlgorithm<TestSuiteChromosome> lastGa = getLastGeneticAlgorithm();
            	if (lastGa instanceof GeneticAlgorithm<?>){
            		return new BestIndividualTestSuiteChromosomeFactory(
            				defaultSeedingFactory, (TestSuiteChromosome) lastGa.getBestIndividual());
            	} else {
            		return defaultSeedingFactory;
            	}
            }
            case SEED_RANDOM_INDIVIDUAL:{
            	logger.info("Using Random Individual Seeding factory");
            	GeneticAlgorithm<TestSuiteChromosome> lastGa = getLastGeneticAlgorithm();
            	if (lastGa instanceof GeneticAlgorithm<?>){
            		return new RandomIndividualTestSuiteChromosomeFactory(
            				defaultSeedingFactory, lastGa);
            	} else {
            		return defaultSeedingFactory;
            	}
            }
            case SEED_BEST_AND_RANDOM_INDIVIDUAL:{
            	logger.info("Using Best and Random Individual Seeding factory");
            	GeneticAlgorithm<TestSuiteChromosome> lastGa = getLastGeneticAlgorithm();
            	if (lastGa instanceof GeneticAlgorithm<?>){
            		return new BIAndRITestSuiteChromosomeFactory(
            				defaultSeedingFactory, lastGa);
            	} else {
            		return defaultSeedingFactory;
            	}
            }
            case SEED_BEST_INDIVIDUAL_METHOD:{
            	logger.info("Using Best Individual (methods) Seeding factory");
            	GeneticAlgorithm<TestSuiteChromosome> lastGa = getLastGeneticAlgorithm();
            	if (lastGa instanceof GeneticAlgorithm<?>){
            		return new BIMethodSeedingTestSuiteChromosomeFactory(
            				defaultSeedingFactory, (TestSuiteChromosome) lastGa.getBestIndividual());
            	} else {
            		return defaultSeedingFactory;
            	}
            }
            case SEED_RANDOM_INDIVIDUAL_METHOD:{
            	logger.info("Using Random Individual (methods) Seeding factory");
            	GeneticAlgorithm<TestSuiteChromosome> lastGa = getLastGeneticAlgorithm();
            	if (lastGa instanceof GeneticAlgorithm<?>){
            		return new RandomMethodSeedingTestSuiteChromosomeFactory(
            				defaultSeedingFactory, lastGa);
            	} else {
            		return defaultSeedingFactory;
            	}
            }
            case SEED_MUTATED_BEST_INDIVIDUAL:{
            	logger.info("Using Mutated Best Individual (methods) Seeding factory");
            	GeneticAlgorithm<TestSuiteChromosome> lastGa = getLastGeneticAlgorithm();
            	if (lastGa instanceof GeneticAlgorithm<?>){
            		return new BIMutatedMethodSeedingTestSuiteChromosomeFactory(
            				defaultSeedingFactory, (TestSuiteChromosome) lastGa.getBestIndividual());
            	} else {
            		return defaultSeedingFactory;
            	}
            }
			default:
				throw new RuntimeException("Unsupported test factory: "
				        + Properties.TEST_FACTORY);
			}
		case REGRESSION:
			return new RegressionTestSuiteChromosomeFactory();
		default:
			switch (Properties.TEST_FACTORY) {
			case ALLMETHODS:
				logger.info("Using all methods chromosome factory");
				return new AllMethodsTestChromosomeFactory();
			case RANDOM:
				logger.info("Using random chromosome factory");
				return new RandomLengthTestFactory();
			case TOURNAMENT:
				logger.info("Using tournament chromosome factory");
				return new TournamentChromosomeFactory<TestChromosome>(
				        (FitnessFunction<TestChromosome>) fitness,
				        new RandomLengthTestFactory());
			case JUNIT:
				logger.info("Using seeding chromosome factory");
				return new JUnitTestCarvedChromosomeFactory(new RandomLengthTestFactory());
			case SERIALIZATION:
                logger.info("Using serialization seeding chromosome factory");
                return new SerializationSuiteChromosomeFactory(new RandomLengthTestFactory());
			default:
				throw new RuntimeException("Unsupported test factory: "
				        + Properties.TEST_FACTORY);
			}
		}
	}

	/**
	 * <p>
	 * getDefaultChromosomeFactory
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public static ChromosomeFactory<? extends Chromosome> getDefaultChromosomeFactory() {
		switch (Properties.STRATEGY) {
		case EVOSUITE:
			return new TestSuiteChromosomeFactory(new RandomLengthTestFactory());
		case REGRESSION:
			return new RegressionTestChromosomeFactory();
		default:
			return new RandomLengthTestFactory();
		}
	}

	/**
	 * <p>
	 * getSecondaryTestObjective
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static SecondaryObjective<?> getSecondaryTestObjective(String name) {
		if (name.equalsIgnoreCase("size"))
			return new MinimizeSizeSecondaryObjective();
		else if (name.equalsIgnoreCase("exceptions"))
			return new org.evosuite.testcase.MinimizeExceptionsSecondaryObjective();
		else
			throw new RuntimeException("ERROR: asked for unknown secondary objective \""
			        + name + "\"");
	}

	/**
	 * <p>
	 * getSecondarySuiteObjective
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static SecondaryObjective<?> getSecondarySuiteObjective(String name) {
		if (name.equalsIgnoreCase("size"))
			return new MinimizeSizeSecondaryObjective();
		else if (name.equalsIgnoreCase("ibranch"))
			return new IBranchSecondaryObjective(FitnessFunctions.getFitnessFunction(Criterion.IBRANCH));
		else if (name.equalsIgnoreCase("archiveibranch"))
			return new IBranchSecondaryObjective(FitnessFunctions.getFitnessFunction(Criterion.ARCHIVEIBRANCH));
		else if (name.equalsIgnoreCase("maxlength"))
			return new MinimizeMaxLengthSecondaryObjective();
		else if (name.equalsIgnoreCase("averagelength"))
			return new MinimizeAverageLengthSecondaryObjective();
		else if (name.equalsIgnoreCase("exceptions"))
			return new MinimizeExceptionsSecondaryObjective();
		else if (name.equalsIgnoreCase("totallength"))
			return new MinimizeTotalLengthSecondaryObjective();
		else
			throw new RuntimeException("ERROR: asked for unknown secondary objective \""
			        + name + "\"");
	}

	/**
	 * <p>
	 * getSecondaryObjectives
	 * </p>
	 * 
	 * @param algorithm
	 *            a {@link org.evosuite.ga.metaheuristics.GeneticAlgorithm} object.
	 */
	public static void getSecondaryObjectives(GeneticAlgorithm<?> algorithm) {
		String objectives = Properties.SECONDARY_OBJECTIVE;

		// check if there are no secondary objectives to optimize
		if (objectives == null || objectives.trim().length() == 0
		        || objectives.trim().equalsIgnoreCase("none"))
			return;

		for (String name : objectives.split(":")) {
			try {
				TestChromosome.addSecondaryObjective(getSecondaryTestObjective(name.trim()));
			} catch (Throwable t) {
			} // Not all objectives make sense for tests
			TestSuiteChromosome.addSecondaryObjective(getSecondarySuiteObjective(name.trim()));
		}
	}

	/**
	 * <p>
	 * getPopulationLimit
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.populationlimit.PopulationLimit} object.
	 */
	public static PopulationLimit getPopulationLimit() {
		switch (Properties.POPULATION_LIMIT) {
		case INDIVIDUALS:
			return new IndividualPopulationLimit();
		case TESTS:
			return new SizePopulationLimit();
		case STATEMENTS:
			return new StatementsPopulationLimit();
		default:
			throw new RuntimeException("Unsupported population limit");
		}
	}

	/**
	 * <p>
	 * getGeneticAlgorithm
	 * </p>
	 * 
	 * @param factory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 * @return a {@link org.evosuite.ga.metaheuristics.GeneticAlgorithm} object.
	 */
	public static <T extends Chromosome> GeneticAlgorithm<T> getGeneticAlgorithm(
	        ChromosomeFactory<T> factory) {
		switch (Properties.ALGORITHM) {
		case ONEPLUSONEEA:
			logger.info("Chosen search algorithm: (1+1)EA");
			return new OnePlusOneEA<T>(factory);
		case MONOTONICGA:
			logger.info("Chosen search algorithm: MonotonicGA");
			{
				MonotonicGA<T> ga = new MonotonicGA<T>(factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					if (Properties.STRATEGY == Strategy.EVOSUITE)
						ga.setReplacementFunction(new TestSuiteReplacementFunction());
					else
						ga.setReplacementFunction(new TestCaseReplacementFunction());
				}
				return ga;
			}
		case STEADYSTATEGA:
			logger.info("Chosen search algorithm: SteadyStateGA");
			{
				SteadyStateGA<T> ga = new SteadyStateGA<T>(factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					if (Properties.STRATEGY == Strategy.EVOSUITE)
						ga.setReplacementFunction(new TestSuiteReplacementFunction());
					else
						ga.setReplacementFunction(new TestCaseReplacementFunction());
				}
				return ga;
			}
		case RANDOM:
			logger.info("Chosen search algorithm: Random");
			return new RandomSearch<T>(factory);
        case NSGAII:
            logger.info("Chosen search algorithm: NSGAII");
            return new NSGAII<T>(factory);
		default:
			logger.info("Chosen search algorithm: StandardGA");
			return new StandardGA<T>(factory);
		}

	}

	/**
	 * Factory method for search algorithm
	 * 
	 * @return a {@link org.evosuite.ga.metaheuristics.GeneticAlgorithm} object.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GeneticAlgorithm<?> setup() {

		ChromosomeFactory<? extends Chromosome> factory = getDefaultChromosomeFactory();
		GeneticAlgorithm<?> ga = getGeneticAlgorithm(factory);

		if (Properties.NEW_STATISTICS)
			ga.addListener(new org.evosuite.statistics.StatisticsListener());

		// How to select candidates for reproduction
		SelectionFunction selection_function = getSelectionFunction();
		selection_function.setMaximize(false);
		ga.setSelectionFunction(selection_function);

		// When to stop the search
		stopping_condition = getStoppingCondition();
		ga.setStoppingCondition(stopping_condition);
		// ga.addListener(stopping_condition);
		if (Properties.STOP_ZERO) {
			ga.addStoppingCondition(zero_fitness);
		}

		if (!(stopping_condition instanceof MaxTimeStoppingCondition)) {
			ga.addStoppingCondition(global_time);
		}

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.MUTATION)
		        || ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION)) {
			if (Properties.STRATEGY == Strategy.ONEBRANCH)
				ga.addStoppingCondition(new MutationTimeoutStoppingCondition());
			else
				ga.addListener(new MutationTestPool());
			// } else if (Properties.CRITERION == Criterion.DEFUSE) {
			// if (Properties.STRATEGY == Strategy.EVOSUITE)
			// ga.addListener(new DefUseTestPool());
		}
		ga.resetStoppingConditions();
		ga.setPopulationLimit(getPopulationLimit());

		// How to cross over
		CrossOverFunction crossover_function = getCrossoverFunction();
		ga.setCrossOverFunction(crossover_function);

		// What to do about bloat
		// MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		// ga.setBloatControl(bloat_control);

		if (Properties.CHECK_BEST_LENGTH) {
			if (Properties.STRATEGY == Strategy.EVOSUITE) {
				RelativeSuiteLengthBloatControl bloat_control = new org.evosuite.testsuite.RelativeSuiteLengthBloatControl();
				ga.addBloatControl(bloat_control);
				ga.addListener(bloat_control);
			} else {
				org.evosuite.testcase.RelativeTestLengthBloatControl bloat_control = new org.evosuite.testcase.RelativeTestLengthBloatControl();
				ga.addBloatControl(bloat_control);
				ga.addListener(bloat_control);
			}
		}
		// ga.addBloatControl(new MaxLengthBloatControl());

		getSecondaryObjectives(ga);

		// Some statistics
		//if (Properties.STRATEGY == Strategy.EVOSUITE)
		//	ga.addListener(SearchStatistics.getInstance());
		// ga.addListener(new MemoryMonitor());
		// ga.addListener(MutationStatistics.getInstance());
		// ga.addListener(BestChromosomeTracker.getInstance());

		if (Properties.DYNAMIC_LIMIT) {
			// max_s = GAProperties.generations * getBranches().size();
			// TODO: might want to make this dependent on the selected coverage
			// criterion
			// TODO also, question: is branchMap.size() really intended here?
			// I think BranchPool.getBranchCount() was intended
			Properties.SEARCH_BUDGET = Properties.SEARCH_BUDGET
			        * (BranchPool.getNumBranchlessMethods(Properties.TARGET_CLASS) + BranchPool.getBranchCountForClass(Properties.TARGET_CLASS) * 2);
			stopping_condition.setLimit(Properties.SEARCH_BUDGET);
			logger.info("Setting dynamic length limit to " + Properties.SEARCH_BUDGET);
		}

		if (Properties.LOCAL_SEARCH_RESTORE_COVERAGE) {
			ga.addListener(BranchCoverageMap.getInstance());
		}

		if (Properties.RECYCLE_CHROMOSOMES) {
			if (Properties.STRATEGY == Strategy.ONEBRANCH)
				ga.addListener(TestCaseRecycler.getInstance());
		}

		if (Properties.SHUTDOWN_HOOK) {
			// ShutdownTestWriter writer = new
			// ShutdownTestWriter(Thread.currentThread());
			ShutdownTestWriter writer = new ShutdownTestWriter();
			ga.addStoppingCondition(writer);
			ga.addStoppingCondition(RMIStoppingCondition.getInstance());

			if (Properties.STOPPING_PORT != -1) {
				SocketStoppingCondition ss = new SocketStoppingCondition();
				ss.accept();
				ga.addStoppingCondition(ss);
			}

			// Runtime.getRuntime().addShutdownHook(writer);
			Signal.handle(new Signal("INT"), writer);
		}

		ga.addListener(new ResourceController());

		return ga;
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
