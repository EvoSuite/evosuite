/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.coverage.dataflow.DefUse;

/**
 * This interface defines the trace data that is collected during execution.
 * 
 * @author Gordon Fraser
 * 
 */
public interface ExecutionTrace {

	/**
	 * Add branch to currently active method call
	 * 
	 * @param branch
	 * @param true_distance
	 * @param false_distance
	 */
	public void branchPassed(int branch, int bytecode_id, double true_distance,
	        double false_distance);

	/**
	 * Retrieve minimum branch distance to false branch
	 * 
	 * @param branchId
	 * @return
	 */
	public double getFalseDistance(int branchId);

	/**
	 * Retrieve minimum branch distance to true branch
	 * 
	 * @param branchId
	 * @return
	 */
	public double getTrueDistance(int branchId);

	/**
	 * Retrieve set of branches that evaluated to true
	 * 
	 * @return
	 */
	public Set<Integer> getCoveredTrueBranches();

	/**
	 * Retrieve set of branches that evaluated to false
	 * 
	 * @return
	 */
	public Set<Integer> getCoveredFalseBranches();

	/**
	 * Retrieve set of branches that were executed
	 * 
	 * @return
	 */
	public Set<Integer> getCoveredPredicates();

	/**
	 * Retrieve execution counts for branches
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getPredicateExecutionCount();

	/**
	 * Retrieve execution counts for methods
	 * 
	 * @return
	 */
	public Map<String, Integer> getMethodExecutionCount();

	/**
	 * Determine if a branch has a true distance stored
	 * 
	 * @param predicateId
	 * @return
	 */
	public boolean hasTrueDistance(int predicateId);

	/**
	 * Determine if a branch has a false distance stored
	 * 
	 * @param predicateId
	 * @return
	 */
	public boolean hasFalseDistance(int predicateId);

	/**
	 * Retrieve map of all minimal true distances
	 * 
	 * @return
	 */
	public Map<Integer, Double> getTrueDistances();

	/**
	 * Retrieve map of all minimal false distances
	 * 
	 * @return
	 */
	public Map<Integer, Double> getFalseDistances();

	/**
	 * Retrieve the set of line numbers covered
	 * 
	 * @param className
	 * @return
	 */
	public Set<Integer> getCoveredLines(String className);

	/**
	 * Retrieve detailed line coverage count
	 * 
	 * @return
	 */
	public Map<String, Map<String, Map<Integer, Integer>>> getCoverageData();

	/**
	 * Retrieve return value data
	 * 
	 * @return
	 */
	public Map<String, Map<String, Map<Integer, Integer>>> getReturnData();

	/**
	 * Retrieve data definitions
	 * 
	 * @return
	 */
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getDefinitionData();

	/**
	 * Retrieve data uses
	 * 
	 * @return
	 */
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getUseData();

	/**
	 * Retrieve the data definitions for a given variable
	 * 
	 * @param variableName
	 * @return
	 */
	public Map<Integer, HashMap<Integer, Integer>> getPassedDefinitions(
	        String variableName);

	/**
	 * Retrieve the data uses for a given variable
	 * 
	 * @param variableName
	 * @return
	 */
	public Map<Integer, HashMap<Integer, Integer>> getPassedUses(String variableName);

	/**
	 * Retrieve the exception thrown in this trace
	 * 
	 * @return
	 */
	public Throwable getExplicitException();

	/**
	 * Retrieve all traced method calls
	 * 
	 * @return
	 */
	public List<MethodCall> getMethodCalls();

	/**
	 * Retrieve the names of all called methods
	 * 
	 * @return
	 */
	public Set<String> getCoveredMethods();

	/**
	 * Retrieve the minimum infection distance for a mutant
	 * 
	 * @param mutationId
	 * @return
	 */
	public double getMutationDistance(int mutationId);

	/**
	 * Retrieve all minimal infection distances
	 * 
	 * @return
	 */
	public Map<Integer, Double> getMutationDistances();

	/**
	 * Determine is a mutant was executed
	 * 
	 * @param mutationId
	 * @return
	 */
	public boolean wasMutationTouched(int mutationId);

	/**
	 * Retrieve IDs of all executed mutants
	 * 
	 * @return
	 */
	public Set<Integer> getTouchedMutants();

	/**
	 * Reset to 0
	 */
	public void clear();

	/**
	 * Adds Definition-Use-Coverage trace information for the given definition.
	 * 
	 * Registers the given caller-Object Traces the occurrence of the given
	 * definition in the passedDefs-field Sets the given definition as the
	 * currently active one for the definitionVariable in the
	 * activeDefinitions-field Adds fake trace information to the currently
	 * active MethodCall in this.stack
	 */
	public void definitionPassed(Object caller, int defID);

	/**
	 * Add a new method call to stack
	 * 
	 * @param className
	 * @param methodName
	 */
	public void enteredMethod(String className, String methodName, Object caller);

	/**
	 * Pop last method call from stack
	 * 
	 * @param classname
	 * @param methodname
	 */
	public void exitMethod(String classname, String methodname);

	/**
	 * Finish all method calls. This is called when a method is not exited
	 * regularly, but through an exception
	 */
	public void finishCalls();

	/**
	 * Returns a copy of this trace where all MethodCall-information traced from
	 * objects other then the one identified by the given objectID is removed
	 * from the finished_calls-field
	 * 
	 * WARNING: this will not affect this.true_distances and other fields of
	 * ExecutionTrace this only affects the finished_calls field (which should
	 * suffice for BranchCoverageFitness-calculation)
	 */
	public ExecutionTrace getTraceForObject(int objectId);

	/**
	 * Returns a copy of this trace where all MethodCall-information associated
	 * with duCounters outside the range of the given duCounter-Start and -End
	 * is removed from the finished_calls-traces
	 * 
	 * finished_calls without any point in the trace at which the given
	 * duCounter range is hit are removed completely
	 * 
	 * Also traces for methods other then the one that holds the given targetDU
	 * are removed as well as trace information that would pass the branch of
	 * the given targetDU If wantToCoverTargetDU is false instead those
	 * targetDUBranch information is removed that would pass the alternative
	 * branch of targetDU
	 * 
	 * The latter is because this method only gets called when the given
	 * targetDU was not active in the given duCounter-range if and only if
	 * wantToCoverTargetDU is set, and since useFitness calculation is on branch
	 * level and the branch of the targetDU can be passed before the targetDU is
	 * passed this can lead to a flawed branchFitness.
	 * 
	 * 
	 * WARNING: this will not affect this.true_distances and other fields of
	 * ExecutionTrace this only affects the finished_calls field (which should
	 * suffice for BranchCoverageFitness-calculation)
	 */
	public ExecutionTrace getTraceInDUCounterRange(DefUse targetDU,
	        boolean wantToCoverTargetDU, int duCounterStart, int duCounterEnd);

	/**
	 * Add line to currently active method call
	 * 
	 * @param line
	 */
	public void linePassed(String className, String methodName, int line);

	/**
	 * Record a mutant execution
	 * 
	 * @param mutationId
	 * @param distance
	 */
	public void mutationPassed(int mutationId, double distance);

	/**
	 * Record a return value
	 * 
	 * @param className
	 * @param methodName
	 * @param value
	 */
	public void returnValue(String className, String methodName, int value);

	/**
	 * Returns a String containing the information in passedDefs and passedUses
	 * 
	 * Used for Definition-Use-Coverage-debugging
	 */
	public String toDefUseTraceInformation();

	/**
	 * Returns a String containing the information in passedDefs and passedUses
	 * filtered for a specific variable
	 * 
	 * Used for Definition-Use-Coverage-debugging
	 */
	public String toDefUseTraceInformation(String targetVar);

	/**
	 * Returns a String containing the information in passedDefs and passedUses
	 * for the given variable
	 * 
	 * Used for Definition-Use-Coverage-debugging
	 */
	public String toDefUseTraceInformation(String var, int objectId);

	/**
	 * Adds Definition-Use-Coverage trace information for the given use.
	 * 
	 * Registers the given caller-Object Traces the occurrence of the given use
	 * in the passedUses-field
	 */
	public void usePassed(Object caller, int useID);

	/**
	 * Set the exception thrown in this trace
	 * 
	 * @param explicitException
	 */
	public void setExplicitException(Throwable explicitException);

	/**
	 * Create a lazy copy
	 * 
	 * @return
	 */
	public ExecutionTrace lazyClone();
}
