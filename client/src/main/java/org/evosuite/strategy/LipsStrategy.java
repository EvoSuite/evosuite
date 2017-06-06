package org.evosuite.strategy;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LipsStrategy extends TestGenerationStrategy {

	private static final Logger logger = LoggerFactory.getLogger(LipsStrategy.class);
	
	@Override	
	public TestSuiteChromosome generateTests() {
		// Set up search algorithm
		PropertiesSuiteGAFactory algorithmFactory = new PropertiesSuiteGAFactory();

		GeneticAlgorithm<TestSuiteChromosome> algorithm = algorithmFactory.getSearchAlgorithm();
		
		
		if(Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD)
			TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(algorithm);

		long startTime = System.currentTimeMillis() / 1000;

		// What's the search target
		List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
		List<TestFitnessFunction> fitnessFunctions = new ArrayList<TestFitnessFunction>();
        for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
            fitnessFunctions.addAll(goalFactory.getCoverageGoals());
        }
		algorithm.addFitnessFunctions((List)fitnessFunctions);

		// if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
		algorithm.addListener(progressMonitor); // FIXME progressMonitor may cause
		// client hang if EvoSuite is
		// executed with -prefix!
		
//		List<TestFitnessFunction> goals = getGoals(true);
		
		LoggingUtils.getEvoLogger().info("* Total number of test goals for LISP: {}", fitnessFunctions.size());
		
		// TODO handle this better by introducing generics
//		for (TestFitnessFunction f : fitnessFunctions) {
//			if (!f.getTargetMethod().contains("<init>")) {
//				Properties.TARGET_METHOD = f.getTargetMethod();
//				break;
//			}
//		}
//		ChromosomeFactory factory = new LispChromosomeFactory(fitnessFunctions.get(0)); // new
																						// RandomLengthTestFactory();

		ChromosomeFactory factory = new RandomLengthTestFactory();
		algorithm.setChromosomeFactory(factory);

//		if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
//			ga.addListener(progressMonitor); // FIXME progressMonitor may cause

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.ALLDEFS) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.STATEMENT) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.RHO) || 
				ArrayUtil.contains(Properties.CRITERION, Criterion.BRANCH) ||
				ArrayUtil.contains(Properties.CRITERION, Criterion.AMBIGUITY))
			ExecutionTracer.enableTraceCalls();

		algorithm.resetStoppingConditions();

		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, fitnessFunctions.size());
		TestSuiteChromosome testSuite = null;

		if (!(Properties.STOP_ZERO && fitnessFunctions.isEmpty()) || ArrayUtil.contains(Properties.CRITERION, Criterion.EXCEPTION)) {
			// Perform search
			LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed());
			LoggingUtils.getEvoLogger().info("* Starting evolution");
			ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

			algorithm.generateSolution();
			List<TestSuiteChromosome> bestSuites = (List<TestSuiteChromosome>) algorithm.getBestIndividuals();
			if (bestSuites.isEmpty()) {
				LoggingUtils.getEvoLogger().warn("Could not find any suitable chromosome");
				return new TestSuiteChromosome();
			}else{
				testSuite = bestSuites.get(0);
			}
		} else {
			zeroFitness.setFinished();
			testSuite = new TestSuiteChromosome();
			for (FitnessFunction<?> ff : testSuite.getFitnessValues().keySet()) {
				testSuite.setCoverage(ff, 1.0);
			}
		}

		long endTime = System.currentTimeMillis() / 1000;

//		goals = getGoals(false); //recalculated now after the search, eg to handle exception fitness
//        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());
        
		// Newline after progress bar
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");
		
		String text = " statements, best individual has fitness: ";
		LoggingUtils.getEvoLogger().info("* Search finished after "
				+ (endTime - startTime)
				+ "s and "
				+ algorithm.getAge()
				+ " generations, "
				+ MaxStatementsStoppingCondition.getNumExecutedStatements()
				+ text
				+ testSuite.getFitness());
		// Search is finished, send statistics
		sendExecutionStatistics();

		return testSuite;
	}
	
	
}
