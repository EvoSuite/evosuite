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
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * Evaluate fitness of a single test case with respect to one def-use pair
 * 
 * @author
 * 
 */
public class DefUseCoverageTestFitness extends TestFitnessFunction {

	private final Definition goalDef;
	private final Use goalUse;
	private final String goalVariable;
	private final BranchCoverageTestFitness defTestFitness;
	private final BranchCoverageTestFitness useTestFitness;
	private Integer coveringObjectID = -1;

	public DefUseCoverageTestFitness(Definition def, Use use) {

		if (!def.getDUVariableName().equals(use.getDUVariableName()))
			throw new IllegalArgumentException(
			        "expect def and use to be for the same variable");

		this.goalDef = def;
		this.goalUse = use;
		this.goalVariable = def.getDUVariableName();
		this.defTestFitness = getTestFitness(def.getCFGVertex());
		this.useTestFitness = getTestFitness(use.getCFGVertex());

	}

	private BranchCoverageTestFitness getTestFitness(CFGVertex v) {

		BranchCoverageTestFitness r;

		if (v.branchID == -1) {
			r = new BranchCoverageTestFitness(new BranchCoverageGoal(v.className,
			        v.className + "." + v.methodName));
		} else {
			ControlFlowGraph cfg = CFGMethodAdapter.getCFG(v.className, v.methodName);
//			int byteIdOfBranch = BranchPool.getBytecodeIDFor(v.branchID);
			Branch b = BranchPool.getBranch(v.branchID);
			r = new BranchCoverageTestFitness(new BranchCoverageGoal(b,
			        v.branchExpressionValue, cfg, v.className, v.methodName));
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		boolean DEBUG = false;
		if(DEBUG)System.out.println("current goal: "+toString());		
		if(DEBUG) System.out.println();
		
		// TODO IDEA FOR AN EVO-SUITE-FEATURE: given a test(suite) for a class, check whether test achieves coverage-criterion
		
		// TODO detect which LV in which methodCall
		// TODO cut ExecutionTrace for branch-fitness-evaluation to Trace of current object only
		// 	subTODO: first mark MethodCalls in ExecutionTracer.finished_calls with a methodID and the current objectID
		// 			 then kick out all non-static MethodCalls for objects other then the one with current objectID
		//			 if goalDef is for a LV, also consider MethodCalls individually		
		// TODO cut ExecutionTrace for branch-fitness-evaluation to Trace of a certain path (between goalDef and overwritingDef)
		//  subTODO: in a MethodCall from the ExecutionTracer add another list holding information about the current duConter
		//			 then allow for cutting the ExecutionTrace in a way that it only holds data associated with a duCounter in a given range
		
		// TODO idea: 	once def is covered, look at all duCounterPositions of goalDef (goalDefPositions):
		//				if goalUse is covered any of these goalDefPositions, return 0
		//				if not return the minimum over all branchUseFitnesses in the trace
		//				where the trace is cut between the goalDefPosition and the next overwritingDefinition
		
		if(result.trace.passedUses.get(this.goalVariable) == null) {
			// trace doesnt even know the variable of this goal
			if(DEBUG) System.out.println("trace had no use-trace-entry for goalVar");
			return getMaxFitness();
		}

		// ASSUMPTION: static definitions in <clinit> are ALWAYS covered!!!
		if(goalDef.isStaticDU() && goalDef.getCFGVertex().methodName.startsWith("<clinit>")) {
			if(DEBUG) System.out.println("Assume definition from <clinit> to always be covered");
			return normalize(useTestFitness.getFitness(individual, result));
		}
		
//		if(traceCoversGoal(result.trace)) {
//			return 0;
//		}

		double fitness = getMaxFitness();
		
		HashSet<Integer> objectPool = new HashSet<Integer>();
		if(result.trace.passedDefs.get(this.goalVariable) != null)
			objectPool.addAll(result.trace.passedDefs.get(this.goalVariable).keySet());
		if(goalDef.isStaticDU()) {
			// in the static case all objects have to be considered
			objectPool.addAll(result.trace.passedUses.get(this.goalVariable).keySet());
			if(DEBUG) System.out.println("Static-goalVariable! Using all known Objects");
		} else {
			// on non-static goalVariables only look at objects that have traces of defs and uses for the goalVariable
			int oldSize = objectPool.size();
			objectPool.retainAll(result.trace.passedUses.get(this.goalVariable).keySet());
			if(DEBUG) System.out.println("NON-Static-goalVariable "+this.goalVariable+"\n#unused objects: "+(oldSize-objectPool.size()));
		}
		
		if(DEBUG) {
			System.out.println("#considered objects: "+objectPool.size()+"\n");
			System.out.println("current test:\n"+result.test.toCode());
			System.out.println("current duTrace: "+result.trace.toDefUseTraceInformation());
			System.out.println();
		}
		
		ExecutionTrace originalTrace = result.trace;
		
		for(Integer objectID : objectPool) {
		
			if(DEBUG) System.out.println("current object: "+objectID);
			
			// TODO: this is only a first heuristic for testing purposes
			//		see "idea" above for how this is supposed to look like

			// known bugs: sometimes accepts a test even though the goalDef is overwritten before the goalUse is reached
			
			if(!goalDef.isStaticDU()) { // only consider trace of current objectID
				result.trace = originalTrace.getTraceForObject(objectID); // TODO !!!
			}
			
			double defFitness = defTestFitness.getFitness(individual, result);
			
			result.trace = originalTrace;
			
			double newFitness = 1 + normalize(defFitness);
			
			// definition not reached yet
			if (defFitness != 0) {
				if(DEBUG) System.out.println("Definition NOT covered on object"+objectID+": "+defFitness);
				if(newFitness<fitness)
					fitness = newFitness;
				continue;
			}
			newFitness = normalize(getMaxFitness());
			
			// definition reached on this object?
			int goalDefPos = getLastGoalDefPos(result.trace,objectID);
			if(goalDefPos == -1) {
				if(DEBUG) System.out.println("Definition NOT covered on this object but on another one");
				// TODO s. cutting ExecutionTrace above
				continue;
			}
			if(DEBUG) System.out.println("Definition covered at duPos "+goalDefPos);
			
			// use covered?
			int goalUsePos = getLastGoalUsePos(result.trace,objectID);
			// use not covered yet
			if(goalUsePos == -1) {
				
				if(!goalDef.isStaticDU()) { // only consider trace of current objectID
					result.trace = originalTrace.getTraceForObject(objectID); // TODO !!!
				}
				
				double useFitness = useTestFitness.getFitness(individual, result); // TODO only look at current objectID
				
				result.trace = originalTrace;
				
				if(DEBUG) System.out.println("Use NOT covered "+useFitness);
				newFitness = normalize(useFitness);
				// TODO bug: this fitness can be 0 because the trace in result still contains traces of other objects!
				if(newFitness == 0) { // use got covered by another object
					if(DEBUG) System.out.println("goal covered abnormally at "+objectID+" for use "+goalUse.getUseID());
					newFitness = normalize(getMaxFitness());
				}
				if(newFitness < fitness)
					fitness = newFitness;
				continue;
			}
			if(DEBUG) System.out.println("goalUse covered at duPos "+goalUsePos);
			
			int activeDefID = getActiveDefIDFor(result.trace,goalUsePos,objectID);
			int activeDefPos = getActiveDefPosFor(result.trace,goalUsePos, objectID);
			
			if(goalDef.isStaticDU() && activeDefID == -1) {
				// TODO known bug: static calls seem not to be present in all ExecutionTraces
				//					maybe trace static calls directly in the ExecutionTracer?
				activeDefID = getActiveDefIDFor(result.trace,goalUsePos,0); // static calls have objectID 0
				activeDefPos = getActiveDefPosFor(result.trace,goalUsePos,0); // static calls have objectID 0
			}
			
			// definition came after use
			if (goalDefPos > goalUsePos) {
				if(DEBUG) System.out.println("Definition came after use");
				// TODO at this point the ExecutionTrace should be cut such that only traces remain, that came after the goalDefPos
				if(newFitness < fitness)
					fitness = newFitness;
				continue;
			}
			//definition was overwritten
			// TODO known bug: if goalDef gets overwritten and there is another 
			// overwriting definition after the goalUsePos this will not be detected
			if (activeDefID != goalDef.getDefID() && goalUsePos>activeDefPos) { 
				if(DEBUG) System.out.println("goalDefinition no longer active at covered use");
				if(newFitness < fitness)
					fitness = newFitness;
				continue;
			}
			
			if(!goalDef.isStaticDU()) { // only consider trace of current objectID
				result.trace = originalTrace.getTraceForObject(objectID); // TODO !!!
			}
			
			double useFitness = useTestFitness.getFitness(individual, result);
			
			result.trace = originalTrace;
			
			if(useFitness == 0) {
//				System.out.println("goal covered normally at object "+objectID);
				if(DEBUG) System.out.println("goal COVERED");
				if(DEBUG) System.out.println("===============================");
				this.coveringObjectID = objectID;
				// TODO: add covering trace information for test comments!!!
				return 0;
			}
			
			if(DEBUG) System.out.println("goalUse not covered yet "+useFitness);
			
			newFitness = normalize(useFitness);
			if(newFitness<fitness)
				fitness = newFitness;
		}
		
//		if(fitness == 0 && !traceCoversGoal(result.trace)) {
//			System.out.println("goal got covered without traceCoversGoal() recognizing it!!! "+toString());
//			System.out.println(result.test.toCode());			
//		}

		if(fitness != 0)
			if(DEBUG) System.out.println("goal NOT COVERED");
		
		if(DEBUG) System.out.println("===============================");
		
		return fitness;
	}

	/**
	 * Only a sanity check function for testing purposes
	 */
	private boolean traceCoversGoal(ExecutionTrace trace) {
		if(trace.passedUses.get(this.goalVariable)==null)
			return false;
		// new idea: look for all the positions of use in trace, for each check active def, if active def is goal def goal is covered

		for(Integer objectID : trace.passedUses.get(this.goalVariable).keySet()) {
			if(objectID.intValue() == 0)
				continue;
			ArrayList<Integer> usePositions = getGoalUsePositions(trace,objectID);
			
			// use not reached
			if (usePositions.size() == 0)
				continue;

			for(Integer usePos : usePositions) {
				
				if (getActiveDefIDFor(trace, usePos, objectID) == goalDef.getDefID())
					return true;
			}
		}

		return false;
	}

	/**
	 * Returns all the duCounterPositions of the goalUse in the given trace
	 */
	private ArrayList<Integer> getGoalUsePositions(ExecutionTrace trace, int objectID) {
		
		ArrayList<Integer> r = new ArrayList<Integer>();
		HashMap<Integer,Integer> useMap = trace.passedUses.get(this.goalVariable).get(objectID);
		
		if(useMap == null)
			return r;
		
		for(Integer usePos : useMap.keySet()) {
			if(useMap.get(usePos) == goalUse.getUseID())
				r.add(usePos);
		}
		
		return r;
	}

	/**
	 * Returns the duCounterPosition of the Definition that is active in the given trace at usePos 
	 */
	private int getActiveDefPosFor(ExecutionTrace trace, int usePos, int objectID) {
		if (trace.passedDefs.get(goalDef.getDUVariableName()) == null)
			return -1;
		
		int lastPos = -1;

		Map<Integer, Integer> defMap = trace.passedDefs.get(this.goalVariable).get(objectID);
		if(defMap != null) {
			for (Integer defPos : defMap.keySet()) {
				if (defPos > usePos)
					continue;
				if(lastPos<defPos) {
					lastPos = defPos;
				}
			}
		}
		return lastPos;
	}
	
	/**
	 * Returns the defID of the Definition that is active in the given trace at usePos 
	 */
	private int getActiveDefIDFor(ExecutionTrace trace, int usePos, int objectID) {
		if (trace.passedDefs.get(goalDef.getDUVariableName()) == null)
			return -1;
		
		int lastDef = -1;
		int lastPos = -1;

		Map<Integer, Integer> defMap = trace.passedDefs.get(this.goalVariable).get(objectID);
		if(defMap != null) {
			for (Integer defPos : defMap.keySet()) {
				if (defPos > usePos)
					continue;
				if(lastPos<defPos) {
					lastDef = defMap.get(defPos);
					lastPos = defPos;
				}
			}
		}
		return lastDef;
	}	
	
	/**
	 * Returns the last active Definition for the goalVariable in the given Trace
	 */
	private int getLastDef(ExecutionTrace trace, Integer objectID) {
		if (trace.passedDefs.get(goalDef.getDUVariableName()) == null)
			return -1;
		
		int lastDef = -1;
		int lastPos = -1;

		HashMap<Integer,Integer> defMap = trace.passedDefs.get(this.goalVariable).get(objectID);
		if(defMap != null) {
			for (Integer defPos : defMap.keySet()) {
				if(defPos>lastPos) {
					lastDef = defMap.get(defPos);
					lastPos = defPos;
				}
			}
		}
		return lastDef;
	}

	/**
	 * Returns the last active definition for the goalVariable in the given trace
	 */
	private int getLastDefPos(ExecutionTrace trace, Integer objectID) {
		int lastPos = -1;
		if (trace.passedDefs.get(this.goalVariable) == null)
			return -1;
		if (trace.passedDefs.get(this.goalVariable).get(objectID) == null)
			return -1;		

		for (Integer defPos : trace.passedDefs.get(this.goalVariable).get(objectID).keySet()) {
			if(lastPos<defPos)
				lastPos = defPos;
		}
		return lastPos;
	}

	/**
	 * Returns the last duCounterPosition of the goalUse in the given trace
	 */
	private int getLastGoalUsePos(ExecutionTrace trace, Integer objectID) {
		if (trace.passedUses.get(this.goalVariable) == null)
			return -1;
		if (trace.passedUses.get(this.goalVariable).get(objectID) == null)
			return -1;		

		int lastPos = -1;
		HashMap<Integer,Integer> useMap = trace.passedUses.get(this.goalVariable).get(objectID);
		for (Integer usePos : useMap.keySet()) {
//			System.out.println("use-pos: "+usePos+" for use "+useMap.get(usePos));
			if(useMap.get(usePos)==goalUse.getUseID() && lastPos<usePos)
				lastPos = usePos;
		}
		return lastPos;
	}
	
	/**
	 *  returns the last duCounterPosition of the goalDef in the given trace
	 */
	private int getLastGoalDefPos(ExecutionTrace trace, Integer objectID) {
		if (trace.passedDefs.get(this.goalVariable) == null)
			return -1;
		if (trace.passedDefs.get(this.goalVariable).get(objectID) == null)
			return -1;		

		int lastPos = -1;
		HashMap<Integer,Integer> defMap = trace.passedDefs.get(this.goalVariable).get(objectID);
		for (Integer defPos : defMap.keySet()) {
			if(defMap.get(defPos)==goalDef.getDefID() && lastPos<defPos)
				lastPos = defPos;
		}
		return lastPos;
	}

	private double getMaxFitness() {

		return 200; // TODO
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {

		individual.setFitness(fitness); // TODO ???
	}

	@Override
	public String toString() {
		return "DUFitness for " + goalDef.getDUVariableName() + " Def " + goalDef.getDefID()
		        + " in " + goalDef.getMethodName() + " branch " + goalDef.getBranchID() + "(l"
		        + goalDef.getLineNumber() + ") Use " + goalUse.getUseID() + " in "
		        + goalUse.getMethodName() + " branch " + goalUse.getBranchID() + " (l"
		        + goalUse.getLineNumber() + ") "+(this.coveringObjectID!=-1?("covered by object "+this.coveringObjectID):"");
	}
	
	@Override
	public boolean equals(Object o) {
		System.out.println("called"); // TODO: somehow doesnt get called
		
		if(!(o instanceof DefUseCoverageTestFitness))
			return false;
		
		try {
			DefUseCoverageTestFitness t = (DefUseCoverageTestFitness)o;
			return t.goalDef.equals(this.goalDef) && t.goalUse.equals(this.goalUse);
		} catch(Exception e) {
			return false;
		}
	}

}
