package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
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

	/**
	 * Returns the defID of the Definition that is active in the given trace at
	 * usePos
	 */
	public static int getActiveDefinitionIdAt(String targetVariable, ExecutionTrace trace, int usePos, int objectId) {

		int lastDef = -1;
		int lastPos = -1;
		Map<Integer, HashMap<Integer, Integer>> objectMap = trace.passedDefinitions.get(targetVariable);
		if (objectMap == null) {
			return -1;
		}
		Map<Integer, Integer> defMap = objectMap.get(objectId);
		if (defMap == null) {
			return -1;
		}
		for (Integer defPos : defMap.keySet()) {
			if (defPos > usePos) {
				continue;
			}
			if (lastPos < defPos) {
				lastDef = defMap.get(defPos);
				lastPos = defPos;
			}
		}
		return lastDef;
	}

	/**
	 * Returns all the duCounterPositions of the goalUse in the given trace
	 */
	public static List<Integer> getDefinitionPositions(Definition targetDefinition, ExecutionTrace trace, int objectId) {

		ArrayList<Integer> r = new ArrayList<Integer>();
		Map<Integer, HashMap<Integer, Integer>> objectMap = trace.passedDefinitions.get(targetDefinition
				.getDUVariableName());
		if (objectMap == null) {
			return r;
		}
		Map<Integer, Integer> defMap = objectMap.get(objectId);
		if (defMap == null) {
			return r;
		}
		for (Integer defPos : defMap.keySet()) {
			if (defMap.get(defPos) == targetDefinition.getDefId()) {
				r.add(defPos);
			}
		}

		return r;
	}

	public static Set<BytecodeInstruction> getDefinitionsIn(String targetVariable, Set<BytecodeInstruction> vertices) {
		Set<BytecodeInstruction> r = new HashSet<BytecodeInstruction>();
		for (BytecodeInstruction vertex : vertices) {
			if (!vertex.isDefinition()) {
				continue;
			}
			Definition currentDefinition = DefUseFactory.makeDefinition(vertex);
			if (currentDefinition.getDUVariableName().equals(targetVariable)) {
				r.add(vertex);
			}
		}
		return r;
	}

	/**
	 * Determines the overwriting definition for the given goalDefPos
	 * 
	 * An overwriting definition position is the duCounter position of the next
	 * definition for goalVariable that was not the goalDefinition
	 * 
	 * If no such definition exists Integer.MAX_VALUE is returned
	 */
	public static int getNextOverwritingDefinitionPosition(Definition targetDefinition, ExecutionTrace objectTrace,
			Integer goalDefPos, Integer objectId) {

		int lastPos = Integer.MAX_VALUE;
		Map<Integer, HashMap<Integer, Integer>> objectMap = objectTrace.passedDefinitions.get(targetDefinition
				.getDUVariableName());
		if (objectMap == null) {
			return lastPos;
		}
		Map<Integer, Integer> defMap = objectMap.get(objectId);
		if (defMap == null) {
			return lastPos;
		}
		for (Integer defPos : defMap.keySet()) {
			if ((defPos > goalDefPos) && (defPos < lastPos) && (defMap.get(defPos) != targetDefinition.getDefId())) {
				lastPos = defPos;
			}
		}

		return lastPos;
	}

	/**
	 * Returns the set of definitionIds from all definitions that overwrite the
	 * goal definition in the given duCounter-range
	 * 
	 * This method expects the given ExecutionTrace not to contain any trace
	 * information for the goal Definition in the given range. If such a trace
	 * is detected this method throws an IllegalStateException!
	 */
	public static Set<Integer> getOverwritingDefinitionIdsBetween(Definition targetDefinition, ExecutionTrace trace,
			int startingDUPos, int endDUPos, int objectId) {

		if (startingDUPos > endDUPos) {
			throw new IllegalArgumentException("start must be lower or equal end");
		}
		Set<Integer> r = new HashSet<Integer>();
		Map<Integer, HashMap<Integer, Integer>> objectMap = trace.passedDefinitions.get(targetDefinition
				.getDUVariableName());
		if (objectMap == null) {
			return r;
		}
		Map<Integer, Integer> defMap = objectMap.get(objectId);
		if (defMap == null) {
			return r;
		}
		for (Integer defPos : defMap.keySet()) {
			if ((defPos < startingDUPos) || (defPos > endDUPos)) {
				continue;
			}
			int defId = defMap.get(defPos);
			if (defId == targetDefinition.getDefId()) {
				throw new IllegalStateException(
						"expect given trace not to have passed goalDefinition in the given duCounter-range");
			}
			r.add(defId);
		}
		return r;
	}

	/**
	 * Returns a Set containing all elements in the given vertex set that are
	 * overwriting definitions for the given targetDefinition
	 */
	public static Set<BytecodeInstruction> getOverwritingDefinitionsIn(Definition targetDefinition,
			Collection<BytecodeInstruction> vertices) {
		Set<BytecodeInstruction> r = new HashSet<BytecodeInstruction>();
		for (BytecodeInstruction vertex : vertices) {
			if (!vertex.isDefinition()) {
				continue;
			}
			BytecodeInstruction vertexInOtherGraph = CFGPool.getRawCFG(vertex.getClassName(), vertex.getMethodName())
					.getInstruction(vertex.getInstructionId());
			Definition currentDefinition = DefUseFactory.makeDefinition(vertexInOtherGraph);
			if (isOverwritingDefinition(targetDefinition, currentDefinition)) {
				r.add(vertex);
			}
		}
		return r;
	}

	/**
	 * Determines the definitionId for targetVar before tagetDUPos in the given
	 * ExecutionTrace
	 * 
	 * If no such definition exists -1 is returned
	 */
	public static int getPreviousDefinitionId(String targetVariable, ExecutionTrace objectTrace, Integer targetDUPos,
			Integer objectId) {

		int prevPos = -1;
		int prevDef = -1;
		Map<Integer, Integer> defMap = objectTrace.passedDefinitions.get(targetVariable).get(objectId);
		for (Integer duPos : defMap.keySet()) {
			if ((duPos < targetDUPos) && (duPos > prevPos)) {
				prevDef = defMap.get(duPos);
				prevPos = duPos;
			}
		}

		return prevDef;
	}

	/**
	 * Returns all the duCounterPositions of the targetUse in the given trace
	 */
	public static List<Integer> getUsePositions(Use targetUse, ExecutionTrace trace, int objectId) {
		ArrayList<Integer> r = new ArrayList<Integer>();
		Map<Integer, HashMap<Integer, Integer>> objectMap = trace.passedUses.get(targetUse.getDUVariableName());
		if (objectMap == null) {
			return r;
		}
		Map<Integer, Integer> useMap = objectMap.get(objectId);
		if (useMap == null) {
			return r;
		}
		for (Integer usePos : useMap.keySet()) {
			if (useMap.get(usePos) == targetUse.getUseId()) {
				r.add(usePos);
			}
		}

		return r;
	}

	/**
	 * Determines if the two given Definitions refer to the same variable but
	 * are different
	 */
	public static boolean isOverwritingDefinition(Definition targetDefinition, Definition definition) {

		if (definition.getDefId() == -1) {
			throw new IllegalArgumentException("expect given Definition to have it's defId set");
		}
		return targetDefinition.getDUVariableName().equals(definition.getDUVariableName())
				&& (targetDefinition.getDefId() != definition.getDefId());
	}

	/**
	 * Prints all information found concerning finished calls of the given
	 * ExecutionTrace
	 */
	public static void printFinishCalls(ExecutionTrace trace) {
		for (MethodCall call : trace.finished_calls) {
			System.out.println("Found MethodCall for: " + call.methodName + " on object " + call.callingObjectID);
			System.out.println("#passed branches: " + call.branchTrace.size());
			for (int i = 0; i < call.defuseCounterTrace.size(); i++) {
				System.out.println(i + ". at Branch " + call.branchTrace.get(i) + " true_dist: "
						+ call.trueDistanceTrace.get(i) + " false_dist: " + call.falseDistanceTrace.get(i)
						+ " duCounter: " + call.defuseCounterTrace.get(i));
				System.out.println();
			}
		}
	}

}
