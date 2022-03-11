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
package org.evosuite.coverage.dataflow;

import org.evosuite.TestGenerationContext;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.MethodCall;

import java.util.*;

/**
 * This class is a library holding all methods needed to analyze an
 * ExecutionTrace with respect to DefUseCoverage fitness calculation
 *
 * @author Andre Mis
 */
public abstract class DefUseExecutionTraceAnalyzer {

    /**
     * Constant <code>timeGetCoveredGoals=0l</code>
     */
    public static long timeGetCoveredGoals = 0L;

    /**
     * Determines the definitionId for targetVar before tagetDUPos in the given
     * ExecutionTrace
     * <p>
     * If no such definition exists -1 is returned
     *
     * @param targetVariable a {@link java.lang.String} object.
     * @param objectTrace    a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     * @param targetDUPos    a {@link java.lang.Integer} object.
     * @param objectId       a {@link java.lang.Integer} object.
     * @return a int.
     */
    public static int getPreviousDefinitionId(String targetVariable,
                                              ExecutionTrace objectTrace, Integer targetDUPos, Integer objectId) {

        int prevPos = -1;
        int prevDef = -1;
        Map<Integer, Integer> defMap = objectTrace.getPassedDefinitions(targetVariable).get(objectId);
        for (Integer duPos : defMap.keySet())
            if (duPos < targetDUPos && duPos > prevPos) {
                prevDef = defMap.get(duPos);
                prevPos = duPos;
            }

        return prevDef;
    }

    /**
     * Determines the overwriting definition for the given goalDefPos
     * <p>
     * An overwriting definition position is the duCounter position of the next
     * definition for goalVariable that was not the goalDefinition
     * <p>
     * If no such definition exists Integer.MAX_VALUE is returned
     *
     * @param targetDefinition a {@link org.evosuite.coverage.dataflow.Definition} object.
     * @param objectTrace      a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     * @param goalDefPos       a {@link java.lang.Integer} object.
     * @param objectId         a {@link java.lang.Integer} object.
     * @return a int.
     */
    public static int getNextOverwritingDefinitionPosition(Definition targetDefinition,
                                                           ExecutionTrace objectTrace, Integer goalDefPos, Integer objectId) {

        int lastPos = Integer.MAX_VALUE;
        Map<Integer, HashMap<Integer, Integer>> objectMap = objectTrace.getPassedDefinitions(targetDefinition.getVariableName());
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
     *
     * @param targetUse a {@link org.evosuite.coverage.dataflow.Use} object.
     * @param trace     a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     * @param objectId  a int.
     * @return a {@link java.util.List} object.
     */
    public static List<Integer> getUsePositions(Use targetUse, ExecutionTrace trace,
                                                int objectId) {
        ArrayList<Integer> r = new ArrayList<>();
        Map<Integer, HashMap<Integer, Integer>> objectMap = trace.getPassedUses(targetUse.getVariableName());
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
     *
     * @param targetDefinition a {@link org.evosuite.coverage.dataflow.Definition} object.
     * @param trace            a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     * @param objectId         a int.
     * @return a {@link java.util.List} object.
     */
    public static List<Integer> getDefinitionPositions(Definition targetDefinition,
                                                       ExecutionTrace trace, int objectId) {

        ArrayList<Integer> r = new ArrayList<>();
        Map<Integer, HashMap<Integer, Integer>> objectMap = trace.getPassedDefinitions(targetDefinition.getVariableName());
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
     * <p>
     * This method expects the given ExecutionTrace not to contain any trace
     * information for the targetDefinition in the given range. If such a trace
     * is detected this method throws an IllegalStateException!
     *
     * @param targetDefinition a {@link org.evosuite.coverage.dataflow.Definition} object.
     * @param trace            a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     * @param startingDUPos    a int.
     * @param endDUPos         a int.
     * @param objectId         a int.
     * @return a {@link java.util.Map} object.
     */
    public static Map<Integer, Integer> getOverwritingDefinitionsBetween(
            Definition targetDefinition, ExecutionTrace trace, int startingDUPos,
            int endDUPos, int objectId) {

        if (startingDUPos > endDUPos)
            throw new IllegalArgumentException("start must be lower or equal end");
        Map<Integer, Integer> r = new HashMap<>();
        Map<Integer, HashMap<Integer, Integer>> objectMap = trace.getPassedDefinitions(targetDefinition.getVariableName());
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

    /**
     * <p>
     * getDefinitionsIn
     * </p>
     *
     * @param targetVariable a {@link java.lang.String} object.
     * @param vertices       a {@link java.util.Set} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<BytecodeInstruction> getDefinitionsIn(String targetVariable,
                                                            Set<BytecodeInstruction> vertices) {
        Set<BytecodeInstruction> r = new HashSet<>();
        for (BytecodeInstruction vertex : vertices) {
            //			if (!vertex.isDefinition())
            //				continue;
            if (!DefUsePool.isKnownAsDefinition(vertex))
                continue;
            Definition currentDefinition = DefUseFactory.makeDefinition(vertex);
            if (currentDefinition.getVariableName().equals(targetVariable))
                r.add(vertex);
        }
        return r;
    }

    /**
     * Returns a Set containing all elements in the given vertex set that are
     * overwriting definitions for the given targetDefinition
     *
     * @param targetDefinition a {@link org.evosuite.coverage.dataflow.Definition} object.
     * @param vertices         a {@link java.util.Collection} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<BytecodeInstruction> getOverwritingDefinitionsIn(
            Definition targetDefinition, Collection<BytecodeInstruction> vertices) {
        Set<BytecodeInstruction> r = new HashSet<>();
        for (BytecodeInstruction vertex : vertices) {
            if (!vertex.isDefinition())
                continue;
            BytecodeInstruction vertexInOtherGraph = GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getRawCFG(vertex.getClassName(),
                    vertex.getMethodName()).getInstruction(vertex.getInstructionId());
            Definition currentDefinition = DefUseFactory.makeDefinition(vertexInOtherGraph);
            if (isOverwritingDefinition(targetDefinition, currentDefinition))
                r.add(vertex);
        }
        return r;
    }

    /**
     * Determines if the two given Definitions refer to the same variable but
     * are different
     *
     * @param targetDefinition a {@link org.evosuite.coverage.dataflow.Definition} object.
     * @param definition       a {@link org.evosuite.coverage.dataflow.Definition} object.
     * @return a boolean.
     */
    public static boolean isOverwritingDefinition(Definition targetDefinition,
                                                  Definition definition) {

        if (definition.getDefId() == -1)
            throw new IllegalArgumentException(
                    "expect given Definition to have it's defId set");
        return targetDefinition.getVariableName().equals(definition.getVariableName())
                && targetDefinition.getDefId() != definition.getDefId();
    }

    /**
     * Returns the defID of the Definition that is active in the given trace at
     * usePos
     *
     * @param targetVariable a {@link java.lang.String} object.
     * @param trace          a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     * @param usePos         a int.
     * @param objectId       a int.
     * @return a int.
     */
    public static int getActiveDefinitionIdAt(String targetVariable,
                                              ExecutionTrace trace, int usePos, int objectId) {

        int lastDef = -1;
        int lastPos = -1;
        Map<Integer, HashMap<Integer, Integer>> objectMap = trace.getPassedDefinitions(targetVariable);
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

    public static Object getActiveObjectAtDefinition(ExecutionTrace trace, String targetVariable,
                                                     int objectId, int usePos) {

        Map<String, HashMap<Integer, HashMap<Integer, Object>>> defDataObjects = trace.getDefinitionDataObjects();
        defDataObjects.get(targetVariable).get(objectId);

        Object lastDef = null;
        int lastPos = -1;
        Map<Integer, HashMap<Integer, Object>> objectMap = defDataObjects.get(targetVariable);
        if (objectMap == null)
            return -1;
        Map<Integer, Object> defMap = objectMap.get(objectId);
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

    public static Object getActiveObjectAtUse(ExecutionTrace trace, String targetVariable,
                                              int objectId, int usePos) {

        Map<String, HashMap<Integer, HashMap<Integer, Object>>> defDataObjects = trace.getDefinitionDataObjects();
        defDataObjects.get(targetVariable).get(objectId);

        Object lastDef = null;
        int lastPos = -1;
        Map<Integer, HashMap<Integer, Object>> objectMap = defDataObjects.get(targetVariable);
        if (objectMap == null)
            return -1;
        Map<Integer, Object> defMap = objectMap.get(objectId);
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
     *
     * @param trace a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     */
    public static void printFinishCalls(ExecutionTrace trace) {
        for (MethodCall call : trace.getMethodCalls()) {
            System.out.println("Found MethodCall for: " + call.methodName + " on object "
                    + call.callingObjectID);
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

    /**
     * <p>
     * getCoveredGoals
     * </p>
     *
     * @param results a {@link java.util.List} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<DefUseCoverageTestFitness> getCoveredGoals(
            List<ExecutionResult> results) {

        // did an experiment here: subject ncs.Bessj
        // so as it turns out the getCoveredGoals() might have to run through up
        // to 50k entry big duTraces and thus take up another 20-25% memory, but
        // if you disable this you have to take more time in single fitness
        // calculations and gain worse coverage / less statements per time
        // so for ncs.Bessj with this disabled:

        /*
         * ida@ubuntu:~/Bachelor/again/evosuite/examples/ncs$ ^C
         * ida@ubuntu:~/Bachelor/again/evosuite/examples/ncs$ ../../EvoSuite
         * -generateSuite -criterion defuse -class ncs.Bessj Generating tests
         * for class ncs.Bessj Test criterion: All DU Pairs Setting up search
         * algorithm for whole suite generation Goal computation took: 41ms
         * Starting evolution Alternative fitness calculation disabled!
         * [Progress:=====> 19%] [Cov:========================> 71%] Search
         * finished after 600s and 6 generations, 19420 statements, best
         * individual has fitness 46.49999999592326 Minimizing result Generated
         * 4 tests with total length 17 Resulting TestSuite's coverage: 71%
         * GA-Budget: - ShutdownTestWriter : 0 / 0 - GlobalTime : 602 / 600
         * Finished! - ZeroFitness : 46 / 0 - MaxStatements : 20.881 / 100.000
         * Covered 92/129 goals Time spent optimizing covered goals analysis:
         * 0ms Time spent executing tests: 59980ms Writing JUnit test cases to
         * evosuite-tests/DEFUSE Time spent calculating single fitnesses:
         * 540751ms Done!
         */

        // and enabled:
        /*
         * ida@ubuntu:~/Bachelor/again/evosuite/examples/ncs$ ../../EvoSuite
         * -generateSuite -criterion defuse -class ncs.Bessj Generating tests
         * for class ncs.Bessj Test criterion: All DU Pairs Setting up search
         * algorithm for whole suite generation Goal computation took: 42ms
         * Starting evolution Alternative fitness calculation disabled!
         * [Progress:=======> 25%] [Cov:========================> 71%] Search
         * finished after 600s and 11 generations, 25732 statements, best
         * individual has fitness 46.49999999553073 Minimizing result Generated
         * 4 tests with total length 19 Resulting TestSuite's coverage: 71%
         * GA-Budget: - ShutdownTestWriter : 0 / 0 - MaxStatements : 28.030 /
         * 100.000 - ZeroFitness : 46 / 0 - GlobalTime : 604 / 600 Finished!
         * Covered 92/129 goals Time spent optimizing covered goals analysis:
         * 414365ms Time spent executing tests: 87669ms Writing JUnit test cases
         * to evosuite-tests/DEFUSE Time spent calculating single fitnesses:
         * 100324ms Done!
         */

        // so we have 25% more executed statements, which means this will stay
        // enabled

        //		System.out.println("start");
        long start = System.currentTimeMillis();

        Set<DefUseCoverageTestFitness> r = new HashSet<>();

        for (ExecutionResult result : results) {
            Set<DefUseCoverageTestFitness> goals = getCoveredGoals(result);
            r.addAll(goals);
        }

        timeGetCoveredGoals += System.currentTimeMillis() - start;
        //		System.out.println("end");

        return r;
    }

    /**
     * <p>
     * getCoveredGoals
     * </p>
     *
     * @param result a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<DefUseCoverageTestFitness> getCoveredGoals(ExecutionResult result) {

        Set<DefUseCoverageTestFitness> r = new HashSet<>();

        Map<String, HashMap<Integer, HashMap<Integer, Integer>>> passedDefs = result.getTrace().getDefinitionData();
        Map<String, HashMap<Integer, HashMap<Integer, Integer>>> passedUses = result.getTrace().getUseData();

        for (String goalVariable : passedDefs.keySet()) {
            if (passedUses.get(goalVariable) == null)
                continue;
            for (Integer objectId : passedDefs.get(goalVariable).keySet()) {
                if (passedUses.get(goalVariable).get(objectId) == null)
                    continue;

                // DONE sort use map too, merge to one big trace => way better
                // performance
                Map<Integer, Integer> currentDefMap = passedDefs.get(goalVariable).get(objectId);
                Map<Integer, Integer> currentUseMap = passedUses.get(goalVariable).get(objectId);

                List<Integer> duCounterTrace = new ArrayList<>(
                        currentDefMap.keySet());
                duCounterTrace.addAll(currentUseMap.keySet());
                //				System.out.println(duCounterTrace.size()); oO for ncs.Bessj these can be up to 50k entries big
                Collections.sort(duCounterTrace);
                int traceLength = duCounterTrace.size();
                Integer[] sortedDefDUTrace = duCounterTrace.toArray(new Integer[traceLength]);

                int activeDef = -1;
                for (int i = 0; i < traceLength; i++) {
                    int currentDUCounter = sortedDefDUTrace[i];

                    if (currentDefMap.containsKey(currentDUCounter)) {
                        activeDef = currentDefMap.get(currentDUCounter);
                    } else if (activeDef != -1) {
                        int currentUse = currentUseMap.get(currentDUCounter);
                        DefUseCoverageTestFitness currentGoal = DefUseCoverageFactory.retrieveGoal(activeDef,
                                currentUse);
                        if (currentGoal != null)
                            r.add(currentGoal);
                    }
                }
            }
        }

        return r;
    }

    //	private static Set<DefUseCoverageTestFitness> getGoalsFor(int activeDef,
    //			Set<Integer> coveredUses) {
    //
    //		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();
    //		Definition def = DefUsePool.getDefinitionByDefId(activeDef);
    //
    //		List<DefUseCoverageTestFitness> validGoals = DefUseCoverageFactory
    //				.getDUGoals();
    //
    //		for (Integer coveredUse : coveredUses) {
    //			Use use = DefUsePool.getUseByUseId(coveredUse);
    //			DefUseCoverageTestFitness goal = DefUseCoverageFactory.createGoal(
    //					def, use);
    //
    //			if (validGoals.contains(goal))
    //				r.add(goal);
    //		}
    //
    //		return r;
    //	}
    //
    //	public static Set<Integer> getUsesBetween(
    //			Map<Integer, Integer> currentUseMap, int currentDUCounter,
    //			int nextDUCounter) {
    //
    //		Set<Integer> r = new HashSet<Integer>();
    //		for (Integer duCounter : currentUseMap.keySet())
    //			if (currentDUCounter < duCounter && duCounter < nextDUCounter)
    //				r.add(currentUseMap.get(duCounter));
    //
    //		return r;
    //	}
}
