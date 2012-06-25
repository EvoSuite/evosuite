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
package org.evosuite.coverage.dataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.statement.StatementCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
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

	public static long alternativeTime = 0l;

	private final static boolean DEBUG = Properties.DEFUSE_DEBUG_MODE;

	private static Logger logger = LoggerFactory.getLogger(DefUseFitnessCalculator.class);

	// if alternative fitness calculation is disabled ignore the following
	// valid modes: "sum", "min", "max", "avg", "single"
	// private static final String ALTERNATIVE_FITNESS_CALCULATION_MODE =
	// Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE;
	// if the mode isn't "sum" the following are ignored
	//	private static boolean PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT = Properties.PENALIZE_OVERWRITING_DEFINITIONS_FLAT;
	//	private static boolean PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY = Properties.PENALIZE_OVERWRITING_DEFINITIONS_LINEARLY;
	private static double SINGLE_ALTERNATIVE_FITNESS_RANGE = 1.0; // Properties.ALTERNATIVE_FITNESS_RANGE;
	// ensure alternative fitness configuration is valid
	static {
		if (Properties.CRITERION == Criterion.DEFUSE)
			if (Properties.ENABLE_ALTERNATIVE_FITNESS_CALCULATION) {
				System.out.println("* Alternative fitness calculation enabled");
				// + Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE);
				//				if (!Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE
				//						.equals(AlternativeFitnessCalculationMode.SUM)) {
				//
				//					PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT = false;
				//					PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY = false;
				//					SINGLE_ALTERNATIVE_FITNESS_RANGE = 1;
				//				}
				// else {
				// System.out.println("  - Single alternative fitness range: "
				// + SINGLE_ALTERNATIVE_FITNESS_RANGE);
				// if (PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT)
				// System.out
				// .println("  - Penalizing multiple overwriting definitions: flat");
				// if (PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY)
				// System.out
				// .println("  - Penalizing multiple overwriting definitions: linearly");
				// }
			} else {
				System.out.println("* Alternative fitness calculation disabled!");
			}
	}

	private final DefUseCoverageTestFitness goal;
	private final TestChromosome individual;
	private final ExecutionResult result;

	private final Definition goalDefinition;
	private final Use goalUse;
	private final TestFitnessFunction goalDefinitionFitness;
	private final TestFitnessFunction goalUseFitness;
	private final String goalVariable;

	public DefUseFitnessCalculator(DefUseCoverageTestFitness goal,
	        TestChromosome individual, ExecutionResult result) {
		this.goal = goal;
		this.individual = individual;
		this.result = result;

		this.goalDefinition = goal.getGoalDefinition();
		this.goalUse = goal.getGoalUse();
		this.goalDefinitionFitness = goal.getGoalDefinitionFitness();
		this.goalUseFitness = goal.getGoalUseFitness();
		this.goalVariable = goalUse.getDUVariableName();
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

		if (!hasEntriesForId(result.getTrace().getPassedUses(goalVariable),
		                     goalUse.getUseId()))
			return calculateUseFitnessForCompleteTrace();

		// Case 3.
		// filter the trace for each considerable object that passed both the
		// goalDefinition and the goalUse, cut the traces between goalDef
		// occurrences and overwritingDef occurrences and calculate useFitness
		// and possibly overwriting/alternativeFitness
		return calculateFitnessForObjects();
	}

	private double calculateFitnessForObjects() {

		// select considerable objects
		Set<Integer> objects = determineConsiderableObjects(goal, result.getTrace());

		// calculate minimal fitness over all objects
		double fitness = 1;
		for (Integer object : objects) {
			logger.debug("current object: " + object);
			if (!hasEntriesForId(result.getTrace().getPassedDefinitions(goalVariable),
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
			if (Properties.CRITERION == Criterion.DEFUSE && fitness == 0.0)
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
				if (Properties.CRITERION == Criterion.DEFUSE)
					goal.setCovered(individual, objectTrace, objectId);
				return 0.0;
			} else {
				if (Properties.ENABLE_ALTERNATIVE_FITNESS_CALCULATION) {

					long start = System.currentTimeMillis();

					// goalDefinition was not active at usePos
					// if it was active before, we might have a overwriting
					// definition
					if (hasEntryLowerThan(goalDefinitionPositions, usePos)) {

						int goalDefPos = getMaxEntryLowerThan(goalDefinitionPositions,
						                                      usePos);

						// first check if there was yet another occurrence of
						// goalUse between usePos and goalDefPos. if so, we
						// discard that as an overwriting definition
						if (!hasEntryInBetween(usePositions, goalDefPos, usePos)) {

							// Case 3.2.1
							// calculate fitness for not taking branch of
							// overwriting definition
							double alternativeFitness = calculateAlternativeFitness(objectTrace,
							                                                        objectId,
							                                                        usePos,
							                                                        goalDefPos);
							if (alternativeFitness <= 0.0 || alternativeFitness > 1.0)
								throw new IllegalStateException(
								        "alternative fitness expected to be in (0,1] "
								                + alternativeFitness);
							if (alternativeFitness < fitness) {
								//								System.out
								//										.println("alternative boost: "
								//												+ fitness + " -> "
								//												+ alternativeFitness);
								fitness = alternativeFitness;
							}
						}
					}
					alternativeTime += System.currentTimeMillis() - start;
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
	public double calculateAlternativeFitness(ExecutionTrace objectTrace,
	        Integer objectId, Integer usePos, int lastGoalDefPos) {

		Map<Integer, Integer> overwritingDefs = DefUseExecutionTraceAnalyzer.getOverwritingDefinitionsBetween(goalDefinition,
		                                                                                                      objectTrace,
		                                                                                                      lastGoalDefPos + 1,
		                                                                                                      usePos,
		                                                                                                      objectId);

		if (overwritingDefs.isEmpty())
			throw new IllegalStateException(
			        "if goalDefinition was passed before goalUse but is no longer active there must be an overwriting definition");

		// if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE
		// .equals(AlternativeFitnessCalculationMode.SINGLE)
		// &&
		if (overwritingDefs.keySet().size() != 1)
			return 1.0;

		double alternativeFitness = 0.0;
		// if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE
		// .equals(AlternativeFitnessCalculationMode.MIN))
		// alternativeFitness = 1.0;

		// int overwritingDefinitionCount = 0;
		for (Integer overwritingDefId : overwritingDefs.keySet()) {
			// overwritingDefinitionCount++;
			double overwritingFitness = calculateAlternatveFitnessForOverwritingDefinition(objectTrace,
			                                                                               objectId,
			                                                                               overwritingDefId,
			                                                                               lastGoalDefPos,
			                                                                               overwritingDefs.get(overwritingDefId));

			if (overwritingFitness <= 0.0
			        || overwritingFitness > SINGLE_ALTERNATIVE_FITNESS_RANGE)
				throw new IllegalStateException(
				        "expected this definition to be >0 and <=SINGLEALTERNATIVE_FITNESS");

			// respect penalizing configuration parameters
			// if (PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_FLAT)
			// overwritingFitness += overwritingDefinitionCount - 1;
			// if (PENALIZE_MULTIPLE_OVERWRITING_DEFINITIONS_LINEARLY)
			// overwritingFitness *= overwritingDefinitionCount;

			// // respect alternative fitness calculation mode
			// if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE
			// .equals(AlternativeFitnessCalculationMode.MIN))
			// alternativeFitness = Math.min(alternativeFitness,
			// overwritingFitness);
			// else if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE
			// .equals(AlternativeFitnessCalculationMode.MAX))
			// alternativeFitness = Math.max(alternativeFitness,
			// overwritingFitness);
			// else
			alternativeFitness += overwritingFitness;
		}
		// if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE
		// .equals(AlternativeFitnessCalculationMode.SUM))
		// alternativeFitness = normalize(alternativeFitness);
		// else if (Properties.ALTERNATIVE_FITNESS_CALCULATION_MODE
		// .equals(AlternativeFitnessCalculationMode.AVG))
		// alternativeFitness = alternativeFitness / overwritingDefIds.size();
		// if (alternativeFitness <= 0 || alternativeFitness > 1)
		// throw new IllegalStateException(
		// "calculated alternative fitness out of bounds");
		// //
		// System.out.println("calculated alternative fitness: "
		// + alternativeFitness);

		return alternativeFitness;
		//		return normalize(alternativeFitness);
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
	public double calculateAlternatveFitnessForOverwritingDefinition(
	        ExecutionTrace objectTrace, Integer objectId, int overwritingDefId,
	        int traceStart, int traceEnd) {

		Definition overwritingDefinition = DefUsePool.getDefinitionByDefId(overwritingDefId);
		if (overwritingDefinition == null)
			throw new IllegalStateException(
			        "expect DefUsePool to know definitions traced by instrumented code. defId: "
			                + overwritingDefId);

		// if the overwritingDefinition is in a root-branch it's not really
		// avoidable
		if (overwritingDefinition.isRootBranchDependent())
			return SINGLE_ALTERNATIVE_FITNESS_RANGE;

		// get alternative branch
		StatementCoverageTestFitness overwritingFunction = new StatementCoverageTestFitness(
		        overwritingDefinition);

		double overwritingFitness;
		try {
			overwritingFitness = calculateFitnessForDURange(objectTrace, objectId,
			                                                overwritingFunction,
			                                                overwritingDefinition, true,
			                                                traceStart, traceEnd, false);
			if (overwritingFitness != 0.0)
				return SINGLE_ALTERNATIVE_FITNESS_RANGE;

		} catch (UnexpectedFitnessException e) {
			throw new IllegalStateException("should be impossible");
		}
		// throw new IllegalStateException(
		// "expect fitness of overwritingDefinition in cut trace to be 0.0 since def was passed");

		BranchCoverageTestFitness coveringOverwritingFitness = overwritingFunction.getLastCoveringFitness();

		if (coveringOverwritingFitness.getBranch() == null)
			throw new IllegalStateException(
			        "impossible if overwritingDef not rootBranchDependent()");
		// return SINGLE_ALTERNATIVE_FITNESS_RANGE;

		TestFitnessFunction alternativeFitness = BranchCoverageFactory.createBranchCoverageTestFitness(coveringOverwritingFitness.getBranch(),
		                                                                                               !coveringOverwritingFitness.getBranchExpressionValue());

		// calculate alternative fitness
		try {
			double newFitness = calculateFitnessForDURange(objectTrace, objectId,
			                                               alternativeFitness,
			                                               overwritingDefinition, false,
			                                               traceStart, traceEnd, true);
			if (newFitness > 1) {
				int approachPart = (int) newFitness;
				newFitness -= approachPart;
			}
			if (newFitness <= 0 || newFitness > 1) {
				throw new IllegalStateException(
				        "single alternative fitness out of expected range: " + newFitness);
			}

			//			if (newFitness != 0.5) {
			//				System.out.println("HAH: " + newFitness);
			//				System.out.println(alternativeFitness.toString());
			//				System.out.println(overwritingDefinition.toString());
			//				System.out.println(result.test.toCode());
			//				System.out.println(objectTrace.toDefUseTraceInformation(
			//						goalVariable, objectId));
			//			}

			return SINGLE_ALTERNATIVE_FITNESS_RANGE * newFitness;
		} catch (UnexpectedFitnessException e) {
			return SINGLE_ALTERNATIVE_FITNESS_RANGE;
		}

		// // debugging stuff
		// if (DEBUG && newFitness == 0.0) {
		// // debugging purposes
		// // preFitnessDebugInfo(result,false);
		// System.out.println("object trace: ");
		// DefUseExecutionTraceAnalyzer.printFinishCalls(objectTrace);
		// System.out.println();
		// System.out.println("cut trace:");
		// ExecutionTrace cutTrace = objectTrace.getTraceInDUCounterRange(
		// overwritingDefinition, false, duCounterStart, duCounterEnd);
		// DefUseExecutionTraceAnalyzer.printFinishCalls(cutTrace);
		// System.out.println("cut from " + duCounterStart + " to "
		// + duCounterEnd);
		// System.out.println("overwritingDef: "
		// + overwritingDefinition.toString());
		// System.out.println("on object " + objectId);
		// System.out.println("alternative branch fitness: "
		// + alternativeBranchFitness.toString());
		// throw new IllegalStateException(
		// "expect fitness to be >0 if trace information that passed the alternative branch should have been removed");
		// }

		// TODO research: shouldn't this fitness always be >0 and <1 (because
		// approach level is 0 and branch distance is normalized)
		// thing is i'm pretty sure it should be as described, but since our
		// control dependencies are crap right now
		// this fitness can be >1, meaning the approach level can be >0 when the
		// overwritingDef was control dependent from more then one Branch
		// this can for example happen when you have a branch expression with
		// several sub-expressions separated by an ||

		// quick fix: when the fitness is >1 assume it was the flaw described
		// above and only look at branch-distance part of fitness (decimal
		// places)
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
		if (hasEntriesForId(result.getTrace().getPassedDefinitions(goalDefinition.getDUVariableName()),
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
		if (hasEntriesForId(result.getTrace().getPassedUses(goalUse.getDUVariableName()),
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

		int previousDefId = DefUseExecutionTraceAnalyzer.getPreviousDefinitionId(goalDefinition.getDUVariableName(),
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

	public static double normalize(double value) {
		// TODO just copied this from FitnessFunction because it was not visible
		// from here
		return value / (1.0 + value);
	}

	public static boolean hasEntryLowerThan(List<Integer> list, Integer border) {
		for (Integer pos : list)
			if (pos < border)
				return true;
		return false;
	}

	public static boolean hasEntryInBetween(List<Integer> list, Integer start, Integer end) {
		for (Integer pos : list)
			if (start < pos && pos < end)
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
