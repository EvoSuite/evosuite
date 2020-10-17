package org.evosuite.strategy;

import org.evosuite.Properties;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.mapelites.MAPElites;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MAPElitesStrategy extends TestGenerationStrategy {
  private static final Logger logger = LoggerFactory.getLogger(MAPElitesStrategy.class);

  @Override
  public TestSuiteChromosome generateTests() {
    // Set up search algorithm
    LoggingUtils.getEvoLogger().info("* Setting up search algorithm for MAP-Elites search with choice {}", Properties.MAP_ELITES_CHOICE.name());

    PropertiesMapElitesSearchFactory algorithmFactory = new PropertiesMapElitesSearchFactory();
    MAPElites algorithm = algorithmFactory.getSearchAlgorithm();

    if (Properties.SERIALIZE_GA || Properties.CLIENT_ON_THREAD)
      TestGenerationResultBuilder.getInstance().setGeneticAlgorithm(algorithm);

    long startTime = System.currentTimeMillis() / 1000;

    // What's the search target
    List<TestSuiteFitnessFunction> fitnessFunctions = getFitnessFunctions();

    if (Properties.TRACK_DIVERSITY) {
      //  DiversityObserver requires TestSuiteChromosomes, but the MAPElites algorithm only works
      //  with TestChromosomes.
      throw new RuntimeException("Tracking population diversity is not supported by MAPElites");
    }

    if (ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.DEFUSE)
        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.ALLDEFS)
        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.STATEMENT)
        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.RHO)
        || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.AMBIGUITY))
      ExecutionTracer.enableTraceCalls();

    algorithm.resetStoppingConditions();
    
    List<TestFitnessFunction> goals = this.getGoals();
    
    algorithm.addTestFitnessFunctions(goals);
    
    if (!canGenerateTestsForSUT()) {
      LoggingUtils.getEvoLogger()
          .info("* Found no testable methods in the target class " + Properties.TARGET_CLASS);
      
      ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());

      return new TestSuiteChromosome();
    }
    
 // Perform search
    LoggingUtils.getEvoLogger().info("* Using seed {}", Randomness.getSeed() );
    LoggingUtils.getEvoLogger().info("* Starting evolution");
    ClientServices.getInstance().getClientNode().changeState(ClientState.SEARCH);

    algorithm.generateSolution();
    TestSuiteChromosome testSuite = getSuiteWithFitness(algorithm, fitnessFunctions);

    long endTime = System.currentTimeMillis() / 1000;
    
    ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Goals, goals.size());
    
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

  private TestSuiteChromosome createMergedSolution(Collection<TestChromosome> population) {
    TestSuiteChromosome suite = new TestSuiteChromosome();
    suite.addTests(population);
    return suite;
  }

  private TestSuiteChromosome getSuiteWithFitness(GeneticAlgorithm<TestChromosome> algorithm, List<TestSuiteFitnessFunction> fitnessFunctions) {
    List<TestChromosome> population = algorithm.getPopulation();
    TestSuiteChromosome suite = createMergedSolution(population);
    for (TestSuiteFitnessFunction fitnessFunction : fitnessFunctions) {
      fitnessFunction.getFitness(suite);
    }

    return suite;
  }

  private List<TestFitnessFunction> getGoals() {
    List<TestFitnessFactory<? extends TestFitnessFunction>> goalFactories = getFitnessFactories();
    List<TestFitnessFunction> fitnessFunctions = new ArrayList<>();
          for (TestFitnessFactory<? extends TestFitnessFunction> goalFactory : goalFactories) {
              fitnessFunctions.addAll(goalFactory.getCoverageGoals());
          }
    return fitnessFunctions;
  }
}
