package org.evosuite.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.mapelites.MAPElites;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.novelty.SuiteFitnessEvaluationListener;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.testsuite.similarity.DiversityObserver;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MAPElitesStrategy extends TestGenerationStrategy {
  private static final Logger logger = LoggerFactory.getLogger(MAPElitesStrategy.class);

  @Override
  public TestSuiteChromosome generateTests() {
    // Set up search algorithm
    LoggingUtils.getEvoLogger().info("* Setting up search algorithm for MAP-Elites search with choice {}", Properties.MAP_ELITES_CHOICE.name());

    PropertiesMapElitesSearchFactory algorithmFactory = new PropertiesMapElitesSearchFactory();
    MAPElites<TestChromosome> algorithm = algorithmFactory.getSearchAlgorithm();

    if (Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD)
      TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(algorithm);

    long startTime = System.currentTimeMillis() / 1000;

    // What's the search target
    List<TestSuiteFitnessFunction> fitnessFunctions = getFitnessFunctions();
    SuiteFitnessEvaluationListener listener = new SuiteFitnessEvaluationListener(fitnessFunctions);
    algorithm.addListener(listener);
    
    if (Properties.TRACK_DIVERSITY)
      algorithm.addListener(new DiversityObserver());

    if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.DEFUSE)
        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.ALLDEFS)
        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.STATEMENT)
        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.RHO)
        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.AMBIGUITY))
      ExecutionTracer.enableTraceCalls();

    algorithm.resetStoppingConditions();
    
    if (!canGenerateTestsForSUT()) {
      LoggingUtils.getEvoLogger()
          .info("* Found no testable methods in the target class " + Properties.TARGET_CLASS);
      
      ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, getGoals(true).size());

      return new TestSuiteChromosome();
    }
    
 // Perform search
    LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed() );
    LoggingUtils.getEvoLogger().info("* Starting evolution");
    ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

    algorithm.generateSolution();
    TestSuiteChromosome testSuite = listener.getSuiteWithFitness(algorithm);
    
    long endTime = System.currentTimeMillis() / 1000;
    
    ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, getGoals(false).size());
    
    // Newline after progress bar
    if (Properties.SHOW_PROGRESS)
        LoggingUtils.getEvoLogger().info("");

    if(!Properties.IS_RUNNING_A_SYSTEM_TEST) { //avoid printing time related info in system tests due to lack of determinism
        LoggingUtils.getEvoLogger().info("* Search finished after "
                + (endTime - startTime)
                + "s and "
                + algorithm.getAge()
                + " generations, "
                + MaxStatementsStoppingCondition.getNumExecutedStatements()
                + " statements, best individual has fitness: "
                + testSuite.getFitness());
    }

    // Search is finished, send statistics
    sendExecutionStatistics();
    
    return testSuite;
  }
  
  private List<TestFitnessFunction> getGoals(boolean verbose) {
    List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
    List<TestFitnessFunction> goals = new ArrayList<>();

    if(goalFactories.size() == 1) {
        TestFitnessFactory<? extends TestFitnessFunction> factory = goalFactories.iterator().next();
        goals.addAll(factory.getCoverageGoals());

        if(verbose) {
            LoggingUtils.getEvoLogger().info("* Total number of test goals: {}", factory.getCoverageGoals().size());
            if (Properties.PRINT_GOALS) {
                for (TestFitnessFunction goal : factory.getCoverageGoals())
                    LoggingUtils.getEvoLogger().info("" + goal.toString());
            }
        }
    } else {
        if(verbose) {
            LoggingUtils.getEvoLogger().info("* Total number of test goals: ");
        }

        for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
            goals.addAll(goalFactory.getCoverageGoals());

            if(verbose) {
                LoggingUtils.getEvoLogger().info("  - " + goalFactory.getClass().getSimpleName().replace("CoverageFactory", "")
                        + " " + goalFactory.getCoverageGoals().size());
                if (Properties.PRINT_GOALS) {
                    for (TestFitnessFunction goal : goalFactory.getCoverageGoals())
                        LoggingUtils.getEvoLogger().info("" + goal.toString());
                }
            }
        }
    }
    return goals;
}
}
