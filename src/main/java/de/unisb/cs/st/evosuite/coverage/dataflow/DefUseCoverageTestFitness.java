/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;

/*
// (0) TODO IDEA FOR AN EVO-SUITE-FEATURE: 
//		 given a test(suite) for a class, check how many goals of each coverage criterion it covers
// (1) DONE detect which LV in which methodCall
//		i don't think this needs to be done anymore. its not possible to cover a use in a method call for a LV without covering a definition first
// (2) DONE cut ExecutionTrace for branch-fitness-evaluation to Trace of current object only
// 	    subTODO: first mark MethodCalls in ExecutionTracer.finished_calls with a methodID and the current objectID
// 			 then kick out all non-static MethodCalls for objects other then the one with current objectID
//			 if goalDef is for a LV, also consider MethodCalls individually (OUTDATED! s. above)		
// (3) DONE cut ExecutionTrace for branch-fitness-evaluation to Trace of a certain path (between goalDef and overwritingDef)
//  subTODO: in a MethodCall from the ExecutionTracer add another list holding information about the current duConter
//			 then allow for cutting the ExecutionTrace in a way that it only holds data associated for duCounters in a given range
// (4) DONE idea: 	once goalDef is covered, look at all duCounterPositions of goalDef (goalDefPositions):
//				if goalUse is covered any of these goalDefPositions, return 0
//				if not return the minimum over all branchUseFitness in the trace
//				where the trace is cut between the goalDefPosition and the next overwritingDefinition
//		turns out you don't need to cut between goalDefs and overwritingDefs
//		all u need to do is filter out all traces where the goalDef is not active for use-fitness-calculation
// (5) DONE:	well that didn't turn out too well. you have to consider all passed definitions
//				separately as described in (3) and (4) after all - see for example MeanTestClass.mean()

// other things one could/should do:
//	- display local variable names as in source code
//	- take different methodIds into account! 
//	- right now there seems to be a bug when definitions at the end of a method 
//		are paired with a use at the beginning of it
//	- inter.method and inter.class data flow analysis
//	- implement DefUseCoverageSuiteFitness
//	- various optimizations
// 	- for example one should reuse tests that reach a certain definition 
//		when looking for another goal with that definition
//	- fix control dependencies analysis
//	- implement real ReachingDefinitions algorithm
//	- even more information in resulting tests?
 * 		- cool would be to mark the statements in the test that caused the covering hits to goalUse and goalDef!
 *  - handle exceptions
 *  - worry about rounding errors: all that normalizing is insane
 *  	- should stretch the range for possible CoverageFitness to something like 0-100 at least
 *  - care for integer overflows (espacially in alternative fitness calculation)
 *  - 
 */

/**
 * Evaluate fitness of a single test case with respect to one Definition-Use pair
 * 
 * For more information look at the comment from method getDistance()
 * 
 * @author Andre Mis
 */
public class DefUseCoverageTestFitness extends TestFitnessFunction {

	// debugging flags
	private final static boolean DEBUG = true;
	private final static boolean PRINT_DEBUG = DEBUG && false;
	
	// configuration parameters
	private final double ALTERNATIVE_FITNESS_RANGE = 1;
	
	// the Definition-Use pair
	private final String goalVariable;
	private final Use goalUse;	
	private final Definition goalDefinition;
	private final BranchCoverageTestFitness goalDefinitionBranchTestFitness;
	private final BranchCoverageTestFitness goalUseBranchTestFitness;
	
	// coverage information
	private Integer coveringObjectId = -1;
	private ExecutionTrace coveringTrace;
	
	
	// constructors
	
	/**
	 * Creates a Definition-Use-Coverage goal for the given
	 * Definition and Use 
	 */
	public DefUseCoverageTestFitness(Definition def, Use use) {
		if (!def.getDUVariableName().equals(use.getDUVariableName()))
			throw new IllegalArgumentException(
			        "expect def and use to be for the same variable");

		this.goalDefinition = def;
		this.goalUse = use;
		this.goalVariable = def.getDUVariableName();
		this.goalDefinitionBranchTestFitness = getBranchTestFitness(def.getCFGVertex());
		this.goalUseBranchTestFitness = getBranchTestFitness(use.getCFGVertex());
	}
	
	/**
	 * Used for Parameter-Uses
	 * 
	 * Creates a goal that tries to cover the given Use
	 */
	public DefUseCoverageTestFitness(Use use) {
		if(!use.getCFGVertex().isParameterUse)
			throw new IllegalArgumentException("this constructor is only for Parameter-Uses");

		goalVariable = use.getDUVariableName();
		goalDefinition = null;
		goalDefinitionBranchTestFitness = null;
		goalUse = use;
		goalUseBranchTestFitness = getBranchTestFitness(use.getCFGVertex());
	}

	// BranchCoverageTestFitness factory methods - ... might want to encapsulate these in an actual factory
	
	/**
	 * Creates a BranchCoverageTestFitness for the branch
	 * that the given CFGVertex is control dependent on 
	 */
	private BranchCoverageTestFitness getBranchTestFitness(CFGVertex v) {
		return getBranchTestFitness(v,!v.branchExpressionValue);
	}
	
	/**
	 * Creates a BranchCoverageTestFitness for the alternative branch
	 * of the branch that the given CFGVertex is control dependent on 
	 */
	private BranchCoverageTestFitness getAlternativeBranchTestFitness(CFGVertex v) {
		return getBranchTestFitness(v,v.branchExpressionValue);
	}
	
	private BranchCoverageTestFitness getBranchTestFitness(CFGVertex v, boolean targetExpressionValue) {
		BranchCoverageTestFitness r;
		if (v.branchId == -1) {
			r = getRootBranchTestFitness(v);
		} else {
			ControlFlowGraph cfg = CFGMethodAdapter.getCFG(v.className, v.methodName);
			Branch b = BranchPool.getBranch(v.branchId);
			r = new BranchCoverageTestFitness(new BranchCoverageGoal(b,
			        targetExpressionValue, cfg, v.className, v.methodName));
		}
		return r;
	}

	/**
	 * Creates a BranchCoverageTestFitness for the root-branch of the method of the given DefUse 
	 */
	private BranchCoverageTestFitness getRootBranchTestFitness(CFGVertex v) {
		return new BranchCoverageTestFitness(new BranchCoverageGoal(v.className,
		        v.className + "." + v.methodName));
	}

	// fitness calculation methods
	
	/**
	 * Calculates the Definition-Use-Coverage fitness for this Definition-Use-Pair on the given ExecutionResult
	 * 
	 * The fitness is calculated as follows:
	 * 
	 * 1.  If the goalDefinition is not passed in the result at all:
	 * 	 	This method returns 1 + normalize(goalDefinitionFitness) where goalDefinition equals
	 * 		the BranchCoverageTestFitness for the Branch that the CFGVertex of
	 * 		this goals definition is control dependent on (goalDefinitionBranch)
	 * 
	 * 2.  If the goalDefinition is passed, but the goalUse is not passed at all:
	 * 		This method returns the normalized BranchCoverageTestFitness for the Branch that
	 * 		the CFGVertex of this goals Use is control dependent on (goalUseBranch)
	 * 
	 * 3.  If both the goalDefinition and the goalUse were passed at least once in the given result:
	 * 		1. If and only if at any goalUsePosition the active definition was the goalDefinition the
	 * 		   Definition-Use-Pair of this goal is covered and the method returns 0
	 * 
	 * 		2. Otherwise this method returns the minimum of the following:
	 * 			1. For all goalUsePositions if there was an overwriting definition, 
	 * 				the normalized sum over all such overwriting definitions of the normalized
	 * 				BranchCoverageTestFitness for not taking the branch with the overwriting definition
	 * 				 look at calculateAltenativeFitness()
	 *  
	 * 			2. For all goalDefPositions the normalized BranchCoverageTestFitness for the goalUseBranch
	 * 				in the ExecutionTrace where every trace information is filtered out except
	 * 				the information traced between the occurrence of the goalDefinitionPosition
	 * 				and the next overwritingDefinitionPosition
	 * 				 look at calculateUseFitnessForDefinitionPosition()
	 * 
	 * If this goals definition is not a static variable the trace information of 
	 * all constructed objects of the CUT are handled separately and the minimum over all
	 * individually calculated fitness is returned   
	 * 		
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		preFitnessDebugInfo(result,true);
		// at first handle special cases where definition is assumed to be covered if use is covered:
		if(isSpecialGoalDefinition()) {
			double useFitness = calculateUseFitnessForCompleteTrace(individual,result);
			if(useFitness == 0.0)
				setCovered(individual,result.trace,-1);
			return normalize(useFitness);
		}
		// Case 1.
		// now check if goalDefinition was passed at all before considering individual objects
		double defFitness = calculateDefFitnessForCompleteTrace(individual, result);
		if (defFitness != 0) {
			logger.debug("Definition not covered with fitness "+defFitness);
			return 1 + normalize(defFitness);
		}
		// Case 2.
		// if the use was not passed at all just calculate the fitness 
		// over all objects without any filtering
		if(!hasEntriesForId(result.trace.passedUses.get(goalVariable), goalUse.getUseId())) {
			double useFitness = calculateUseFitnessForCompleteTrace(individual, result);
			if(useFitness==0.0)
				throw new IllegalStateException("expect usefitness to be >0 if use wasn't passed");
			return normalize(useFitness);
		}
		// select considerable objects
		Set<Integer> objectPool = getObjectPool(result.trace);
		// calculate minimal fitness over all objects
		double fitness = 1;
		for(Integer objectId : objectPool) {
			logger.debug("current object: "+objectId);
			if(!hasEntriesForId(result.trace.passedDefinitions.get(goalVariable), 
							objectId, goalDefinition.getDefId())) {
				logger.debug("Discarded object "+objectId+" - goalDefinition not passed");
				continue;
			}
			double newFitness = calculateFitnessForObject(individual,result,objectId);
			if(newFitness<fitness) {
				fitness=newFitness;
//				System.out.println("calculated fitness: "+fitness);
			}
			if(fitness==0.0)
				return 0.0;
		}
		postFitnessDebugInfo(individual, result, fitness);
//		System.out.println("calculated fitness: "+fitness);
		return fitness;
	}
		
	/**
	 * Calculates the DefUseCoverage fitness for this DefUsePair considering 
	 *  only the objectId'th CUT-Object in the ExecutionResult 
	 * 
	 * Gets called for all CUT-objects in the ExecutionResult whenever 
	 * the goalDefinition and the goalUse were passed at least once 
	 * on that object.
	 */
	private double calculateFitnessForObject(TestChromosome individual,
			ExecutionResult result, Integer objectId) {
		
		// filter out trace information from other objects
		ExecutionTrace objectTrace = result.trace.getTraceForObject(objectId);
		double fitness = 1;
		// handle special definition case
		if(isSpecialGoalDefinition()) {
			double useFitness = executeBranchFitnessForTrace(individual,result,objectTrace,goalUseBranchTestFitness);
			fitness = normalize(useFitness);
			if(fitness==0.0)
				setCovered(individual, objectTrace, objectId);
			return fitness; 
		}
		// check if goalDefinition is active at any goalUsePosition
		List<Integer> usePositions = getGoalUsePositions(objectTrace,objectId);
		List<Integer> goalDefinitionPositions = getGoalDefinitionPositions(objectTrace,objectId);
		for(Integer usePos : usePositions) {
			int activeDefId = getActiveDefIdAt(objectTrace, usePos, objectId);
			if (activeDefId == goalDefinition.getDefId()) {
				// Case 3.1.
				setCovered(individual, objectTrace, objectId);
				return 0.0;
			} else {
				// goalDefinition was not active at usePos
				// if it was active before, we have a overwriting definition
				if(hasEntryLowerThan(goalDefinitionPositions, usePos)) {
					// Case 3.2.1 
					// calculate fitness for not taking branch of overwriting definition
					double alternativeFitness = calculateAlternativeFitness(individual, result, 
							objectTrace, objectId, usePos, goalDefinitionPositions);
					if(alternativeFitness == 0.0)
						throw new IllegalStateException("an alternative fitness must never be 0");
					if(alternativeFitness<fitness)
						fitness = alternativeFitness;
				}
			}
		}
		// Case 3.2.2
		// calculate minimal useFitness over all goalDefPositions
		// TODO: this can be optimized! for example if the goalDef is never overwritten by another 
		// definition but is passed a lot this causes major overhead that is totally unnecessary
		if(goalUse.getBranchId() != -1)
			for(Integer goalDefinitionPos : goalDefinitionPositions) {
				double useFitness = calculateUseFitnessForDefinitionPos(individual, result, objectTrace, objectId, goalDefinitionPos);
				double newFitness = normalize(useFitness);
				if(newFitness<fitness)
					fitness=newFitness;
			}
		return fitness;
	}
	
	/**
	 * Computes the alternative fitness for all overwriting definitions for the given usePos
	 * 
	 * The alternative fitness is calculated as follows:
	 * 
	 * For each definition that overwrote the goalDefinition before usePos was reached
	 * this method calculates the BranchCoverageTestFitness for the alternative branch
	 * that would not have passed the overwriting definition.
	 * 
	 * Let' call each such calculated fitness an "alternative fitness for an overwriting definition"
	 *  s. calculateAlternativeFitnessForOverwritingDefinition()
	 * 
	 * All of these are summed up and normalized again, which yields the alternative fitness
	 */
	private double calculateAlternativeFitness(TestChromosome individual, ExecutionResult result,
			ExecutionTrace objectTrace, Integer objectId, Integer usePos, 
			List<Integer> goalDefinitionPositions) {
		
		int lastGoalDUPos = getMaxEntryLowerThan(goalDefinitionPositions,usePos);
		Set<Integer> overwritingDefIds = getOverwritingDefinitionIdsBetween(objectTrace, lastGoalDUPos+1, usePos, objectId);
		if(overwritingDefIds.isEmpty())
			throw new IllegalStateException("if goalDefinition was passed before goalUse but is no longer active there must be an overwriting definition");
		double alternativeFitness = 0.0; 
		for(Integer overwritingDefId : overwritingDefIds) {	
			double overwritingFitness = 
				calculateAlternatveFitnessForOverwritingDefinition(individual, result,
						objectTrace, objectId, overwritingDefId, lastGoalDUPos, usePos);
			if(overwritingFitness <= 0.0 || overwritingFitness > ALTERNATIVE_FITNESS_RANGE)
				throw new IllegalStateException("expected this definition to be normalized between >0 and ALTERNATIVE_FITNESS_RANGE");
			alternativeFitness += overwritingFitness;
		}
		alternativeFitness = normalize(alternativeFitness);
//		System.out.println("calculated alternative fitness: "+alternativeFitness);
		return alternativeFitness;
	}

	/**
	 * Computes the "alternative fitness for an overwriting definition" as follows:
	 * 
	 * The definition must overwrite the goalDefinition before usePos was reached
	 * Then this method calculates the BranchCoverageTestFitness for the alternative branch
	 * that would not have passed the overwriting definition.
	 * 
	 * Such a fitness should be in the interval (0,1]:
	 * 	- 1 if and only if the overwriting definition was in a root-branch
	 *  - otherwise the approach level should have been 0 so the resulting fitness 
	 *  	is a normalized branch distance which is in [0,1) by construction
	 *  - since every point in the ExecutionTrace where the alternative branch would
	 *  	have actually been hit (which can happen for example in loops) is filtered out 
	 *  	the resulting fitness can't be 0
	 *  
	 *  An alternative fitness for an overwriting definition is uses for the calculation
	 *  of the alternative fitness of a given ExecutionResult
	 *   s. calculateAlternativeFitness()
	 *   
	 *  Since calculateAlternativeFitness() normalizes the return of this method again
	 *  the above described fitness is linearly stretched to be in the interval (0,ALTERNATIVE_FITNESS_RANGE]
	 */
	private double calculateAlternatveFitnessForOverwritingDefinition(
			TestChromosome individual, ExecutionResult result,
			ExecutionTrace objectTrace, Integer objectId, int overwritingDefId,
			int lastGoalDuPos, Integer usePos) {
		
		Definition overwritingDefinition = DefUsePool.getDefinitionByDefId(overwritingDefId);
		if(overwritingDefinition==null)
			throw new IllegalStateException("expect DefUsePool to know definitions traced by instrumented code. defId: "+overwritingDefId);
		// if the overwritingDefinition is in a root-branch it's not really avoidable
		if(overwritingDefinition.getBranchId()!=-1) {
			Branch branchToAvoid = BranchPool.getBranch(overwritingDefinition.getBranchId());
			if(branchToAvoid==null)
				throw new IllegalStateException("expect BranchPool to know all non-root-branches");
			// get alternative branch
			BranchCoverageTestFitness alternativeBranchFitness = 
				getAlternativeBranchTestFitness(branchToAvoid.getCFGVertex());
			// set up duCounter interval to which the trace should be cut
			int duCounterStart = lastGoalDuPos;
			int duCounterEnd = usePos;
			// calculate fitness
			double newFitness = calculateFitnessForDURange(individual, result, objectTrace, 
					objectId, alternativeBranchFitness, 
					overwritingDefinition,false,
					duCounterStart, duCounterEnd);
			if(newFitness == 0.0) // this can happen when both the false and the true-branch where passed in the considered range
				return ALTERNATIVE_FITNESS_RANGE;
				
			// TODO research: shouldn't this fitness always be >=0 and <1 (because approach level is 0 and branch distance is normalized)
			// thing is i'm pretty sure it should be as described, but since our control dependencies are crap right now
			// this fitness can be >1, meaning the approach level can be >0 when the overwritingDef was control dependent from more then one Branch
			// this can for example happen when you have a branch expression with several sub-expressions separated by an ||

			// quick fix: when the fitness is >1 assume it was the flaw described above and only look at branch-distance part of fitness (decimal places)
			if(newFitness>1) {
				int approachPart = (int)newFitness;
				newFitness-=approachPart;
			}
			if(newFitness < 0 || newFitness > 2) {
				preFitnessDebugInfo(result,false);
				System.out.println();
				ExecutionTrace cutTrace = objectTrace.getTraceInDUCounterRange(overwritingDefinition,!overwritingDefinition.getCFGVertex().branchExpressionValue,duCounterStart,duCounterEnd);
				printFinishCalls(cutTrace);
				System.out.println("overwritingDef: "+overwritingDefinition.toString());
				System.out.println("on object "+objectId);
				System.out.println("branch to avoid: Branch "+branchToAvoid.getBranchId()+" bytecode "+branchToAvoid.getBytecodeId());
				System.out.println("alternative branch fitness: "+alternativeBranchFitness.toString());
				throw new IllegalStateException("unexpected, was: "+newFitness);
			}
			return ALTERNATIVE_FITNESS_RANGE*newFitness;
		}
		
		return ALTERNATIVE_FITNESS_RANGE;
	}

	/**
	 * Determines the BranchCoverageTestFitness of goalDefinitionBranch considering the full ExecutionTrace
	 * 
	 * Is called on every call to getDistance() if the goalDefinition isn't special
	 *  s. isSpecialGoalDefinition()
	 */
	private double calculateDefFitnessForCompleteTrace(TestChromosome individual, ExecutionResult result) {
		if(isSpecialGoalDefinition())
			return 0.0;
		// check ExecutionTrace.passedDefinitions first, because calculating BranchTestFitness takes time
		if(hasEntriesForId(result.trace.passedDefinitions.get(goalVariable),goalDefinition.getDefId()))
			return 0.0;
		// return calculated fitness
		return goalDefinitionBranchTestFitness.getFitness(individual, result);
	}
	
	/**
	 * Determines the BranchCoverageTestFitness of goalUseBranch considering the full ExecutionTrace
	 * 
	 * Is called on every call to getDistance() if the goalDefinition isn't special
	 *  s. isSpecialGoalDefinition()
	 */
	private double calculateUseFitnessForCompleteTrace(TestChromosome individual,
			ExecutionResult result) {
		// check ExecutionTrace.passedUses first, because calculating BranchTestFitness takes time
		if(hasEntriesForId(result.trace.passedUses.get(goalVariable),goalUse.getUseId()))
			return 0.0;
		// return calculated fitness
		return goalUseBranchTestFitness.getFitness(individual, result);
	}
		
	/**
	 * Calculates the goalUseFitness for the given goalDefinitionPos as follows:
	 * 
	 * For every goalDefinitionPosition in the objectTrace for the given objectId
	 * this method gets called by getDistance() to determine the fitness of the
	 * goalUseBranch considering only information traced between the
	 * goalDefinitionPosition and the occurrence of the next overwriting definition
	 * 
	 * In order to do that the ExecutionTrace is filtered using calculateFitnessForDURange()
	 */
	private double calculateUseFitnessForDefinitionPos(TestChromosome individual, ExecutionResult result, 
			ExecutionTrace targetTrace, Integer objectId, int goalDefinitionPos) {
		
		int overwritingDefPos = getNextOverwritingDefinitionPos(targetTrace,goalDefinitionPos,objectId);
		double fitness = calculateFitnessForDURange(individual, result, targetTrace, objectId, 
				goalUseBranchTestFitness, goalUse, true, goalDefinitionPos, overwritingDefPos);

		return fitness;
	}
	
	/**
	 * Used to calculate a BranchCoverageTestFitness considering 
	 *  only trace information in a given range of duCounter positions
	 *  
	 * Filters the ExecutionTrace using ExecutionTrace.getTraceInDUCounterRange()
	 * to only contain information made between duCounterStart and duCounterEnd
	 * 
	 * Additionally, if wantToCoverTargetDU is set all points in the trace
	 * where the given targetDUBranch would be passed is filtered out in
	 * order to prevent miscalculations. 
	 * Again ExecutionTrace.getTraceInDUCounterRange() holds more information   
	 * 
	 * This method gets called for alternative fitness calculation - calculateAlternativeFitness() -
	 * and in order to determine the goalUseBranch fitness between a goalUsePos and its next
	 *  overwriting definition - calculateUseFitnessForDefinitionPos() 
	 * 
	 *   
	 */
	private double calculateFitnessForDURange(TestChromosome individual, ExecutionResult result, 
			ExecutionTrace targetTrace, Integer objectId, BranchCoverageTestFitness targetFitness, 
			DefUse targetDU, boolean wantToCoverTargetDU, int duCounterStart, int duCounterEnd) {
		
		// filter trace
		ExecutionTrace cutTrace = targetTrace.getTraceInDUCounterRange(targetDU,wantToCoverTargetDU,duCounterStart,duCounterEnd);
		double fitness = executeBranchFitnessForTrace(individual, result, cutTrace, targetFitness);
		// sanity check
		if(fitness == 0.0 && wantToCoverTargetDU) {
			System.out.println(cutTrace.toDefUseTraceInformation());
			System.out.println("duPosStart: "+duCounterStart);
			System.out.println("duPosEnd: "+duCounterEnd);
			int targetUseBranchBytecode = BranchPool.getBytecodeIdFor(targetDU.getBranchId());
			System.out.println("targetDU-branch-bytecode: "+targetUseBranchBytecode);
			printFinishCalls(cutTrace);
			throw new IllegalStateException("use cant have fitness 0 in this cut trace: "+cutTrace.toDefUseTraceInformation(goalVariable, objectId));
		}
		return fitness;		
	}
	
	// other core methods

	/**
	 * Executes the BranchCoverageTest.getFitness() function on the given
	 *  ExecutionResult but using the given targetTrace
	 *  
	 * The ExecutionResult is left in it's original state after execution
	 */
	private static double executeBranchFitnessForTrace(TestChromosome individual, ExecutionResult result, 
			ExecutionTrace targetTrace, BranchCoverageTestFitness targetFitness) {
		
		ExecutionTrace originalTrace = result.trace;
		result.trace = targetTrace;
		double fitness = targetFitness.getFitness(individual,result);
		result.trace = originalTrace;
		return fitness;
	}
	
	/**
	 * Determines whether the goalDefinition is assumed to always be covered
	 * 
	 * This is the case for static definitions in <clinit> and Parameter-Definitions
	 */
	private boolean isSpecialGoalDefinition() {
		// handle special cases
		if(goalDefinition == null 
				|| (goalDefinition.isStaticDU() && goalDefinition.getCFGVertex().methodName.startsWith("<clinit>"))) {
			if(DEBUG) {
				if(goalDefinition == null)
					logger.debug("Assume Parameter-Definition to be covered if the Parameter-Use is covered");
				else
					logger.debug("Assume definition from <clinit> to always be covered");
			}
			return true;
		}
		return false;
	}

	/**
	 * Determines the object Pool of the given trace for this DefUsePair
	 * 
	 * If the goalVariable is static all objects are considered
	 * otherwise only those objects that have passed both the goalUse and
	 * the goalDefinition are considered 
	 */
	private Set<Integer> getObjectPool(ExecutionTrace trace) {
		Set<Integer> objectPool = new HashSet<Integer>();
		if(trace.passedUses.get(this.goalVariable) == null)
			return objectPool;		
		if(trace.passedDefinitions.get(this.goalVariable) != null)
			objectPool.addAll(trace.passedDefinitions.get(this.goalVariable).keySet());
		if(goalDefinition == null || goalDefinition.isStaticDU()) {
			// in the static case all objects have to be considered
			objectPool.addAll(trace.passedUses.get(this.goalVariable).keySet());
			if(DEBUG) logger.debug("Static-goalVariable! Using all known Objects");
		} else {
			// on non-static goalVariables only look at objects that have traces of defs and uses for the goalVariable
			int oldSize = objectPool.size();
			objectPool.retainAll(trace.passedUses.get(this.goalVariable).keySet());
			if(DEBUG) {
				logger.debug("NON-Static-goalVariable "+this.goalVariable);
				logger.debug("#unused objects: "+(oldSize-objectPool.size()));
				Set<Integer> discardedObjects = new HashSet<Integer>();
				discardedObjects.addAll(trace.passedDefinitions.get(this.goalVariable).keySet());
				discardedObjects.removeAll(trace.passedUses.get(this.goalVariable).keySet());
				for(Integer id : discardedObjects) {
					logger.debug("  discarded object "+id);
				}
			}
		}
		if(DEBUG) {
			logger.debug("#considered objects: "+objectPool.size());
			for(Integer id : objectPool) {
				logger.debug("  object "+id);
			}
		}
		return objectPool;
	}

	// trace analysis methods

	/**
	 * Determines the overwriting definition for the given goalDefPos
	 * 
	 * An overwriting definition position is the duCounter position of 
	 * the next definition for goalVariable that was not the goalDefinition
	 * 
	 * If no such definition exists Integer.MAX_VALUE is returned
	 */
	private int getNextOverwritingDefinitionPos(ExecutionTrace objectTrace,
			Integer defPos, Integer objectId) {
		
		int lastPos = Integer.MAX_VALUE;
		Map<Integer,Integer> defMap = objectTrace.passedDefinitions.get(goalVariable).get(objectId);
		for(Integer duPos : defMap.keySet())
			if(duPos>defPos && duPos<lastPos && defMap.get(duPos) != goalDefinition.getDefId())
				lastPos = duPos;
		
		return lastPos;
	}

	/**
	 * Returns all the duCounterPositions of the goalUse in the given trace
	 */
	private List<Integer> getGoalUsePositions(ExecutionTrace trace, int objectId) {
		ArrayList<Integer> r = new ArrayList<Integer>();
		HashMap<Integer,Integer> useMap = trace.passedUses.get(this.goalVariable).get(objectId);
		if(useMap == null)
			return r;
		for(Integer usePos : useMap.keySet())
			if(useMap.get(usePos) == goalUse.getUseId())
				r.add(usePos);
		
		return r;
	}
	
	/**
	 * Returns all the duCounterPositions of the goalUse in the given trace
	 */
	private List<Integer> getGoalDefinitionPositions(ExecutionTrace trace, int objectId) {
		ArrayList<Integer> r = new ArrayList<Integer>();
		HashMap<Integer,Integer> defMap = trace.passedDefinitions.get(this.goalVariable).get(objectId);
		if(defMap == null)
			return r;
		for(Integer defPos : defMap.keySet()) 
			if(defMap.get(defPos) == goalDefinition.getDefId())
				r.add(defPos);
		
		return r;
	}
	
	/**
	 * Returns the set of definitionIds from all definitions that overwrite 
	 * the goal definition in the given duCounter-range
	 * 
	 * This method expects the given ExecutionTrace not to contain any 
	 * trace information for the goal Definition in the given range.
	 * If such a trace is detected this method throws an IllegalStateException!
	 */
	private Set<Integer> getOverwritingDefinitionIdsBetween(ExecutionTrace trace, int startingDUPos, int endDUPos, int objectId) {
		if(startingDUPos>endDUPos)
			throw new IllegalArgumentException("start must be lower or equal end");
		Set<Integer> r = new HashSet<Integer>();
		if (trace.passedDefinitions.get(this.goalVariable) == null)
			return r;
		Map<Integer, Integer> defMap = trace.passedDefinitions.get(this.goalVariable).get(objectId);
		if(defMap==null)
			return r;
		
		for (Integer defPos : defMap.keySet()) {
			if (defPos < startingDUPos || defPos > endDUPos)
				continue;
			int defId = defMap.get(defPos);
			if(defId == goalDefinition.getDefId())
				throw new IllegalStateException("expect given trace not to have passed goalDefinition in the given duCounter-range");
			r.add(defId);
		}
		return r;
	}
	
	/**
	 * Returns the defID of the Definition that is active in the given trace at usePos 
	 */
	private int getActiveDefIdAt(ExecutionTrace trace, int usePos, int objectId) {
		if (trace.passedDefinitions.get(this.goalVariable) == null)
			return -1;
		
		int lastDef = -1;
		int lastPos = -1;
		Map<Integer, Integer> defMap = trace.passedDefinitions.get(this.goalVariable).get(objectId);
		if(defMap == null)
			return -1;
		for (Integer defPos : defMap.keySet()) {
			if (defPos > usePos)
				continue;
			if(lastPos<defPos) {
				lastDef = defMap.get(defPos);
				lastPos = defPos;
			}
		}
		return lastDef;
	}

	// auxiliary methods
	
	private static boolean hasEntryLowerThan(List<Integer> list, Integer border) {
		for(Integer defPos : list)
			if(defPos<border)
				return true;
		return false;
	}
	public Integer getMaxEntryLowerThan(List<Integer> list,Integer border) {
		int lastPos = -1;
		for(Integer defPos : list)
			if(defPos<border && defPos>lastPos)
				lastPos = defPos;
		return lastPos;
	}
	private boolean hasEntriesForId(Map<Integer,HashMap<Integer,Integer>> objectDUMap, int targetId) {
		if(objectDUMap == null)
			return false;
		for(Integer objectId : objectDUMap.keySet())
			if(hasEntriesForId(objectDUMap,objectId,targetId))
				return true;
		
		return false;
	}
	private boolean hasEntriesForId(Map<Integer,HashMap<Integer,Integer>> objectDUMap, Integer objectId, int targetId) {
		if(objectDUMap == null)
			return false;
		if(objectDUMap.get(objectId) == null)
			return false;
		for(Integer defId : objectDUMap.get(objectId).values())
			if(defId.intValue() == targetId)
				return true;
		return false;
	}
	
	// debugging methods

	/**
	 * Only a sanity check function for testing purposes
	 */
	private boolean traceCoversGoal(Chromosome individual, ExecutionTrace trace) {
		if(trace.passedUses.get(this.goalVariable)==null)
			return false;
		Set<Integer> objectPool = getObjectPool(trace);

		for(Integer objectID : objectPool) {
			List<Integer> usePositions = getGoalUsePositions(trace,objectID);
			// use not reached
			if (usePositions.size() == 0)
				continue;
			if(goalUse.isParameterUse())
				return true;
			if(goalDefinition.isStaticDU() && goalDefinition.getMethodName().startsWith("<clinit>"))
				return true;
			
			for(Integer usePos : usePositions) {
				
				if (getActiveDefIdAt(trace, usePos, objectID) == goalDefinition.getDefId())
					return true;
			}
		}

		return false;
	}	
	private void setCovered(Chromosome individual, ExecutionTrace trace, Integer objectId) {
		if(DEBUG) {
			logger.debug("goal COVERED by object "+objectId);
			logger.debug("==============================================================");
		}
		this.coveringObjectId = objectId;
		updateIndividual(individual, 0);
		
		if(DEBUG)
			if(!traceCoversGoal(individual, trace))
				throw new IllegalStateException("calculation flawed. goal wasn't covered");
	}
	private void preFitnessDebugInfo(ExecutionResult result, boolean respectPrintFlag) {
		if(PRINT_DEBUG || !respectPrintFlag) {
			System.out.println("==============================================================");
			System.out.println("current goal: "+toString());
			System.out.println("current test:");
			System.out.println(result.test.toCode());
		}
	}
	private void postFitnessDebugInfo(Chromosome individual, ExecutionResult result, double fitness) {
		if(DEBUG) { 
			if(fitness != 0) {
				if(PRINT_DEBUG) {
					System.out.println("goal NOT COVERED. fitness: "+fitness);
					System.out.println("==============================================================");
				}
				if(traceCoversGoal(individual, result.trace))
					throw new IllegalStateException("calculation flawed. goal was covered but fitness was "+fitness);
			} else
				throw new IllegalStateException("inconsistent state. this should have been detected earlier");
		}
	}	
	private void printFinishCalls(ExecutionTrace trace) {
		for (MethodCall call : trace.finished_calls) {
			System.out.println("Found MethodCall for: " + call.method_name
					+ " on object " + call.callingObjectID);
			System.out.println("#passed branches: " + call.branch_trace.size());
			for (int i = 0; i < call.defuse_counter_trace.size(); i++) {
				System.out.println(i + ". at Branch "
						+ call.branch_trace.get(i) 
						+ " true_dist: " + call.true_distance_trace.get(i) 
						+ " false_dist: "+ call.false_distance_trace.get(i)
						+ " duCounter: " + call.defuse_counter_trace.get(i));
				System.out.println();
			}
		}
	}
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}
	
	// getter methods
	
	public ExecutionTrace getCoveringTrace() {
		return coveringTrace;
	}
	public String getGoalVariable() {
		return goalVariable;
	}
	public int getCoveringObjectId() {
		return coveringObjectId;
	}
	
	// methods inherited from Object
	
	@Override
	public String toString() {
		StringBuffer r = new StringBuffer();
		r.append("DUFitness for:");
		r.append("\n\t");
		if(goalDefinition == null)
			r.append("Parameter-Definition "+goalUse.getLocalVarNr()+" for method "+goalUse.getMethodName());
		else
			r.append(goalDefinition.toString());
		r.append("\n\t");
		r.append(goalUse.toString());
		return r.toString();
	}
	
	@Override
	public boolean equals(Object o) {
//		System.out.println("called"); // TODO: somehow doesnt get called
		if(!(o instanceof DefUseCoverageTestFitness))
			return false;
		try {
			DefUseCoverageTestFitness t = (DefUseCoverageTestFitness)o;
			return t.goalDefinition.equals(this.goalDefinition) && t.goalUse.equals(this.goalUse);
		} catch(Exception e) {
			return false;
		}
	}	
	
}
