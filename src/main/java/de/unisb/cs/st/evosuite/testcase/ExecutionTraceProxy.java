/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.coverage.dataflow.DefUse;
import de.unisb.cs.st.evosuite.testcase.ExecutionTraceImpl.BranchEval;

/**
 * @author gordon
 * 
 */
public class ExecutionTraceProxy implements ExecutionTrace, Cloneable {

	private ExecutionTraceImpl trace;

	public ExecutionTraceProxy() {
		this.trace = new ExecutionTraceImpl();
	}

	public ExecutionTraceProxy(ExecutionTraceImpl trace) {
		this.trace = trace;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#branchPassed(int, int, double, double)
	 */
	@Override
	public void branchPassed(int branch, int bytecode_id, double true_distance,
	        double false_distance) {
		copyOnWrite();
		trace.branchPassed(branch, bytecode_id, true_distance, false_distance);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#clear()
	 */
	@Override
	public void clear() {
		copyOnWrite();
		trace.clear();
	}

	public void copyOnWrite() {
		if (trace.getProxyCount() > 1) {
			trace.removeProxy();
			trace = trace.clone();
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#definitionPassed(java.lang.Object, int)
	 */
	@Override
	public void definitionPassed(Object caller, int defID) {
		copyOnWrite();
		trace.definitionPassed(caller, defID);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#enteredMethod(java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void enteredMethod(String className, String methodName, Object caller) {
		copyOnWrite();
		trace.enteredMethod(className, methodName, caller);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#exitMethod(java.lang.String, java.lang.String)
	 */
	@Override
	public void exitMethod(String classname, String methodname) {
		copyOnWrite();
		trace.exitMethod(classname, methodname);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#finishCalls()
	 */
	@Override
	public void finishCalls() {
		copyOnWrite();
		trace.finishCalls();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getCoverageData()
	 */
	@Override
	public Map<String, Map<String, Map<Integer, Integer>>> getCoverageData() {
		return trace.getCoverageData();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getCoveredFalseBranches()
	 */
	@Override
	public Set<Integer> getCoveredFalseBranches() {
		return trace.getCoveredFalseBranches();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getCoveredLines(java.lang.String)
	 */
	@Override
	public Set<Integer> getCoveredLines(String className) {
		return trace.getCoveredLines(className);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getCoveredMethods()
	 */
	@Override
	public Set<String> getCoveredMethods() {
		return trace.getCoveredMethods();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getCoveredPredicates()
	 */
	@Override
	public Set<Integer> getCoveredPredicates() {
		return trace.getCoveredPredicates();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getCoveredTrueBranches()
	 */
	@Override
	public Set<Integer> getCoveredTrueBranches() {
		return trace.getCoveredTrueBranches();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getDefinitionData()
	 */
	@Override
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getDefinitionData() {
		return trace.getDefinitionData();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTraceInterface#getExplicitException()
	 */
	@Override
	public Throwable getExplicitException() {
		return trace.getExplicitException();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getFalseDistance(int)
	 */
	@Override
	public Double getFalseDistance(int branchId) {
		return trace.getFalseDistance(branchId);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getFalseDistances()
	 */
	@Override
	public Map<Integer, Double> getFalseDistances() {
		return trace.getFalseDistances();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getMethodCalls()
	 */
	@Override
	public List<MethodCall> getMethodCalls() {
		return trace.getMethodCalls();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getMethodExecutionCount()
	 */
	@Override
	public Map<String, Integer> getMethodExecutionCount() {
		return trace.getMethodExecutionCount();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getMutationDistance(int)
	 */
	@Override
	public double getMutationDistance(int mutationId) {
		return trace.getMutationDistance(mutationId);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getMutationDistances()
	 */
	@Override
	public Map<Integer, Double> getMutationDistances() {
		return trace.getMutationDistances();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getPassedDefinitions(java.lang.String)
	 */
	@Override
	public Map<Integer, HashMap<Integer, Integer>> getPassedDefinitions(
	        String variableName) {
		return trace.getPassedDefinitions(variableName);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getPassedUses(java.lang.String)
	 */
	@Override
	public Map<Integer, HashMap<Integer, Integer>> getPassedUses(String variableName) {
		return trace.getPassedUses(variableName);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getPredicateExecutionCount()
	 */
	@Override
	public Map<Integer, Integer> getPredicateExecutionCount() {
		return trace.getPredicateExecutionCount();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getReturnData()
	 */
	@Override
	public Map<String, Map<String, Map<Integer, Integer>>> getReturnData() {
		return trace.getReturnData();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getTouchedMutants()
	 */
	@Override
	public Set<Integer> getTouchedMutants() {
		return trace.getTouchedMutants();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#getTraceForObject(int)
	 */
	@Override
	public ExecutionTrace getTraceForObject(int objectId) {
		return trace.getTraceForObject(objectId);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#getTraceInDUCounterRange(de.unisb.cs.st.evosuite.coverage.dataflow.DefUse, boolean, int, int)
	 */
	@Override
	public ExecutionTrace getTraceInDUCounterRange(DefUse targetDU,
	        boolean wantToCoverTargetDU, int duCounterStart, int duCounterEnd) {
		return trace.getTraceInDUCounterRange(targetDU, wantToCoverTargetDU,
		                                      duCounterStart, duCounterEnd);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getTrueDistance(int)
	 */
	@Override
	public Double getTrueDistance(int branchId) {
		return trace.getTrueDistance(branchId);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getTrueDistances()
	 */
	@Override
	public Map<Integer, Double> getTrueDistances() {
		return trace.getTrueDistances();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#getUseData()
	 */
	@Override
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getUseData() {
		return trace.getUseData();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#hasFalseDistance(int)
	 */
	@Override
	public boolean hasFalseDistance(int predicateId) {
		return trace.hasFalseDistance(predicateId);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#hasTrueDistance(int)
	 */
	@Override
	public boolean hasTrueDistance(int predicateId) {
		return trace.hasTrueDistance(predicateId);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#lazyClone()
	 */
	@Override
	public ExecutionTrace lazyClone() {
		ExecutionTraceProxy copy = new ExecutionTraceProxy(trace);
		trace.addProxy();
		return copy;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#linePassed(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void linePassed(String className, String methodName, int line) {
		copyOnWrite();
		trace.linePassed(className, methodName, line);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#mutationPassed(int, double)
	 */
	@Override
	public void mutationPassed(int mutationId, double distance) {
		copyOnWrite();
		trace.mutationPassed(mutationId, distance);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#returnValue(java.lang.String, java.lang.String, int)
	 */
	@Override
	public void returnValue(String className, String methodName, int value) {
		copyOnWrite();
		trace.returnValue(className, methodName, value);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTraceInterface#setExplicitException(java.lang.Throwable)
	 */
	@Override
	public void setExplicitException(Throwable explicitException) {
		copyOnWrite();
		trace.setExplicitException(explicitException);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#toDefUseTraceInformation()
	 */
	@Override
	public String toDefUseTraceInformation() {
		return trace.toDefUseTraceInformation();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#toDefUseTraceInformation(java.lang.String)
	 */
	@Override
	public String toDefUseTraceInformation(String targetVar) {
		return trace.toDefUseTraceInformation(targetVar);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#toDefUseTraceInformation(java.lang.String, int)
	 */
	@Override
	public String toDefUseTraceInformation(String var, int objectId) {
		return trace.toDefUseTraceInformation(var, objectId);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.IExecutionTrace#usePassed(java.lang.Object, int)
	 */
	@Override
	public void usePassed(Object caller, int useID) {
		copyOnWrite();
		trace.usePassed(caller, useID);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.ExecutionTrace#wasMutationTouched(int)
	 */
	@Override
	public boolean wasMutationTouched(int mutationId) {
		return trace.wasMutationTouched(mutationId);
	}

	@Override
	public List<BranchEval> getBranchesTrace() {
		return trace.getBranchesTrace();
	}

	@Override
	public Map<Integer, Double> getFalseDistancesSum() {
		return trace.getTrueDistancesSum();
	}

	@Override
	public Map<Integer, Double> getTrueDistancesSum() {
		return trace.getTrueDistancesSum();
	}

	@Override
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getPassedUses() {
		return trace.getPassedUses();
	}
}
