package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;

/**
 * 
 * This class is a library holding all methods needed to analyze an
 * ExecutionTrace with respect to DefUseCoverage fitness calculation
 * 
 * @author Andre Mis
 */
public abstract class DefUseExecutionTraceAnalyzer {

	public static long timeGetCoveredGoals = 0l;
	
	/**
	 * Determines the definitionId for targetVar before tagetDUPos in the given
	 * ExecutionTrace
	 * 
	 * If no such definition exists -1 is returned
	 */
	public static int getPreviousDefinitionId(String targetVariable,
			ExecutionTrace objectTrace, Integer targetDUPos, Integer objectId) {

		int prevPos = -1;
		int prevDef = -1;
		Map<Integer, Integer> defMap = objectTrace.passedDefinitions.get(
				targetVariable).get(objectId);
		for (Integer duPos : defMap.keySet())
			if (duPos < targetDUPos && duPos > prevPos) {
				prevDef = defMap.get(duPos);
				prevPos = duPos;
			}

		return prevDef;
	}

	/**
	 * Determines the overwriting definition for the given goalDefPos
	 * 
	 * An overwriting definition position is the duCounter position of the next
	 * definition for goalVariable that was not the goalDefinition
	 * 
	 * If no such definition exists Integer.MAX_VALUE is returned
	 */
	public static int getNextOverwritingDefinitionPosition(
			Definition targetDefinition, ExecutionTrace objectTrace,
			Integer goalDefPos, Integer objectId) {

		int lastPos = Integer.MAX_VALUE;
		Map<Integer, HashMap<Integer, Integer>> objectMap = objectTrace.passedDefinitions
				.get(targetDefinition.getDUVariableName());
		if (objectMap == null)
			return lastPos;
		Map<Integer, Integer> defMap = objectMap.get(objectId);
		if (defMap == null)
			return lastPos;
		for (Integer defPos : defMap.keySet())
			if (defPos > goalDefPos && defPos < lastPos
					&& defMap.get(defPos) != targetDefinition.getDefId())
				lastPos = defPos;

		return lastPos;
	}

	/**
	 * Returns all the duCounterPositions of the targetUse in the given trace
	 */
	public static List<Integer> getUsePositions(Use targetUse,
			ExecutionTrace trace, int objectId) {
		ArrayList<Integer> r = new ArrayList<Integer>();
		Map<Integer, HashMap<Integer, Integer>> objectMap = trace.passedUses
				.get(targetUse.getDUVariableName());
		if (objectMap == null)
			return r;
		Map<Integer, Integer> useMap = objectMap.get(objectId);
		if (useMap == null)
			return r;
		for (Integer usePos : useMap.keySet())
			if (useMap.get(usePos) == targetUse.getUseId())
				r.add(usePos);

		return r;
	}

	/**
	 * Returns all the duCounterPositions of the goalUse in the given trace
	 */
	public static List<Integer> getDefinitionPositions(
			Definition targetDefinition, ExecutionTrace trace, int objectId) {

		ArrayList<Integer> r = new ArrayList<Integer>();
		Map<Integer, HashMap<Integer, Integer>> objectMap = trace.passedDefinitions
				.get(targetDefinition.getDUVariableName());
		if (objectMap == null)
			return r;
		Map<Integer, Integer> defMap = objectMap.get(objectId);
		if (defMap == null)
			return r;
		for (Integer defPos : defMap.keySet())
			if (defMap.get(defPos) == targetDefinition.getDefId())
				r.add(defPos);

		return r;
	}

	/**
	 * Returns the a map of definitionIds from all definitions that overwrite
	 * the goal definition in the given duCounter-range pointing to the duPos at
	 * which they first overwrote.
	 * 
	 * This method expects the given ExecutionTrace not to contain any trace
	 * information for the targetDefinition in the given range. If such a trace
	 * is detected this method throws an IllegalStateException!
	 */
	public static Map<Integer, Integer> getOverwritingDefinitionsBetween(
			Definition targetDefinition, ExecutionTrace trace,
			int startingDUPos, int endDUPos, int objectId) {

		if (startingDUPos > endDUPos)
			throw new IllegalArgumentException(
					"start must be lower or equal end");
		Map<Integer, Integer> r = new HashMap<Integer, Integer>();
		Map<Integer, HashMap<Integer, Integer>> objectMap = trace.passedDefinitions
				.get(targetDefinition.getDUVariableName());
		if (objectMap == null)
			return r;
		Map<Integer, Integer> defMap = objectMap.get(objectId);
		if (defMap == null)
			return r;
		for (Integer defPos : defMap.keySet()) {
			if (defPos < startingDUPos || defPos > endDUPos)
				continue;
			int defId = defMap.get(defPos);
			if (defId == targetDefinition.getDefId())
				throw new IllegalStateException(
						"expect given trace not to have passed goalDefinition in the given duCounter-range");
			if (r.get(defId) == null)
				r.put(defId, defPos);
		}
		return r;
	}

	public static Set<BytecodeInstruction> getDefinitionsIn(
			String targetVariable, Set<BytecodeInstruction> vertices) {
		Set<BytecodeInstruction> r = new HashSet<BytecodeInstruction>();
		for (BytecodeInstruction vertex : vertices) {
			if (!vertex.isDefinition())
				continue;
			Definition currentDefinition = DefUseFactory.makeDefinition(vertex);
			if (currentDefinition.getDUVariableName().equals(targetVariable))
				r.add(vertex);
		}
		return r;
	}

	/**
	 * Returns a Set containing all elements in the given vertex set that are
	 * overwriting definitions for the given targetDefinition
	 */
	public static Set<BytecodeInstruction> getOverwritingDefinitionsIn(
			Definition targetDefinition,
			Collection<BytecodeInstruction> vertices) {
		Set<BytecodeInstruction> r = new HashSet<BytecodeInstruction>();
		for (BytecodeInstruction vertex : vertices) {
			if (!vertex.isDefinition())
				continue;
			BytecodeInstruction vertexInOtherGraph = CFGPool.getRawCFG(
					vertex.getClassName(), vertex.getMethodName())
					.getInstruction(vertex.getInstructionId());
			Definition currentDefinition = DefUseFactory
					.makeDefinition(vertexInOtherGraph);
			if (isOverwritingDefinition(targetDefinition, currentDefinition))
				r.add(vertex);
		}
		return r;
	}

	/**
	 * Determines if the two given Definitions refer to the same variable but
	 * are different
	 */
	public static boolean isOverwritingDefinition(Definition targetDefinition,
			Definition definition) {

		if (definition.getDefId() == -1)
			throw new IllegalArgumentException(
					"expect given Definition to have it's defId set");
		return targetDefinition.getDUVariableName().equals(
				definition.getDUVariableName())
				&& targetDefinition.getDefId() != definition.getDefId();
	}

	/**
	 * Returns the defID of the Definition that is active in the given trace at
	 * usePos
	 */
	public static int getActiveDefinitionIdAt(String targetVariable,
			ExecutionTrace trace, int usePos, int objectId) {

		int lastDef = -1;
		int lastPos = -1;
		Map<Integer, HashMap<Integer, Integer>> objectMap = trace.passedDefinitions
				.get(targetVariable);
		if (objectMap == null)
			return -1;
		Map<Integer, Integer> defMap = objectMap.get(objectId);
		if (defMap == null)
			return -1;
		for (Integer defPos : defMap.keySet()) {
			if (defPos > usePos)
				continue;
			if (lastPos < defPos) {
				lastDef = defMap.get(defPos);
				lastPos = defPos;
			}
		}
		return lastDef;
	}

	/**
	 * Prints all information found concerning finished calls of the given
	 * ExecutionTrace
	 */
	public static void printFinishCalls(ExecutionTrace trace) {
		for (MethodCall call : trace.finished_calls) {
			System.out.println("Found MethodCall for: " + call.methodName
					+ " on object " + call.callingObjectID);
			System.out.println("#passed branches: " + call.branchTrace.size());
			for (int i = 0; i < call.defuseCounterTrace.size(); i++) {
				System.out.println(i + ". at Branch " + call.branchTrace.get(i)
						+ " true_dist: " + call.trueDistanceTrace.get(i)
						+ " false_dist: " + call.falseDistanceTrace.get(i)
						+ " duCounter: " + call.defuseCounterTrace.get(i));
				System.out.println();
			}
		}
	}

	public static Set<DefUseCoverageTestFitness> getCoveredGoals(
			List<ExecutionResult> results) {

		long start = System.currentTimeMillis();
		
		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();

		for (ExecutionResult result : results) {
			Set<DefUseCoverageTestFitness> goals = getCoveredGoals(result);
			r.addAll(goals);
		}

		timeGetCoveredGoals += System.currentTimeMillis() - start;
		
		return r;
	}

	public static Set<DefUseCoverageTestFitness> getCoveredGoals(
			ExecutionResult result) {

		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();
		
		Map<String, HashMap<Integer, HashMap<Integer, Integer>>> passedDefs = result
				.getTrace().passedDefinitions;
		Map<String, HashMap<Integer, HashMap<Integer, Integer>>> passedUses = result
		.getTrace().passedUses;
		
		for(String goalVariable : passedDefs.keySet()) {
			if(passedUses.get(goalVariable) == null)
				continue;
			for(Integer objectId : passedDefs.get(goalVariable).keySet()) {
				if(passedUses.get(goalVariable).get(objectId) == null)
					continue;
				
				Map<Integer,Integer> currentDefMap = passedDefs.get(goalVariable).get(objectId);
				Map<Integer,Integer> currentUseMap = passedUses.get(goalVariable).get(objectId);
				List<Integer> duCounterTrace = new ArrayList<Integer>(currentDefMap.keySet());
				Collections.sort(duCounterTrace);
				int traceLength = duCounterTrace.size();
				Integer[] sortedDUTrace = duCounterTrace.toArray(new Integer[traceLength]);
				
				for(int i=0;i<traceLength;i++) {
					int currentDUCounter = sortedDUTrace[i];
					int activeDef = currentDefMap.get(currentDUCounter);
					
					int nextDUCounter;
					if(i != traceLength-1) {
						// definition that was overwritten
						nextDUCounter = sortedDUTrace[i+1];
					} else {
						// last active definition
						nextDUCounter = Integer.MAX_VALUE;
					}

					Set<Integer> coveredUses = getUsesBetween(currentUseMap, currentDUCounter, nextDUCounter);
					Set<DefUseCoverageTestFitness> coveredGoals = getGoalsFor(activeDef,coveredUses);
					r.addAll(coveredGoals);
				}
			}
		}

		return r;
	}

	private static Set<DefUseCoverageTestFitness> getGoalsFor(
			int activeDef, Set<Integer> coveredUses) {
		
		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();
		Definition def = DefUsePool.getDefinitionByDefId(activeDef);
		
		List<DefUseCoverageTestFitness> validGoals = DefUseCoverageFactory.getDUGoals();
		
		for(Integer coveredUse : coveredUses) {
			Use use = DefUsePool.getUseByUseId(coveredUse);
			DefUseCoverageTestFitness goal = DefUseCoverageFactory.createGoal(def, use);
			
			if(validGoals.contains(goal))
				r.add(goal);
		}
		
		return r;
	}

	public static Set<Integer> getUsesBetween(
			Map<Integer, Integer> currentUseMap, int currentDUCounter,
			int nextDUCounter) {
		
		Set<Integer> r = new HashSet<Integer>();
		for(Integer duCounter : currentUseMap.keySet())
			if(currentDUCounter<duCounter && duCounter<nextDUCounter)
				r.add(currentUseMap.get(duCounter));
		
		return r;
	}
}
