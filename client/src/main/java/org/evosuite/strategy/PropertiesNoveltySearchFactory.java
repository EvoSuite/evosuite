package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.ShutdownTestWriter;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.mutation.MutationTestPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.archive.ArchiveTestChromosomeFactory;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.NoveltySearch;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.crossover.SinglePointCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointFixedCrossOver;
import org.evosuite.ga.operators.crossover.SinglePointRelativeCrossOver;
import org.evosuite.ga.operators.ranking.FastNonDominatedSorting;
import org.evosuite.ga.operators.ranking.RankBasedPreferenceSorting;
import org.evosuite.ga.operators.ranking.RankingFunction;
import org.evosuite.ga.operators.selection.*;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.RMIStoppingCondition;
import org.evosuite.ga.stoppingconditions.SocketStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.statistics.StatisticsListener;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.factories.AllMethodsTestChromosomeFactory;
import org.evosuite.testcase.factories.JUnitTestCarvedChromosomeFactory;
import org.evosuite.testcase.factories.RandomLengthTestFactory;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testcase.secondaryobjectives.TestCaseSecondaryObjective;
import org.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.factories.SerializationSuiteChromosomeFactory;
import org.evosuite.testsuite.factories.TestSuiteChromosomeFactory;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.ResourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;

public class PropertiesNoveltySearchFactory extends PropertiesSearchAlgorithmFactory<TestSuiteChromosome> {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesNoveltySearchFactory.class);

    private ChromosomeFactory<TestSuiteChromosome> getChromosomeFactory() {
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
            case NOVELTY:
                return new TestSuiteChromosomeFactory(new RandomLengthTestFactory());
            default:
                throw new RuntimeException("Unsupported test factory: "
                        + Properties.TEST_FACTORY);
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
            case RANK_CROWD_DISTANCE_TOURNAMENT:
                return new TournamentSelectionRankAndCrowdingDistanceComparator<>();
            case NOVELTY_RANK_TOURNAMENT:
                return new TournamentSelectionNoveltyAndRankComparator<>();
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
            default:
                throw new RuntimeException("Unknown crossover function: "
                        + Properties.CROSSOVER_FUNCTION);
        }
    }

    private RankingFunction<TestSuiteChromosome> getRankingFunction() {
      switch (Properties.RANKING_TYPE) {
        case FAST_NON_DOMINATED_SORTING:
          return new FastNonDominatedSorting<>();
        case PREFERENCE_SORTING:
        default:
          return new RankBasedPreferenceSorting<>();
      }
    }

    @Override
    public NoveltySearch<TestSuiteChromosome> getSearchAlgorithm() {
        logger.info("Chosen search algorithm: NOVELTY");
        ChromosomeFactory<TestSuiteChromosome> factory = getChromosomeFactory();

        NoveltySearch<TestSuiteChromosome> ga = new NoveltySearch<>(factory);

        if (Properties.NEW_STATISTICS)
            ga.addListener(new StatisticsListener());

        // How to select candidates for reproduction
        SelectionFunction<TestSuiteChromosome> selectionFunction = getSelectionFunction();
        selectionFunction.setMaximize(false);
        ga.setSelectionFunction(selectionFunction);

        RankingFunction<TestSuiteChromosome> ranking_function = getRankingFunction();
        ga.setRankingFunction(ranking_function);

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

        if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.MUTATION)
                || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.STRONGMUTATION)) {
            if (Properties.STRATEGY == Properties.Strategy.ONEBRANCH)
                ga.addStoppingCondition(new MutationTimeoutStoppingCondition());
            else
                ga.addListener(new MutationTestPool());
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

        TestCaseSecondaryObjective.setSecondaryObjectives();

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
