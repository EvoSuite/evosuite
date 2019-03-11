package org.evosuite.ga.metaheuristics.mapelites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
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
public class MAPElites<T extends TestChromosome> extends GeneticAlgorithm<T> {
  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(MAPElites.class);

  private final Map<BranchCoverageTestFitness, Map<FeatureVector, T>> populationMap;
  private final Set<FeatureVector> droppedFeatureVectors;
  
  private final int featureVectorPossibilityCount;
  
  private final List<T> bestIndividuals;
  
  public MAPElites(ChromosomeFactory<T> factory) {
    super(factory);
    this.bestIndividuals = new LinkedList<>();
    this.droppedFeatureVectors = new LinkedHashSet<>();
    TestResultObserver observer = new TestResultObserver();
    this.featureVectorPossibilityCount = observer.getPossibilityCount();
    TestCaseExecutor.getInstance().addObserver(observer);

    List<BranchCoverageTestFitness> branchCoverage = new BranchCoverageFactory().getCoverageGoals();

    this.populationMap = new LinkedHashMap<>(branchCoverage.size());
    branchCoverage.forEach(branch -> {
      this.populationMap.put(branch, new LinkedHashMap<>());
    });
  }
  
  /**
   * Mutate one branch on average
   * @return The chromosomes to be mutated
   */
  private List<T> getToMutateWithChance() {
    final double chance = 1.0 / populationMap.size();

    // Required to prevent concurrent modification
    List<T> toMutate = new ArrayList<>(1);

    for (Entry<BranchCoverageTestFitness, Map<FeatureVector, T>> entry : populationMap.entrySet()) {

      if (Randomness.nextDouble() <= chance) {
        T chromosome = Randomness.choice(entry.getValue().values());
        toMutate.add(chromosome);
      }
    }
    
    return toMutate;
  }
  
  
  /**
   * Mutate every branch
   * @return The chromosomes to be mutated
   */
  private List<T> getToMutateAll() {
    List<T> toMutate = new ArrayList<>(populationMap.values().size());
    
    for(Map<FeatureVector, T> entry : populationMap.values()) {
      T chromosome = Randomness.choice(entry.values());
      toMutate.add(chromosome);
    }
    
    return toMutate;
  }
  
  /**
   * Mutate exactly one branch and one chromosome
   * @return The chromosomes to be mutated
   */
  private List<T> getToMutateRandom() {
    List<T> toMutate = new ArrayList<>(1);
    Map<FeatureVector, T> entry = Randomness.choice(populationMap.values());
    T chromosome = Randomness.choice(entry.values());
    toMutate.add(chromosome);
    return toMutate;
  }
  
  @Override
  protected void evolve() {
    List<T> toMutate;
    
    switch(Properties.MAP_ELITES_CHOICE) {
      case ALL:
        toMutate = this.getToMutateAll();
        break;
      case SINGLE:
        toMutate = this.getToMutateRandom();
        break;
      default:
      case SINGLE_AVG:
        toMutate = this.getToMutateWithChance();
        break;
    }
    
    for(T chromosome : toMutate) {
      T mutation = (T)chromosome.clone();
      notifyMutation(mutation);
      mutation.mutate();
      this.add(mutation);
    }
    
    if(Randomness.nextBoolean()) {
      this.add(this.getRandomPopulation(1).get(0));
    }

    ++currentIteration;
  }
  
  private void add(T chromosome) {
    analyzeChromosome(chromosome);
  }

  private double getDensity() {
    int n = this.featureVectorPossibilityCount;
    
    Set<FeatureVector>  vectors = new LinkedHashSet<>();
    
    this.populationMap.values().forEach(entry -> vectors.addAll(entry.keySet()));
        
    vectors.addAll(this.droppedFeatureVectors);
    
    int z = vectors.size();
    
    // TODO MOSA Sparcity + Add feature vector extraction there for timing
    
    double density = z/(double)n;
    logger.debug("Density: {}, Vectors: {}", density, vectors);
    return density;
  }
  
  private void analyzeChromosome(final T chromosome) {
    final Iterator<Entry<BranchCoverageTestFitness, Map<FeatureVector, T>>> it =
        this.populationMap.entrySet().iterator();

    while (it.hasNext()) {
      final Entry<BranchCoverageTestFitness, Map<FeatureVector, T>> entry = it.next();
      final BranchCoverageTestFitness branchFitness = entry.getKey();
      final Map<FeatureVector, T> featureMap = entry.getValue(); 
      
      final double fitness = branchFitness.getFitness(chromosome);
      
      final List<FeatureVector> features = chromosome.getLastExecutionResult().getFeatureVectors();

      for (FeatureVector feature : features) {
        T old = featureMap.get(feature);

        if (old == null || old.getFitness(branchFitness) >= fitness) {
          featureMap.put(feature, chromosome);
        }
      }
      
      if(branchFitness.isCovered(chromosome)) {
        // Remove from map. Covering chromosomes are stored in Archive.getArchiveInstance() and this.coveringChromosomes.
        it.remove();
        
        this.droppedFeatureVectors.addAll(featureMap.keySet());
        this.bestIndividuals.add(chromosome);
      }
    }
  }


  @Override
  public void initializePopulation() {
    notifySearchStarted();
    currentIteration = 0;

    // Set up initial population
    List<T> population = this.getRandomPopulation(Properties.POPULATION);

    for (T chromosome : population) {    
      this.analyzeChromosome(chromosome);
    }
  }
  
  @Override
  public T getBestIndividual() {
    if(this.bestIndividuals.isEmpty()) {
        return this.chromosomeFactory.getChromosome();
    }
    
    return this.bestIndividuals.get(0);
  }

  @Override
  public List<T> getBestIndividuals() {
     throw new UnsupportedOperationException();
  }
  
  private void updateAndSortBest() {
    for(Map<FeatureVector, T> branch : this.populationMap.values()) {
        this.bestIndividuals.addAll(branch.values());
    }
    
    if (isMaximizationFunction()) {
      Collections.sort(this.bestIndividuals, Collections.reverseOrder());
    } else {
      Collections.sort(this.bestIndividuals);
    }
  }
  
  @Override
  public List<T> getPopulation() {
    return this.bestIndividuals;
  }
  
  @Override
  public void generateSolution() {
    initializePopulation();

    currentIteration = 0;

    ClientServices.getInstance().getClientNode()
    .trackOutputVariable(RuntimeVariable.DensityTimeline, this.getDensity());
    
    while (!isFinished()) {
      evolve();
      
      ClientServices.getInstance().getClientNode()
      .trackOutputVariable(RuntimeVariable.DensityTimeline, this.getDensity());
      
      this.notifyIteration();
    }

    updateAndSortBest();
    notifySearchFinished();
  }

}
