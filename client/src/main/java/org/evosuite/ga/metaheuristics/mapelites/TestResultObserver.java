package org.evosuite.ga.metaheuristics.mapelites;

import java.io.Serializable;
import java.util.Arrays;

import org.evosuite.Properties;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorManager;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;

/**
 * 
 * @author Felix Prasse
 *
 */
public class TestResultObserver extends ExecutionObserver implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private final Inspector[] inspectors;

  private final Class<?> targetClass;

  public TestResultObserver() {
    this.targetClass = Properties.getInitializedTargetClass();
    
    this.inspectors =
        InspectorManager.getInstance().getInspectors(this.targetClass).toArray(new Inspector[0]);

    // Sort by method name to ensure a consistent feature vector order.
    Arrays.sort(this.inspectors, (a, b) -> a.getMethodCall().compareTo(b.getMethodCall()));
  }
  
  public int getPossibilityCount() {
    return FeatureVector.getPossibilityCount(this.inspectors);
  }
  
  public int getFeatureVectorLength() {
	  return this.inspectors.length;
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
      result.addFeatureVector(vector);
    }
  }

  @Override
  public void clear() {
    //  Do nothing
  }
}