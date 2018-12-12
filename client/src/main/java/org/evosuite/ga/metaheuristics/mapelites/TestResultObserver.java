package org.evosuite.ga.metaheuristics.mapelites;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorManager;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;

/**
 * 
 * @author Felix Prasse
 *
 */
class TestResultObserver extends ExecutionObserver implements TestFeatureMap, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private final Inspector[] inspectors;
  private final Map<TestCase, FeatureVector> caseToFeature;
  private final Class<?> targetClass;

  public TestResultObserver() {
    this.caseToFeature = new HashMap<>();
    this.targetClass = this.getTargetClass();
    
    this.inspectors =
        InspectorManager.getInstance().getInspectors(this.targetClass).toArray(new Inspector[0]);

    // Sort by method name to ensure a consistent feature vector order.
    Arrays.sort(this.inspectors, (a, b) -> a.getMethodCall().compareTo(b.getMethodCall()));
  }
  
  private Class<?> getTargetClass() {
    try {
      return TestGenerationContext.getInstance().getClassLoaderForSUT()
          .loadClass(RuntimeSettings.className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
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
    for(Object instance : scope.getObjects(this.targetClass)) {
      this.caseToFeature.put(result.test, new FeatureVector(this.inspectors, instance));
    }
  }

  @Override
  public void clear() {
    //  Do nothing
  }

  @Override
  public FeatureVector get(TestCase test) {
    return this.caseToFeature.get(test);
  }
}