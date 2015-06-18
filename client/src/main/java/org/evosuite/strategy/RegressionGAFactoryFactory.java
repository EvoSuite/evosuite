package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.ShutdownTestWriter;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.Strategy;
import org.evosuite.Properties.TheReplacementFunction;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.archive.ArchiveTestChromosomeFactory;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.mutation.MutationTestPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.ga.Archive;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessReplacementFunction;
import org.evosuite.ga.MinimizeSizeSecondaryObjective;
import org.evosuite.coverage.ibranch.IBranchSecondaryObjective;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.RandomSearch;
import org.evosuite.ga.metaheuristics.SteadyStateGA;
import org.evosuite.ga.metaheuristics.NSGAII;
import org.evosuite.ga.metaheuristics.OnePlusOneEA;
import org.evosuite.ga.metaheuristics.StandardGA;
import org.evosuite.ga.metaheuristics.MonotonicGA;
import org.evosuite.regression.RegressionTestChromosomeFactory;
import org.evosuite.regression.RegressionTestSuiteChromosomeFactory;
import org.evosuite.statistics.StatisticsListener;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.crossover.SinglePointCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointFixedCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointRelativeCrossOver;
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
import org.evosuite.testcase.factories.AllMethodsTestChromosomeFactory;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testsuite.secondaryobjectives.MinimizeAverageLengthSecondaryObjective;
import org.evosuite.testsuite.secondaryobjectives.MinimizeExceptionsSecondaryObjective;
import org.evosuite.testsuite.secondaryobjectives.MinimizeMaxLengthSecondaryObjective;
import org.evosuite.testsuite.secondaryobjectives.MinimizeTotalLengthSecondaryObjective;
import org.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import org.evosuite.testsuite.factories.SerializationSuiteChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.factories.TestSuiteChromosomeFactory;
import org.evosuite.testsuite.TestSuiteReplacementFunction;
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
public class RegressionGAFactoryFactory extends PropertiesSearchAlgorithmFactory<TestSuiteChromosome> {

	
	protected ChromosomeFactory<?> getChromosomeFactory() {
		switch (Properties.STRATEGY) {
		case REGRESSION:
			return new RegressionTestSuiteChromosomeFactory();
		case REGRESSIONTESTS:
			return new RegressionTestChromosomeFactory();	
		default:
			return new RandomLengthTestFactory();
		}
	}
	
	private <T extends Chromosome> GeneticAlgorithm<T> getGeneticAlgorithm(ChromosomeFactory<T> factory) {
		switch (Properties.ALGORITHM) {
		case ONEPLUSONEEA:
			logger.info("Chosen search algorithm: (1+1)EA");
			{
				OnePlusOneEA<T> ga = new OnePlusOneEA<T>(factory);
				if (Properties.TEST_ARCHIVE)
					ga.setArchive((Archive<T>) TestsArchive.instance);

				return ga;
			}
		case MONOTONICGA:
			logger.info("Chosen search algorithm: SteadyStateGA");
			{
				MonotonicGA<T> ga = new MonotonicGA<T>(factory);
	            if (Properties.TEST_ARCHIVE)
	            	ga.setArchive((Archive<T>) TestsArchive.instance);

				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					ga.setReplacementFunction(new TestSuiteReplacementFunction());
				}
				return ga;
			}
		case STEADYSTATEGA:
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			{
				SteadyStateGA<T> ga = new SteadyStateGA<>(factory);
	            if (Properties.TEST_ARCHIVE)
	            	ga.setArchive((Archive<T>) TestsArchive.instance);

				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					// user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					// use default
					ga.setReplacementFunction(new TestSuiteReplacementFunction());
				}
				return ga;
			}
		case RANDOM:
			logger.info("Chosen search algorithm: Random");
			{
                RandomSearch<T> ga = new RandomSearch<T>(factory);
                if (Properties.TEST_ARCHIVE)
                        ga.setArchive((Archive<T>) TestsArchive.instance);

                return ga;
			}
        case NSGAII:
            logger.info("Chosen search algorithm: NSGAII");
            return new NSGAII<T>(factory);
		default:
			logger.info("Chosen search algorithm: StandardGA");
            {
                StandardGA<T> ga = new StandardGA<T>(factory);
                if (Properties.TEST_ARCHIVE)
                        ga.setArchive((Archive<T>) TestsArchive.instance);
                return ga;
            }
		}
	}
	
	private SelectionFunction<TestSuiteChromosome> getSelectionFunction() {
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
		case COVERAGE:
			if (Properties.STRATEGY != Properties.Strategy.EVOSUITE)
				throw new RuntimeException(
				        "Coverage crossover function requires test suite mode");

			return new org.evosuite.ga.operators.crossover.CoverageCrossOver();
		default:
			throw new RuntimeException("Unknown crossover function: "
			        + Properties.CROSSOVER_FUNCTION);
		}
	}

	/**
	 * <p>
	 * getSecondarySuiteObjective
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.search.ga.SecondaryObjective} object.
	 */
	private SecondaryObjective<TestSuiteChromosome> getSecondarySuiteObjective(String name) {
		if (name.equalsIgnoreCase("size"))
			return new MinimizeSizeSecondaryObjective<>();
		else if (name.equalsIgnoreCase("ibranch"))
			return new IBranchSecondaryObjective();
		else if (name.equalsIgnoreCase("archiveibranch"))
			return new IBranchSecondaryObjective();
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

	private void getSecondaryObjectives(GeneticAlgorithm<?> algorithm) {
		String objectives = Properties.SECONDARY_OBJECTIVE;

		// check if there are no secondary objectives to optimize
		if (objectives == null || objectives.trim().length() == 0
		        || objectives.trim().equalsIgnoreCase("none"))
			return;

		for (String name : objectives.split(":")) {
			TestSuiteChromosome.addSecondaryObjective(getSecondarySuiteObjective(name.trim()));
		}
	}
	
	@Override
	public GeneticAlgorithm<?> getSearchAlgorithm() {
		ChromosomeFactory<?> factory = getChromosomeFactory();
		
		// FIXXME
		GeneticAlgorithm<?> ga = getGeneticAlgorithm(factory);

		if (Properties.NEW_STATISTICS)
			ga.addListener(new StatisticsListener());

		// How to select candidates for reproduction
		SelectionFunction selectionFunction = getSelectionFunction();
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
