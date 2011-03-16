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
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;

/**
 * Evaluate fitness of a single test case with respect to one Definition-Use pair
 * 
 * @author Andre Mis
 * 
 */
public class DefUseCoverageTestFitness extends TestFitnessFunction {

	private final static boolean DEBUG = true;	
	
	private final Use goalUse;	
	private final Definition goalDefinition;
	private final String goalVariable;
	private final BranchCoverageTestFitness definitionBranchTestFitness;
	private final BranchCoverageTestFitness useBranchTestFitness;
	private Integer coveringObjectID = -1;
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
		this.definitionBranchTestFitness = getBranchTestFitness(def);
		this.useBranchTestFitness = getBranchTestFitness(use);
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
		definitionBranchTestFitness = null;
		goalUse = use;
		useBranchTestFitness = getBranchTestFitness(use);
	}

	private BranchCoverageTestFitness getBranchTestFitness(DefUse du) {
		BranchCoverageTestFitness r;
		CFGVertex v = du.getCFGVertex();
		if (v.branchID == -1) {
			r = getRootBranchTestFitness(du);
		} else {
			ControlFlowGraph cfg = CFGMethodAdapter.getCFG(v.className, v.methodName);
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

	/**
	 * Calculates the Definition-Use-Coverage fitness for this Definition-Use-Pair on the given ExecutionResult
	 * 
	 * The fitness is calculated as follows:
	 * 
	 * If the goalDefinition is not passed in the result at all:
	 * 	 	This method returns the BranchCoverageTestFitness for the Branch that
	 * 		the CFGVertex of this goals definition is control dependent on (goalDefinitionBranch)
	 * 
	 * If the goalDefinition is passed, but the goalUse is not passed at all:
	 * 		This method returns the BranchCoverageTestFitness for the Branch that
	 * 		the CFGVertex of this goals Use is control dependent on (goalUseBranch)
	 * 
	 * If both the goalDefinition and the goalUse were passed at least once in the given result:
	 * 		If and only if at any goalUsePosition the active definition was the goalDefinition the
	 * 		Definition-Use-Pair of this goal is covered and the method returns 0
	 * 		Otherwise this method returns the minimum of the following:
	 * 			1) For all goalUsePositions the BranchCoverageTestFitness of not taking the overwritingDefinition 
	 * 			2) For all goalDefPositions the BranchCoverageTestFitness for the  goalUseBranch
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

		printFitnessDebugInfo(result);
		Set<Integer> objectPool = getObjectPool(result.trace);
		if(!DEBUG && traceCoversGoal(individual,result.trace,objectPool))
			return 0.0;
			
		// known bugs:
		//	- sometimes seems not to detect when the goalDef was already overwritten at goalUsePos :( 
		//		(s. MeanTestClass Target for def in <init> with use in mean:l45)
		// FIXED!
		
		// (0) TODO IDEA FOR AN EVO-SUITE-FEATURE: 
		//		 given a test(suite) for a class, check whether test achieves coverage-criterion
		
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
		
		// (5) TODO:	well that didn't turn out too well. you have to consider all passed definitions
		//				separately as described in (3) and (4) after all - see for example MeanTestClass.mean()
		
		if(result.trace.passedUses.get(this.goalVariable) == null) {
			// trace doesnt even know the variable of this goal
			if(DEBUG) System.out.println("trace had no use-trace-entry for goalVar");
			return getMaxFitness();
		}

		// ASSUMPTION: static definitions in <clinit> are ALWAYS covered!
		if(goalDefinition == null || (goalDefinition.isStaticDU() && goalDefinition.getCFGVertex().methodName.startsWith("<clinit>"))) {
			if(DEBUG) {
				if(goalDefinition == null)
					System.out.println("Assume Parameter-Definition to be covered if the Parameter-Use is covered");
				else
					System.out.println("Assume definition from <clinit> to always be covered");
			}
			return normalize(useBranchTestFitness.getFitness(individual, result));
		}
		
		ExecutionTrace originalTrace = result.trace;
		double fitness = getMaxFitness();

		for(Integer objectID : objectPool) {

			if(DEBUG) System.out.println("  ===  CURRENT OBJECT "+objectID+"  === ");
			
			ExecutionTrace traceForObject = originalTrace.getTraceForObject(objectID);
			if(DEBUG) System.out.println("Finish-Calls: ");
			if(DEBUG) printFinishCalls(traceForObject);
			
			double defFitness = calculateDefFitness(individual, result, traceForObject, objectID);
			double newFitness = 1 + normalize(defFitness);
			
			if (defFitness != 0) {
				if(DEBUG) System.out.println("Definition NOT covered on object "+objectID+" with fitness "+defFitness);
				if(newFitness<fitness)
					fitness = newFitness;
				continue;
			}
			
//			if(DEBUG) {
//				// definition reached on this object? -- should no longer happen
//				int goalDefPos = getLastGoalDefPos(result.trace,objectID);
//				if(goalDefPos == -1) {
//					System.out.println("Definition NOT covered on this object but on another one");
//					
//					throw new IllegalStateException("Fitness on cut Trace should not have been 0");
//				}
//				System.out.println("Definition covered at duPos "+goalDefPos);
//			}
			
			double useFitness = calculateUseFitness(individual, result, traceForObject, objectID, objectPool);
			newFitness = normalize(useFitness);
			
			if(useFitness == 0)
				return 0;
			
			if(newFitness<fitness)
				fitness = newFitness;	
		}
		if(DEBUG) { 
			if(fitness != 0) {
				System.out.println("goal NOT COVERED. fitness: "+fitness);
				System.out.println("==============================================================");
				if(traceCoversGoal(individual,result.trace,objectPool))
					throw new IllegalStateException("calculation flawed. goal was covered but fitness was "+fitness);
			} else
				throw new IllegalStateException("inconsistent state. this should have been detected earlier");
		}
		updateIndividual(individual, fitness);
		return fitness;
	}
	
	private void setCovered(Chromosome individual, ExecutionTrace trace, Integer objectID, Set<Integer> objectPool) {
		if(DEBUG) {
			System.out.println("goal COVERED by object "+objectID);
			System.out.println("==============================================================");
		}
		if(objectPool.size() > 1)
			this.coveringObjectID = objectID;
		else
			this.coveringObjectID = -1;
		updateIndividual(individual, 0);
		if(DEBUG)
			if(!traceCoversGoal(individual, trace, objectPool))
				throw new IllegalStateException("calculation is flawd! goal wasn't covered");
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
		if(!goalDefinition.isStaticDU()) // only consider trace of current objectID
			result.trace = traceForObject;
		// calculate fitness
		double defFitness = definitionBranchTestFitness.getFitness(individual, result);
		// revert trace changes
		result.trace = originalTrace;
		return defFitness;
	}

	private double calculateUseFitness(TestChromosome individual, 
			ExecutionResult result, ExecutionTrace traceForObject, 
			Integer objectID, Set<Integer> objectPool) {

		if(DEBUG) System.out.println(" === USE-FITNESS-CALCULATION ===");
		// prepare trace
		ExecutionTrace originalTrace = result.trace;
		ExecutionTrace fitnessTrace;
		if(!goalDefinition.isStaticDU()) {
			// only consider trace of current objectID
			fitnessTrace = traceForObject.getTraceForDUPair(goalDefinition,goalUse); 
			result.trace = fitnessTrace;
		} else { 
			fitnessTrace = originalTrace.getTraceForDUPair(goalDefinition,goalUse);
			result.trace = fitnessTrace;
		}
		if(DEBUG) System.out.println("fitnessTrace:\n"+fitnessTrace.toDefUseTraceInformation());
		// calculate fitness
		double useFitness = useBranchTestFitness.getFitness(individual, result);
		// revert trace changes
		result.trace = originalTrace;
		if(DEBUG) {
			System.out.println("Finish-Calls:");
			printFinishCalls(fitnessTrace);
			System.out.println("goalUseFitness was "+useFitness);
			int goalUsePos = getLastGoalUsePos(result.trace,objectID);
			if(goalUsePos == -1) {
				if(useFitness == 0) // sanity check
					throw new IllegalStateException("expect useFitness to be >0 for cut trace if use is not covered");
				System.out.println("goalUse NOT covered "+useFitness);
			} else
				System.out.println("goalUse covered at duPos "+goalUsePos);
		}
		if(useFitness == 0.0)
			setCovered(individual,fitnessTrace,objectID,objectPool);
		return useFitness;
	}

	private Set<Integer> getObjectPool(ExecutionTrace trace) {
		Set<Integer> objectPool = new HashSet<Integer>();
		if(trace.passedUses.get(this.goalVariable) == null)
			return objectPool;		
		if(trace.passedDefs.get(this.goalVariable) != null)
			objectPool.addAll(trace.passedDefs.get(this.goalVariable).keySet());
		if(goalDefinition == null || goalDefinition.isStaticDU()) {
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
				Set<Integer> discardedObjects = new HashSet<Integer>();
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
			if(defMap.get(defPos)==goalDefinition.getDefID() && lastPos<defPos)
				lastPos = defPos;
		}
		return lastPos;
	}
	
	private double getMaxFitness() {
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}

	private void printFitnessDebugInfo(ExecutionResult result) {
		if(DEBUG) {
			System.out.println("==============================================================");
			System.out.println();
			System.out.println("current goal: "+toString());
			System.out.println();
			System.out.println("current test:\n"+result.test.toCode());
			System.out.println();
		}
	}

	private void printFinishCalls(ExecutionTrace trace) {
		for(MethodCall call : trace.finished_calls) {
			if(call.branch_trace.size() != call.active_definitions_trace.size())
				throw new IllegalStateException("expect MethodCall traces to all have the same length");
			System.out.println("Found MethodCall for: "+call.method_name+" on object "+call.callingObjectID);
			System.out.println("#passed branches: "+call.branch_trace.size());
			for(int i=0;i<call.active_definitions_trace.size();i++) {
				System.out.println(i+". at Branch "+call.branch_trace.get(i)+ " true_dist: "+call.true_distance_trace.get(i)+" false_dist: "+call.false_distance_trace.get(i));
				HashMap<String,Integer> activeDefs = call.active_definitions_trace.get(i);
				System.out.println("  #activeDefs: "+activeDefs.keySet().size());
				for(String var : activeDefs.keySet()) {
					System.out.println("   Definition "+activeDefs.get(var)+" for var "+var);
				}
				System.out.println();
			}
		}
	}	
	
	


	/**
	 * Returns the duCounterPosition of the Definition that is active in the given trace at usePos 
	 */
	private int getActiveDefPosFor(ExecutionTrace trace, int usePos, int objectID) {
		if (trace.passedDefs.get(this.goalVariable) == null)
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
		if (trace.passedDefs.get(this.goalVariable) == null)
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
	 * Only a sanity check function for testing purposes
	 */
	private boolean traceCoversGoal(Chromosome individual, ExecutionTrace trace, Set<Integer> objectPool) {
		if(trace.passedUses.get(this.goalVariable)==null)
			return false;
		// new idea: look for all the positions of use in trace, for each check active definition: 
		// if active definition is goal definition goal is covered

		for(Integer objectID : objectPool) {
//			if(objectID.intValue() == 0)
//				continue;
			ArrayList<Integer> usePositions = getGoalUsePositions(trace,objectID);
			// use not reached
			if (usePositions.size() == 0)
				continue;
			if(goalUse.isParameterUse())
				return true;
			if(goalDefinition.isStaticDU() && goalDefinition.getMethodName().startsWith("<clinit>"))
				return true;
			
			for(Integer usePos : usePositions) {
				
				if (getActiveDefIDFor(trace, usePos, objectID) == goalDefinition.getDefID()) {
					if(!DEBUG)
						setCovered(individual,trace,objectID,objectPool);
					return true;
				}
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
	

	// getter-methods
	
	public ExecutionTrace getCoveringTrace() {
		return coveringTrace;
	}
	
	public String getGoalVariable() {
		return goalVariable;
	}
	
	public int getCoveringObjectID() {
		return coveringObjectID;
	}
	
	// inherited from Object
	
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
			r.append(goalDefinition.getDefID() + " in " + goalDefinition.getMethodName()+"."+goalDefinition.getBytecodeID()); 
			r.append(" branch " + goalDefinition.getBranchID() + (goalDefinition.getCFGVertex().branchExpressionValue?"t":"f"));
			r.append(" line "+ goalDefinition.getLineNumber());
		}
		
		r.append("\n\t");
		r.append("Use " + goalUse.getUseID() + " in " + goalUse.getMethodName()+"."+goalUse.getBytecodeID()); 
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
			return t.goalDefinition.equals(this.goalDefinition) && t.goalUse.equals(this.goalUse);
		} catch(Exception e) {
			return false;
		}
	}	
	
	
	
	/*
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
	*/
}
