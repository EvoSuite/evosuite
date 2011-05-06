package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.AlternativeFitnessCalculationMode;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * 
 * This class is like a library containing all methods needed to calculate a
 * DefUseCoverage-Fitness
 * 
 * @author Andre Mis
 */
public class DefUseFitnessCalculations {

	private final static boolean DEBUG = Properties.DEFUSE_DEBUG_MODE;

	private static Logger logger = Logger.getLogger(DefUseFitnessCalculations.class);

	// TODO: move these to Properties?

	// alternative fitness calculation
	public static final boolean ENABLE_ALTERNATIVE_FITNESS_CALCULATION = Properties.ENABLE_ALTERNATIVE_FITNESS_CALCULATION;

	// if alternative fitness calculation is disabled ignore the following
	// valid modes: "sum", "min", "max", "avg", "single"
	//private static final String ALTERNATIVE_FITNESS_CALCULATION_MODE = Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE;
	// if the mode isn't "sum" the following are ignored
	private static boolean PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT = Properties.PENALIZE_OVERWRITING_DEFINITIONS_FLAT;
	private static boolean PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY = Properties.PENALIZE_OVERWRITING_DEFINITIONS_LINEARLY;
	private static double SINGLE_ALTERNATIVE_FITNESS_RANGE = Properties.ALTERNATIVE_FITNESS_RANGE;
	// ensure alternative fitness configuration is valid
	static {
		if (Properties.CRITERION == Criterion.DEFUSE)
			if (ENABLE_ALTERNATIVE_FITNESS_CALCULATION) {
				System.out.println("* Alternative fitness calculation mode: "
				        + Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE);
				if (!Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE.equals(AlternativeFitnessCalculationMode.SUM)) {

					PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT = false;
					PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY = false;
					SINGLE_ALTERNATIVE_FITNESS_RANGE = 1;
				} else {
					System.out.println("  - Single alternative fitness range: "
					        + SINGLE_ALTERNATIVE_FITNESS_RANGE);
					if (PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT)
						System.out.println("  - Penalizing multiple overwriting definitions: flat");
					if (PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY)
						System.out.println("  - Penalizing multiple overwriting definitions: linearly");
				}
			} else {
				System.out.println("* Alternative fitness calculation disabled!");
			}
	}

	// main Definition-Use fitness calculation methods

	/**
	 * Calculates the DefinitionUseCoverage fitness for the given DUPair on the
	 * given ExecutionResult
	 * 
	 * The fitness is calculated as follows:
	 * 
	 * 1. If the goalDefinition is not passed in the result at all: This method
	 * returns 1 + normalize(goalDefinitionFitness) where goalDefinition equals
	 * the BranchCoverageTestFitness for the Branch that the CFGVertex of this
	 * goals definition is control dependent on (goalDefinitionBranch)
	 * 
	 * 2. If the goalDefinition is passed, but the goalUse is not passed at all:
	 * This method returns the normalized BranchCoverageTestFitness for the
	 * Branch that the CFGVertex of this goals Use is control dependent on
	 * (goalUseBranch)
	 * 
	 * 3. If both the goalDefinition and the goalUse were passed at least once
	 * in the given result: 1. If and only if at any goalUsePosition the active
	 * definition was the goalDefinition the Definition-Use-Pair of this goal is
	 * covered and the method returns 0
	 * 
	 * 2. Otherwise this method returns the minimum of the following: 1. For all
	 * goalUsePositions if there was an overwriting definition, the normalized
	 * sum over all such overwriting definitions of the normalized
	 * BranchCoverageTestFitness for not taking the branch with the overwriting
	 * definition look at calculateAltenativeFitness()
	 * 
	 * 2. For all goalDefPositions the normalized BranchCoverageTestFitness for
	 * the goalUseBranch in the ExecutionTrace where every trace information is
	 * filtered out except the information traced between the occurrence of the
	 * goalDefinitionPosition and the next overwritingDefinitionPosition look at
	 * calculateUseFitnessForDefinitionPosition()
	 * 
	 * If this goals definition is not a static variable the trace information
	 * of all constructed objects of the CUT are handled separately and the
	 * minimum over all individually calculated fitness is returned
	 */
	public static double calculateDUFitness(DefUseCoverageTestFitness goal,
	        TestChromosome individual, ExecutionResult result) {

		Definition goalDefinition = goal.getGoalDefinition();
		Use goalUse = goal.getGoalUse();
		BranchCoverageTestFitness goalDefinitionBranchFitness = goal.getGoalDefinitionBranchFitness();
		BranchCoverageTestFitness goalUseBranchFitness = goal.getGoalUseBranchFitness();
		String goalVariable = goalUse.getDUVariableName();

		// at first handle special cases where definition is assumed to be covered if use is covered:
		if (isSpecialDefinition(goalDefinition)) {
			double useFitness = calculateUseFitnessForCompleteTrace(goalUse,
			                                                        goalUseBranchFitness,
			                                                        individual, result);
			if (useFitness == 0.0)
				goal.setCovered(individual, result.getTrace(), -1);

			return normalize(useFitness);
		}
		// Case 1.
		// now check if goalDefinition was passed at all before considering individual objects
		double defFitness = calculateDefFitnessForCompleteTrace(goalDefinition,
		                                                        goalDefinitionBranchFitness,
		                                                        individual, result);
		if (defFitness != 0) {
			logger.debug("Definition not covered with fitness " + defFitness);
			return 1 + normalize(defFitness);
		}
		// Case 2.
		// if the use was not passed at all just calculate the fitness 
		// over all objects without any filtering
		if (!hasEntriesForId(result.getTrace().passedUses.get(goalVariable),
		                     goalUse.getUseId())) {
			double useFitness = calculateUseFitnessForCompleteTrace(goalUse,
			                                                        goalUseBranchFitness,
			                                                        individual, result);
			if (useFitness == 0.0)
				throw new IllegalStateException(
				        "expect usefitness to be >0 if use wasn't passed");
			
			return normalize(useFitness);
		}
		// select considerable objects
		Set<Integer> objectPool = getObjectPool(goal, result.getTrace());
		// calculate minimal fitness over all objects
		double fitness = 1;
		for (Integer objectId : objectPool) {
			logger.debug("current object: " + objectId);
			if (!hasEntriesForId(result.getTrace().passedDefinitions.get(goalVariable),
			                     objectId, goalDefinition.getDefId())) {
				logger.debug("Discarded object " + objectId
				        + " - goalDefinition not passed");
				continue;
			}
			double newFitness = calculateFitnessForObject(goal, individual, result,
			                                              objectId);
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
	public static double calculateFitnessForObject(DefUseCoverageTestFitness goal,
	        TestChromosome individual, ExecutionResult result, Integer objectId) {

		Definition goalDefinition = goal.getGoalDefinition();
		Use goalUse = goal.getGoalUse();
		BranchCoverageTestFitness goalUseBranchFitness = goal.getGoalUseBranchFitness();
		String goalVariable = goalDefinition.getDUVariableName();

		// filter out trace information from other objects
		ExecutionTrace objectTrace = result.getTrace().getTraceForObject(objectId);
		double fitness = 1;
		// handle special definition case
		if (isSpecialDefinition(goalDefinition)) {
			double useFitness = executeBranchFitnessForTrace(individual, result,
			                                                 objectTrace,
			                                                 goalUseBranchFitness);
			fitness = normalize(useFitness);
			if (fitness == 0.0)
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
		for (Integer usePos : usePositions) {
			int activeDefId = DefUseExecutionTraceAnalyzer.getActiveDefinitionIdAt(goalVariable,
			                                                                       objectTrace,
			                                                                       usePos,
			                                                                       objectId);
			if (activeDefId == goalDefinition.getDefId()) {
				// Case 3.1.
				goal.setCovered(individual, objectTrace, objectId);
				return 0.0;
			} else {
				if (ENABLE_ALTERNATIVE_FITNESS_CALCULATION) {
					// goalDefinition was not active at usePos
					// if it was active before, we have a overwriting definition
					if (hasEntryLowerThan(goalDefinitionPositions, usePos)) {
						// Case 3.2.1 
						// calculate fitness for not taking branch of overwriting definition
						double alternativeFitness = calculateAlternativeFitness(goalDefinition,
						                                                        individual,
						                                                        result,
						                                                        objectTrace,
						                                                        objectId,
						                                                        usePos,
						                                                        goalDefinitionPositions);
						if (alternativeFitness <= 0.0 || alternativeFitness > 1.0)
							throw new IllegalStateException(
							        "alternative fitness expected to be in (0,1]");
						if (alternativeFitness < fitness)
							fitness = alternativeFitness;
					}
				}
			}
		}
		// Case 3.2.2
		// calculate minimal useFitness over all goalDefPositions
		// DONE: this can be optimized! for example if the goalDef is never overwritten by another 
		// 		  definition but is passed a lot this causes major overhead that is totally unnecessary
		//  idea: you only have to do this if the last definition for goalVar was not goalDefinitionId
		if (goalUse.getBranchId() != -1)
			for (Integer goalDefinitionPos : goalDefinitionPositions) {
				double useFitness = calculateUseFitnessForDefinitionPos(goalDefinition,
				                                                        goalUse,
				                                                        goalUseBranchFitness,
				                                                        individual,
				                                                        result,
				                                                        objectTrace,
				                                                        objectId,
				                                                        goalDefinitionPos);
				double newFitness = normalize(useFitness);
				if (newFitness < fitness)
					fitness = newFitness;
			}
		return fitness;
	}

	/**
	 * Computes the alternative fitness for all overwriting definitions for the
	 * given usePos
	 * 
	 * The alternative fitness is calculated as follows:
	 * 
	 * For each definition that overwrote the goalDefinition before usePos was
	 * reached this method calculates the BranchCoverageTestFitness for the
	 * alternative branch that would not have passed the overwriting definition.
	 * 
	 * Let' call each such calculated fitness an
	 * "alternative fitness for an overwriting definition" s.
	 * calculateAlternativeFitnessForOverwritingDefinition()
	 * 
	 * Now there are several possibilities how the alternative fitness for all
	 * overwriting definitions can be computed from the alternative fitness for
	 * a single overwriting definition. The mode can be configured by setting
	 * ALTERNATIVE_FITNESS_CALCULATION_MODE to the respective value noted:
	 * 
	 * - all singled fitness are summed up and normalized again - MODE: "sum" -
	 * if you wish not to normalize the sum if there was only 1 overwriting
	 * definition set DONT_NORMALIZE_SINGLE_OVERWRITING_DEFINITION_SUM - in
	 * "sum"-mode you can also stretch each individual overwriting definition
	 * fitness by using SINGLE_ALTERNATIVE_FITNESS_RANGE - the minimum/maximum
	 * over all single fitness is taken - MODE: "min"/"max" - the average over
	 * all single fitness is taken - MODE: "avg" - only consider traces where
	 * there is just one overwriting - MODE: "single" definition and return the
	 * single alternative fitness
	 * 
	 * Additionally there are two mutually not exclusive ways to penalize
	 * multiple overwriting definitions:
	 * 
	 * - PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT adds i to the i'th
	 * single fitness - PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY
	 * multiplies the i'th fitness by i
	 * 
	 * 
	 */
	public static double calculateAlternativeFitness(Definition goalDefinition,
	        TestChromosome individual, ExecutionResult result,
	        ExecutionTrace objectTrace, Integer objectId, Integer usePos,
	        List<Integer> goalDefinitionPositions) {

		int lastGoalDUPos = getMaxEntryLowerThan(goalDefinitionPositions, usePos);
		Set<Integer> overwritingDefIds = DefUseExecutionTraceAnalyzer.getOverwritingDefinitionIdsBetween(goalDefinition,
		                                                                                                 objectTrace,
		                                                                                                 lastGoalDUPos + 1,
		                                                                                                 usePos,
		                                                                                                 objectId);

		if (overwritingDefIds.isEmpty())
			throw new IllegalStateException(
			        "if goalDefinition was passed before goalUse but is no longer active there must be an overwriting definition");
		if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE.equals(AlternativeFitnessCalculationMode.SINGLE)
		        && overwritingDefIds.size() != 1)
			return 1.0;

		double alternativeFitness = 0.0;
		if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE.equals(AlternativeFitnessCalculationMode.MIN))
			alternativeFitness = 1.0;
		int overwritingDefinitionCount = 0;
		for (Integer overwritingDefId : overwritingDefIds) {
			overwritingDefinitionCount++;
			double overwritingFitness = calculateAlternatveFitnessForOverwritingDefinition(individual,
			                                                                               result,
			                                                                               objectTrace,
			                                                                               objectId,
			                                                                               overwritingDefId,
			                                                                               lastGoalDUPos,
			                                                                               usePos);
			if (overwritingFitness <= 0.0
			        || overwritingFitness > SINGLE_ALTERNATIVE_FITNESS_RANGE)
				throw new IllegalStateException(
				        "expected this definition to be between >0 and SINLE_ALTERNATIVE_FITNESS_RANGE");
			// respect penalizing configuration parameters
			if (PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT)
				overwritingFitness += overwritingDefinitionCount - 1;
			if (PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY)
				overwritingFitness *= overwritingDefinitionCount;
			// respect alternative fitness calculation mode
			if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE.equals(AlternativeFitnessCalculationMode.MIN))
				alternativeFitness = Math.min(alternativeFitness, overwritingFitness);
			else if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE.equals(AlternativeFitnessCalculationMode.MAX))
				alternativeFitness = Math.max(alternativeFitness, overwritingFitness);
			else
				alternativeFitness += overwritingFitness;
		}
		if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE.equals(AlternativeFitnessCalculationMode.SUM))
			alternativeFitness = normalize(alternativeFitness);
		else if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE.equals(AlternativeFitnessCalculationMode.AVG))
			alternativeFitness = alternativeFitness / overwritingDefIds.size();
		if (alternativeFitness <= 0 || alternativeFitness > 1)
			throw new IllegalStateException(
			        "calculated alternative fitness out of bounds");
		//		System.out.println("calculated alternative fitness: "+alternativeFitness);		
		return alternativeFitness;
	}

	/**
	 * Computes the "alternative fitness for an overwriting definition" as
	 * follows:
	 * 
	 * The definition must overwrite the goalDefinition before usePos was
	 * reached Then this method calculates the BranchCoverageTestFitness for the
	 * alternative branch that would not have passed the overwriting definition.
	 * 
	 * Such a fitness should be in the interval (0,1]: - 1 if and only if the
	 * overwriting definition was in a root-branch - otherwise the approach
	 * level should have been 0 so the resulting fitness is a normalized branch
	 * distance which is in [0,1) by construction - since every point in the
	 * ExecutionTrace where the alternative branch would have actually been hit
	 * (which can happen for example in loops) is filtered out the resulting
	 * fitness can't be 0
	 * 
	 * An alternative fitness for an overwriting definition is uses for the
	 * calculation of the alternative fitness of a given ExecutionResult s.
	 * calculateAlternativeFitness()
	 * 
	 * Since calculateAlternativeFitness() normalizes the return of this method
	 * again the above described fitness is linearly stretched to be in the
	 * interval (0,SINGLE_ALTERNATIVE_FITNESS_RANGE]
	 */
	public static double calculateAlternatveFitnessForOverwritingDefinition(
	        TestChromosome individual, ExecutionResult result,
	        ExecutionTrace objectTrace, Integer objectId, int overwritingDefId,
	        int lastGoalDuPos, Integer usePos) {

		Definition overwritingDefinition = DefUsePool.getDefinitionByDefId(overwritingDefId);
		if (overwritingDefinition == null)
			throw new IllegalStateException(
			        "expect DefUsePool to know definitions traced by instrumented code. defId: "
			                + overwritingDefId);
		// if the overwritingDefinition is in a root-branch it's not really avoidable
		if (overwritingDefinition.getBranchId() == -1)
			return SINGLE_ALTERNATIVE_FITNESS_RANGE;

		// get alternative branch
		BranchCoverageTestFitness alternativeBranchFitness = getAlternativeBranchTestFitness(overwritingDefinition.getCFGVertex());
		// set up duCounter interval to which the trace should be cut
		int duCounterStart = lastGoalDuPos;
		int duCounterEnd = usePos;
		// calculate fitness
		double newFitness = calculateFitnessForDURange(individual, result, objectTrace,
		                                               objectId,
		                                               alternativeBranchFitness,
		                                               overwritingDefinition, false,
		                                               duCounterStart, duCounterEnd);
		// debugging stuff
		if (DEBUG && newFitness == 0.0) {
			// debugging purposes
			//				preFitnessDebugInfo(result,false);
			System.out.println("object trace: ");
			DefUseExecutionTraceAnalyzer.printFinishCalls(objectTrace);
			System.out.println();
			System.out.println("cut trace:");
			ExecutionTrace cutTrace = objectTrace.getTraceInDUCounterRange(overwritingDefinition,
			                                                               false,
			                                                               duCounterStart,
			                                                               duCounterEnd);
			DefUseExecutionTraceAnalyzer.printFinishCalls(cutTrace);
			System.out.println("cut from " + duCounterStart + " to " + duCounterEnd);
			System.out.println("overwritingDef: " + overwritingDefinition.toString());
			System.out.println("on object " + objectId);
			System.out.println("alternative branch fitness: "
			        + alternativeBranchFitness.toString());
			throw new IllegalStateException(
			        "expect fitness to be >0 if trace information that passed the alternative branch should have been removed");
		}

		// TODO research: shouldn't this fitness always be >0 and <1 (because approach level is 0 and branch distance is normalized)
		// thing is i'm pretty sure it should be as described, but since our control dependencies are crap right now
		// this fitness can be >1, meaning the approach level can be >0 when the overwritingDef was control dependent from more then one Branch
		// this can for example happen when you have a branch expression with several sub-expressions separated by an ||

		// quick fix: when the fitness is >1 assume it was the flaw described above and only look at branch-distance part of fitness (decimal places)
		if (newFitness > 1) {
			int approachPart = (int) newFitness;
			newFitness -= approachPart;
		}
		if (newFitness <= 0 || newFitness > 1) {
			throw new IllegalStateException(
			        "single alternative fitness out of expected range: " + newFitness);
		}
		return SINGLE_ALTERNATIVE_FITNESS_RANGE * newFitness;
	}

	/**
	 * Determines the BranchCoverageTestFitness of goalDefinitionBranch
	 * considering the full ExecutionTrace
	 * 
	 * Is called on every call to getDistance() if the goalDefinition isn't
	 * special s. isSpecialGoalDefinition()
	 */
	public static double calculateDefFitnessForCompleteTrace(Definition targetDefinition,
	        BranchCoverageTestFitness targetFitness, TestChromosome individual,
	        ExecutionResult result) {
		if (isSpecialDefinition(targetDefinition))
			return 0.0;
		// check ExecutionTrace.passedDefinitions first, because calculating BranchTestFitness takes time
		if (hasEntriesForId(result.getTrace().passedDefinitions.get(targetDefinition.getDUVariableName()),
		                    targetDefinition.getDefId()))
			return 0.0;
		// return calculated fitness
		return targetFitness.getFitness(individual, result);
	}

	/**
	 * Determines the BranchCoverageTestFitness of goalUseBranch considering the
	 * full ExecutionTrace
	 * 
	 * Is called on every call to getDistance() if the goalDefinition isn't
	 * special s. isSpecialGoalDefinition()
	 */
	public static double calculateUseFitnessForCompleteTrace(Use targetUse,
	        BranchCoverageTestFitness targetFitness, TestChromosome individual,
	        ExecutionResult result) {

		// check ExecutionTrace.passedUses first, because calculating BranchTestFitness takes time
		if (hasEntriesForId(result.getTrace().passedUses.get(targetUse.getDUVariableName()),
		                    targetUse.getUseId()))
			return 0.0;
		// return calculated fitness
		return targetFitness.getFitness(individual, result);
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
	 */
	public static double calculateUseFitnessForDefinitionPos(Definition targetDefinition,
	        Use targetUse, BranchCoverageTestFitness targetUseBranchTestFitness,
	        TestChromosome individual, ExecutionResult result,
	        ExecutionTrace targetTrace, Integer objectId, int goalDefinitionPos) {

		int previousDefId = DefUseExecutionTraceAnalyzer.getPreviousDefinitionId(targetDefinition.getDUVariableName(),
		                                                                         targetTrace,
		                                                                         goalDefinitionPos,
		                                                                         objectId);
		if (previousDefId == targetDefinition.getDefId())
			return 1;

		int overwritingDefPos = DefUseExecutionTraceAnalyzer.getNextOverwritingDefinitionPosition(targetDefinition,
		                                                                                          targetTrace,
		                                                                                          goalDefinitionPos,
		                                                                                          objectId);
		double fitness = calculateFitnessForDURange(individual, result, targetTrace,
		                                            objectId, targetUseBranchTestFitness,
		                                            targetUse, true, goalDefinitionPos,
		                                            overwritingDefPos);

		return fitness;
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
	 * This method gets called for alternative fitness calculation -
	 * calculateAlternativeFitness() - and in order to determine the
	 * goalUseBranch fitness between a goalUsePos and its next overwriting
	 * definition - calculateUseFitnessForDefinitionPos()
	 * 
	 * 
	 */
	public static double calculateFitnessForDURange(TestChromosome individual,
	        ExecutionResult result, ExecutionTrace targetTrace, Integer objectId,
	        BranchCoverageTestFitness targetFitness, DefUse targetDU,
	        boolean wantToCoverTargetDU, int duCounterStart, int duCounterEnd) {

		// filter trace
		ExecutionTrace cutTrace = targetTrace.getTraceInDUCounterRange(targetDU,
		                                                               wantToCoverTargetDU,
		                                                               duCounterStart,
		                                                               duCounterEnd);
		// calculate fitness
		double fitness = executeBranchFitnessForTrace(individual, result, cutTrace,
		                                              targetFitness);
		// sanity check
		if (fitness == 0.0 && wantToCoverTargetDU) {
			System.out.println(cutTrace.toDefUseTraceInformation());
			System.out.println("duPosStart: " + duCounterStart);
			System.out.println("duPosEnd: " + duCounterEnd);
			int targetUseBranchBytecode = BranchPool.getBytecodeIdFor(targetDU.getBranchId());
			System.out.println("targetDU-branch-bytecode: " + targetUseBranchBytecode);
			DefUseExecutionTraceAnalyzer.printFinishCalls(cutTrace);
			throw new IllegalStateException("use cant have fitness 0 in this cut trace: "
			        + cutTrace.toDefUseTraceInformation(targetDU.getDUVariableName(),
			                                            objectId));
		}
		return fitness;
	}

	// other core methods

	/**
	 * Executes the BranchCoverageTest.getFitness() function on the given
	 * ExecutionResult but using the given targetTrace
	 * 
	 * The ExecutionResult is left in it's original state after execution
	 */
	public static double executeBranchFitnessForTrace(TestChromosome individual,
	        ExecutionResult result, ExecutionTrace targetTrace,
	        BranchCoverageTestFitness targetFitness) {

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
	public static boolean isSpecialDefinition(Definition definition) {
		if (definition == null
		        || (definition.isStaticDU() && definition.getCFGVertex().methodName.startsWith("<clinit>"))) {

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
	private static Set<Integer> getObjectPool(DefUseCoverageTestFitness goal,
	        ExecutionTrace trace) {
		String goalVariable = goal.getGoalVariable();
		Definition goalDefinition = goal.getGoalDefinition();

		Set<Integer> objectPool = new HashSet<Integer>();
		if (trace.passedUses.get(goalVariable) == null)
			return objectPool;
		if (trace.passedDefinitions.get(goalVariable) != null)
			objectPool.addAll(trace.passedDefinitions.get(goalVariable).keySet());
		if (goalDefinition == null || goalDefinition.isStaticDU()) {
			// in the static case all objects have to be considered
			objectPool.addAll(trace.passedUses.get(goalVariable).keySet());
			if (DEBUG)
				logger.debug("Static-goalVariable! Using all known Objects");
		} else {
			// on non-static goalVariables only look at objects that have traces of defs and uses for the goalVariable
			int oldSize = objectPool.size();
			objectPool.retainAll(trace.passedUses.get(goalVariable).keySet());
			if (DEBUG) {
				logger.debug("NON-Static-goalVariable " + goalVariable);
				logger.debug("#unused objects: " + (oldSize - objectPool.size()));
				Set<Integer> discardedObjects = new HashSet<Integer>();
				discardedObjects.addAll(trace.passedDefinitions.get(goalVariable).keySet());
				discardedObjects.removeAll(trace.passedUses.get(goalVariable).keySet());
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

	public static double normalize(double value) {
		// TODO just copied this from FitnessFunction because it was not visible from here
		return value / (1.0 + value);
	}

	public static boolean hasEntryLowerThan(List<Integer> list, Integer border) {
		for (Integer defPos : list)
			if (defPos < border)
				return true;
		return false;
	}

	public static Integer getMaxEntryLowerThan(List<Integer> list, Integer border) {
		int lastPos = -1;
		for (Integer defPos : list)
			if (defPos < border && defPos > lastPos)
				lastPos = defPos;
		return lastPos;
	}

	public static boolean hasEntriesForId(
	        Map<Integer, HashMap<Integer, Integer>> objectDUMap, int targetId) {
		if (objectDUMap == null)
			return false;
		for (Integer objectId : objectDUMap.keySet())
			if (hasEntriesForId(objectDUMap, objectId, targetId))
				return true;

		return false;
	}

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
	 */
	public static boolean traceCoversGoal(DefUseCoverageTestFitness goal,
	        Chromosome individual, ExecutionTrace trace) {
		String goalVariable = goal.getGoalVariable();
		Use goalUse = goal.getGoalUse();
		Definition goalDefinition = goal.getGoalDefinition();

		if (trace.passedUses.get(goalVariable) == null)
			return false;
		Set<Integer> objectPool = getObjectPool(goal, trace);

		for (Integer objectID : objectPool) {
			List<Integer> usePositions = DefUseExecutionTraceAnalyzer.getUsePositions(goalUse,
			                                                                          trace,
			                                                                          objectID);
			// use not reached
			if (usePositions.size() == 0)
				continue;
			if (goalUse.isParameterUse())
				return true;
			if (goalDefinition.isStaticDU()
			        && goalDefinition.getMethodName().startsWith("<clinit>"))
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

	// BranchCoverageTestFitness factory methods
	// TODO ... might want to encapsulate these somehow different
	// maybe even make them new constructors for BranchCoverageTestFitness, that seems reasonable

	/**
	 * Creates a BranchCoverageTestFitness for the branch that the given
	 * CFGVertex is control dependent on
	 */
	public static BranchCoverageTestFitness getBranchTestFitness(CFGVertex v) {
		return getBranchTestFitness(v, !v.branchExpressionValue);
	}

	/**
	 * Creates a BranchCoverageTestFitness for the alternative branch of the
	 * branch that the given CFGVertex is control dependent on
	 */
	public static BranchCoverageTestFitness getAlternativeBranchTestFitness(CFGVertex v) {
		return getBranchTestFitness(v, v.branchExpressionValue);
	}

	/**
	 * Creates a BranchCoverageTestFitness for the branch the given CFGVertex is
	 * control dependent on but considering the given targetExpressionValue as
	 * the branchExpressionValue
	 */
	public static BranchCoverageTestFitness getBranchTestFitness(CFGVertex v,
	        boolean targetExpressionValue) {
		BranchCoverageTestFitness r;
		if (v.branchId == -1) {
			r = getRootBranchTestFitness(v);
		} else {
			ControlFlowGraph cfg = CFGMethodAdapter.getMinimizedCFG(v.className,
			                                                        v.methodName);
			Branch b = BranchPool.getBranch(v.branchId);
			r = new BranchCoverageTestFitness(new BranchCoverageGoal(b,
			        targetExpressionValue, cfg, v.className, v.methodName));
		}
		return r;
	}

	/**
	 * Creates a BranchCoverageTestFitness for the root-branch of the method of
	 * the given DefUse
	 */
	public static BranchCoverageTestFitness getRootBranchTestFitness(CFGVertex v) {
		return new BranchCoverageTestFitness(new BranchCoverageGoal(v.className,
		        v.className + "." + v.methodName));
	}
}
