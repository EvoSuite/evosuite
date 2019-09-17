package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.ShutdownTestWriter;
import org.evosuite.TestGenerationContext;
import org.evosuite.Properties.Algorithm;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.mutation.MutationTestPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.archive.ArchiveTestChromosomeFactory;
import org.evosuite.ga.metaheuristics.mapelites.MAPElites;
import org.evosuite.ga.operators.crossover.CrossOverFunction;
import org.evosuite.ga.operators.ranking.RankingFunction;
import org.evosuite.ga.operators.selection.BinaryTournamentSelectionCrowdedComparison;
import org.evosuite.ga.operators.selection.FitnessProportionateSelection;
import org.evosuite.ga.operators.selection.RankSelection;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.ga.operators.selection.TournamentSelection;
import org.evosuite.ga.operators.selection.TournamentSelectionRankAndCrowdingDistanceComparator;
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
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.ResourceController;

import sun.misc.Signal;

public class PropertiesMapElitesSearchFactory
    extends PropertiesSearchAlgorithmFactory<TestChromosome> {

  private ChromosomeFactory<TestChromosome> getChromosomeFactory() {
    switch (Properties.TEST_FACTORY) {
      case ALLMETHODS:
        logger.info("Using all methods chromosome factory");
        return new AllMethodsTestChromosomeFactory();
      case RANDOM:
        logger.info("Using random chromosome factory");
        return new RandomLengthTestFactory();
      case ARCHIVE:
        logger.info("Using archive chromosome factory");
        return new ArchiveTestChromosomeFactory();
      case JUNIT:
        logger.info("Using seeding chromosome factory");
        JUnitTestCarvedChromosomeFactory factory =
            new JUnitTestCarvedChromosomeFactory(new RandomLengthTestFactory());
        return factory;
      case SERIALIZATION:
        logger.info("Using serialization seeding chromosome factory");
        return new RandomLengthTestFactory();
      default:
        throw new RuntimeException("Unsupported test factory: " + Properties.TEST_FACTORY);
    }

  }

  @Override
  public MAPElites<TestChromosome> getSearchAlgorithm() {
    ChromosomeFactory<TestChromosome> factory = getChromosomeFactory();
    MAPElites<TestChromosome> ga = new MAPElites<>(factory);

    if (Properties.NEW_STATISTICS)
      ga.addListener(new StatisticsListener());

    // When to stop the search
    StoppingCondition stopping_condition = getStoppingCondition();
    ga.setStoppingCondition(stopping_condition);

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

    if (Properties.CHECK_BEST_LENGTH) {
      RelativeSuiteLengthBloatControl bloat_control =
          new org.evosuite.testsuite.RelativeSuiteLengthBloatControl();
      ga.addBloatControl(bloat_control);
      ga.addListener(bloat_control);
    }

    TestCaseSecondaryObjective.setSecondaryObjectives();

    if (Properties.DYNAMIC_LIMIT) {
      // max_s = GAProperties.generations * getBranches().size();
      // TODO: might want to make this dependent on the selected coverage
      // criterion
      // TODO also, question: is branchMap.size() really intended here?
      // I think BranchPool.getBranchCount() was intended
      Properties.SEARCH_BUDGET = Properties.SEARCH_BUDGET
          * (BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
              .getNumBranchlessMethods(Properties.TARGET_CLASS)
              + BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
                  .getBranchCountForClass(Properties.TARGET_CLASS) * 2);
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

      Signal.handle(new Signal("INT"), writer);
    }

    ga.addListener(new ResourceController());
    return ga;
  }
}
