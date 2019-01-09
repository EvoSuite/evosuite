package org.evosuite.ga.metaheuristics.mapelites;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.runtime.Random;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.IterUtil;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MAP-Elites implementation
 * 
 * <p>
 * <b>Reference: </b> Mouret, Jean-Baptiste, and Jeff Clune. "Illuminating search spaces by mapping
 * elites." arXiv preprint arXiv:1504.04909 (2015).
 * </p>
 * 
 * @author Felix Prasse
 *
 * @param <T> Solution type
 */
public class MAPElites<T extends Chromosome> extends GeneticAlgorithm<T> {
  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(MAPElites.class);



  // TODO Replace this with a proper archive.
  // branch -> feature (stop when coverage for branch reached)
  private final Map<BranchCoverageTestFitness, Map<FeatureVector, Chromosome>> populationMap;

  public MAPElites(ChromosomeFactory<T> factory) {
    super(factory);


    TestCaseExecutor.getInstance().addObserver(new TestResultObserver());

    List<BranchCoverageTestFitness> branchCoverage = new BranchCoverageFactory().getCoverageGoals();

    this.populationMap = new HashMap<>(branchCoverage.size());
    branchCoverage.forEach(branch -> this.populationMap.put(branch, new HashMap<>()));
  }

  @Override
  protected void evolve() {
    for (Entry<BranchCoverageTestFitness, Map<FeatureVector, Chromosome>> entry : populationMap
        .entrySet()) {
      // TODO How to determine whether a branch is covered already?

      final double chance = 1.0 / populationMap.size();
      if (Random.nextDouble() >= chance) {
        Chromosome chromosome = Randomness.choice(entry.getValue().values());

        notifyMutation(chromosome);
        chromosome.mutate();

        // Determine fitness
        calculateFitness();

        analyzeChromosome(chromosome);
      }
    }

    // TODO Own strategy based upon NoveltyStrategy?

    ++currentIteration;
  }

  private void storeIfBetter(final Chromosome chromosome, final double fitness,
      final BranchCoverageTestFitness branchFitness, final FeatureVector feature) {

    final Map<FeatureVector, Chromosome> featureMap = this.populationMap.get(branchFitness);

    Chromosome old = featureMap.get(feature);

    if (old == null || old.getFitness(branchFitness) > fitness) {
      featureMap.put(feature, chromosome);
    }
  }

  private void analyzeTestChromosome(final TestChromosome chromosome) {
    final ExecutionResult executionResult = chromosome.getLastExecutionResult();
    final List<FeatureVector> features = executionResult.getTrace().getFeatureVectors();

    for (BranchCoverageTestFitness branchFitness : this.populationMap.keySet()) {

      final double fitness = branchFitness.getFitness(chromosome, executionResult);
      for (FeatureVector feature : features) {
        storeIfBetter(chromosome, fitness, branchFitness, feature);
      }
    }
  }


  private void analyzeChromosome(Chromosome chromosome) {
    if (chromosome instanceof TestChromosome) {
      analyzeTestChromosome((TestChromosome) chromosome);
    } else {
      /*
       * TODO Could either work with TestSuiteChromosome#getLastExecutionResults or
       * TestSuiteChromosome#getTestChromosomes This situation occurs when Properties.STRATEGY =
       * WholeTestSuiteStrategy Should this be implemented or should we simply restrict this
       * algorithm to TestChromosome? If we restrict it update the generics!
       */


      throw new UnsupportedOperationException(
          "Chromosome of type " + chromosome.getClass().getName() + " is not supported");
    }
  }


  @Override
  public void initializePopulation() {
    notifySearchStarted();
    currentIteration = 0;

    // Set up initial population
    generateInitialPopulation(Properties.POPULATION);
    // Determine fitness
    calculateFitness();

    for (T chromosome : this.population) {
      this.analyzeChromosome(chromosome);
    }
  }



  @Override
  public void generateSolution() {
    if (population.isEmpty()) {
      initializePopulation();
      assert !population.isEmpty() : "Could not create any test";
    }

    currentIteration = 0;

    while (!isFinished()) {
      evolve();
      this.notifyIteration();
    }

    updateBestIndividualFromArchive();
    notifySearchFinished();
  }

}
