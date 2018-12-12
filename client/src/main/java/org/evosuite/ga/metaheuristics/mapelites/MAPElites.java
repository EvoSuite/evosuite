package org.evosuite.ga.metaheuristics.mapelites;

import java.util.HashMap;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.TestCaseExecutor;
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
  private Map<Object, T> populationMap;
  
  private final TestFeatureMap testFeatureMap; 
  

  public MAPElites(ChromosomeFactory<T> factory) {
    super(factory);
    this.populationMap = new HashMap<>();
    
    TestResultObserver observer = new TestResultObserver();
    TestCaseExecutor.getInstance().addObserver(observer);
    this.testFeatureMap = observer;
  }

  @Override
  protected void evolve() {
    T chromosome = Randomness.choice(population);

    notifyMutation(chromosome);
    chromosome.mutate();

    // Determine fitness
    calculateFitness();

    storeIfBetter(chromosome);

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
  
  private FeatureVector getFeatureVector(T chromosome) {
    // TODO What about TestSuiteChromosome?!
    // TODO Properties.STRATEGY currently runs WholeTestSuiteStrategy resulting in TestSuiteChromosome instead of TestChromosome
    TestCase testCase = ((TestChromosome) chromosome).getTestCase();
    return this.testFeatureMap.get(testCase);
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
      FeatureVector test = this.getFeatureVector(chromosome);
      logger.warn("TEST----------", test);
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
