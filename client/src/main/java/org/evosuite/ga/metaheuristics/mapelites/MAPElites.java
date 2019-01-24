package org.evosuite.ga.metaheuristics.mapelites;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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
  
  public MAPElites(ChromosomeFactory<T> factory) {
    super(factory);

    this.droppedFeatureVectors = new HashSet<>();
    TestResultObserver observer = new TestResultObserver();
    this.featureVectorPossibilityCount = observer.getPossibilityCount();
    TestCaseExecutor.getInstance().addObserver(observer);

    List<BranchCoverageTestFitness> branchCoverage = new BranchCoverageFactory().getCoverageGoals();

    this.populationMap = new LinkedHashMap<>(branchCoverage.size());
    branchCoverage.forEach(branch -> {
      this.populationMap.put(branch, new LinkedHashMap<>());
      // TODO This will not work with a subclass of TestChromosome...
      super.addFitnessFunction((FitnessFunction<T>) branch);
    });
  }
  
  @Override
  protected void evolve() {
    final double chance = 1.0 / populationMap.size();
    
    // Required to prevent concurrent modification
    List<T> toMutate = new ArrayList<>(1);
    
    for (Entry<BranchCoverageTestFitness, Map<FeatureVector, T>> entry : populationMap
        .entrySet()) {
     
      if (Randomness.nextDouble() <= chance) {
        T chromosome = Randomness.choice(entry.getValue().values());
        toMutate.add(chromosome);
      }
    }
    
    for(T chromosome : toMutate) {
      notifyMutation(chromosome);
      chromosome.mutate();

      analyzeChromosome(chromosome);
    }

    ++currentIteration;
  }

  private double getDensity() {
    int n = this.featureVectorPossibilityCount;
    Set<FeatureVector>  vectors = this.populationMap
        .values()
        .stream()
        .flatMap(m -> m.keySet().stream())
        .collect(Collectors.toSet());
        
    vectors.addAll(this.droppedFeatureVectors);
    
    int z = vectors.size();
    
    // TODO MOSA Sparcity + Add feature vector extraction there for timing
    
    return z/(double)n;
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
        // Remove from map. Covering chromosomes are stored in Archive.getArchiveInstance().
        it.remove();
        
        this.droppedFeatureVectors.addAll(featureMap.keySet());
      }
    }
  }


  @Override
  public void initializePopulation() {
    notifySearchStarted();
    currentIteration = 0;

    // Set up initial population
    generateInitialPopulation(Properties.POPULATION);

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

    ClientServices.getInstance().getClientNode()
    .trackOutputVariable(RuntimeVariable.DensityTimeline, this.getDensity());
    
    while (!isFinished()) {
      evolve();
      
      ClientServices.getInstance().getClientNode()
      .trackOutputVariable(RuntimeVariable.DensityTimeline, this.getDensity());
      
      // TODO Generate TestSuite and call BranchCoverageSuiteFitness
      
      this.notifyIteration();
    }

    updateBestIndividualFromArchive();
    notifySearchFinished();
  }

}
