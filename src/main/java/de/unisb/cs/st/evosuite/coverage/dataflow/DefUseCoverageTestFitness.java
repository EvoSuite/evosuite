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

import java.text.NumberFormat;
import java.util.Set;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.coverage.statement.StatementCoverageTestFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.graphs.GraphPool;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.graphs.cfg.RawControlFlowGraph;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.utils.Randomness;

/*
 * // (0) TODO IDEA FOR AN EVO-SUITE-FEATURE: // given a test(suite) for a
 * class, check how many goals of each coverage criterion it covers // (1) DONE
 * detect which LV in which methodCall // i don't think this needs to be done
 * anymore. its not possible to cover a use in a method call for a LV without
 * covering a definition first // (2) DONE cut ExecutionTrace for
 * branch-fitness-evaluation to Trace of current object only // subTODO: first
 * mark MethodCalls in ExecutionTracer.finished_calls with a methodID and the
 * current objectID // then kick out all non-static MethodCalls for objects
 * other then the one with current objectID // if goalDef is for a LV, also
 * consider MethodCalls individually (OUTDATED! s. above) // (3) DONE cut
 * ExecutionTrace for branch-fitness-evaluation to Trace of a certain path
 * (between goalDef and overwritingDef) // subTODO: in a MethodCall from the
 * ExecutionTracer add another list holding information about the current
 * duConter // then allow for cutting the ExecutionTrace in a way that it only
 * holds data associated for duCounters in a given range // (4) DONE idea: once
 * goalDef is covered, look at all duCounterPositions of goalDef
 * (goalDefPositions): // if goalUse is covered any of these goalDefPositions,
 * return 0 // if not return the minimum over all branchUseFitness in the trace
 * // where the trace is cut between the goalDefPosition and the next
 * overwritingDefinition // turns out you don't need to cut between goalDefs and
 * overwritingDefs // all u need to do is filter out all traces where the
 * goalDef is not active for use-fitness-calculation // (5) DONE: well that
 * didn't turn out too well. you have to consider all passed definitions //
 * separately as described in (3) and (4) after all - see for example
 * MeanTestClass.mean()
 * 
 * // Other things one could/should do: (TODO-list) // - display local variable
 * names as in source code // - take different methodIds into account! // -
 * inter.method and inter.class data flow analysis - want to drop intra-part //
 * - implement DefUseCoverageSuiteFitness - DONE first rudimentary
 * implementation // - various optimizations // - DONE: for example one should
 * reuse tests that reach a certain definition or use // when looking for
 * another goal with that definition or use respectively idea: implement some
 * sort of chromosome pool for each CUT - keep track of "good" chromosomes, that
 * cover defs and uses and use them for initial population when looking for
 * goals concerning these DUs - DONE also if one would implement the above point
 * it would be very profitable to order the goals such that easy goals are
 * searched for first and for harder ones (deep in the CDG) later // - fix
 * control dependencies analysis // - implement real ReachingDefinitions
 * algorithm // - even more information in resulting tests? - cool would be to
 * mark the statements in the test that caused the covering hits to goalUse and
 * goalDef! - handle exceptions - worry about rounding errors: all that
 * normalizing is insane - should stretch the range for possible CoverageFitness
 * to something like 0-100 at least - might want to make a fitness bigger/more
 * precise then double - care for integer overflows (especially in alternative
 * fitness calculation) - DONE: private methods don't seem to be a problem at
 * all. they don't need special treatment - TODO: clean up TestSuiteGenerator! -
 * DONE: At least check for all remaining uncovered goals if they are covered by
 * the test that just covered a goal
 * 
 * - DONE: Kick out ControlDependencyTestClass-loops that take forever! - handle
 * timeouts in tests - refactor: separate DefUseCoverageGoal and
 * DefUseCoverageTestFitness - implement hashCode() functions? -
 * NoStoppingCondition .. maybe even serialize populations and resume evolving
 * later - now this is more of a theoretical one but still: - how does one find
 * the "perfect" configuration for the GA? - well build another GA to find out,
 * each chromosome is a configuration - mutation and crossovers seems very easy
 * - fitness is calculated depending on covered goals, time needed / goal,
 * consumed resources etc ... i would so love to do that :D - SearchStatistics
 * ... well ... where to start :D - i think the report is a very essential part
 * of EvoSuite - it's the way users will in the end see EvoSuite and maybe even
 * the way they interact with it (TODO? :) ) - there is so much cool stuff one
 * could do: - give a complete analysis of the CUT in terms of different
 * coverage perspectives - provide the user with the possibility to compare
 * different runs - link test run information to analysis - polish test run
 * view: - jump to different parts (fitness, tests, etc) - make things
 * "expandable" (you know hide/show all test cases with a small -/+ next to it
 * and stuff like that) - show TestCase comments - even better: link tests to
 * goals they cover, see analysis part above - mark where in a test a certain
 * goal is covered: - color lines differently depending on goal one hovers his
 * mouse above in the covered goal description - ... i could go on but i guess
 * one gets the point
 * 
 * - in order to do all that SearchStatistics should be get a complete
 * refactor-marathon-overhaul: - make distinction between HTML-generation and
 * statistics part, interlink them via .csv-files - maybe don't generate any
 * HTML at all but rather just put all relevant data in .csv-files together with
 * plots in a special directory which in turn can be visualized in all kinds of
 * ways. out the top of my head i'd say PHP would be very suited for that
 * 
 * - maybe encapsulate different HTML-generation-parts in separate classes like
 * one for Code, one for plots etc. - well just come up with a nice class model
 * is all i'm trying to say i guess - srsly though, this SearchStatistcs class
 * is a mess :D and buggy as hell too it seems - found a bug: s.
 * ExceptionTestClass: sometimes passedLine is called before enteredMethod in
 * instrumented code - so DefUseCoverage goal computation scales pretty badly,
 * why not separate goal analysis part and test creation like with -setup you
 * could run -analyze_goals or something and it would serialize the goals on
 * disc so the computed goals can be reused in later test creations ... srsly
 * analysis takes minutes on bigger CUTs! - look at
 * MultipleControlDependeciesTestClass.test(): respect that a CFGVertex can have
 * multiple branches it is control dependent on
 * 
 * 
 * things to write about: - DefUse is just awesome! - better chance of passing
 * hard branches (?) - more thorough tests - #times each branch is executed -
 * infeasible path problem - extract future work from unfinished ToDo-list ;) -
 * why alternative definition - different modes, pro and cons - average doesn't
 * take into account how many overwriting definitions there were - sum seems to
 * be the most reasonable, maybe with stretched single alternative fitness 10? -
 * harder than branch coverage - chromosomes more valuable! - see part above
 * about chromosome pool and initial population and stuff - so it makes sense to
 * recycle chromosomes - which leads to difficulty and preordering
 * 
 * Questions: - BranchCoverageGoal also treats root branches with expression
 * value true!
 */

/**
 * Evaluate fitness of a single test case with respect to one Definition-Use
 * pair
 * 
 * For more information look at the comment from method getDistance()
 * 
 * @author Andre Mis
 */
public class DefUseCoverageTestFitness extends TestFitnessFunction {

	public enum DefUsePairType {INTRA_METHOD, INTER_METHOD, INTRA_CLASS, PARAMETER};

	private static final long serialVersionUID = 1L;

	public static long singleFitnessTime = 0l;
	
	// debugging flags
	private final static boolean DEBUG = Properties.DEFUSE_DEBUG_MODE;
	private final static boolean PRINT_DEBUG = false;

	// the Definition-Use pair
	private final String goalVariable;
	private final Use goalUse;
	private final Definition goalDefinition;
	
	// TODO du-pair-type-enum
//	private final boolean isInterMethodPair;
//	private final boolean isIntraClassPair;
	private final DefUsePairType type;
	
	private final TestFitnessFunction goalDefinitionFitness;
	private final TestFitnessFunction goalUseFitness;

	private int difficulty = -1;
	public static long difficulty_time = 0l; // experiment 

	// coverage information
	private Integer coveringObjectId = -1;
	private ExecutionTrace coveringTrace;

	// constructors

	/**
	 * Creates a Definition-Use-Coverage goal for the given Definition and Use
	 */
	public DefUseCoverageTestFitness(Definition def, Use use, DefUsePairType type) {
		if(def==null)
			throw new IllegalArgumentException("null given for definition. type: "+type.toString());
		if(use==null)
			throw new IllegalArgumentException("null given for use. def was "+def.toString()+". type: "+type.toString());
		if (!def.getDUVariableName().equals(use.getDUVariableName()))
			throw new IllegalArgumentException(
			        "expect def and use to be for the same variable: \n"+def.toString()+"\n"+use.toString());
		if(def.isLocalDU() && !type.equals(DefUsePairType.INTRA_METHOD))
			throw new IllegalArgumentException("local variables can only be part of INTRA-METHOD pairs: "+type.toString()+" "+def.toString()+" "+use.toString());
		
		
		this.goalDefinition = def;
		this.goalUse = use;
		this.goalVariable = def.getDUVariableName();
		this.goalDefinitionFitness = new StatementCoverageTestFitness(goalDefinition);
		this.goalUseFitness = new StatementCoverageTestFitness(goalUse);
		
		this.type = type;
		
//		this.isInterMethodPair = isInterMethodPair;
//		this.isIntraClassPair = isIntraClassPair;
	}

	/**
	 * Used for Parameter-Uses
	 * 
	 * Creates a goal that tries to cover the given Use
	 */
	public DefUseCoverageTestFitness(Use use) {
		if (!use.isParameterUse())
			throw new IllegalArgumentException(
			        "this constructor is only for Parameter-Uses");

		goalVariable = use.getDUVariableName();
		goalDefinition = null;
		goalDefinitionFitness = null;
		goalUse = use;
		goalUseFitness = new StatementCoverageTestFitness(goalUse);
		
		this.type = DefUsePairType.PARAMETER;
		
//		isInterMethodPair = false;
//		isIntraClassPair = false;
	}

	/**
	 * Calculates the DefUseCoverage test fitness for this goal
	 * 
	 * Look at DefUseCoverageCalculations.calculateDUFitness() for more
	 * information
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		preFitnessDebugInfo(result, true);

		long start = System.currentTimeMillis();
		
		DefUseFitnessCalculator calculator = new DefUseFitnessCalculator(this, individual,
                result);
		
		double fitness = calculator.calculateDUFitness();

		if(Properties.CRITERION == Criterion.DEFUSE && fitness == 0.0)
			setCovered(individual, result.getTrace(), -1); // TODO objectId wrong
		
		postFitnessDebugInfo(individual, result, fitness);

		singleFitnessTime += System.currentTimeMillis() - start;
		
		updateIndividual(individual, fitness);
		
		return fitness;
	}

	/**
	 * Used by DefUseCoverageSuiteFitness
	 * 
	 * Simply call getFitness(TestChromosome,ExecutionResult) with a dummy
	 * TestChromosome The chromosome is used only for updateIndividual()
	 * anyways.
	 */
	public double getFitness(ExecutionResult result) {
		TestChromosome dummy = new TestChromosome();
		return getFitness(dummy, result);
	}

	/**
	 * First approximation: A DUGoal is similar to another one if the goalDef or
	 * goalUse branch of this goal is similar to the goalDef or goalUse branch
	 * of the other goal
	 * 
	 * TODO should be: Either make it configurable or choose one: - first
	 * approximation as described above - similar if goal definition or use are
	 * equal - something really fancy considering potential overwriting
	 * definitions and stuff
	 */
	@Override
	public boolean isSimilarTo(TestFitnessFunction goal) {
		return false; // disabled for now
//		if (goal instanceof BranchCoverageTestFitness) {
//			BranchCoverageTestFitness branchFitness = (BranchCoverageTestFitness) goal;
//			if (goalDefinitionBranchFitness != null
//			        && branchFitness.isSimilarTo(goalDefinitionBranchFitness))
//				return true;
//			return branchFitness.isSimilarTo(goalUseBranchFitness);
//		}
//		try {
//			DefUseCoverageTestFitness other = (DefUseCoverageTestFitness) goal;
//			if (goalDefinitionBranchFitness != null
//			        && goalDefinitionBranchFitness.isSimilarTo(other))
//				return true;
//			return goalUseBranchFitness.isSimilarTo(other);
//		} catch (ClassCastException e) {
//			return false;
//		}
	}

	/**
	 * If the goalDefinition is null, meaning the goalVariable is a
	 * Parameter-Variable this method returns the goalUseDifficulty, otherwise
	 * the product of goalUseDifficulty and goalDefinitionDicciculty is returned
	 * 
	 * Since the computation of DefUSeCoverageTestFitness difficulty takes some
	 * time the computation takes place only the first time this method is
	 * called. On later invocations this method returns the stored result from
	 * the previous computation.
	 * 
	 * consult calculateDifficulty() for more information
	 */
	@Override
	public int getDifficulty() {
		if (difficulty == -1)
			difficulty = calculateDifficulty();
		return difficulty;
	}

	/**
	 * Calculates the difficulty of this DefUseCoverage goal as follows
	 * 
	 * goalUseBranchDifficulty * goalDefinitionBranchDifficult *
	 * (1+instructionsInBetween/10) * (10*overwritingDefinitionsInBetween+1)^2
	 * 
	 * Since ordering by difficulty as it stands would result in a deterministic
	 * order in which the goals will always be searched for. Do have best of
	 * both worlds one can set the property "randomize_difficulty" to randomly
	 * multiply the deterministic difficulty by something between 0.5 and 2.0 -
	 * effectively returning something between the half and two times the
	 * difficulty
	 */
	private int calculateDifficulty() {
		long start = System.currentTimeMillis();
		int overallDifficulty = calculateUseDifficulty();
		overallDifficulty *= calculateDefinitionDifficulty();
		overallDifficulty *= (getInstructionsInBetweenDU().size() / 3) + 1;
		if (overallDifficulty <= 0.0) {
			throw new IllegalStateException("difficulty out of bounds - overflow?"
			        + overallDifficulty);
		}
		int overDefs = getPotentialOverwritingDefinitions().size();
		overallDifficulty *= Math.pow(10 * overDefs + 1, 2);
		if (overallDifficulty <= 0.0)
			throw new IllegalStateException("difficulty out of bounds - overflow? "
			        + overallDifficulty);
		difficulty_time += System.currentTimeMillis() - start;
		if (Properties.RANDOMIZE_DIFFICULTY) {
			float modifier = 1.5f * Randomness.nextFloat() + 0.5f;
			overallDifficulty = Math.round(overallDifficulty * modifier);
		}
		if (overallDifficulty <= 0.0)
			throw new IllegalStateException("difficulty out of bounds - overflow? "
			        + overallDifficulty);
		difficulty = overallDifficulty;
		return overallDifficulty;
	}

	/**
	 * Returns the goalDefinitionBranchDifficulty
	 */
	public int calculateDefinitionDifficulty() {
		return 0; // disabled for now
//		if (goalDefinitionBranchFitness == null)
//			return 1;
//		int defDifficulty = goalDefinitionBranchFitness.getDifficulty();
//		return defDifficulty;
	}

	/**
	 * Returns the goalUseBranchDifficulty
	 * 
	 */
	public int calculateUseDifficulty() {
		return 0; // disabled for now
//		int useDifficulty = goalUseBranchFitness.getDifficulty();
//		return useDifficulty;
	}

	/**
	 * Returns the definitions to the goalVaraible coming after the
	 * goalDefinition and before the goalUse in their respective methods
	 */
	public Set<BytecodeInstruction> getPotentialOverwritingDefinitions() {
		Set<BytecodeInstruction> instructionsInBetween = getInstructionsInBetweenDU();
		if (goalDefinition != null)
			return DefUseExecutionTraceAnalyzer.getOverwritingDefinitionsIn(goalDefinition,
			                                                                instructionsInBetween);
		else
			return DefUseExecutionTraceAnalyzer.getDefinitionsIn(goalVariable,
			                                                     instructionsInBetween);
	}

	/**
	 * Return a set containing all CFGVertices that occur in the complete CFG
	 * after the goalDefinition and before the goalUse.
	 * 
	 * It's pretty much the union of getInstructionsAfterGoalDefinition() and
	 * getInstructionsBeforeGoalUse(), except if the DU is in one method and the
	 * goalDefinition comes before the goalUse, then the intersection of the two
	 * sets is returned.
	 * 
	 * If the goalDefinition is a Parameter-Definition only the CFGVertices
	 * before the goalUse are considered.
	 */
	public Set<BytecodeInstruction> getInstructionsInBetweenDU() {
		Set<BytecodeInstruction> previousInstructions = getInstructionsBeforeGoalUse();
		if (goalDefinition != null) {
			Set<BytecodeInstruction> laterInstructions = getInstructionsAfterGoalDefinition();
			if (goalDefinition.getInstructionId() < goalUse.getInstructionId()
			        && goalDefinition.getMethodName().equals(goalUse.getMethodName())) {
				// they are in the same method and definition comes before use => intersect sets
				previousInstructions.retainAll(laterInstructions);
			} else {
				// otherwise take the union
				previousInstructions.addAll(laterInstructions);
			}
		}
		return previousInstructions;
	}

	/**
	 * Returns a set containing all CFGVertices in the goal definition method
	 * that come after the definition.
	 * 
	 * Look at ControlFlowGraph.getLaterInstructionInMethod() for details
	 */
	public Set<BytecodeInstruction> getInstructionsAfterGoalDefinition() {
		RawControlFlowGraph cfg = GraphPool.getRawCFG(goalDefinition.getClassName(),
		                                                       goalDefinition.getMethodName());
		BytecodeInstruction defVertex = cfg.getInstruction(goalDefinition.getInstructionId());
		Set<BytecodeInstruction> r = cfg.getLaterInstructionsInMethod(defVertex);
//		for (BytecodeInstruction v : r) {
//			v.setMethodName(goalDefinition.getMethodName());
//			v.setClassName(goalDefinition.getClassName());
//		}
		return r;
	}

	/**
	 * Returns a set containing all CFGVertices in the goal use method that come
	 * before the goal use.
	 * 
	 * Look at ControlFlowGraph.getPreviousInstructionInMethod() for details
	 */
	public Set<BytecodeInstruction> getInstructionsBeforeGoalUse() {
		RawControlFlowGraph cfg = GraphPool.getRawCFG(goalUse.getClassName(),
		                                                       goalUse.getMethodName());
		BytecodeInstruction useVertex = cfg.getInstruction(goalUse.getInstructionId());
		Set<BytecodeInstruction> r = cfg.getPreviousInstructionsInMethod(useVertex);
//		for (BytecodeInstruction v : r) {
//			v.setMethodName(goalUse.getMethodName());
//			v.setClassName(goalUse.getClassName());
//		}
		return r;
	}

	// debugging methods

	public void setCovered(Chromosome individual, ExecutionTrace trace, Integer objectId) {
		if (PRINT_DEBUG) {
			logger.debug("goal COVERED by object " + objectId);
			logger.debug("==============================================================");
		}
		this.coveringObjectId = objectId;
		updateIndividual(individual, 0);

		if (DEBUG)
			if (!DefUseFitnessCalculator.traceCoversGoal(this, individual, trace))
				throw new IllegalStateException("calculation flawed. goal wasn't covered");
	}

	private void preFitnessDebugInfo(ExecutionResult result, boolean respectPrintFlag) {
		if (PRINT_DEBUG || !respectPrintFlag) {
			System.out.println("==============================================================");
			System.out.println("current goal: " + toString());
			System.out.println("current test:");
			System.out.println(result.test.toCode());
		}
	}

	private void postFitnessDebugInfo(Chromosome individual, ExecutionResult result,
	        double fitness) {
		if (DEBUG) {
			if (fitness != 0) {
				if (PRINT_DEBUG) {
					System.out.println("goal NOT COVERED. fitness: " + fitness);
					System.out.println("==============================================================");
				}
				if(DefUseFitnessCalculator.traceCoversGoal(this, individual, result.getTrace()))
					throw new IllegalStateException("calculation flawed. goal was covered but fitness was "+fitness);
			}
		}
	}

	// 	---			Getter 		---

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

	public TestFitnessFunction getGoalUseFitness() {
		return goalUseFitness;
	}

	public TestFitnessFunction getGoalDefinitionFitness() {
		return goalDefinitionFitness;
	}
	
	public boolean isInterMethodPair() {
		return type.equals(DefUsePairType.INTER_METHOD);
	}
	
	public boolean isIntraClassPair() {
		return type.equals(DefUsePairType.INTRA_CLASS);
	}
	
	public DefUsePairType getType() {
		return type;
	}
	
	public boolean isParameterGoal() {
		return goalDefinition == null;
	}

	// ---		Inherited from Object 			---

	@Override
	public String toString() {
		StringBuffer r = new StringBuffer();
		r.append(type.toString());
		r.append("-Definition-Use-Pair");
		if (difficulty != -1)
			r.append("- Difficulty "
			        + NumberFormat.getIntegerInstance().format(difficulty));
		r.append("\n\t");
		if (isParameterGoal())
			r.append("Parameter-Definition " + goalUse.getLocalVar() + " for method "
			        + goalUse.getMethodName());
		else
			r.append(goalDefinition.toString());
		r.append("\n\t");
		r.append(goalUse.toString());
		return r.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((goalDefinition == null) ? 0 : goalDefinition.hashCode());
		result = prime * result + ((goalUse == null) ? 0 : goalUse.hashCode());
		result = prime * result
				+ ((goalVariable == null) ? 0 : goalVariable.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefUseCoverageTestFitness other = (DefUseCoverageTestFitness) obj;
		if (goalDefinition == null) {
			if (other.goalDefinition != null)
				return false;
		} else if (!goalDefinition.equals(other.goalDefinition))
			return false;
		if (goalUse == null) {
			if (other.goalUse != null)
				return false;
		} else if (!goalUse.equals(other.goalUse))
			return false;
		if (goalVariable == null) {
			if (other.goalVariable != null)
				return false;
		} else if (!goalVariable.equals(other.goalVariable))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result
//				+ ((goalDefinition == null) ? 0 : goalDefinition.hashCode());
//		result = prime * result + ((goalUse == null) ? 0 : goalUse.hashCode());
//		result = prime * result + (isInterMethodPair ? 1231 : 1237);
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		DefUseCoverageTestFitness other = (DefUseCoverageTestFitness) obj;
//		if (goalDefinition == null) {
//			if (other.goalDefinition != null)
//				return false;
//		} else if (!goalDefinition.equals(other.goalDefinition))
//			return false;
//		if (goalUse == null) {
//			if (other.goalUse != null)
//				return false;
//		} else if (!goalUse.equals(other.goalUse))
//			return false;
//		if (type != other.type)
//			return false;
//		return true;
//	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result
//				+ ((goalDefinition == null) ? 0 : goalDefinition.hashCode());
//		result = prime * result + ((goalUse == null) ? 0 : goalUse.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		DefUseCoverageTestFitness other = (DefUseCoverageTestFitness) obj;
//		if (goalDefinition == null) {
//			if (other.goalDefinition != null)
//				return false;
//		} else if (!goalDefinition.equals(other.goalDefinition))
//			return false;
//		if (goalUse == null) {
//			if (other.goalUse != null)
//				return false;
//		} else if (!goalUse.equals(other.goalUse))
//			return false;
//		return true;
//	}

	
//	@Override
//	public boolean equals(Object o) {
//		if (o == this)
//			return true;
//		if (o == null)
//			return false;
//		if (!(o instanceof DefUseCoverageTestFitness))
//			return false;
//
//		DefUseCoverageTestFitness t = (DefUseCoverageTestFitness) o;
//		if (t.goalUse.useId != this.goalUse.useId)
//			return false;
//		if (goalDefinition == null) {
//			if (t.goalDefinition == null)
//				return true;
//			else
//				return false;
//		}
//		if (t.goalDefinition == null)
//			return false;
//		
//		return t.goalDefinition.defId == this.goalDefinition.defId;
//	}

}
