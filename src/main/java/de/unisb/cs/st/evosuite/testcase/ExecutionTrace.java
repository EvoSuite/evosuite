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

	public double getFalseDistance(int branchId);

	public double getTrueDistance(int branchId);

	public Set<Integer> getCoveredTrueBranches();

	public Set<Integer> getCoveredFalseBranches();

	public Set<Integer> getCoveredPredicates();

	public Map<Integer, Integer> getPredicateExecutionCount();

	public Map<String, Integer> getMethodExecutionCount();

	public boolean hasTrueDistance(int predicateId);

	public boolean hasFalseDistance(int predicateId);

	public Map<Integer, Double> getTrueDistances();

	public Map<Integer, Double> getFalseDistances();

	public Set<Integer> getCoveredLines(String className);

	public Map<String, Map<String, Map<Integer, Integer>>> getCoverageData();

	public Map<String, Map<String, Map<Integer, Integer>>> getReturnData();

	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getDefinitionData();

	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getUseData();

	public List<MethodCall> getMethodCalls();

	/**
	 * Reset to 0
	 */
	public void clear();

	/**
	 * Create a lazy copy
	 */
	// public ExecutionTrace clone();

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

	public Set<String> getCoveredMethods();

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

	public void mutationPassed(int mutationId, double distance);

	public double getMutationDistance(int mutationId);

	public Map<Integer, Double> getMutationDistances();

	public boolean wasMutationTouched(int mutationId);

	public Set<Integer> getTouchedMutants();

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

	public Map<Integer, HashMap<Integer, Integer>> getPassedDefinitions(
	        String variableName);

	public Map<Integer, HashMap<Integer, Integer>> getPassedUses(String variableName);

	public Throwable getExplicitException();

	public void setExplicitException(Throwable explicitException);

	public ExecutionTrace lazyClone();
}
