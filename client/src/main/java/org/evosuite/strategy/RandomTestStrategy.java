package org.evosuite.strategy;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.archive.ArchiveTestChromosomeFactory;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.StatisticsSender;
import org.evosuite.testcase.factories.AllMethodsTestChromosomeFactory;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iteratively generate random tests. If adding the random test
 * leads to improved fitness, keep it, otherwise drop it again.
 * 
 * @author gordon
 *
 */
public class RandomTestStrategy extends TestGenerationStrategy {

	private static final Logger logger = LoggerFactory.getLogger(RandomTestStrategy.class);
	
	@Override
	public TestSuiteChromosome generateTests() {
		LoggingUtils.getEvoLogger().info("* Using random test generation");

		List<TestSuiteFitnessFunction> fitnessFunctions = getFitnessFunctions();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		for (TestSuiteFitnessFunction fitnessFunction : fitnessFunctions)
			suite.addFitness( fitnessFunction);

		List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
		LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
		for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
			goals.addAll(goalFactory.getCoverageGoals());
			LoggingUtils.getEvoLogger().info("  - " + goalFactory.getClass().getSimpleName().replace("CoverageFactory", "")
					+ " " + goalFactory.getCoverageGoals().size());
		}
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals,
				goals.size());

	
		ChromosomeFactory<TestChromosome> factory = getChromosomeFactory();

		StoppingCondition stoppingCondition = getStoppingCondition();
		for (FitnessFunction<?> fitness_function : fitnessFunctions)
			((TestSuiteFitnessFunction)fitness_function).getFitness(suite);
		ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

		while (!isFinished(suite, stoppingCondition)) {
			TestChromosome test = factory.getChromosome();
			TestSuiteChromosome clone = suite.clone();
			clone.addTest(test);
			for (FitnessFunction<?> fitness_function : fitnessFunctions) {
				((TestSuiteFitnessFunction)fitness_function).getFitness(clone);
				logger.debug("Old fitness: {}, new fitness: {}", suite.getFitness(),
						clone.getFitness());
			}
			if (clone.compareTo(suite) < 0) {
				suite = clone;
				StatisticsSender.executedAndThenSendIndividualToMaster(clone);				
			}
		}
		//statistics.searchFinished(suiteGA);
		LoggingUtils.getEvoLogger().info("* Search Budget:");
		LoggingUtils.getEvoLogger().info("\t- " + stoppingCondition.toString());
		
		// In the GA, these statistics are sent via the SearchListener when notified about the GA completing
		// Search is finished, send statistics
		sendExecutionStatistics();

		// TODO: Check this: Fitness_Evaluations = getNumExecutedTests?
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Fitness_Evaluations, MaxTestsStoppingCondition.getNumExecutedTests());

		return suite;	
	}

	protected ChromosomeFactory<TestChromosome> getChromosomeFactory() {
		switch (Properties.TEST_FACTORY) {
		case ALLMETHODS:
			return new AllMethodsTestChromosomeFactory();
		case RANDOM:
			return new RandomLengthTestFactory();
		case ARCHIVE:
			return new ArchiveTestChromosomeFactory();
		case JUNIT:
			return new JUnitTestCarvedChromosomeFactory(
					new RandomLengthTestFactory());
		default:
			throw new RuntimeException("Unsupported test factory: "
					+ Properties.TEST_FACTORY);
		}
		
	}
	

	
}
