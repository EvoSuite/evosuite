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
package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.Strategy;
import org.evosuite.Properties.TheReplacementFunction;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.MinimizeSizeSecondaryObjective;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.ga.FitnessReplacementFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.MonotonicGA;
import org.evosuite.ga.metaheuristics.NSGAII;
import org.evosuite.ga.metaheuristics.OnePlusOneEA;
import org.evosuite.ga.metaheuristics.StandardGA;
import org.evosuite.ga.metaheuristics.SteadyStateGA;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.crossover.SinglePointCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointFixedCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointRelativeCrossOver;
import org.evosuite.ga.operators.selection.BinaryTournamentSelectionCrowdedComparison;
import org.evosuite.ga.operators.selection.FitnessProportionateSelection;
import org.evosuite.ga.operators.selection.RankSelection;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.ga.operators.selection.TournamentSelection;
import org.evosuite.ga.metaheuristics.RandomSearch;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.seeding.TestCaseRecycler;
import org.evosuite.testcase.TestCaseReplacementFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.factories.AllMethodsTestChromosomeFactory;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.utils.ArrayUtil;

/**
 * Factory for GA for tests
 * 
 * @author gordon
 *
 */
public class PropertiesTestGAFactory extends PropertiesSearchAlgorithmFactory<TestChromosome> {

	protected ChromosomeFactory<TestChromosome> getChromosomeFactory() {
		switch (Properties.STRATEGY) {
		case ONEBRANCH:
			switch (Properties.TEST_FACTORY) {
			case ALLMETHODS:
				logger.info("Using all methods chromosome factory");
				return new AllMethodsTestChromosomeFactory();
			case RANDOM:
				logger.info("Using random chromosome factory");
				return new RandomLengthTestFactory();
			case JUNIT:
				logger.info("Using seeding chromosome factory");
				return new JUnitTestCarvedChromosomeFactory(new RandomLengthTestFactory());
			default:
			}
		case ENTBUG:
			return new RandomLengthTestFactory();
		default:
			break;
		}
		throw new RuntimeException("Unsupported test factory: "
		        + Properties.TEST_FACTORY);
	}
	
	private GeneticAlgorithm<TestChromosome> getGeneticAlgorithm(ChromosomeFactory<TestChromosome> factory) {
		switch (Properties.ALGORITHM) {
		case ONEPLUSONEEA:
			logger.info("Chosen search algorithm: (1+1)EA");
			return new OnePlusOneEA<TestChromosome>(factory);
		case MONOTONICGA:
			logger.info("Chosen search algorithm: SteadyStateGA");
			{
				MonotonicGA<TestChromosome> ga = new MonotonicGA<TestChromosome>(factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					ga.setReplacementFunction(new TestCaseReplacementFunction());
				}
				return ga;
			}
		case STEADYSTATEGA:
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			{
				SteadyStateGA<TestChromosome> ga = new SteadyStateGA<TestChromosome>(factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					ga.setReplacementFunction(new TestCaseReplacementFunction());
				}
				return ga;
			}
		case RANDOM:
			logger.info("Chosen search algorithm: Random");
			return new RandomSearch<TestChromosome>(factory);
        case NSGAII:
            logger.info("Chosen search algorithm: NSGAII");
            return new NSGAII<TestChromosome>(factory);
		default:
			logger.info("Chosen search algorithm: StandardGA");
			return new StandardGA<TestChromosome>(factory);
		}
	}
	
	private SelectionFunction<TestChromosome> getSelectionFunction() {
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
	
	private CrossOverFunction getCrossoverFunction() {
		switch (Properties.CROSSOVER_FUNCTION) {
		case SINGLEPOINTFIXED:
			return new SinglePointFixedCrossOver();
		case SINGLEPOINTRELATIVE:
			return new SinglePointRelativeCrossOver();
		case SINGLEPOINT:
			return new SinglePointCrossOver();
		default:
			throw new RuntimeException("Unknown crossover function: "
			        + Properties.CROSSOVER_FUNCTION);
		}
	}
	
	

	/**
	 * <p>
	 * getSecondaryTestObjective
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.search.ga.SecondaryObjective} object.
	 */
	private SecondaryObjective<TestChromosome> getSecondaryTestObjective(String name) {
		if (name.equalsIgnoreCase("size"))
			return new MinimizeSizeSecondaryObjective<>();
		else if (name.equalsIgnoreCase("exceptions"))
			return new org.evosuite.testcase.MinimizeExceptionsSecondaryObjective();
		else
			throw new RuntimeException("ERROR: asked for unknown secondary objective \""
			        + name + "\"");
	}

	private void getSecondaryObjectives(GeneticAlgorithm<TestChromosome> algorithm) {
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
		}
	}
	
	@Override
	public GeneticAlgorithm<TestChromosome> getSearchAlgorithm() {
		ChromosomeFactory<TestChromosome> factory = getChromosomeFactory();
		
		// FIXXME
		GeneticAlgorithm<TestChromosome> ga = getGeneticAlgorithm(factory);

		if (Properties.NEW_STATISTICS)
			ga.addListener(new org.evosuite.statistics.StatisticsListener());

		// How to select candidates for reproduction
		SelectionFunction<TestChromosome> selection_function = getSelectionFunction();
		selection_function.setMaximize(false);
		ga.setSelectionFunction(selection_function);

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
			ga.addStoppingCondition(new MutationTimeoutStoppingCondition());
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
			org.evosuite.testcase.RelativeTestLengthBloatControl bloat_control = new org.evosuite.testcase.RelativeTestLengthBloatControl();
			ga.addBloatControl(bloat_control);
			ga.addListener(bloat_control);
		}

		getSecondaryObjectives(ga);

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

		// TODO: This seems to be whole test suite specific
		// if (Properties.LOCAL_SEARCH_RESTORE_COVERAGE) {
		// 	ga.addListener(BranchCoverageMap.getInstance());
		// }

		if (Properties.RECYCLE_CHROMOSOMES) {
			if (Properties.STRATEGY == Strategy.ONEBRANCH)
				ga.addListener(TestCaseRecycler.getInstance());
		}

		// ga.addListener(new ResourceController<TestChromosome>());
		return ga;
	}

}
