package org.evosuite.ga.metaheuristics.mapelites;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.TestCaseExecutor;
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
  private final Map<Object, T> populationMap;
  

  public MAPElites(ChromosomeFactory<T> factory) {
    super(factory);
    this.populationMap = new HashMap<>();
    
    final TestResultObserver observer = new TestResultObserver();
    TestCaseExecutor.getInstance().addObserver(observer);
  }

  @Override
  protected void evolve() {
    T chromosome = Randomness.choice(population);

    notifyMutation(chromosome);
    chromosome.mutate();

    // Determine fitness
    calculateFitness();

    storeIfBetter(chromosome);

    // branch -> feature (stop when coverage for branch reached)
    // TODO Own strategy based upon NoveltyStrategy?
    
    ++currentIteration;
  }

  private void storeIfBetter(T chromosome) {
    // TODO Feature descriptor
    Object featureDesc = null;

    /*
     * Branchcoveragefactory (MOSA)
     */

    T existing = this.populationMap.get(featureDesc);
    if (null == existing || existing.getFitness() < chromosome.getFitness()) {
      this.populationMap.put(featureDesc, chromosome);
    }
  }
  
  private List<FeatureVector> getFeatureVectors(T chromosome) {
    if(chromosome instanceof TestChromosome) {
      ExecutionTrace trace = ((TestChromosome) chromosome).getLastExecutionResult().getTrace();
      return trace.getFeatureVectors();
    } else if(chromosome instanceof TestSuiteChromosome) {
   /*
    *  TODO Could either work with  
    *  TestSuiteChromosome#getLastExecutionResults or TestSuiteChromosome#getTestChromosomes
    *  This situation occurs when Properties.STRATEGY = WholeTestSuiteStrategy
    */
    }
   
    throw new UnsupportedOperationException("Chromosome of type " + chromosome.getClass().getName() + " is not supported");
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
      List<FeatureVector> test = this.getFeatureVectors(chromosome);
      logger.warn("TEST----------", IterUtil.join(test));
    }

    // TODO Store

    this.notifyIteration();
  }

  

  @Override
  public void generateSolution() {
    if (population.isEmpty()) {
      initializePopulation();
      assert!population.isEmpty() : "Could not create any test";
    }
  }

}
