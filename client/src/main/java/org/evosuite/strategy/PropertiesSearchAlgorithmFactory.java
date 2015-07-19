package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.populationlimit.IndividualPopulationLimit;
import org.evosuite.ga.populationlimit.PopulationLimit;
import org.evosuite.ga.populationlimit.SizePopulationLimit;
import org.evosuite.ga.stoppingconditions.MaxFitnessEvaluationsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.TimeDeltaStoppingCondition;
import org.evosuite.testsuite.StatementsPopulationLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for GAs
 * 
 * @author gordon
 *
 * @param <T>
 */
public abstract class PropertiesSearchAlgorithmFactory<T extends Chromosome>  {

	protected static final Logger logger = LoggerFactory.getLogger(PropertiesSearchAlgorithmFactory.class);


	protected PopulationLimit getPopulationLimit() {
		switch (Properties.POPULATION_LIMIT) {
		case INDIVIDUALS:
			return new IndividualPopulationLimit();
		case TESTS:
			return new SizePopulationLimit();
		case STATEMENTS:
			return (PopulationLimit) new StatementsPopulationLimit();
		default:
			throw new RuntimeException("Unsupported population limit");
		}
	}
	
	protected StoppingCondition getStoppingCondition() {
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
		case TIMEDELTA:
			return new TimeDeltaStoppingCondition();
		default:
			logger.warn("Unknown stopping condition: " + Properties.STOPPING_CONDITION);
			return new MaxGenerationStoppingCondition();
		}
	}
	
	public static GeneticAlgorithm<?> createSearchAlgorithm() {
		switch(Properties.ALGORITHM) {
		case MONOTONICGA:
		case NSGAII:
		case ONEPLUSONEEA:
		case RANDOM:
		case STANDARDGA:
		case STEADYSTATEGA:
			return new PropertiesSuiteGAFactory().getSearchAlgorithm();
		default:
			throw new RuntimeException("Unsupported algorithm: "+Properties.ALGORITHM);
		}
	}
	
	public abstract GeneticAlgorithm<T> getSearchAlgorithm();
}
