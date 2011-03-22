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

import de.unisb.cs.st.evosuite.Properties;
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

// Other things one could/should do: (TODO-list)
//	- display local variable names as in source code
//	- take different methodIds into account! 
//	- right now there seems to be a bug when definitions at the end of a method 
//		are paired with a use at the beginning of it
//	- inter.method and inter.class data flow analysis
//	- implement DefUseCoverageSuiteFitness
//	- various optimizations
// 	- TODO: for example one should reuse tests that reach a certain definition or use 
//		when looking for another goal with that definition or use respectively
 * 		idea: implement some sort of chromosome pool for each CUT
 * 			- keep track of "good" chromosomes, that cover defs and uses and 
 * 				use them for initial population when looking for goals concerning these DUs 
//	- fix control dependencies analysis
//	- implement real ReachingDefinitions algorithm
//	- even more information in resulting tests?
 * 		- cool would be to mark the statements in the test that caused the covering hits to goalUse and goalDef!
 *  - handle exceptions
 *  - worry about rounding errors: all that normalizing is insane
 *  	- should stretch the range for possible CoverageFitness to something like 0-100 at least
 *      - might want to make a fitness bigger/more precise then double  
 *  - care for integer overflows (especially in alternative fitness calculation)
 *  - private methods?
 *  - TODO: clean up TestSuiteGenerator! 
 *  	- At least check for all remaining uncovered goals if they are covered by the goal that was just covered
 *  
 *  - DONE: Kick out ControlDependencyTestClass-loops that take forever!
 *  - handle timeouts in tests

 *  
 * things to write about:
 *  - DefUse is just awesome!
 *   - better chance of passing hard branches (?)
 *   - more thorough tests
 *   - #times each branch is executed
 *  - infeasible path problem
 *  - extract future work from unfinished todo-list ;)
 *  - why alternative definition
 *   - different modes, pro and cons
 *    - average doesn't take into account how many overwriting definitions there were
 *    - sum seems to be the most reasonable, maybe with stretched single alternative fitness 10? 
 *   -  
 *  
 *  Questions:
 *   - s. testSuite
 *   - BranchCoverageGoal also treats root branches with expression value true!
 *   - "134242 WARN  [execute] -  Thread survived - unsafe operation." ???
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
	private final static boolean PRINT_DEBUG = false;
	
	
	// the Definition-Use pair
	private final String goalVariable;
	private final Use goalUse;	
	private final Definition goalDefinition;
	private final BranchCoverageTestFitness goalDefinitionBranchFitness;
	private final BranchCoverageTestFitness goalUseBranchFitness;
	
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
		this.goalDefinitionBranchFitness = DefUseFitnessCalculations.getBranchTestFitness(def.getCFGVertex());
		this.goalUseBranchFitness = DefUseFitnessCalculations.getBranchTestFitness(use.getCFGVertex());
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
		goalDefinitionBranchFitness = null;
		goalUse = use;
		goalUseBranchFitness = DefUseFitnessCalculations.getBranchTestFitness(use.getCFGVertex());
	}


	/**
	 * Calculates the DefUseCoverage test fitness for this goal
	 * 
	 * Look at DefUseCoverageCalculations.calculateDUFitness() for more information
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		preFitnessDebugInfo(result,true);
		
		double fitness = DefUseFitnessCalculations.calculateDUFitness(this, individual, result);

		postFitnessDebugInfo(individual, result, fitness);

		return fitness;
	}
	
	/**
	 * Used by DefUseCoverageSuiteFitness
	 * 
	 * Simply call getFitness(TestChromosome,ExecutionResult) with a dummy TestChromosome
	 * The chromosome is used only for updateIndividual() anyways. 
	 */
	public double getFitness(ExecutionResult result) {
		TestChromosome dummy = new TestChromosome();
		return getFitness(dummy,result);
	}
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}
	

	// debugging methods
	
	public  void setCovered(Chromosome individual, ExecutionTrace trace, Integer objectId) {
		if(DEBUG) {
			logger.debug("goal COVERED by object "+objectId);
			logger.debug("==============================================================");
		}
		this.coveringObjectId = objectId;
		updateIndividual(individual, 0);
		
		if(DEBUG)
			if(!DefUseFitnessCalculations.traceCoversGoal(this, individual, trace))
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
				if(DefUseFitnessCalculations.traceCoversGoal(this, individual, result.trace))
					throw new IllegalStateException("calculation flawed. goal was covered but fitness was "+fitness);
			}
		}
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
	public Definition getGoalDefinition() {
		return goalDefinition;
	}
	public Use getGoalUse() {
		return goalUse;
	}
	public BranchCoverageTestFitness getGoalUseBranchFitness() {
		return goalUseBranchFitness;
	}
	public BranchCoverageTestFitness getGoalDefinitionBranchFitness() {
		return goalDefinitionBranchFitness;
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
