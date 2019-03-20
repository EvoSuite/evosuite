package org.evosuite.ga.metaheuristics.mapelites;

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
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.TestCaseExecutor;
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
public class MAPElites<T extends TestChromosome> extends GeneticAlgorithm<T> {
  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(MAPElites.class);

  private final Map<Branch, Map<FeatureVector, T>> populationMap;
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
      this.populationMap.put(new Branch(branch), new LinkedHashMap<>());
    });
  }
  
  /**
   * Mutate one branch on average
   * @return The chromosomes to be mutated
   */
  private Set<T> getToMutateWithChance() {
    Set<T> toMutate = new LinkedHashSet<>(1);

    List<Branch> minima = getMinimalBranches();
    
    final double chance = 1.0 / minima.size();
    
    for (Branch branch : minima) {
      if (Randomness.nextDouble() <= chance) {
        branch.getCounter().increment();
        
        T chromosome = Randomness.choice(this.populationMap.get(branch).values());
        
        if(chromosome != null) {
          toMutate.add(chromosome);
        }
      }
    }
    
    return toMutate;
  }
  
  
  /**
   * Mutate every branch
   * @return The chromosomes to be mutated
   */
  private Set<T> getToMutateAll() {
    Set<T> toMutate = new LinkedHashSet<>(populationMap.values().size());
    
    for(Map<FeatureVector, T> entry : populationMap.values()) {
      T chromosome = Randomness.choice(entry.values());
      
      if(chromosome != null) {
        toMutate.add(chromosome);
      }
    }
    
    return toMutate;
  }
  
  private List<Branch> getMinimalBranches() {
    return IterUtil.minList(this.populationMap.keySet(), 
        (a,b) -> a.getCounter().compareTo(b.getCounter()));
  }
  
  /**
   * Mutate exactly one branch and one chromosome
   * @return The chromosomes to be mutated
   */
  private Set<T> getToMutateRandom() {
    Set<T> toMutate = new LinkedHashSet<>(1);
    
    List<Branch> minima = getMinimalBranches();
    
    Branch selectedBranch = Randomness.choice(minima);
    
    if(selectedBranch == null) {
      return toMutate;
    }
    
    selectedBranch.getCounter().increment();
    Map<FeatureVector, T> entry = this.populationMap.get(selectedBranch);
    
    T chromosome = Randomness.choice(entry.values());
    
    if(chromosome != null) {
      toMutate.add(chromosome);
    }

    return toMutate;
  }
  
  @Override
  protected void evolve() {
    Set<T> toMutate;
    
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
      Chromosome clone = chromosome.clone();
      T mutation = (T)clone;
      notifyMutation(mutation);
      mutation.mutate();
      
      if(mutation.isChanged() && !isTooLong(mutation)) {
        this.analyzeChromosome(mutation);
      }
    }
    
    if((toMutate.isEmpty() && Properties.MAP_ELITES_CHOICE != Properties.MapElitesChoice.SINGLE_AVG)
        || Randomness.nextDouble() <= Properties.MAP_ELITES_RANDOM) {
      this.analyzeChromosome(this.getRandomPopulation(1).get(0));
    }

    ++currentIteration;
  }
  
  private int getFoundVectorCount() {
    Set<FeatureVector>  vectors = new LinkedHashSet<>();
    
    this.populationMap.values().forEach(entry -> vectors.addAll(entry.keySet()));
        
    vectors.addAll(this.droppedFeatureVectors);
    
    return vectors.size();
  }
  
  private void sendFeatureData() {
    int foundVectorCount = this.getFoundVectorCount();
    double density = this.getDensity(foundVectorCount);
    
    ClientServices.getInstance().getClientNode()
    .trackOutputVariable(RuntimeVariable.DensityTimeline, density);
    
    ClientServices.getInstance().getClientNode()
    .trackOutputVariable(RuntimeVariable.FeaturesFound, foundVectorCount);
  }
  
  private double getDensity(int foundVectorCount) {
    int n = this.featureVectorPossibilityCount;
    int z = this.getFoundVectorCount();
    
    double density = z/(double)n;
    return density;
  }
  
  private void analyzeChromosome(final T chromosome) {
    final Iterator<Entry<Branch, Map<FeatureVector, T>>> it =
        this.populationMap.entrySet().iterator();

    while (it.hasNext()) {
      final Entry<Branch, Map<FeatureVector, T>> entry = it.next();
      final Branch branchFitness = entry.getKey();
      final Map<FeatureVector, T> featureMap = entry.getValue(); 
      
      final double fitness = branchFitness.getFitness(chromosome);
      
      final List<FeatureVector> features = chromosome.getLastExecutionResult().getFeatureVectors();

      for (FeatureVector feature : features) {
        T old = featureMap.get(feature);

        if (old == null || branchFitness.getFitness(old) >= fitness) {
          featureMap.put(feature, chromosome);
          branchFitness.getCounter().reset();
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

    if(population.isEmpty()) {
      throw new IllegalStateException();
    }
    
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
    
    ClientServices.getInstance().getClientNode()
    .trackOutputVariable(RuntimeVariable.FeatureSize, this.featureVectorPossibilityCount);

    this.sendFeatureData();
    
    while (!isFinished()) {
      evolve();
      
      this.sendFeatureData();
      
      this.notifyIteration();
    }

    updateAndSortBest();
    notifySearchFinished();
  }

}
