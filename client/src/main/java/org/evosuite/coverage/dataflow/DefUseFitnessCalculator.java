/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.utils.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class is like a library containing all methods needed to calculate a
 * DefUseCoverage-Fitness
 * 
 * @author Andre Mis
 */
public class DefUseFitnessCalculator {

	/** Constant <code>alternativeTime=0l</code> */
	public static long alternativeTime = 0l;

	private final static boolean DEBUG = Properties.DEFUSE_DEBUG_MODE;

	private static Logger logger = LoggerFactory.getLogger(DefUseFitnessCalculator.class);

	private final DefUseCoverageTestFitness goal;
	private final TestChromosome individual;
	private final ExecutionResult result;

	private final Definition goalDefinition;
	private final Use goalUse;
	private final TestFitnessFunction goalDefinitionFitness;
	private final TestFitnessFunction goalUseFitness;
	// private final String goalVariable;
	private final String defVariable;
	private final String useVariable;

	/**
	 * <p>
	 * Constructor for DefUseFitnessCalculator.
	 * </p>
	 * 
	 * @param goal
	 *            a
	 *            {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness}
	 *            object.
	 * @param individual
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param result
	 *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 */
	public DefUseFitnessCalculator(DefUseCoverageTestFitness goal,
	        TestChromosome individual, ExecutionResult result) {
		this.goal = goal;
		this.individual = individual;
		this.result = result;

		this.goalDefinition = goal.getGoalDefinition();
		this.goalUse = goal.getGoalUse();
		this.goalDefinitionFitness = goal.getGoalDefinitionFitness();
		this.goalUseFitness = goal.getGoalUseFitness();
		// this.goalVariable = goalUse.getVariableName();
		if (goalDefinition == null)
			this.defVariable = goalUse.getVariableName();
		else
			this.defVariable = goalDefinition.getVariableName();
		this.useVariable = goalUse.getVariableName();
	}

	// main Definition-Use fitness calculation methods

	/**
	 * Calculates the DefinitionUseCoverage fitness for the given DUPair on the
	 * given ExecutionResult
	 * 
	 * The fitness is calculated as follows:
	 * 
	 * If the goalDefinition is not passed in the result at all: This method
	 * returns 1 + normalize(goalDefinitionFitness) where goalDefinition equals
	 * the BranchCoverageTestFitness for the Branch that the CFGVertex of this
	 * goals definition is control dependent on (goalDefinitionBranch)
	 * 
	 * If the goalDefinition is passed, but the goalUse is not passed at all:
	 * This method returns the normalized BranchCoverageTestFitness for the
	 * Branch that the CFGVertex of this goals Use is control dependent on
	 * (goalUseBranch)
	 * 
	 * If both the goalDefinition and the goalUse were passed at least once in
	 * the given result: 1. If and only if at any goalUsePosition the active
	 * definition was the goalDefinition the Definition-Use-Pair of this goal is
	 * covered and the method returns 0
	 * 
	 * Otherwise this method returns the minimum of the following: 1. For all
	 * goalUsePositions if there was an overwriting definition, the normalized
	 * sum over all such overwriting definitions of the normalized
	 * BranchCoverageTestFitness for not taking the branch with the overwriting
	 * definition look at calculateAltenativeFitness()
	 * 
	 * For all goalDefPositions the normalized BranchCoverageTestFitness for the
	 * goalUseBranch in the ExecutionTrace where every trace information is
	 * filtered out except the information traced between the occurrence of the
	 * goalDefinitionPosition and the next overwritingDefinitionPosition look at
	 * calculateUseFitnessForDefinitionPosition()
	 * 
	 * If this goals definition is not a static variable the trace information
	 * of all constructed objects of the CUT are handled separately and the
	 * minimum over all individually calculated fitness is returned
	 * 
	 * @return a double.
	 */
	public double calculateDUFitness() {

		//		if (!goalVariable.equals("targetField"))
		//			return 0.0;

		// at first handle special cases where definition is assumed to be
		// covered if use is covered:

		if (isSpecialDefinition(goalDefinition))
			return calculateUseFitnessForCompleteTrace();

		// Case 1.
		// now check if goalDefinition was passed at all before considering
		// individual objects

		double defFitness = calculateDefFitnessForCompleteTrace();
		if (defFitness != 0)
			return 1 + defFitness;

		// Case 2.
		// if the use was not passed at all just calculate the fitness
		// over all objects without any filtering

		/* FIXXME: This doesn't seem to make much sense
		if (!hasEntriesForId(result.getTrace().getPassedUses(goalVariable),
		                     goalUse.getUseId()))
			return calculateUseFitnessForCompleteTrace();
			*/

		// Case 3.
		// filter the trace for each considerable object that passed both the
		// goalDefinition and the goalUse, cut the traces between goalDef
		// occurrences and overwritingDef occurrences and calculate useFitness
		// and possibly overwriting/alternativeFitness
		return calculateFitnessForObjects();
	}

	public double calculateFitnessForObjects() {

		// select considerable objects
		Set<Integer> objects = determineConsiderableObjects(goal, result.getTrace());

		// calculate minimal fitness over all objects
		double fitness = 1;
		for (Integer object : objects) {
			logger.debug("current object: " + object);
			if (!hasEntriesForId(result.getTrace().getPassedDefinitions(defVariable),
			                     object, goalDefinition.getDefId()))
				continue;

			double newFitness = calculateFitnessForObject(object);
			if (newFitness < fitness)
				fitness = newFitness;
			if (fitness == 0.0)
				return 0.0;
		}
		return fitness;
	}

	/**
	 * Calculates the DefUseCoverage fitness for this DefUsePair considering
	 * only the objectId'th CUT-Object in the ExecutionResult
	 * 
	 * Gets called for all CUT-objects in the ExecutionResult whenever the
	 * goalDefinition and the goalUse were passed at least once on that object.
	 */
	private double calculateFitnessForObject(Integer objectId) {

		// filter out trace information from other objects
		ExecutionTrace objectTrace = result.getTrace().getTraceForObject(objectId);
		double fitness = 1;
		// handle special definition case TODO already handled!?
		if (isSpecialDefinition(goalDefinition)) {
			double useFitness = callTestFitnessFunctionForTrace(objectTrace,
			                                                    goalUseFitness);
			fitness = normalize(useFitness);
			if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE) && fitness == 0.0)
				goal.setCovered(individual, objectTrace, objectId);
			return fitness;
		}

		// check if goalDefinition is active at any goalUsePosition
		List<Integer> usePositions = DefUseExecutionTraceAnalyzer.getUsePositions(goalUse,
		                                                                          objectTrace,
		                                                                          objectId);
		List<Integer> goalDefinitionPositions = DefUseExecutionTraceAnalyzer.getDefinitionPositions(goalDefinition,
		                                                                                            objectTrace,
		                                                                                            objectId);

		if (!defVariable.equals(useVariable)) {
			logger.debug("Checking an aliasing case: " + goalDefinition + "\n" + goalUse);
		}

		for (Integer usePos : usePositions) {
			int activeDefId = DefUseExecutionTraceAnalyzer.getActiveDefinitionIdAt(defVariable,
			                                                                       objectTrace,
			                                                                       usePos,
			                                                                       objectId);
			logger.debug("Activedef at position " + usePos + " is: "
			        + DefUsePool.getDefinitionByDefId(activeDefId));
			if (activeDefId == goalDefinition.getDefId()) {
				// Case 3.1.
			    if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE))
					goal.setCovered(individual, objectTrace, objectId); {
						if(!defVariable.equals(useVariable)) {
							// Check if object is equal
							Object definedObject = DefUseExecutionTraceAnalyzer.getActiveObjectAtDefinition(result.getTrace(), defVariable, objectId, usePos);
							Object usedObject = DefUseExecutionTraceAnalyzer.getActiveObjectAtUse(result.getTrace(), useVariable, objectId, usePos);
							logger.info(definedObject+", "+usedObject);
							if(definedObject == usedObject) {
								logger.debug("That's the target def we're looking for, and objects are equal!");
								return 0.0;
							}
							else {
								logger.debug("That's the target def we're looking for, but objects are not equal!");
								continue;
							}
						}
						return 0.0;
					}
			}
		}
		// Case 3.2.2
		// calculate minimal useFitness over all goalDefPositions
		// DONE: this can be optimized! for example if the goalDef is never
		// overwritten by another
		// definition but is passed a lot this causes major overhead that is
		// totally unnecessary
		// idea: you only have to do this if the last definition for goalVar was
		// not goalDefinitionId
		if (!goalUse.isRootBranchDependent())
			// if goal use is root branch
			// dependent useFitness will
			// always be 1.0
			for (Integer goalDefinitionPos : goalDefinitionPositions) {
				double useFitness;
				try {
					useFitness = calculateUseFitnessForDefinitionPos(objectTrace,
					                                                 objectId,
					                                                 goalDefinitionPos);
				} catch (UnexpectedFitnessException e) {
					continue;
				}
				// if(useFitness == 0.0)
				// throw new
				// IllegalStateException("unexpected: should have been detected earlier");
				double newFitness = normalize(useFitness);
				if (newFitness < fitness)
					fitness = newFitness;

			}
		return fitness;
	}

	

	/**
	 * Determines the BranchCoverageTestFitness of goalDefinitionBranch
	 * considering the full ExecutionTrace
	 * 
	 * Is called on every call to getDistance() if the goalDefinition isn't
	 * special s. isSpecialGoalDefinition()
	 */
	private double calculateDefFitnessForCompleteTrace() {
		if (isSpecialDefinition(goalDefinition))
			return 0.0;
		// check ExecutionTrace.passedDefinitions first, because calculating
		// BranchTestFitness takes time
		if (hasEntriesForId(result.getTrace().getPassedDefinitions(goalDefinition.getVariableName()),
		                    goalDefinition.getDefId()))
			return 0.0;

		// return calculated fitness
		double fitness = goalDefinitionFitness.getFitness(individual, result);

		// check for false positive: TODO warn?
		// this can happen, when a du is within a catch-block, since we
		// currently don't handle these correctly in the CDG department
		if (fitness == 0.0)
			return 1.0;

		return normalize(fitness);
	}

	/**
	 * Determines the BranchCoverageTestFitness of goalUseBranch considering the
	 * full ExecutionTrace
	 * 
	 * Is called on every call to getDistance() if the goalDefinition isn't
	 * special s. isSpecialGoalDefinition()
	 */
	private double calculateUseFitnessForCompleteTrace() {

		// check ExecutionTrace.passedUses first, because calculating
		// BranchTestFitness takes time
		if (hasEntriesForId(result.getTrace().getPassedUses(goalUse.getVariableName()),
		                    goalUse.getUseId()))
			return 0.0;

		// return calculated fitness
		double fitness = goalUseFitness.getFitness(individual, result);

		// check for false positive: TODO warn?
		// this can happen, when a du is within a catch-block, since we
		// currently don't handle these correctly in the CDG department
		if (fitness == 0.0)
			return 1.0;

		return normalize(fitness);
	}

	/**
	 * Calculates the goalUseFitness for the given goalDefinitionPos as follows:
	 * 
	 * For every goalDefinitionPosition in the objectTrace for the given
	 * objectId this method gets called by getDistance() to determine the
	 * fitness of the goalUseBranch considering only information traced between
	 * the goalDefinitionPosition and the occurrence of the next overwriting
	 * definition
	 * 
	 * In order to do that the ExecutionTrace is filtered using
	 * calculateFitnessForDURange()
	 * 
	 * To avoid making unnecessary calculations only calculate the fitness if
	 * the definition coming before goalDefinitionPos was not the
	 * goalDefinition. If it was, 1 is returned.
	 * 
	 * @throws UnexpectedFitnessException
	 * 
	 */
	private double calculateUseFitnessForDefinitionPos(ExecutionTrace targetTrace,
	        Integer objectId, int goalDefinitionPos) throws UnexpectedFitnessException {

		int previousDefId = DefUseExecutionTraceAnalyzer.getPreviousDefinitionId(goalDefinition.getVariableName(),
		                                                                         targetTrace,
		                                                                         goalDefinitionPos,
		                                                                         objectId);
		if (previousDefId == goalDefinition.getDefId())
			return 1.0;

		int overwritingDefPos = DefUseExecutionTraceAnalyzer.getNextOverwritingDefinitionPosition(goalDefinition,
		                                                                                          targetTrace,
		                                                                                          goalDefinitionPos,
		                                                                                          objectId);

		try {
			return calculateFitnessForDURange(targetTrace, objectId, goalUseFitness,
			                                  goalUse, true, goalDefinitionPos,
			                                  overwritingDefPos, true);
		} catch (UnexpectedFitnessException e) {
			return 1.0;
		}
	}

	/**
	 * Used to calculate a BranchCoverageTestFitness considering only trace
	 * information in a given range of duCounter positions
	 * 
	 * Filters the ExecutionTrace using
	 * ExecutionTrace.getTraceInDUCounterRange() to only contain information
	 * made between duCounterStart and duCounterEnd
	 * 
	 * Additionally, if wantToCoverTargetDU is set all points in the trace where
	 * the given targetDUBranch would be passed is filtered out in order to
	 * prevent miscalculations. Again ExecutionTrace.getTraceInDUCounterRange()
	 * holds more information
	 * 
	 * 
	 * 
	 * This method gets called for alternative fitness calculation -
	 * calculateAlternativeFitness() - and in order to determine the
	 * goalUseBranch fitness between a goalUsePos and its next overwriting
	 * definition - calculateUseFitnessForDefinitionPos()
	 * 
	 * @throws UnexpectedFitnessException
	 * 
	 * 
	 */
	private double calculateFitnessForDURange(ExecutionTrace targetTrace,
	        Integer objectId, TestFitnessFunction targetFitness, DefUse targetDU,
	        boolean wantToCoverTargetDU, int duCounterStart, int duCounterEnd,
	        boolean expectNotToBeZero) throws UnexpectedFitnessException {

		// filter trace
		ExecutionTrace cutTrace = targetTrace.getTraceInDUCounterRange(targetDU,
		                                                               wantToCoverTargetDU,
		                                                               duCounterStart,
		                                                               duCounterEnd);
		// calculate fitness
		double fitness = callTestFitnessFunctionForTrace(cutTrace, targetFitness);

		// TODO see comment in calculateDUFitness() Case2.
		// // sanity check
		// if (fitness == 0.0 && !wantToCoverTargetDU) {
		//		
		// System.out.println(this.goal.toString());
		//		
		// System.out.println(cutTrace.toDefUseTraceInformation());
		// DefUseExecutionTraceAnalyzer.printFinishCalls(cutTrace);
		//		
		// System.out.println("duPosStart: " + duCounterStart);
		// System.out.println("duPosEnd: " + duCounterEnd);
		// // int targetUseBranchBytecode =
		// // targetDU.getControlDependentBranch()
		// // .getInstruction().getInstructionId();
		// // System.out.println("targetDU-branch-bytecode: "
		// // + targetUseBranchBytecode);
		// throw new IllegalStateException(targetFitness.toString()
		// + " cant have fitness 0 in this cut trace: "
		// + cutTrace.toDefUseTraceInformation(targetDU
		// .getDUVariableName(), objectId));
		// }
		if (expectNotToBeZero && fitness == 0.0)
			throw new UnexpectedFitnessException();

		// else
		// throw new IllegalStateException(
		// "avoiding fitness should not be 0 if instruction was actually passed "
		// + targetFitness.toString());

		return fitness;
	}

	// other core methods

	/**
	 * Executes the TestFitnessFunction.getFitness() function on the given
	 * ExecutionResult but using the given targetTrace
	 * 
	 * The ExecutionResult is left in it's original state after execution
	 */
	private double callTestFitnessFunctionForTrace(ExecutionTrace targetTrace,
	        TestFitnessFunction targetFitness) {

		ExecutionTrace originalTrace = result.getTrace();
		result.setTrace(targetTrace);
		double fitness = targetFitness.getFitness(individual, result);
		result.setTrace(originalTrace);
		return fitness;
	}

	/**
	 * Determines whether the given definition is assumed to always be covered
	 * 
	 * This is the case for static definitions in <clinit> and
	 * Parameter-Definitions
	 */
	private static boolean isSpecialDefinition(Definition definition) {
		if (definition == null
		        || (definition.isStaticDefUse() && definition.getMethodName().startsWith("<clinit>"))) {

			if (definition == null)
				logger.debug("Assume Parameter-Definition to be covered if the Parameter-Use is covered");
			else
				logger.debug("Assume definition from <clinit> to always be covered");

			return true;
		}
		return false;
	}

	/**
	 * Determines the object Pool of the given trace for this DefUsePair
	 * 
	 * If the goalVariable is static all objects are considered otherwise only
	 * those objects that have passed both the goalUse and the goalDefinition
	 * are considered
	 */
	private static Set<Integer> determineConsiderableObjects(
	        DefUseCoverageTestFitness goal, ExecutionTrace trace) {
		String goalVariable = goal.getGoalVariable();
		Definition goalDefinition = goal.getGoalDefinition();

		Set<Integer> objectPool = new HashSet<Integer>();
		if (trace.getPassedUses(goalVariable) == null)
			return objectPool;
		if (trace.getPassedDefinitions(goalVariable) != null)
			objectPool.addAll(trace.getPassedDefinitions(goalVariable).keySet());
		if (goalDefinition == null || goalDefinition.isStaticDefUse()) {
			// in the static case all objects have to be considered
			objectPool.addAll(trace.getPassedUses(goalVariable).keySet());
			if (DEBUG)
				logger.debug("Static-goalVariable! Using all known Objects");
		} else {
			// on non-static goalVariables only look at objects that have traces
			// of defs and uses for the goalVariable
			int oldSize = objectPool.size();
			objectPool.retainAll(trace.getPassedUses(goalVariable).keySet());
			if (DEBUG) {
				logger.debug("NON-Static-goalVariable " + goalVariable);
				logger.debug("#unused objects: " + (oldSize - objectPool.size()));
				Set<Integer> discardedObjects = new HashSet<Integer>();
				discardedObjects.addAll(trace.getPassedDefinitions(goalVariable).keySet());
				discardedObjects.removeAll(trace.getPassedUses(goalVariable).keySet());
				for (Integer id : discardedObjects) {
					logger.debug("  discarded object " + id);
				}
			}
		}
		if (DEBUG) {
			logger.debug("#considered objects: " + objectPool.size());
			for (Integer id : objectPool) {
				logger.debug("  object " + id);
			}
		}
		return objectPool;
	}

	// auxiliary methods

	/**
	 * <p>
	 * normalize
	 * </p>
	 * 
	 * @param value
	 *            a double.
	 * @return a double.
	 */
	public static double normalize(double value) {
		// TODO just copied this from FitnessFunction because it was not visible
		// from here
		return value / (1.0 + value);
	}

	/**
	 * <p>
	 * hasEntryLowerThan
	 * </p>
	 * 
	 * @param list
	 *            a {@link java.util.List} object.
	 * @param border
	 *            a {@link java.lang.Integer} object.
	 * @return a boolean.
	 */
	public static boolean hasEntryLowerThan(List<Integer> list, Integer border) {
		for (Integer pos : list)
			if (pos < border)
				return true;
		return false;
	}

	/**
	 * <p>
	 * hasEntryInBetween
	 * </p>
	 * 
	 * @param list
	 *            a {@link java.util.List} object.
	 * @param start
	 *            a {@link java.lang.Integer} object.
	 * @param end
	 *            a {@link java.lang.Integer} object.
	 * @return a boolean.
	 */
	public static boolean hasEntryInBetween(List<Integer> list, Integer start, Integer end) {
		for (Integer pos : list)
			if (start < pos && pos < end)
				return true;
		return false;
	}

	/**
	 * <p>
	 * getMaxEntryLowerThan
	 * </p>
	 * 
	 * @param list
	 *            a {@link java.util.List} object.
	 * @param border
	 *            a {@link java.lang.Integer} object.
	 * @return a {@link java.lang.Integer} object.
	 */
	public static Integer getMaxEntryLowerThan(List<Integer> list, Integer border) {
		int lastPos = -1;
		for (Integer defPos : list)
			if (defPos < border && defPos > lastPos)
				lastPos = defPos;
		return lastPos;
	}

	/**
	 * <p>
	 * hasEntriesForId
	 * </p>
	 * 
	 * @param objectDUMap
	 *            a {@link java.util.Map} object.
	 * @param targetId
	 *            a int.
	 * @return a boolean.
	 */
	public static boolean hasEntriesForId(
	        Map<Integer, HashMap<Integer, Integer>> objectDUMap, int targetId) {
		if (objectDUMap == null)
			return false;
		for (Integer objectId : objectDUMap.keySet())
			if (hasEntriesForId(objectDUMap, objectId, targetId))
				return true;

		return false;
	}

	/**
	 * <p>
	 * hasEntriesForId
	 * </p>
	 * 
	 * @param objectDUMap
	 *            a {@link java.util.Map} object.
	 * @param objectId
	 *            a {@link java.lang.Integer} object.
	 * @param targetId
	 *            a int.
	 * @return a boolean.
	 */
	public static boolean hasEntriesForId(
	        Map<Integer, HashMap<Integer, Integer>> objectDUMap, Integer objectId,
	        int targetId) {
		if (objectDUMap == null)
			return false;
		if (objectDUMap.get(objectId) == null)
			return false;
		for (Integer defId : objectDUMap.get(objectId).values())
			if (defId.intValue() == targetId)
				return true;
		return false;
	}

	/**
	 * Only a sanity check function for testing purposes
	 * 
	 * @param goal
	 *            a
	 *            {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness}
	 *            object.
	 * @param individual
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param trace
	 *            a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
	 * @return a boolean.
	 */
	public static boolean traceCoversGoal(DefUseCoverageTestFitness goal,
	        Chromosome individual, ExecutionTrace trace) {
		String goalVariable = goal.getGoalVariable();
		Use goalUse = goal.getGoalUse();
		Definition goalDefinition = goal.getGoalDefinition();

		if (trace.getPassedUses(goalVariable) == null)
			return false;
		Set<Integer> objectPool = determineConsiderableObjects(goal, trace);

		for (Integer objectID : objectPool) {
			List<Integer> usePositions = DefUseExecutionTraceAnalyzer.getUsePositions(goalUse,
			                                                                          trace,
			                                                                          objectID);
			// use not reached
			if (usePositions.size() == 0)
				continue;
			//			if (goalUse.isParameterUse())
			//				return true;
			if (isSpecialDefinition(goalDefinition))
				return true;

			for (Integer usePos : usePositions) {

				if (DefUseExecutionTraceAnalyzer.getActiveDefinitionIdAt(goalVariable,
				                                                         trace, usePos,
				                                                         objectID) == goalDefinition.getDefId())
					return true;
			}
		}

		return false;
	}

}
