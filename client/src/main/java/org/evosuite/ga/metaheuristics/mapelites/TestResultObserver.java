package org.evosuite.ga.metaheuristics.mapelites;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.evosuite.Properties;
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
class TestResultObserver extends ExecutionObserver implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private final Inspector[] inspectors;
  
  // TODO WrapperClass
  private final Class<?> targetClass;

  public TestResultObserver() {
    this.targetClass = this.getTargetClass();
    
    this.inspectors =
        InspectorManager.getInstance().getInspectors(this.targetClass).toArray(new Inspector[0]);

    // Sort by method name to ensure a consistent feature vector order.
    Arrays.sort(this.inspectors, (a, b) -> a.getMethodCall().compareTo(b.getMethodCall()));
  }
  
  private Class<?> getTargetClass() {
    return Properties.getInitializedTargetClass();
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
      FeatureVector vector = new FeatureVector(this.inspectors, instance);
      result.getTrace().addFeatureVector(vector);
    }
  }

  @Override
  public void clear() {
    //  Do nothing
  }
}