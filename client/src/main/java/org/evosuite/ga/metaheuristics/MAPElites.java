package org.evosuite.ga.metaheuristics;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
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

  private Map<Object, T> populationMap;

  public MAPElites(ChromosomeFactory<T> factory) {
    super(factory);
    this.populationMap = new HashMap<>();
  }

  @Override
  protected void evolve() {
    T chromosome = Randomness.choice(population);

    // TODO Can also use crossover!
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

    T existing = this.populationMap.get(featureDesc);
    if (null == existing || existing.getFitness() < chromosome.getFitness()) {
      this.populationMap.put(featureDesc, chromosome);
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

    // TODO Feature descriptor

    // TODO Store

    this.notifyIteration();
  }

  @Override
  public void generateSolution() {
    // TODO Auto-generated method stub

  }

}
