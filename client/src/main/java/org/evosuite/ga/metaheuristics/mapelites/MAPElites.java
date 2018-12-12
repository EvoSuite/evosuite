package org.evosuite.ga.metaheuristics.mapelites;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorManager;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
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

  //private Map<TestCase, >
  
  private class TestResultObserver extends ExecutionObserver {

    @Override
    public void output(int position, String output) {
      // Do nothing
    }

    @Override
    public void beforeStatement(Statement statement, Scope scope) {
      // Do nothing
      
    }

    @Override
    public void afterStatement(Statement statement, Scope scope, Throwable exception) {
      // Do nothing
    }
    
    @Override
    public void testExecutionFinished(ExecutionResult result, Scope scope) {
      for(Object instance : scope.getObjects(targetClass)) {
        
    
        /*
         *  TODO Inspectors are stored in an ArrayList and therefore the order does not seem to change.
         *  Using it this way might prove problematic since that is an implementation detail.
         *  
         *  Sort by alphabet
         */
        //result.test
        /*
         * Store vector in execution result
         */
        
        Object[] featureVector = inspectors.stream().map(inspector -> {
          try {
            return inspector.getValue(instance);
          } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
          }
        }).toArray();
      }
    }

    @Override
    public void clear() {
      // TODO Auto-generated method stub
      
    }
  }
  
  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(MAPElites.class);

  // TODO Replace this with a proper archive.
  private Map<Object, T> populationMap;
  
  private final Class<?> targetClass;
  private final List<Inspector> inspectors;

  public MAPElites(ChromosomeFactory<T> factory) {
    super(factory); 
    this.populationMap = new HashMap<>();
    this.targetClass = getTargetClass();
    
    TestCaseExecutor.getInstance().addObserver(new TestResultObserver());
    
    this.inspectors = InspectorManager.getInstance().getInspectors(this.targetClass);
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


  @Override
  public void initializePopulation() {
    notifySearchStarted();
    currentIteration = 0;

    // Set up initial population
    generateInitialPopulation(Properties.POPULATION);
    // Determine fitness
    calculateFitness();

    for(T chromosome : this.population) {
      TestCase testCase = ((TestChromosome)chromosome).getTestCase();
      // TODO size vs sizeWithAssertions
      List<VariableReference> refs = testCase.getObjects(this.targetClass, testCase.size());
      
      // TODO Obtain scope! (ExecutionObserver?)
      testCase.getObject(refs.get(0), null);
    }
    // TODO Feature descriptor

    // TODO Store

    this.notifyIteration();
  }
  
  private Class<?> getTargetClass() {
    try {
      return TestGenerationContext.getInstance()
          .getClassLoaderForSUT().loadClass(RuntimeSettings.className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void generateSolution() {
    // TODO Auto-generated method stub

  }

}
