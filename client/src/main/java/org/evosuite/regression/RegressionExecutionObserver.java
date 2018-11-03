/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.regression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegressionExecutionObserver extends ExecutionObserver {

  private static final Logger logger = LoggerFactory.getLogger(RegressionExecutionObserver.class);

  List<Map<Integer, Map<String, Map<String, Object>>>> currentObjectMapPool = new ArrayList<>();
  List<Map<Integer, Map<String, Map<String, Object>>>> currentRegressionObjectMapPool = new ArrayList<>();
  private boolean isRegression = false;
  private boolean isDisabled = true;
  private List<List<Map<Integer, Map<String, Map<String, Object>>>>> objectMapPool = new ArrayList<>();

  /*
   * Explanation of the "magic" code belows:
   *
   * These are object values, given a scope.
   * List - ([optional] the following data can be obtained after executing each statement), of
   * List - Scope contains a number of statements, of
   * Map - Integer: variable reference position, of
   * Map - String: class of the variable reference, of
   * Map - String: class of the object, of primitive object value.
   */
  private List<List<Map<Integer, Map<String, Map<String, Object>>>>> regressionObjectMapPool = new ArrayList<List<Map<Integer, Map<String, Map<String, Object>>>>>();

  public void enable() {
    isDisabled = false;
  }

  public void disable() {
    isDisabled = true;
  }

  void setRegressionFlag(boolean isRegression) {
    this.isRegression = isRegression;
  }

  @Override
  public void afterStatement(Statement statement, Scope scope,
      Throwable exception) {

  }

  void resetObjPool() {
    currentObjectMapPool = new ArrayList<>();
    currentRegressionObjectMapPool = new ArrayList<>();
  }

  /*
   * The following two methods are for when we measure object distance after each execution
   * (currently unused)
   */
  public void addToPools() {
    objectMapPool.add(currentObjectMapPool);
    regressionObjectMapPool.add(currentRegressionObjectMapPool);
  }

  public void addToPools(
      List<Map<Integer, Map<String, Map<String, Object>>>> currentObjectMapPool,
      List<Map<Integer, Map<String, Map<String, Object>>>> currentRegressionObjectMapPool) {
    objectMapPool.add(currentObjectMapPool);
    regressionObjectMapPool.add(currentRegressionObjectMapPool);
  }

  void clearPools() {
    objectMapPool = new ArrayList<>();
    regressionObjectMapPool = new ArrayList<>();
  }

  @Override
  public void testExecutionFinished(ExecutionResult r, Scope scope) {
    ObjectFields scopeObjectFields = new ObjectFields(scope);

    if (isDisabled) {
      return;
    }

    if (isRegression) {
      currentRegressionObjectMapPool.add(scopeObjectFields.getObjectVariables());
    } else {
      currentObjectMapPool.add(scopeObjectFields.getObjectVariables());
    }
  }

  @Override
  public void output(int position, String output) {

  }

  @Override
  public void beforeStatement(Statement statement, Scope scope) {

  }

  @Override
  public void clear() {

  }

}
