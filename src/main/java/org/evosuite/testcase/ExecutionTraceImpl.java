/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.dataflow.DefUse;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.dataflow.Definition;
import org.evosuite.coverage.dataflow.Use;
import org.evosuite.coverage.ibranch.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keep a trace of the program execution
 * 
 * @author Gordon Fraser
 */
public class ExecutionTraceImpl implements ExecutionTrace, Cloneable {

	public static class BranchEval {
		private final int branchId;
		private CallContext context = null;
		private final double falseDistance;
		private final double trueDistance;

		public BranchEval(int branchId, double trueDistance, double falseDistance) {
			this.branchId = branchId;
			this.trueDistance = trueDistance;
			this.falseDistance = falseDistance;
		}

		public BranchEval(int branchId, double trueDistance, double falseDistance,
		        CallContext context) {
			this.branchId = branchId;
			this.trueDistance = trueDistance;
			this.falseDistance = falseDistance;
			this.context = context;
		}

		public int getBranchId() {
			return branchId;
		}

		public CallContext getContext() {
			return context;
		}

		public double getFalseDistance() {
			return falseDistance;
		}

		public double getTrueDistance() {
			return trueDistance;
		}

		@Override
		public String toString() {
			return "BranchEval [branchId=" + branchId + ", trueDistance=" + trueDistance
			        + ", falseDistance=" + falseDistance + "]";
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ExecutionTrace.class);

	/** Constant <code>traceCalls=false</code> */
	public static boolean traceCalls = false;

	/** Constant <code>traceCoverage=true</code> */
	public static boolean traceCoverage = true;

	private static void checkSaneCall(MethodCall call) {
		if (!((call.trueDistanceTrace.size() == call.falseDistanceTrace.size())
		        && (call.falseDistanceTrace.size() == call.defuseCounterTrace.size()) && (call.defuseCounterTrace.size() == call.branchTrace.size()))) {
			throw new IllegalStateException(
			        "insane MethodCall: traces should all be of equal size. "
			                + call.explain());
		}

	}

	/**
	 * <p>
	 * disableTraceCalls
	 * </p>
	 */
	public static void disableTraceCalls() {
		traceCalls = false;
	}

	/**
	 * <p>
	 * enableTraceCalls
	 * </p>
	 */
	public static void enableTraceCalls() {
		traceCalls = true;
	}

	/**
	 * <p>
	 * enableTraceCoverage
	 * </p>
	 */
	public static void enableTraceCoverage() {
		traceCoverage = true;
	}

	/**
	 * Removes from the given ExecutionTrace all finished_calls with an index in
	 * removableCalls
	 */
	private static void removeFinishCalls(ExecutionTraceImpl trace,
	        ArrayList<Integer> removableCalls) {
		Collections.sort(removableCalls);
		for (int i = removableCalls.size() - 1; i >= 0; i--) {
			int toRemove = removableCalls.get(i);
			MethodCall removed = trace.finishedCalls.remove(toRemove);
			if (removed == null) {
				throw new IllegalStateException(
				        "trace.finished_calls not allowed to contain null");
			}
		}
	}

	/**
	 * Removes from the given MethodCall all trace information with an index in
	 * removableIndices
	 */
	private static void removeFromFinishCall(MethodCall call,
	        ArrayList<Integer> removableIndices) {
		checkSaneCall(call);

		Collections.sort(removableIndices);
		for (int i = removableIndices.size() - 1; i >= 0; i--) {
			int removableIndex = removableIndices.get(i);
			Integer removedBranch = call.branchTrace.remove(removableIndex);
			Double removedTrue = call.trueDistanceTrace.remove(removableIndex);
			Double removedFalse = call.falseDistanceTrace.remove(removableIndex);
			Integer removedCounter = call.defuseCounterTrace.remove(removableIndex);
			if ((removedCounter == null) || (removedBranch == null)
			        || (removedTrue == null) || (removedFalse == null)) {
				throw new IllegalStateException(
				        "trace.finished_calls-traces not allowed to contain null");
			}
		}
	}

	private List<BranchEval> branchesTrace = new ArrayList<BranchEval>();

	public Map<Integer, CallContext> callStacks = Collections.synchronizedMap(new HashMap<Integer, CallContext>());

	// Coverage information
	public Map<String, Map<String, Map<Integer, Integer>>> coverage = Collections.synchronizedMap(new HashMap<String, Map<String, Map<Integer, Integer>>>());

	public Map<Integer, Integer> coveredFalse = Collections.synchronizedMap(new HashMap<Integer, Integer>());

	public Map<String, Integer> coveredMethods = Collections.synchronizedMap(new HashMap<String, Integer>());

	public Map<Integer, Integer> coveredPredicates = Collections.synchronizedMap(new HashMap<Integer, Integer>());

	public Map<Integer, Integer> coveredTrue = Collections.synchronizedMap(new HashMap<Integer, Integer>());

	// number of seen Definitions and uses for indexing purposes
	private int duCounter = 0;
	// The last explicitly thrown exception is kept here
	private Throwable explicitException = null;

	public Map<Integer, Double> falseDistances = Collections.synchronizedMap(new HashMap<Integer, Double>());
	private final Map<Integer, Double> falseDistancesSum = Collections.synchronizedMap(new HashMap<Integer, Double>());
	// finished_calls;
	public List<MethodCall> finishedCalls = Collections.synchronizedList(new ArrayList<MethodCall>());
	public Map<Integer, Object> knownCallerObjects = Collections.synchronizedMap(new HashMap<Integer, Object>());
	// to differentiate between different MethodCalls
	private int methodId = 0;
	public Map<Integer, Double> mutantDistances = Collections.synchronizedMap(new HashMap<Integer, Double>());
	// for defuse-coverage it is important to keep track of all the objects that called the ExecutionTracer
	private int objectCounter = 0;
	// for each Variable-Name these maps hold the data for which objectID
	// at which time (duCounter) which Definition or Use was passed
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> passedDefinitions = Collections.synchronizedMap(new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>());
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> passedUses = Collections.synchronizedMap(new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>());

	private int proxyCount = 1;
	// Data information
	public Map<String, Map<String, Map<Integer, Integer>>> returnData = Collections.synchronizedMap(new HashMap<String, Map<String, Map<Integer, Integer>>>());

	// active calls
	Deque<MethodCall> stack = new LinkedList<MethodCall>();

	public Set<Integer> touchedMutants = Collections.synchronizedSet(new HashSet<Integer>());

	public Map<Integer, Double> trueDistances = Collections.synchronizedMap(new HashMap<Integer, Double>());

	private final Map<Integer, Double> trueDistancesSum = Collections.synchronizedMap(new HashMap<Integer, Double>());

	/**
	 * <p>
	 * Constructor for ExecutionTraceImpl.
	 * </p>
	 */
	public ExecutionTraceImpl() {
		stack.add(new MethodCall("", "", 0, 0, 0)); // Main method
	}

	/**
	 * <p>
	 * addProxy
	 * </p>
	 */
	public void addProxy() {
		proxyCount++;
	}

	/**
	 * <p>
	 * removeProxy
	 * </p>
	 */
	public void removeProxy() {
		proxyCount--;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Add branch to currently active method call
	 */
	@Override
	public void branchPassed(int branch, int bytecode_id, double true_distance,
	        double false_distance) {

		assert (true_distance >= 0.0);
		assert (false_distance >= 0.0);
		updateTopStackMethodCall(branch, bytecode_id, true_distance, false_distance);

		if (traceCoverage) {
			if (!coveredPredicates.containsKey(branch))
				coveredPredicates.put(branch, 1);
			else
				coveredPredicates.put(branch, coveredPredicates.get(branch) + 1);

			if (true_distance == 0.0) {
				if (!coveredTrue.containsKey(branch))
					coveredTrue.put(branch, 1);
				else
					coveredTrue.put(branch, coveredTrue.get(branch) + 1);

			}

			if (false_distance == 0.0) {
				if (!coveredFalse.containsKey(branch))
					coveredFalse.put(branch, 1);
				else
					coveredFalse.put(branch, coveredFalse.get(branch) + 1);
			}
		}

		if (!trueDistances.containsKey(branch))
			trueDistances.put(branch, true_distance);
		else
			trueDistances.put(branch, Math.min(trueDistances.get(branch), true_distance));

		if (!falseDistances.containsKey(branch))
			falseDistances.put(branch, false_distance);
		else
			falseDistances.put(branch,
			                   Math.min(falseDistances.get(branch), false_distance));

		if (!trueDistancesSum.containsKey(branch))
			trueDistancesSum.put(branch, true_distance);
		else
			trueDistancesSum.put(branch, trueDistancesSum.get(branch) + true_distance);

		if (!falseDistancesSum.containsKey(branch))
			falseDistancesSum.put(branch, false_distance);
		else
			falseDistancesSum.put(branch, falseDistancesSum.get(branch) + false_distance);

		if (Properties.BRANCH_EVAL) {
			if (Properties.CRITERION == Criterion.IBRANCH) {
				branchesTrace.add(new BranchEval(branch, true_distance, false_distance,
				        new CallContext(Thread.currentThread().getStackTrace())));
			} else {
				branchesTrace.add(new BranchEval(branch, true_distance, false_distance));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Reset to 0
	 */
	@Override
	public void clear() {
		finishedCalls = new ArrayList<MethodCall>();
		stack = new LinkedList<MethodCall>();

		// stack.clear();
		// finished_calls.clear();
		stack.add(new MethodCall("", "", 0, 0, 0)); // Main method
		coverage = new HashMap<String, Map<String, Map<Integer, Integer>>>();
		returnData = new HashMap<String, Map<String, Map<Integer, Integer>>>();

		methodId = 0;
		duCounter = 0;
		objectCounter = 0;
		knownCallerObjects = new HashMap<Integer, Object>();
		trueDistances = new HashMap<Integer, Double>();
		falseDistances = new HashMap<Integer, Double>();
		mutantDistances = new HashMap<Integer, Double>();
		touchedMutants = new HashSet<Integer>();
		coveredMethods = new HashMap<String, Integer>();
		coveredPredicates = new HashMap<Integer, Integer>();
		coveredTrue = new HashMap<Integer, Integer>();
		coveredFalse = new HashMap<Integer, Integer>();
		passedDefinitions = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
		passedUses = new HashMap<String, HashMap<Integer, HashMap<Integer, Integer>>>();
		branchesTrace = new ArrayList<BranchEval>();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Create a deep copy
	 */
	@Override
	public ExecutionTraceImpl clone() {

		ExecutionTraceImpl copy = new ExecutionTraceImpl();
		for (MethodCall call : finishedCalls) {
			copy.finishedCalls.add(call.clone());
		}
		// copy.finished_calls.addAll(finished_calls);
		copy.coverage = new HashMap<String, Map<String, Map<Integer, Integer>>>();
		if (coverage != null) {
			copy.coverage.putAll(coverage);
		}
		copy.returnData = new HashMap<String, Map<String, Map<Integer, Integer>>>();
		copy.returnData.putAll(returnData);
		/*
		 * if(stack != null && !stack.isEmpty() && stack.peek().method_name !=
		 * null && stack.peek().method_name.equals("")) {
		 * logger.info("Copying main method");
		 * copy.finished_calls.add(stack.peek()); }
		 */
		copy.trueDistances.putAll(trueDistances);
		copy.falseDistances.putAll(falseDistances);
		copy.coveredMethods.putAll(coveredMethods);
		copy.coveredPredicates.putAll(coveredPredicates);
		copy.coveredTrue.putAll(coveredTrue);
		copy.coveredFalse.putAll(coveredFalse);
		copy.touchedMutants.addAll(touchedMutants);
		copy.mutantDistances.putAll(mutantDistances);
		copy.passedDefinitions.putAll(passedDefinitions);
		copy.passedUses.putAll(passedUses);
		copy.branchesTrace.addAll(branchesTrace);
		copy.methodId = methodId;
		copy.duCounter = duCounter;
		copy.objectCounter = objectCounter;
		copy.knownCallerObjects.putAll(knownCallerObjects);
		copy.proxyCount = 1;
		return copy;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Adds Definition-Use-Coverage trace information for the given definition.
	 * 
	 * Registers the given caller-Object Traces the occurrence of the given
	 * definition in the passedDefs-field Sets the given definition as the
	 * currently active one for the definitionVariable in the
	 * activeDefinitions-field Adds fake trace information to the currently
	 * active MethodCall in this.stack
	 */
	@Override
	public void definitionPassed(Object caller, int defID) {

		if (!traceCalls) {
			return;
		}

		Definition def = DefUsePool.getDefinitionByDefId(defID);
		if (def == null) {
			throw new IllegalStateException(
			        "expect DefUsePool to known defIDs that are passed by instrumented code");
		}

		String varName = def.getDUVariableName();

		int objectID = registerObject(caller);

		// if this is a static variable, treat objectID as zero for consistency in the representation of static data
		if (objectID != 0 && def.isStaticDefUse())
			objectID = 0;
		if (passedDefinitions.get(varName) == null)
			passedDefinitions.put(varName,
			                      new HashMap<Integer, HashMap<Integer, Integer>>());
		HashMap<Integer, Integer> defs = passedDefinitions.get(varName).get(objectID);
		if (defs == null)
			defs = new HashMap<Integer, Integer>();
		defs.put(duCounter, defID);
		passedDefinitions.get(varName).put(objectID, defs);

		//		logger.trace(duCounter+": set active definition for var "+def.getDUVariableName()+" on object "+objectID+" to Def "+defID);
		duCounter++;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Add a new method call to stack
	 */
	@Override
	public void enteredMethod(String className, String methodName, Object caller) {
		if (traceCoverage) {
			String id = className + "." + methodName;
			if (!coveredMethods.containsKey(id)) {
				coveredMethods.put(id, 1);
			} else {
				coveredMethods.put(id, coveredMethods.get(id) + 1);
			}
		}
		if (traceCalls) {
			int callingObjectID = registerObject(caller);
			methodId++;
			MethodCall call = new MethodCall(className, methodName, methodId,
			        callingObjectID, stack.size());
			if (Properties.CRITERION == Criterion.DEFUSE
			        || Properties.CRITERION == Criterion.ALLDEFS
			        || TestSuiteGenerator.analyzing) {
				call.branchTrace.add(-1);
				call.trueDistanceTrace.add(1.0);
				call.falseDistanceTrace.add(0.0);
				call.defuseCounterTrace.add(duCounter);
				// TODO line_trace ?
			}
			stack.push(call);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ExecutionTraceImpl other = (ExecutionTraceImpl) obj;
		if (coverage == null) {
			if (other.coverage != null) {
				return false;
			}
		} else if (!coverage.equals(other.coverage)) {
			return false;
		}
		if (finishedCalls == null) {
			if (other.finishedCalls != null) {
				return false;
			}
		} else if (!finishedCalls.equals(other.finishedCalls)) {
			return false;
		}
		if (returnData == null) {
			if (other.returnData != null) {
				return false;
			}
		} else if (!returnData.equals(other.returnData)) {
			return false;
		}
		if (stack == null) {
			if (other.stack != null) {
				return false;
			}
		} else if (!stack.equals(other.stack)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Pop last method call from stack
	 */
	@Override
	public void exitMethod(String classname, String methodname) {
		if (traceCalls) {
			if (!stack.isEmpty() && !(stack.peek().methodName.equals(methodname))) {
				logger.debug("Expecting " + stack.peek().methodName + ", got "
				        + methodname);

				if (stack.peek().methodName.equals("")
				        && !stack.peek().branchTrace.isEmpty()) {
					logger.debug("Found main method");
					finishedCalls.add(stack.pop());
				} else {
					logger.debug("Bugger!");
					// Usually, this happens if we use mutation testing and the
					// mutation
					// causes an unexpected exception or timeout
					stack.pop();
				}
			} else {
				finishedCalls.add(stack.pop());
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void finishCalls() {
		logger.debug("At the end, we have " + stack.size() + " calls left on stack");
		while (!stack.isEmpty()) {
			finishedCalls.add(stack.pop());
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<BranchEval> getBranchesTrace() {
		return branchesTrace;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getCoverageData()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<String, Map<String, Map<Integer, Integer>>> getCoverageData() {
		return coverage;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getCoveredFalseBranches()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Integer> getCoveredFalseBranches() {
		Set<Integer> covered = new HashSet<Integer>();
		for (Entry<Integer, Double> entry : falseDistances.entrySet()) {
			if (entry.getValue() == 0.0)
				covered.add(entry.getKey());
		}

		return covered;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getCoveredLines()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Integer> getCoveredLines(String className) {
		Set<Integer> coveredLines = new HashSet<Integer>();
		for (Entry<String, Map<String, Map<Integer, Integer>>> entry : coverage.entrySet()) {
			if (entry.getKey().startsWith(className)) {
				for (Map<Integer, Integer> methodentry : entry.getValue().values()) {
					coveredLines.addAll(methodentry.keySet());
				}
			}
		}
		return coveredLines;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getCoveredMethods()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<String> getCoveredMethods() {
		return coveredMethods.keySet();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getCoveredPredicates()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Integer> getCoveredPredicates() {
		return coveredPredicates.keySet();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getCoveredTrueBranches()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Integer> getCoveredTrueBranches() {
		Set<Integer> covered = new HashSet<Integer>();
		for (Entry<Integer, Double> entry : trueDistances.entrySet()) {
			if (entry.getValue() == 0.0)
				covered.add(entry.getKey());
		}

		return covered;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getDefinitionData()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getDefinitionData() {
		return passedDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public Throwable getExplicitException() {
		return explicitException;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getFalseDistance(int)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFalseDistance(int branchId) {
		return falseDistances.get(branchId);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getFalseDistances()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<Integer, Double> getFalseDistances() {
		return falseDistances;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getMethodCalls()
	 */
	/** {@inheritDoc} */
	@Override
	public List<MethodCall> getMethodCalls() {
		return finishedCalls;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getMethodExecutionCount()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<String, Integer> getMethodExecutionCount() {
		return coveredMethods;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getMutationDistance(int)
	 */
	/** {@inheritDoc} */
	@Override
	public double getMutationDistance(int mutationId) {
		return mutantDistances.get(mutationId);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getMutationDistances()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<Integer, Double> getMutationDistances() {
		return mutantDistances;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getPassedDefinitions(java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public Map<Integer, HashMap<Integer, Integer>> getPassedDefinitions(
	        String variableName) {
		return passedDefinitions.get(variableName);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getPassedUses(java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public Map<Integer, HashMap<Integer, Integer>> getPassedUses(String variableName) {
		return passedUses.get(variableName);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getPredicateExecutionCount()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<Integer, Integer> getPredicateExecutionCount() {
		return coveredPredicates;
	}

	/**
	 * <p>
	 * Getter for the field <code>proxyCount</code>.
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getProxyCount() {
		return proxyCount;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getReturnData()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<String, Map<String, Map<Integer, Integer>>> getReturnData() {
		return returnData;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getTouchedMutants()
	 */
	/** {@inheritDoc} */
	@Override
	public Set<Integer> getTouchedMutants() {
		return touchedMutants;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns a copy of this trace where all MethodCall-information traced from
	 * objects other then the one identified by the given objectID is removed
	 * from the finished_calls-field
	 * 
	 * WARNING: this will not affect this.true_distances and other fields of
	 * ExecutionTrace this only affects the finished_calls field (which should
	 * suffice for BranchCoverageFitness-calculation)
	 */
	@Override
	public ExecutionTrace getTraceForObject(int objectId) {
		ExecutionTraceImpl r = clone();
		ArrayList<Integer> removableCalls = new ArrayList<Integer>();
		for (int i = 0; i < r.finishedCalls.size(); i++) {
			MethodCall call = r.finishedCalls.get(i);
			if ((call.callingObjectID != objectId) && (call.callingObjectID != 0)) {
				removableCalls.add(i);
			}
		}
		removeFinishCalls(r, removableCalls);
		return new ExecutionTraceProxy(r);
	}

	/**
	 * {@inheritDoc}
	 * 
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
	@Override
	public ExecutionTrace getTraceInDUCounterRange(DefUse targetDU,
	        boolean wantToCoverTargetDU, int duCounterStart, int duCounterEnd) {

		if (duCounterStart > duCounterEnd) {
			throw new IllegalArgumentException("start has to be lesser or equal end");
			/*
			// DONE: bug
			// this still has a major flaw: s. MeanTestClass.mean():
			// right now its like we map branches to activeDefenitions
			// but especially in the root branch of a method
			// activeDefenitions change during execution time
			// FIX: in order to avoid these false positives remove all information 
			//		for a certain branch if some information for that branch is supposed to be removed
			//  subTodo	since branchPassed() only gets called when a branch is passed initially
			// 			fake calls to branchPassed() have to be made whenever a DU is passed 
			// 			s. definitionPassed(), usePassed() and addFakeActiveMethodCallInformation()

			// DONE: new bug
			// 	turns out thats an over-approximation that makes it 
			// 	impossible to cover some potentially coverable goals
			
			// completely new:
			// if your definition gets overwritten in a trace
			// the resulting fitness should be the fitness of not taking the branch with the overwriting definition
			// DONE: in order to do that don't remove older trace information for an overwritten branch
			// 		 but rather set the true and false distance of that previous branch information to the distance of not taking the overwriting branch
			// done differently: s. DefUseCoverageTestFitness.getFitness()
			 */
		}

		ExecutionTraceImpl r = clone();
		Branch targetDUBranch = targetDU.getControlDependentBranch();
		ArrayList<Integer> removableCalls = new ArrayList<Integer>();
		for (int callPos = 0; callPos < r.finishedCalls.size(); callPos++) {
			MethodCall call = r.finishedCalls.get(callPos);
			// check if call is for the method of targetDU
			if (!call.methodName.equals(targetDU.getMethodName())) {
				removableCalls.add(callPos);
				continue;
			}
			ArrayList<Integer> removableIndices = new ArrayList<Integer>();
			for (int i = 0; i < call.defuseCounterTrace.size(); i++) {
				int currentDUCounter = call.defuseCounterTrace.get(i);
				int currentBranchBytecode = call.branchTrace.get(i);

				if (currentDUCounter < duCounterStart || currentDUCounter > duCounterEnd)
					removableIndices.add(i);
				else if (currentBranchBytecode == targetDUBranch.getInstruction().getInstructionId()) {
					// only remove this point in the trace if it would cover targetDU
					boolean targetExpressionValue = targetDU.getControlDependentBranchExpressionValue();
					if (targetExpressionValue) {
						if (call.trueDistanceTrace.get(i) == 0.0)
							removableIndices.add(i);
					} else {
						if (call.falseDistanceTrace.get(i) == 0.0)
							removableIndices.add(i);
					}

				}
			}
			removeFromFinishCall(call, removableIndices);
			if (call.defuseCounterTrace.size() == 0)
				removableCalls.add(callPos);
		}
		removeFinishCalls(r, removableCalls);
		return new ExecutionTraceProxy(r);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getTrueDistance(int)
	 */
	/** {@inheritDoc} */
	@Override
	public double getTrueDistance(int branchId) {
		return trueDistances.get(branchId);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getTrueDistances()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<Integer, Double> getTrueDistances() {
		return trueDistances;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#getUseData()
	 */
	/** {@inheritDoc} */
	@Override
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getUseData() {
		return passedUses;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#wasCoveredFalse(int)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasFalseDistance(int predicateId) {
		return falseDistances.containsKey(predicateId);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coverage == null) ? 0 : coverage.hashCode());
		result = prime * result
		        + ((finishedCalls == null) ? 0 : finishedCalls.hashCode());
		result = prime * result + ((returnData == null) ? 0 : returnData.hashCode());
		result = prime * result + ((stack == null) ? 0 : stack.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#wasCoveredTrue(int)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean hasTrueDistance(int predicateId) {
		return trueDistances.containsKey(predicateId);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#lazyClone()
	 */
	/** {@inheritDoc} */
	@Override
	public ExecutionTrace lazyClone() {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean stackHasMethod(String methodName) {
		for (MethodCall call : stack) {
			if (call.methodName.equals(methodName))
				return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Add line to currently active method call
	 */
	@Override
	public void linePassed(String className, String methodName, int line) {
		if (traceCalls) {
			if (stack.isEmpty()) {
				logger.info("Method stack is empty: " + className + "." + methodName
				        + " - l" + line); // TODO switch back
				// logger.debug to
				// logger.warn
			} else {
				boolean empty = false;
				if (!stack.peek().methodName.equals(methodName)) {
					if (stack.peek().methodName.equals(""))
						return;

					if (stackHasMethod(methodName)) {
						do {
							logger.debug("Popping method " + stack.peek().methodName
							        + " because we were looking for " + methodName);
							finishedCalls.add(stack.pop());
						} while (!stack.isEmpty()
						        && !stack.peek().methodName.equals(methodName)
						        && !stack.peek().methodName.equals(""));
					} else {

						logger.warn("Popping method " + stack.peek().methodName
						        + " because we were looking for " + methodName);
						logger.warn("Current stack: " + stack);
						finishedCalls.add(stack.pop());
					}
					if (stack.isEmpty()) {
						logger.warn("Method stack is empty: " + className + "."
						        + methodName + " - l" + line); // TODO switch back
						empty = true;
					}
				}
				if (!empty)
					stack.peek().lineTrace.add(line);
			}
		}
		if (traceCoverage) {
			if (!coverage.containsKey(className)) {
				coverage.put(className, new HashMap<String, Map<Integer, Integer>>());
			}

			if (!coverage.get(className).containsKey(methodName)) {
				coverage.get(className).put(methodName, new HashMap<Integer, Integer>());
			}

			if (!coverage.get(className).get(methodName).containsKey(line)) {
				coverage.get(className).get(methodName).put(line, 1);
			} else {
				coverage.get(className).get(methodName).put(line,
				                                            coverage.get(className).get(methodName).get(line) + 1);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void mutationPassed(int mutationId, double distance) {

		touchedMutants.add(mutationId);
		if (!mutantDistances.containsKey(mutationId)) {
			mutantDistances.put(mutationId, distance);
		} else {
			mutantDistances.put(mutationId,
			                    Math.min(distance, mutantDistances.get(mutationId)));
		}
	}

	/**
	 * Returns the objecectId for the given object.
	 * 
	 * The ExecutionTracer keeps track of all objects it gets called from in
	 * order to distinguish them later in the fitness calculation for the
	 * defuse-Coverage-Criterion.
	 */
	private int registerObject(Object caller) {
		if (caller == null) {
			return 0;
		}
		for (Integer objectId : knownCallerObjects.keySet()) {
			if (knownCallerObjects.get(objectId) == caller) {
				return objectId;
			}
		}
		// object unknown so far
		objectCounter++;
		knownCallerObjects.put(objectCounter, caller);
		return objectCounter;
	}

	/** {@inheritDoc} */
	@Override
	public void returnValue(String className, String methodName, int value) {
		if (!returnData.containsKey(className)) {
			returnData.put(className, new HashMap<String, Map<Integer, Integer>>());
		}

		if (!returnData.get(className).containsKey(methodName)) {
			returnData.get(className).put(methodName, new HashMap<Integer, Integer>());
		}

		if (!returnData.get(className).get(methodName).containsKey(value)) {
			// logger.info("Got return value "+value);
			returnData.get(className).get(methodName).put(value, 1);
		} else {
			// logger.info("Got return value again "+value);
			returnData.get(className).get(methodName).put(value,
			                                              returnData.get(className).get(methodName).get(value) + 1);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setExplicitException(Throwable explicitException) {
		this.explicitException = explicitException;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns a String containing the information in passedDefs and passedUses
	 * 
	 * Used for Definition-Use-Coverage-debugging
	 */
	@Override
	public String toDefUseTraceInformation() {
		StringBuffer r = new StringBuffer();
		for (String var : passedDefinitions.keySet()) {
			r.append("  for variable: " + var + ": ");
			for (Integer objectId : passedDefinitions.get(var).keySet()) {
				if (passedDefinitions.get(var).keySet().size() > 1) {
					r.append("\n\ton object " + objectId + ": ");
				}
				r.append(toDefUseTraceInformation(var, objectId));
			}
			r.append("\n  ");
		}
		return r.toString();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns a String containing the information in passedDefs and passedUses
	 * filtered for a specific variable
	 * 
	 * Used for Definition-Use-Coverage-debugging
	 */
	@Override
	public String toDefUseTraceInformation(String targetVar) {
		StringBuffer r = new StringBuffer();
		for (Integer objectId : passedDefinitions.get(targetVar).keySet()) {
			if (passedDefinitions.get(targetVar).keySet().size() > 1) {
				r.append("\n\ton object " + objectId + ": ");
			}
			r.append(toDefUseTraceInformation(targetVar, objectId));
		}
		r.append("\n  ");

		return r.toString();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Returns a String containing the information in passedDefs and passedUses
	 * for the given variable
	 * 
	 * Used for Definition-Use-Coverage-debugging
	 */
	@Override
	public String toDefUseTraceInformation(String var, int objectId) {
		if (passedDefinitions.get(var) == null) {
			return "";
		}
		if ((objectId == -1) && (passedDefinitions.get(var).keySet().size() == 1)) {
			objectId = (Integer) passedDefinitions.get(var).keySet().toArray()[0];
		}
		if (passedDefinitions.get(var).get(objectId) == null) {
			return "";
		}
		// gather all DUs
		String[] duTrace = new String[this.duCounter];
		for (int i = 0; i < this.duCounter; i++) {
			duTrace[i] = "";
		}
		for (Integer duPos : passedDefinitions.get(var).get(objectId).keySet()) {
			duTrace[duPos] = "(" + duPos + ":Def "
			        + passedDefinitions.get(var).get(objectId).get(duPos) + ")";
		}
		if ((passedUses.get(var) != null) && (passedUses.get(var).get(objectId) != null)) {
			for (Integer duPos : passedUses.get(var).get(objectId).keySet()) {
				duTrace[duPos] = "(" + duPos + ":Use "
				        + passedUses.get(var).get(objectId).get(duPos) + ")";
			}
		}
		// build up the String
		StringBuffer r = new StringBuffer();
		for (String s : duTrace) {
			r.append(s);
			if (s.length() > 0) {
				r.append(", ");
			}
		}
		// remove last ", "
		String traceString = r.toString();
		if (traceString.length() > 2) {
			return traceString.substring(0, traceString.length() - 2);
		}
		return traceString;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		for (MethodCall m : finishedCalls) {
			ret.append(m);
		}
		ret.append("\nCovered methods: ");
		for (Entry<String, Integer> entry : coveredMethods.entrySet()) {
			ret.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		ret.append("\nCovered predicates: ");
		for (Entry<Integer, Integer> entry : coveredPredicates.entrySet()) {
			ret.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		ret.append("\nTrue distances: ");
		for (Entry<Integer, Double> entry : trueDistances.entrySet()) {
			ret.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		ret.append("\nFalse distances: ");
		for (Entry<Integer, Double> entry : falseDistances.entrySet()) {
			ret.append(entry.getKey() + ": " + entry.getValue() + ", ");
		}
		return ret.toString();
	}

	/**
	 * Adds trace information to the active MethodCall in this.stack
	 */
	private void updateTopStackMethodCall(int branch, int bytecode_id,
	        double true_distance, double false_distance) {

		if (traceCalls) {
			if (stack.isEmpty()) {
				return;
			}
			stack.peek().branchTrace.add(branch); // was: bytecode_id
			stack.peek().trueDistanceTrace.add(true_distance);
			stack.peek().falseDistanceTrace.add(false_distance);
			assert ((true_distance == 0.0) || (false_distance == 0.0));
			// TODO line_trace ?
			if (Properties.CRITERION == Criterion.DEFUSE
			        || Properties.CRITERION == Criterion.ALLDEFS
			        || TestSuiteGenerator.analyzing) {
				stack.peek().defuseCounterTrace.add(duCounter);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Adds Definition-Use-Coverage trace information for the given use.
	 * 
	 * Registers the given caller-Object Traces the occurrence of the given use
	 * in the passedUses-field
	 */
	@Override
	public void usePassed(Object caller, int useID) {

		if (!traceCalls) // TODO ???
			return;

		Use use = DefUsePool.getUseByUseId(useID);

		int objectID = registerObject(caller);
		// if this is a static variable, treat objectID as zero for consistency in the representation of static data
		if (objectID != 0) {
			if (use == null)
				throw new IllegalStateException(
				        "expect DefUsePool to known defIDs that are passed by instrumented code");
			if (use.isStaticDefUse())
				objectID = 0;
		}
		String varName = use.getDUVariableName();
		if (passedUses.get(varName) == null)
			passedUses.put(varName, new HashMap<Integer, HashMap<Integer, Integer>>());

		HashMap<Integer, Integer> uses = passedUses.get(varName).get(objectID);
		if (uses == null)
			uses = new HashMap<Integer, Integer>();

		uses.put(duCounter, useID);
		passedUses.get(varName).put(objectID, uses);
		duCounter++;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.ExecutionTrace#wasMutationTouched(int)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean wasMutationTouched(int mutationId) {
		return touchedMutants.contains(mutationId);
	}

	/** {@inheritDoc} */
	@Override
	public Map<Integer, Double> getFalseDistancesSum() {
		return falseDistancesSum;
	}

	/** {@inheritDoc} */
	@Override
	public Map<Integer, Double> getTrueDistancesSum() {
		return trueDistancesSum;
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, HashMap<Integer, HashMap<Integer, Integer>>> getPassedUses() {
		return passedUses;
	}
}
