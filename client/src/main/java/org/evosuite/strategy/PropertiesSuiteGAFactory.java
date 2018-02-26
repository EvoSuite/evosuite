/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.Strategy;
import org.evosuite.Properties.TheReplacementFunction;
import org.evosuite.ShutdownTestWriter;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.mutation.MutationTestPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessReplacementFunction;
import org.evosuite.ga.metaheuristics.BreederGA;
import org.evosuite.ga.metaheuristics.CellularGA;
import org.evosuite.ga.archive.ArchiveTestChromosomeFactory;
import org.evosuite.ga.metaheuristics.*;
import org.evosuite.ga.metaheuristics.lips.LIPS;
import org.evosuite.ga.metaheuristics.mosa.MOSA;
import org.evosuite.ga.metaheuristics.mulambda.MuLambdaEA;
import org.evosuite.ga.metaheuristics.mulambda.MuPlusLambdaEA;
import org.evosuite.ga.metaheuristics.mulambda.OnePlusLambdaLambdaGA;
import org.evosuite.ga.metaheuristics.mulambda.OnePlusOneEA;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.crossover.SinglePointCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointFixedCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointRelativeCrossOver;
import org.evosuite.ga.operators.crossover.UniformCrossOver;
import org.evosuite.ga.operators.selection.BinaryTournamentSelectionCrowdedComparison;
import org.evosuite.ga.operators.selection.FitnessProportionateSelection;
import org.evosuite.ga.operators.selection.RankSelection;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.ga.operators.selection.TournamentSelection;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.RMIStoppingCondition;
import org.evosuite.ga.stoppingconditions.SocketStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.regression.RegressionTestSuiteChromosomeFactory;
import org.evosuite.statistics.StatisticsListener;
import org.evosuite.testcase.factories.AllMethodsTestChromosomeFactory;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteReplacementFunction;
import org.evosuite.testsuite.factories.SerializationSuiteChromosomeFactory;
import org.evosuite.testsuite.factories.TestSuiteChromosomeFactory;
import org.evosuite.testsuite.secondaryobjectives.TestSuiteSecondaryObjective;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.ResourceController;
import sun.misc.Signal;

/**
 * Factory for GA on test suites
 * 
 * @author gordon
 *
 */
@SuppressWarnings("restriction")
public class PropertiesSuiteGAFactory extends PropertiesSearchAlgorithmFactory<TestSuiteChromosome> {

	
	protected ChromosomeFactory<TestSuiteChromosome> getChromosomeFactory() {
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
			case JUNIT:
				logger.info("Using seeding chromosome factory");
				JUnitTestCarvedChromosomeFactory factory = new JUnitTestCarvedChromosomeFactory(
				        new RandomLengthTestFactory());
				return new TestSuiteChromosomeFactory(factory);
            case SERIALIZATION:
                logger.info("Using serialization seeding chromosome factory");
                return new SerializationSuiteChromosomeFactory(
                        new RandomLengthTestFactory());
			default:
				throw new RuntimeException("Unsupported test factory: "
				        + Properties.TEST_FACTORY);
			}
		case REGRESSION:
			return new RegressionTestSuiteChromosomeFactory();
		case MOSUITE:
			return new TestSuiteChromosomeFactory(new RandomLengthTestFactory());
		default:
			throw new RuntimeException("Unsupported test factory: "
					+ Properties.TEST_FACTORY);
		}
	}
	
	protected GeneticAlgorithm<TestSuiteChromosome> getGeneticAlgorithm(ChromosomeFactory<TestSuiteChromosome> factory) {
		switch (Properties.ALGORITHM) {
		case ONE_PLUS_ONE_EA:
			logger.info("Chosen search algorithm: (1+1)EA");
			{
				OnePlusOneEA<TestSuiteChromosome> ga = new OnePlusOneEA<TestSuiteChromosome>(factory);
				return ga;
			}
		case MU_PLUS_LAMBDA_EA:
		    logger.info("Chosen search algorithm: (Mu+Lambda)EA");
            {
                MuPlusLambdaEA<TestSuiteChromosome> ga = new MuPlusLambdaEA<TestSuiteChromosome>(factory, Properties.MU, Properties.LAMBDA);
                return ga;
            }
		case MU_LAMBDA_EA:
			logger.info("Chosen search algorithm: (Mu,Lambda)EA");
			return new MuLambdaEA<TestSuiteChromosome>(factory, Properties.MU, Properties.LAMBDA);
		case MONOTONIC_GA:
			logger.info("Chosen search algorithm: MonotonicGA");
			{
				MonotonicGA<TestSuiteChromosome> ga = new MonotonicGA<TestSuiteChromosome>(factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					ga.setReplacementFunction(new TestSuiteReplacementFunction());
				}
				return ga;
			}
		case CELLULAR_GA:
			logger.info("Chosen search algorithm: CellularGA");
			{
				CellularGA<TestSuiteChromosome> ga = new CellularGA<TestSuiteChromosome>(Properties.MODEL, factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					ga.setReplacementFunction(new TestSuiteReplacementFunction());
				}
				return ga;
			}
		case STEADY_STATE_GA:
			logger.info("Chosen search algorithm: Steady-StateGA");
			{
				SteadyStateGA<TestSuiteChromosome> ga = new SteadyStateGA<>(factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					ga.setReplacementFunction(new TestSuiteReplacementFunction());
				}
				return ga;
			}
		case BREEDER_GA:
			logger.info("Chosen search algorithm: BreederGA");
		{
			BreederGA<TestSuiteChromosome> ga = new BreederGA<>(factory);
			return ga;
		}
		case RANDOM_SEARCH:
			logger.info("Chosen search algorithm: Random");
			{
                RandomSearch<TestSuiteChromosome> ga = new RandomSearch<TestSuiteChromosome>(factory);
                return ga;
			}
        case NSGAII:
            logger.info("Chosen search algorithm: NSGAII");
            return new NSGAII<TestSuiteChromosome>(factory);
        case SPEA2:
            logger.info("Chosen search algorithm: SPEA2");
            return new SPEA2<TestSuiteChromosome>(factory);
        case MOSA:
        	logger.info("Chosen search algorithm: MOSA");
            return new MOSA<TestSuiteChromosome>(factory);
        case ONE_PLUS_LAMBDA_LAMBDA_GA:
            logger.info("Chosen search algorithm: 1 + (lambda, lambda)GA");
            {
              OnePlusLambdaLambdaGA<TestSuiteChromosome> ga = new OnePlusLambdaLambdaGA<TestSuiteChromosome>(factory, Properties.LAMBDA);
              return ga;
            }
        case MIO:
          logger.info("Chosen search algorithm: MIO");
          {
              MIO<TestSuiteChromosome> ga = new MIO<TestSuiteChromosome>(factory);
              return ga;
          }
        case STANDARD_CHEMICAL_REACTION:
            logger.info("Chosen search algorithm: Standard Chemical Reaction Optimization");
            {
              StandardChemicalReaction<TestSuiteChromosome> ga = new StandardChemicalReaction<TestSuiteChromosome>(factory);
              return ga;
            }
        case LIPS:
        	logger.info("Chosen search algorithm: LIPS");
            return new LIPS<TestSuiteChromosome>(factory);
		default:
			logger.info("Chosen search algorithm: StandardGA");
            {
                StandardGA<TestSuiteChromosome> ga = new StandardGA<TestSuiteChromosome>(factory);
                return ga;
            }
		}
	}
	
	protected SelectionFunction<TestSuiteChromosome> getSelectionFunction() {
		switch (Properties.SELECTION_FUNCTION) {
		case ROULETTEWHEEL:
			return new FitnessProportionateSelection<>();
		case TOURNAMENT:
			return new TournamentSelection<>();
		case BINARY_TOURNAMENT:
		    return new BinaryTournamentSelectionCrowdedComparison<>();
		default:
			return new RankSelection<>();
		}
	}
	
	protected CrossOverFunction getCrossoverFunction() {
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

			return new org.evosuite.ga.operators.crossover.CoverageCrossOver();
		case UNIFORM:
			return new UniformCrossOver();
		default:
			throw new RuntimeException("Unknown crossover function: "
			        + Properties.CROSSOVER_FUNCTION);
		}
	}
	
	@Override
	public GeneticAlgorithm<TestSuiteChromosome> getSearchAlgorithm() {
		ChromosomeFactory<TestSuiteChromosome> factory = getChromosomeFactory();
		
		// FIXXME
		GeneticAlgorithm<TestSuiteChromosome> ga = getGeneticAlgorithm(factory);

		if (Properties.NEW_STATISTICS)
			ga.addListener(new StatisticsListener());

		// How to select candidates for reproduction
		SelectionFunction<TestSuiteChromosome> selectionFunction = getSelectionFunction();
		selectionFunction.setMaximize(false);
		ga.setSelectionFunction(selectionFunction);

		// When to stop the search
		StoppingCondition stopping_condition = getStoppingCondition();
		ga.setStoppingCondition(stopping_condition);
		// ga.addListener(stopping_condition);
		if (Properties.STOP_ZERO) {
			ga.addStoppingCondition(new ZeroFitnessStoppingCondition());
		}

		if (!(stopping_condition instanceof MaxTimeStoppingCondition)) {
			ga.addStoppingCondition(new GlobalTimeStoppingCondition());
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
			RelativeSuiteLengthBloatControl bloat_control = new org.evosuite.testsuite.RelativeSuiteLengthBloatControl();
			ga.addBloatControl(bloat_control);
			ga.addListener(bloat_control);
		}
		// ga.addBloatControl(new MaxLengthBloatControl());

		TestSuiteSecondaryObjective.setSecondaryObjectives();

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
			        * (BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getNumBranchlessMethods(Properties.TARGET_CLASS) + BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCountForClass(Properties.TARGET_CLASS) * 2);
			stopping_condition.setLimit(Properties.SEARCH_BUDGET);
			logger.info("Setting dynamic length limit to " + Properties.SEARCH_BUDGET);
		}

		if (Properties.LOCAL_SEARCH_RESTORE_COVERAGE) {
			org.evosuite.ga.metaheuristics.SearchListener map = BranchCoverageMap.getInstance(); 
			ga.addListener(map);
		}

		if (Properties.SHUTDOWN_HOOK) {
			// ShutdownTestWriter writer = new
			// ShutdownTestWriter(Thread.currentThread());
			ShutdownTestWriter writer = new ShutdownTestWriter();
			ga.addStoppingCondition(writer);
			RMIStoppingCondition rmi = RMIStoppingCondition.getInstance();
			ga.addStoppingCondition(rmi);

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


}
