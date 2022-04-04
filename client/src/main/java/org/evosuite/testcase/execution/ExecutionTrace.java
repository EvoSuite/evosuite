/*
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

package org.evosuite.testcase.execution;

import org.evosuite.coverage.dataflow.DefUse;
import org.evosuite.setup.CallContext;
import org.evosuite.testcase.execution.ExecutionTraceImpl.BranchEval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines the trace data that is collected during execution.
 *
 * @author Gordon Fraser
 */
public interface ExecutionTrace {

    /**
     * Add branch to currently active method call
     *
     * @param branch         a int.
     * @param true_distance  a double.
     * @param false_distance a double.
     * @param bytecode_id    a int.
     */
    void branchPassed(int branch, int bytecode_id, double true_distance, double false_distance);

    /**
     * Retrieve minimum branch distance to false branch
     *
     * @param branchId a int.
     * @return a double.
     */
    double getFalseDistance(int branchId);

    /**
     * Retrieve minimum branch distance to true branch
     *
     * @param branchId a int.
     * @return a double.
     */
    double getTrueDistance(int branchId);

    /**
     * Retrieve set of branches that evaluated to true
     *
     * @return a {@link java.util.Set} object.
     */
    Set<Integer> getCoveredTrueBranches();

    /**
     * Retrieve set of branches that evaluated to false
     *
     * @return a {@link java.util.Set} object.
     */
    Set<Integer> getCoveredFalseBranches();

    /**
     * Retrieve set of branches that were executed
     *
     * @return a {@link java.util.Set} object.
     */
    Set<Integer> getCoveredPredicates();

    /**
     * Retrieve set of definitions that were executed
     *
     * @return a {@link java.util.Set} object.
     */
    Set<Integer> getCoveredDefinitions();

    /**
     * Retrieve execution counts for branches
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Integer> getPredicateExecutionCount();

    /**
     * Retrieve execution counts for methods
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, Integer> getMethodExecutionCount();

    /**
     * Retrieve execution counts for definitions
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Integer> getDefinitionExecutionCount();

    /**
     * Determine if a branch has a true distance stored
     *
     * @param predicateId a int.
     * @return a boolean.
     */
    boolean hasTrueDistance(int predicateId);

    /**
     * Determine if a branch has a false distance stored
     *
     * @param predicateId a int.
     * @return a boolean.
     */
    boolean hasFalseDistance(int predicateId);

    /**
     * Retrieve map of all minimal true distances
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Double> getTrueDistances();

    /**
     * Retrieve map of all minimal false distances
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Double> getFalseDistances();

    /**
     * Retrieve map of all minimal true distances
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Map<CallContext, Double>> getTrueDistancesContext();

    /**
     * Retrieve map of all minimal false distances
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Map<CallContext, Double>> getFalseDistancesContext();

    /**
     * Retrieve map of all context method counts
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, Map<CallContext, Integer>> getMethodContextCount();

    /**
     * Retrieve number of predicate executions
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Map<CallContext, Integer>> getPredicateContextExecutionCount();

    /**
     * Retrieve the set of line numbers covered
     *
     * @param className a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    Set<Integer> getCoveredLines(String className);

    /**
     * Retrieve the set of line numbers covered of
     * {@link org.evosuite.Properties.TARGET_CLASS} class
     *
     * @return a {@link java.util.Set} object.
     */
    Set<Integer> getCoveredLines();

    /**
     * Retrieve the set of all line numbers covered
     *
     * @return
     */
    Set<Integer> getAllCoveredLines();

    /**
     * Retrieve detailed line coverage count
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, Map<String, Map<Integer, Integer>>> getCoverageData();

    /**
     * Retrieve return value data
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, Map<String, Map<Integer, Integer>>> getReturnData();

    /**
     * Retrieve data definitions
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getDefinitionData();

    /**
     * Retrieve data definitions
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, HashMap<Integer, HashMap<Integer, Object>>> getDefinitionDataObjects();

    /**
     * Retrieve data uses
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getUseData();

    /**
     * Retrieve data uses
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, HashMap<Integer, HashMap<Integer, Object>>> getUseDataObjects();

    /**
     * Retrieve the data definitions for a given variable
     *
     * @param variableName a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, HashMap<Integer, Integer>> getPassedDefinitions(String variableName);

    /**
     * Retrieve the data uses for a given variable
     *
     * @param variableName a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, HashMap<Integer, Integer>> getPassedUses(String variableName);

    /**
     * Retrieve the exception thrown in this trace
     *
     * @return a {@link java.lang.Throwable} object.
     */
    Throwable getExplicitException();

    /**
     * Retrieve all traced method calls
     *
     * @return a {@link java.util.List} object.
     */
    List<MethodCall> getMethodCalls();

    /**
     * Retrieve the names of all called methods
     *
     * @return a {@link java.util.Set} object.
     */
    Set<String> getCoveredMethods();

    /**
     * Retrieve the names of all covered branchless methods
     *
     * @return a {@link java.util.Set} object.
     */
    Set<String> getCoveredBranchlessMethods();

    /**
     * Retrieve the minimum infection distance for a mutant
     *
     * @param mutationId a int.
     * @return a double.
     */
    double getMutationDistance(int mutationId);

    /**
     * Retrieve all minimal infection distances
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Double> getMutationDistances();

    /**
     * Determine is a mutant was executed
     *
     * @param mutationId a int.
     * @return a boolean.
     */
    boolean wasMutationTouched(int mutationId);

    /**
     * Retrieve IDs of all executed mutants
     *
     * @return a {@link java.util.Set} object.
     */
    Set<Integer> getTouchedMutants();

    /**
     * Retrieve IDs of all executed mutants with an infection distance == 0.0
     *
     * @return a {@link java.util.Set} object.
     */
    Set<Integer> getInfectedMutants();

    /**
     * Reset to 0
     */
    void clear();

    /**
     * Adds Definition-Use-Coverage trace information for the given definition.
     * <p>
     * Registers the given caller-Object Traces the occurrence of the given
     * definition in the passedDefs-field Sets the given definition as the
     * currently active one for the definitionVariable in the
     * activeDefinitions-field Adds fake trace information to the currently
     * active MethodCall in this.stack
     *
     * @param caller a {@link java.lang.Object} object.
     * @param defID  a int.
     */
    void definitionPassed(Object object, Object caller, int defID);

    /**
     * Add a new method call to stack
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param caller     a {@link java.lang.Object} object.
     */
    void enteredMethod(String className, String methodName, Object caller);

    /**
     * Pop last method call from stack
     *
     * @param classname  a {@link java.lang.String} object.
     * @param methodname a {@link java.lang.String} object.
     */
    void exitMethod(String classname, String methodname);

    /**
     * Finish all method calls. This is called when a method is not exited
     * regularly, but through an exception
     */
    void finishCalls();

    /**
     * Returns a copy of this trace where all MethodCall-information traced from
     * objects other then the one identified by the given objectID is removed
     * from the finished_calls-field
     * <p>
     * WARNING: this will not affect this.true_distances and other fields of
     * ExecutionTrace this only affects the finished_calls field (which should
     * suffice for BranchCoverageFitness-calculation)
     *
     * @param objectId a int.
     * @return a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     */
    ExecutionTrace getTraceForObject(int objectId);

    /**
     * Returns a copy of this trace where all MethodCall-information associated
     * with duCounters outside the range of the given duCounter-Start and -End
     * is removed from the finished_calls-traces
     * <p>
     * finished_calls without any point in the trace at which the given
     * duCounter range is hit are removed completely
     * <p>
     * Also traces for methods other then the one that holds the given targetDU
     * are removed as well as trace information that would pass the branch of
     * the given targetDU If wantToCoverTargetDU is false instead those
     * targetDUBranch information is removed that would pass the alternative
     * branch of targetDU
     * <p>
     * The latter is because this method only gets called when the given
     * targetDU was not active in the given duCounter-range if and only if
     * wantToCoverTargetDU is set, and since useFitness calculation is on branch
     * level and the branch of the targetDU can be passed before the targetDU is
     * passed this can lead to a flawed branchFitness.
     * <p>
     * <p>
     * WARNING: this will not affect this.true_distances and other fields of
     * ExecutionTrace this only affects the finished_calls field (which should
     * suffice for BranchCoverageFitness-calculation)
     *
     * @param targetDU            a {@link org.evosuite.coverage.dataflow.DefUse} object.
     * @param wantToCoverTargetDU a boolean.
     * @param duCounterStart      a int.
     * @param duCounterEnd        a int.
     * @return a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     */
    ExecutionTrace getTraceInDUCounterRange(DefUse targetDU, boolean wantToCoverTargetDU, int duCounterStart,
                                            int duCounterEnd);

    /**
     * Add line to currently active method call
     *
     * @param line       a int.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     */
    void linePassed(String className, String methodName, int line);

    /**
     * Record a mutant execution
     *
     * @param mutationId a int.
     * @param distance   a double.
     */
    void mutationPassed(int mutationId, double distance);

    /**
     * Record a return value
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param value      a int.
     */
    void returnValue(String className, String methodName, int value);

    /**
     * Returns a String containing the information in passedDefs and passedUses
     * <p>
     * Used for Definition-Use-Coverage-debugging
     *
     * @return a {@link java.lang.String} object.
     */
    String toDefUseTraceInformation();

    /**
     * Returns a String containing the information in passedDefs and passedUses
     * filtered for a specific variable
     * <p>
     * Used for Definition-Use-Coverage-debugging
     *
     * @param targetVar a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String toDefUseTraceInformation(String targetVar);

    /**
     * Returns a String containing the information in passedDefs and passedUses
     * for the given variable
     * <p>
     * Used for Definition-Use-Coverage-debugging
     *
     * @param var      a {@link java.lang.String} object.
     * @param objectId a int.
     * @return a {@link java.lang.String} object.
     */
    String toDefUseTraceInformation(String var, int objectId);

    /**
     * Adds Definition-Use-Coverage trace information for the given use.
     * <p>
     * Registers the given caller-Object Traces the occurrence of the given use
     * in the passedUses-field
     *
     * @param caller a {@link java.lang.Object} object.
     * @param useID  a int.
     */
    void usePassed(Object object, Object caller, int useID);

    /**
     * Set the exception thrown in this trace
     *
     * @param explicitException a {@link java.lang.Throwable} object.
     */
    void setExplicitException(Throwable explicitException);

    /**
     * Create a lazy copy
     *
     * @return a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     */
    ExecutionTrace lazyClone();

    /**
     * <p>
     * getBranchesTrace
     * </p>
     *
     * @return a {@link java.util.List} object.
     */
    List<BranchEval> getBranchesTrace();

    /**
     * <p>
     * getFalseDistancesSum
     * </p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Double> getFalseDistancesSum();

    /**
     * <p>
     * getTrueDistancesSum
     * </p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<Integer, Double> getTrueDistancesSum();

    /**
     * <p>
     * getPassedUses
     * </p>
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getPassedUses();

    /**
     * Retrieve the set of all uses by id
     *
     * @return
     */
    Set<Integer> getPassedUseIDs();

    /**
     * Retrieve the set of all definitions by id
     *
     * @return
     */
    @Deprecated
    Set<Integer> getPassedDefIDs();

    /**
     * Record a PUTSTATIC statement
     *
     * @param classNameWithDots
     * @param fieldName
     */
    void putStaticPassed(String classNameWithDots, String fieldName);

    /**
     * Record a GETSTATIC statement
     *
     * @param classNameWithDots
     * @param fieldName
     */
    void getStaticPassed(String classNameWithDots, String fieldName);

    /**
     * Retrieve a list of those classes that were affected by a PUTSTATIC.
     *
     * @return
     */
    Set<String> getClassesWithStaticWrites();

    /**
     * Retrieve a list of those classes that were affected by a GETSTATIC.
     *
     * @return
     */
    Set<String> getClassesWithStaticReads();

    /**
     * Logs that a <clinit> was completed during this test execution
     *
     * @param classNameWithDots
     */
    void classInitialized(String classNameWithDots);

    /**
     * Returns the list (with no repetitions) following the order in which the
     * <clinit> method was finished during this test execution
     *
     * @return
     */
    List<String> getInitializedClasses();
}
