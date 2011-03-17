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
//	- 
 */

/**
 * Evaluate fitness of a single test case with respect to one Definition-Use pair
 * 
 * @author Andre Mis
 * 
 */
public class DefUseCoverageTestFitness extends TestFitnessFunction {

	private final static boolean DEBUG = true;	
	
	private final String goalVariable;
	private final Use goalUse;	
	private final Definition goalDefinition;
	private final BranchCoverageTestFitness goalDefinitionBranchTestFitness;
	private final BranchCoverageTestFitness goalUseBranchTestFitness;
	
	private Integer coveringObjectId = -1;
	private ExecutionTrace coveringTrace;
	
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
	 * 1.
	 * If the goalDefinition is not passed in the result at all:
	 * 	 	This method returns 1 + normalize(goalDefinitionFitness) where goalDefinition equals
	 * 		the BranchCoverageTestFitness for the Branch that the CFGVertex of
	 * 		this goals definition is control dependent on (goalDefinitionBranch)
	 * 
	 * 2.
	 * If the goalDefinition is passed, but the goalUse is not passed at all:
	 * 		This method returns the normalized BranchCoverageTestFitness for the Branch that
	 * 		the CFGVertex of this goals Use is control dependent on (goalUseBranch)
	 * 
	 * 3.
	 * If both the goalDefinition and the goalUse were passed at least once in the given result:
	 * 		1.
	 * 		If and only if at any goalUsePosition the active definition was the goalDefinition the
	 * 		Definition-Use-Pair of this goal is covered and the method returns 0
	 * 
	 * 		2.
	 * 		Otherwise this method returns the minimum of the following:
	 * 			1. For all goalUsePositions if there was an overwriting definition the normalized 
	 * 				BranchCoverageTestFitness of not taking the branch with the overwriting definition
	 *  
	 * 			2. For all goalDefPositions the normalized BranchCoverageTestFitness for the goalUseBranch
	 * 				in the ExecutionTrace where every trace information is filtered out except
	 * 				the information traced between the occurrence of the goalDefinitionPosition
	 * 				and the next overwritingDefinitionPosition
	 * 
	 * If this goals definition is not a static variable the trace information of 
	 * all constructed objects of the CUT are handled separately and the minimum over all
	 * individually calculated fitness is returned   
	 * 		
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		preFitnessDebugInfo(result);
		// at first handle special cases where definition is assumed to be covered if use is covered:
		if(isSpecialDefinition()) {
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
			logger.debug("  ===  CURRENT OBJECT "+objectId+"  === ");
			if(!isSpecialDefinition() 
					&& !hasEntriesForId(result.trace.passedDefinitions.get(goalVariable), 
							objectId, goalDefinition.getDefId())) {
				
				logger.debug("Discarded object "+objectId+" - goalDefinition not passed");
				continue;
			}
			double newFitness = calculateFitnessForObject(individual,result,objectId);
			if(newFitness<fitness)
				fitness=newFitness;
			if(fitness==0.0)
				return 0.0;
		}
		postFitnessDebugInfo(individual, result, fitness);
		return fitness;
	}
		
	/**
	 * Only gets called when definition is passed on given objectId or definition is special case
	 */
	private double calculateFitnessForObject(TestChromosome individual,
			ExecutionResult result, Integer objectId) {
		
		ExecutionTrace objectTrace = result.trace.getTraceForObject(objectId);
		double fitness = 1;
		// Case 2.
		// handle special definition case
		if(isSpecialDefinition()) {
			double useFitness = calculateUseFitnessForObject(individual,result,objectTrace,objectId);
			fitness = normalize(useFitness);
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
					// Case 3.2.1 calculate fitness for not taking branch of overwriting definition
					int lastGoalDUPos = getMaxEntryLowerThan(goalDefinitionPositions,usePos);
					Set<Integer> overwritingDefIds = getOverwritingDefinitionIdsBetween(objectTrace, lastGoalDUPos+1, usePos, objectId);
					if(overwritingDefIds.isEmpty())
						throw new IllegalStateException("if goalDefinition was passed before goalUse but is no longer active there must be an overwriting definition");
					double alternativeFitness = 0.0; 
					for(Integer overwritingDefId : overwritingDefIds) {	
						double overwritingFitness = calculateAlternatveFitnessForOverwritingDefinition(individual, result,
							objectTrace, objectId, overwritingDefId, lastGoalDUPos, usePos);
						if(overwritingFitness <= 0.0 || overwritingFitness > 1.0)
							throw new IllegalStateException("expected this definition to be normalized or 1");
						alternativeFitness += overwritingFitness;
					}
					alternativeFitness = normalize(alternativeFitness);
					if(alternativeFitness == 0.0)
						throw new IllegalStateException("this fitness must never be 0");
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
			double newFitness = calculateFitnessForDURange(individual, result, objectTrace, objectId, 
					alternativeBranchFitness, overwritingDefinition, duCounterStart, duCounterEnd);
			// TODO research: shouldn't this fitness always be >0 and <1 (because approach level is 0 and branch distance is normalized) 
			newFitness = normalize(newFitness);
			return newFitness;
		}
		
		return 1;
	}

	private double calculateDefFitnessForCompleteTrace(TestChromosome individual, ExecutionResult result) {
		if(isSpecialDefinition())
			return 0.0;
		// check ExecutionTrace.passedDefinitions first, because calculating BranchTestFitness takes time
		if(hasEntriesForId(result.trace.passedDefinitions.get(goalVariable),goalDefinition.getDefId()))
			return 0.0;
		// calculate fitness
		double defFitness = goalDefinitionBranchTestFitness.getFitness(individual, result);
		return defFitness;
	}
	
	private double calculateUseFitnessForCompleteTrace(TestChromosome individual,
			ExecutionResult result) {
		// check ExecutionTrace.passedUses first, because calculating BranchTestFitness takes time
		if(hasEntriesForId(result.trace.passedUses.get(goalVariable),goalUse.getUseId()))
			return 0.0;
		// calculate fitness
		double useFitness = goalUseBranchTestFitness.getFitness(individual, result);
		return useFitness;
	}
		
	private double calculateUseFitnessForDefinitionPos(TestChromosome individual, ExecutionResult result, 
			ExecutionTrace targetTrace, Integer objectId, int goalDefinitionPos) {
		
		int overwritingDefPos = getNextOverwritingDefinitionPos(targetTrace,goalDefinitionPos,objectId);
		double fitness = calculateFitnessForDURange(individual, result, targetTrace,
				objectId, goalUseBranchTestFitness, goalUse, goalDefinitionPos, overwritingDefPos);

		return fitness;
	}
	
	private double calculateFitnessForDURange(TestChromosome individual, ExecutionResult result, 
			ExecutionTrace targetTrace, Integer objectId,
			BranchCoverageTestFitness targetFitness, DefUse targetDU, 
			int duCounterStart, int duCounterEnd) {
		
		// filter trace
		ExecutionTrace originalTrace = result.trace;
		ExecutionTrace cutTrace = targetTrace.getTraceInDUCounterRange(targetDU, duCounterStart,duCounterEnd);
		result.trace = cutTrace;
		// calculate fitness
		double fitness = targetFitness.getFitness(individual,result);
		// revert changes to result
		result.trace = originalTrace;
		// sanity check
		if(fitness == 0.0) {
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

	private double calculateUseFitnessForObject(TestChromosome individual, ExecutionResult result, 
			ExecutionTrace objectTrace, Integer objectId) {
		
		ExecutionTrace originalTrace = result.trace;
		result.trace = objectTrace;
		double useFitness = goalUseBranchTestFitness.getFitness(individual,result);
		result.trace = originalTrace;
		return useFitness;
	}
	
	// semi core methods
	
	private boolean isSpecialDefinition() {
		// handle special cases (Parameters and static definitions in <clinit> are always covered)
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
	private void preFitnessDebugInfo(ExecutionResult result) {
		logger.debug("==============================================================");
		logger.debug("current goal: "+toString());
		logger.debug("current test:");
		logger.debug(result.test.toCode());
	}
	private void postFitnessDebugInfo(Chromosome individual, ExecutionResult result, double fitness) {
		if(DEBUG) { 
			if(fitness != 0) {
				logger.debug("goal NOT COVERED. fitness: "+fitness);
				logger.debug("==============================================================");
				if(traceCoversGoal(individual, result.trace)) {
					System.out.println("test:\n"+result.test.toCode());
					throw new IllegalStateException("calculation flawed. goal was covered but fitness was "+fitness);
				}
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
						+ call.branch_trace.get(i) + " true_dist: "
						+ call.true_distance_trace.get(i) + " false_dist: "
						+ call.false_distance_trace.get(i));
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
		
		r.append("DUFitness for ");
		if(goalUse.isStaticDU())
			r.append("static ");
		r.append(goalUse.getDUVariableType());
		r.append("-Variable \"" + this.goalVariable +"\"");
		if(goalDefinition == null) {
			r.append("\n\tParameter-Definition "+goalUse.getLocalVarNr()+" for method "+goalUse.getMethodName());
		} else {
			r.append("\n\t");
			r.append("Def ");
//			r.append(goalDef.toString());
			r.append(goalDefinition.getDefId() + " in " + goalDefinition.getMethodName()+"."+goalDefinition.getBytecodeId()); 
			r.append(" branch " + goalDefinition.getBranchId() + (goalDefinition.getCFGVertex().branchExpressionValue?"t":"f"));
			r.append(" line "+ goalDefinition.getLineNumber());
		}
		
		r.append("\n\t");
		r.append("Use " + goalUse.getUseId() + " in " + goalUse.getMethodName()+"."+goalUse.getBytecodeId()); 
		r.append(" branch " + goalUse.getBranchId() + (goalUse.getCFGVertex().branchExpressionValue?"t":"f"));
		r.append(" line "+ goalUse.getLineNumber());
		r.append((this.coveringObjectId!=-1?("\n\tcovered by object "+this.coveringObjectId):""));
		
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
