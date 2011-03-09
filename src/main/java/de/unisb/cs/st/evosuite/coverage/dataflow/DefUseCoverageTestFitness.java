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
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;

/**
 * Evaluate fitness of a single test case with respect to one def-use pair
 * 
 * @author Andre Mis
 * 
 */
public class DefUseCoverageTestFitness extends TestFitnessFunction {

	private final static boolean DEBUG = false;	
	
	private final Use goalUse;	
	private final Definition goalDef;
	private final String goalVariable;
	private final BranchCoverageTestFitness defTestFitness;
	private final BranchCoverageTestFitness useTestFitness;
	private Integer coveringObjectID = -1;
	private ExecutionTrace coveringTrace;
	
	public DefUseCoverageTestFitness(Definition def, Use use) {

		if (!def.getDUVariableName().equals(use.getDUVariableName()))
			throw new IllegalArgumentException(
			        "expect def and use to be for the same variable");

		this.goalDef = def;
		this.goalUse = use;
		this.goalVariable = def.getDUVariableName();
		this.defTestFitness = getTestFitness(def);
		this.useTestFitness = getTestFitness(use);
	}
	
	/**
	 * Used for Parameter-Uses
	 */
	public DefUseCoverageTestFitness(Use use) {
		if(!use.getCFGVertex().isParameterUse)
			throw new IllegalArgumentException("this constructor is only for Parameter-Uses");

		goalVariable = use.getDUVariableName();
		goalDef = null;
		defTestFitness = null;
		goalUse = use;
		useTestFitness = getTestFitness(use);
	}

	private BranchCoverageTestFitness getTestFitness(DefUse du) {

		BranchCoverageTestFitness r;
		CFGVertex v = du.getCFGVertex();

		if (v.branchID == -1) {
			r = getRootBranchTestFitness(du);
		} else {
			ControlFlowGraph cfg = CFGMethodAdapter.getCFG(v.className, v.methodName);
//			int byteIdOfBranch = BranchPool.getBytecodeIDFor(v.branchID);
			Branch b = BranchPool.getBranch(v.branchID);
			r = new BranchCoverageTestFitness(new BranchCoverageGoal(b,
			        !v.branchExpressionValue, cfg, v.className, v.methodName));
		}
		return r;
	}

	private BranchCoverageTestFitness getRootBranchTestFitness(DefUse du) {
		CFGVertex v = du.getCFGVertex();
		return new BranchCoverageTestFitness(new BranchCoverageGoal(v.className,
		        v.className + "." + v.methodName));
	}

	public ExecutionTrace getCoveringTrace() {
		return coveringTrace;
	}
	
	public String getGoalVariable() {
		return goalVariable;
	}
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		printFitnessDebugInfo(result);
		
		// known bugs:
		//	- sometimes seems not to detect when the goalDef was already overwritten at goalUsePos :( 
		//		(s. MeanTestClass Target for def in <init> with use in mean:l45)
		
		// TODO IDEA FOR AN EVO-SUITE-FEATURE: given a test(suite) for a class, check whether test achieves coverage-criterion
		
		// DONE detect which LV in which methodCall
		//		i don't think this needs to be done anymore. its not possible to cover a use in a method call for a LV without covering a definition first
		// DONE cut ExecutionTrace for branch-fitness-evaluation to Trace of current object only
		// 	subTODO: first mark MethodCalls in ExecutionTracer.finished_calls with a methodID and the current objectID
		// 			 then kick out all non-static MethodCalls for objects other then the one with current objectID
		//			 if goalDef is for a LV, also consider MethodCalls individually (OUTDATED! s. above)		
		// DONE cut ExecutionTrace for branch-fitness-evaluation to Trace of a certain path (between goalDef and overwritingDef)
		//  subTODO: in a MethodCall from the ExecutionTracer add another list holding information about the current duConter
		//			 then allow for cutting the ExecutionTrace in a way that it only holds data associated with a duCounter in a given range
		
		// DONE idea: 	once goalDef is covered, look at all duCounterPositions of goalDef (goalDefPositions):
		//				if goalUse is covered any of these goalDefPositions, return 0
		//				if not return the minimum over all branchUseFitnesses in the trace
		//				where the trace is cut between the goalDefPosition and the next overwritingDefinition
		//		turns out you don't need to cut between goalDefs and overwritingDefs
		//		all u need to do is filter out all traces where the goalDef is not active for use-fitness-calculation
		
		if(result.trace.passedUses.get(this.goalVariable) == null) {
			// trace doesnt even know the variable of this goal
			if(DEBUG) System.out.println("trace had no use-trace-entry for goalVar");
			return getMaxFitness();
		}

		// ASSUMPTION: static definitions in <clinit> are ALWAYS covered!!!
		if(goalDef == null || (goalDef.isStaticDU() && goalDef.getCFGVertex().methodName.startsWith("<clinit>"))) {
			if(DEBUG) {
				if(goalDef == null)
					System.out.println("Assume Parameter-Definition to be covered if the Parameter-Use is covered");
				else
					System.out.println("Assume definition from <clinit> to always be covered");
			}
			return normalize(useTestFitness.getFitness(individual, result));
		}
		
		HashSet<Integer> objectPool = getObjectPool(result.trace);
		
		ExecutionTrace originalTrace = result.trace;
		double fitness = getMaxFitness();

		for(Integer objectID : objectPool) {
		
			if(DEBUG) System.out.println("current object: "+objectID);
			ExecutionTrace traceForObject = originalTrace.getTraceForObject(objectID);
			
			double defFitness = calculateDefFitness(individual, result, traceForObject, objectID);
			double newFitness = 1 + normalize(defFitness);
			
			if (defFitness != 0) {
				if(DEBUG) System.out.println("Definition NOT covered on object "+objectID+" with fitness "+defFitness);
				if(newFitness<fitness)
					fitness = newFitness;
				continue;
			}
			
			if(DEBUG) {
				// definition reached on this object? -- should no longer happen
				int goalDefPos = getLastGoalDefPos(result.trace,objectID);
				if(goalDefPos == -1) {
					System.out.println("Definition NOT covered on this object but on another one");
					
					throw new IllegalStateException("Fitness on cut Trace should not have been 0");
				}
				System.out.println("Definition covered at duPos "+goalDefPos);
			}
			
			double useFitness = calculateUseFitness(individual, result, traceForObject, objectID);
			newFitness = normalize(useFitness);
			
			if(useFitness == 0) {
				if(DEBUG) System.out.println("goal COVERED by object "+objectID);
				if(DEBUG) System.out.println("===============================");
				if(objectPool.size() > 1)
					this.coveringObjectID = objectID;
				else
					this.coveringObjectID = -1;
				updateIndividual(individual, 0); // ???
				return 0;
			}
			
			if(newFitness<fitness)
				fitness = newFitness;	
		}
		
		if(DEBUG) { 
			if(fitness != 0) {
				System.out.println("goal NOT COVERED. fitness: "+fitness);
				System.out.println("===============================");
			} else
				throw new IllegalStateException("inconsistent state. this should have been detected earlier");
		}
		
		updateIndividual(individual, fitness); // ???
		
		return fitness;
	}
	
	/*		
//			int activeDefID = getActiveDefIDFor(result.trace,goalUsePos,objectID);
//			int activeDefPos = getActiveDefPosFor(result.trace,goalUsePos, objectID);
//			
//			if(goalDef.isStaticDU() && activeDefID == -1) {
//				// TODO known bug: static calls seem not to be present in all ExecutionTraces
//				//					maybe trace static calls directly in the ExecutionTracer?
//				activeDefID = getActiveDefIDFor(result.trace,goalUsePos,0); // static calls have objectID 0
//				activeDefPos = getActiveDefPosFor(result.trace,goalUsePos,0); // static calls have objectID 0
//			}
//			
//			// definition came after use
//			if (goalDefPos > goalUsePos) {
//				if(DEBUG) System.out.println("Definition came after use");
//				// TODO at this point the ExecutionTrace should be cut such that only traces remain, that came after the goalDefPos
//				if(newFitness < fitness)
//					fitness = newFitness;
//				continue;
//			}
//			//definition was overwritten
//			// TODO known bug: if goalDef gets overwritten and there is another 
//			// overwriting definition after the goalUsePos this will not be detected
//			if (activeDefID != goalDef.getDefID() && goalUsePos>activeDefPos) { 
//				if(DEBUG) System.out.println("goalDefinition no longer active at covered use");
//				if(newFitness < fitness)
//					fitness = newFitness;
//				continue;
//			}
//		}
		
//		if(fitness == 0 && !traceCoversGoal(result.trace)) {
//			System.out.println("goal got covered without traceCoversGoal() recognizing it!!! "+toString());
//			System.out.println(result.test.toCode());			
//		}

//	}
	*/
	
	private double calculateDefFitness(TestChromosome individual,
			ExecutionResult result, ExecutionTrace traceForObject,
			Integer objectID) {

		if(DEBUG) System.out.println(" === DEF-FITNESS-CALCULATION ===");
		
		// prepare trace
		ExecutionTrace originalTrace = result.trace;
		if(!goalDef.isStaticDU()) // only consider trace of current objectID
			result.trace = traceForObject;

		// calculate fitness
		double defFitness = defTestFitness.getFitness(individual, result);
		
		// revert trace changes
		result.trace = originalTrace;
		
		return defFitness;
	}

	private double calculateUseFitness(TestChromosome individual, 
			ExecutionResult result, ExecutionTrace traceForObject, 
			Integer objectID) {

		if(DEBUG) System.out.println(" === USE-FITNESS-CALCULATION ===");
		
		// prepare trace
		ExecutionTrace originalTrace = result.trace;
		ExecutionTrace fitnessTrace;
		if(!goalDef.isStaticDU()) {
			// only consider trace of current objectID
			fitnessTrace = traceForObject.getTraceForDefinition(goalDef); 
			result.trace = fitnessTrace;
		} else { 
			fitnessTrace = originalTrace.getTraceForDefinition(goalDef);
			result.trace = fitnessTrace;
		}
		if(DEBUG) System.out.println("fitnessTrace:\n"+fitnessTrace.toDefUseTraceInformation());
		
		// calculate fitness
		double useFitness = useTestFitness.getFitness(individual, result);
		
		// revert trace changes
		result.trace = originalTrace;
		
		if(useFitness == 0.0)
			this.coveringTrace = fitnessTrace;
		
		// sanity checks
		if(DEBUG) {
			System.out.println("goalUseFitness was "+useFitness);
			int goalUsePos = getLastGoalUsePos(result.trace,objectID);
			if(goalUsePos == -1) {
				if(useFitness == 0) // sanity check
					throw new IllegalStateException("expect useFitness to be >0 for cut trace if use is not covered");
				System.out.println("goalUse NOT covered "+useFitness);
			} else {
				if(useFitness != 0) // sanity check
					throw new IllegalStateException("expect useFitness to be 0 for cut trace if use is covered");
				System.out.println("goalUse covered at duPos "+goalUsePos);
			}
		}
		
		return useFitness;
	}

	private HashSet<Integer> getObjectPool(ExecutionTrace trace) {
		HashSet<Integer> objectPool = new HashSet<Integer>();
		if(trace.passedDefs.get(this.goalVariable) != null)
			objectPool.addAll(trace.passedDefs.get(this.goalVariable).keySet());
		if(goalDef.isStaticDU()) {
			// in the static case all objects have to be considered
			objectPool.addAll(trace.passedUses.get(this.goalVariable).keySet());
			if(DEBUG) System.out.println("Static-goalVariable! Using all known Objects");
		} else {
			// on non-static goalVariables only look at objects that have traces of defs and uses for the goalVariable
			int oldSize = objectPool.size();
			objectPool.retainAll(trace.passedUses.get(this.goalVariable).keySet());
			if(DEBUG) {
				System.out.println("NON-Static-goalVariable "+this.goalVariable);
				System.out.println("#unused objects: "+(oldSize-objectPool.size()));
				HashSet<Integer> discardedObjects = new HashSet<Integer>();
				discardedObjects.addAll(trace.passedDefs.get(this.goalVariable).keySet());
				discardedObjects.removeAll(trace.passedUses.get(this.goalVariable).keySet());
				for(Integer id : discardedObjects) {
					System.out.println("  discarded object "+id);
				}
			}
		}
		if(DEBUG) {
			System.out.println("#considered objects: "+objectPool.size());
			for(Integer id : objectPool) {
				System.out.println("  object "+id);
			}
			System.out.println();
		}
		return objectPool;
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

		return 200; // TODO (should be 2)
	}
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {

		individual.setFitness(fitness); // TODO ???
	}

	private void printFitnessDebugInfo(ExecutionResult result) {
		if(DEBUG) {
			System.out.println("===================================");
			System.out.println();
			System.out.println("current goal: "+toString());
			System.out.println();
			System.out.println("current test:\n"+result.test.toCode());
			System.out.println("current duTrace:\n"+result.trace.toDefUseTraceInformation());
			printFinishCalls(result);
			System.out.println();
		}
	}

	private void printFinishCalls(ExecutionResult result) {
		for(MethodCall call : result.trace.finished_calls) {
			System.out.println("Found MethodCall for: "+call.method_name+" on object "+call.callingObjectID);
			System.out.println("Active Definitions: "+call.active_definitions_trace.size());
			int i = 0;
			for(HashMap<String,Integer> activeDefs : call.active_definitions_trace) {
				System.out.println(" #"+i);
				for(String var : activeDefs.keySet()) {
					System.out.println("  Definition "+activeDefs.get(var)+" for var "+var);
				}
				i++;
			}
		}
	}	
	
	@Override
	public String toString() {
		
		StringBuffer r = new StringBuffer();
		
		r.append("DUFitness for ");
		if(goalUse.isStaticDU())
			r.append("static ");
		r.append(goalUse.getDUVariableType());
		r.append("-Variable \"" + this.goalVariable +"\"");
		if(goalDef == null) {
			r.append("\n\tParameter-Definition "+goalUse.getLocalVarNr()+" for method "+goalUse.getMethodName());
		} else {
			r.append("\n\t");
			r.append("Def ");
//			r.append(goalDef.toString());
			r.append(goalDef.getDefID() + " in " + goalDef.getMethodName()); 
			r.append(" branch " + goalDef.getBranchID() + (goalDef.getCFGVertex().branchExpressionValue?"t":"f"));
			r.append(" line "+ goalDef.getLineNumber());
		}
		
		r.append("\n\t");
		r.append("Use " + goalUse.getUseID() + " in " + goalUse.getMethodName()); 
		r.append(" branch " + goalUse.getBranchID() + (goalUse.getCFGVertex().branchExpressionValue?"t":"f"));
		r.append(" line "+ goalUse.getLineNumber());
		r.append((this.coveringObjectID!=-1?("\n\tcovered by object "+this.coveringObjectID):""));
		
		return r.toString();
	}
	
	@Override
	public boolean equals(Object o) {
//		System.out.println("called"); // TODO: somehow doesnt get called
		if(!(o instanceof DefUseCoverageTestFitness))
			return false;
		try {
			DefUseCoverageTestFitness t = (DefUseCoverageTestFitness)o;
			return t.goalDef.equals(this.goalDef) && t.goalUse.equals(this.goalUse);
		} catch(Exception e) {
			return false;
		}
	}	


//	/**
//	 * Returns the duCounterPosition of the Definition that is active in the given trace at usePos 
//	 */
//	private int getActiveDefPosFor(ExecutionTrace trace, int usePos, int objectID) {
//		if (trace.passedDefs.get(goalDef.getDUVariableName()) == null)
//			return -1;
//		
//		int lastPos = -1;
//
//		Map<Integer, Integer> defMap = trace.passedDefs.get(this.goalVariable).get(objectID);
//		if(defMap != null) {
//			for (Integer defPos : defMap.keySet()) {
//				if (defPos > usePos)
//					continue;
//				if(lastPos<defPos) {
//					lastPos = defPos;
//				}
//			}
//		}
//		return lastPos;
//	}
//	
//	/**
//	 * Returns the defID of the Definition that is active in the given trace at usePos 
//	 */
//	private int getActiveDefIDFor(ExecutionTrace trace, int usePos, int objectID) {
//		if (trace.passedDefs.get(goalDef.getDUVariableName()) == null)
//			return -1;
//		
//		int lastDef = -1;
//		int lastPos = -1;
//
//		Map<Integer, Integer> defMap = trace.passedDefs.get(this.goalVariable).get(objectID);
//		if(defMap != null) {
//			for (Integer defPos : defMap.keySet()) {
//				if (defPos > usePos)
//					continue;
//				if(lastPos<defPos) {
//					lastDef = defMap.get(defPos);
//					lastPos = defPos;
//				}
//			}
//		}
//		return lastDef;
//	}	
//	
//	/**
//	 * Only a sanity check function for testing purposes
//	 */
//	private boolean traceCoversGoal(ExecutionTrace trace) {
//		if(trace.passedUses.get(this.goalVariable)==null)
//			return false;
//		// new idea: look for all the positions of use in trace, for each check active def, if active def is goal def goal is covered
//
//		for(Integer objectID : trace.passedUses.get(this.goalVariable).keySet()) {
//			if(objectID.intValue() == 0)
//				continue;
//			ArrayList<Integer> usePositions = getGoalUsePositions(trace,objectID);
//			
//			// use not reached
//			if (usePositions.size() == 0)
//				continue;
//
//			for(Integer usePos : usePositions) {
//				
//				if (getActiveDefIDFor(trace, usePos, objectID) == goalDef.getDefID())
//					return true;
//			}
//		}
//
//		return false;
//	}
//
//	/**
//	 * Returns all the duCounterPositions of the goalUse in the given trace
//	 */
//	private ArrayList<Integer> getGoalUsePositions(ExecutionTrace trace, int objectID) {
//		
//		ArrayList<Integer> r = new ArrayList<Integer>();
//		HashMap<Integer,Integer> useMap = trace.passedUses.get(this.goalVariable).get(objectID);
//		
//		if(useMap == null)
//			return r;
//		
//		for(Integer usePos : useMap.keySet()) {
//			if(useMap.get(usePos) == goalUse.getUseID())
//				r.add(usePos);
//		}
//		
//		return r;
//	}
//	/**
//	 * Returns the last active Definition for the goalVariable in the given Trace
//	 */
//	private int getLastDef(ExecutionTrace trace, Integer objectID) {
//		if (trace.passedDefs.get(goalDef.getDUVariableName()) == null)
//			return -1;
//		
//		int lastDef = -1;
//		int lastPos = -1;
//
//		HashMap<Integer,Integer> defMap = trace.passedDefs.get(this.goalVariable).get(objectID);
//		if(defMap != null) {
//			for (Integer defPos : defMap.keySet()) {
//				if(defPos>lastPos) {
//					lastDef = defMap.get(defPos);
//					lastPos = defPos;
//				}
//			}
//		}
//		return lastDef;
//	}
//
//	/**
//	 * Returns the last active definition for the goalVariable in the given trace
//	 */
//	private int getLastDefPos(ExecutionTrace trace, Integer objectID) {
//		int lastPos = -1;
//		if (trace.passedDefs.get(this.goalVariable) == null)
//			return -1;
//		if (trace.passedDefs.get(this.goalVariable).get(objectID) == null)
//			return -1;		
//
//		for (Integer defPos : trace.passedDefs.get(this.goalVariable).get(objectID).keySet()) {
//			if(lastPos<defPos)
//				lastPos = defPos;
//		}
//		return lastPos;
//	}
}
