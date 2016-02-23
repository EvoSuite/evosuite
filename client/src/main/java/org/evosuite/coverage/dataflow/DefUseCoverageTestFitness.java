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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.statement.StatementCoverageTestFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.utils.ArrayUtil;

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

	public enum DefUsePairType {
		INTRA_METHOD, INTER_METHOD, INTRA_CLASS, PARAMETER
	};

	private static final long serialVersionUID = 1L;

	/** Constant <code>singleFitnessTime=0l</code> */
	public static long singleFitnessTime = 0l;

	// debugging flags
	private final static boolean DEBUG = Properties.DEFUSE_DEBUG_MODE;
	private final static boolean PRINT_DEBUG = false;

	// the Definition-Use pair
	private String goalVariable;
	private transient Use goalUse;
	private transient Definition goalDefinition;

	private DefUsePairType type;

	private TestFitnessFunction goalDefinitionFitness;
	private TestFitnessFunction goalUseFitness;

	// coverage information
	private Integer coveringObjectId = -1;
	private transient ExecutionTrace coveringTrace = null;
	private boolean covered = false;

	// constructors

	/**
	 * Creates a Definition-Use-Coverage goal for the given Definition and Use
	 * 
	 * @param def
	 *            a {@link org.evosuite.coverage.dataflow.Definition} object.
	 * @param use
	 *            a {@link org.evosuite.coverage.dataflow.Use} object.
	 * @param type
	 *            a
	 *            {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType}
	 *            object.
	 */
	public DefUseCoverageTestFitness(Definition def, Use use, DefUsePairType type) {
		if (def == null)
			throw new IllegalArgumentException("null given for definition. type: "
			        + type.toString());
		if (use == null)
			throw new IllegalArgumentException("null given for use. def was "
			        + def.toString() + ". type: " + type.toString());

		initRegularDefUse(def, use, type);
	}

	/**
	 * Used for Parameter-Uses
	 * 
	 * Creates a goal that tries to cover the given Use
	 * 
	 * @param use
	 *            a {@link org.evosuite.coverage.dataflow.Use} object.
	 */
	public DefUseCoverageTestFitness(Use use) {
		if (!use.isParameterUse())
			throw new IllegalArgumentException(
			        "this constructor is only for Parameter-Uses");

		initParameterUse(use);
	}

	private void initRegularDefUse(Definition def, Use use, DefUsePairType type) {
		//if (!def.getVariableName().equals(use.getVariableName()))
		//	throw new IllegalArgumentException(
		//	        "expect def and use to be for the same variable: \n" + def.toString()
		//	                + "\n" + use.toString());
		if (def.isLocalDU() && !type.equals(DefUsePairType.INTRA_METHOD))
			throw new IllegalArgumentException(
			        "local variables can only be part of INTRA-METHOD pairs: \ntype:"
			                + type.toString() + "\ndef:" + def.toString() + "\nuse:"
			                + use.toString());

		this.goalDefinition = def;
		this.goalUse = use;
		this.goalVariable = def.getVariableName();
		this.goalDefinitionFitness = new StatementCoverageTestFitness(goalDefinition);
		this.goalUseFitness = new StatementCoverageTestFitness(goalUse);

		this.type = type;
	}

	private void initParameterUse(Use use) {
		goalVariable = use.getVariableName();
		goalDefinition = null;
		goalDefinitionFitness = null;
		goalUse = use;
		goalUseFitness = new StatementCoverageTestFitness(goalUse);

		this.type = DefUsePairType.PARAMETER;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Calculates the DefUseCoverage test fitness for this goal
	 * 
	 * Look at DefUseCoverageCalculations.calculateDUFitness() for more
	 * information
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		preFitnessDebugInfo(result, true);

		long start = System.currentTimeMillis();

		DefUseFitnessCalculator calculator = new DefUseFitnessCalculator(this,
		        individual, result);

		double fitness = calculator.calculateDUFitness();

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE) && fitness == 0.0)
			setCovered(individual, result.getTrace(), -1); // TODO objectId wrong

		postFitnessDebugInfo(individual, result, fitness);

		singleFitnessTime += System.currentTimeMillis() - start;

		updateIndividual(this, individual, fitness);

		return fitness;
	}

	/**
	 * Used by DefUseCoverageSuiteFitness
	 * 
	 * Simply call getFitness(TestChromosome,ExecutionResult) with a dummy
	 * TestChromosome The chromosome is used only for updateIndividual()
	 * anyways.
	 * 
	 * @param result
	 *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 * @return a double.
	 */
	public double getFitness(ExecutionResult result) {
		TestChromosome dummy = new TestChromosome();
		return getFitness(dummy, result);
	}

	public boolean isAlias() {
		return goalDefinition != null ? !goalUse.getVariableName().equals(goalDefinition.getVariableName())
		        : false;
	}

	/**
	 * Returns the definitions to the goalVaraible coming after the
	 * goalDefinition and before the goalUse in their respective methods
	 * 
	 * @return a {@link java.util.Set} object.
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
	 * 
	 * @return a {@link java.util.Set} object.
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
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<BytecodeInstruction> getInstructionsAfterGoalDefinition() {
		RawControlFlowGraph cfg = GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getRawCFG(goalDefinition.getClassName(),
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
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public Set<BytecodeInstruction> getInstructionsBeforeGoalUse() {
		RawControlFlowGraph cfg = GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getRawCFG(goalUse.getClassName(),
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

	/**
	 * <p>
	 * setCovered
	 * </p>
	 * 
	 * @param individual
	 *            a {@link org.evosuite.ga.Chromosome} object.
	 * @param trace
	 *            a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
	 * @param objectId
	 *            a {@link java.lang.Integer} object.
	 */
	public void setCovered(TestChromosome individual, ExecutionTrace trace,
	        Integer objectId) {
		if (PRINT_DEBUG) {
			logger.debug("goal COVERED by object " + objectId);
			logger.debug("==============================================================");
		}
		this.coveringObjectId = objectId;
		updateIndividual(this, individual, 0);

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
				if (DefUseFitnessCalculator.traceCoversGoal(this, individual,
				                                            result.getTrace()))
					throw new IllegalStateException(
					        "calculation flawed. goal was covered but fitness was "
					                + fitness);
			}
		}
	}

	// 	---			Getter 		---

	/**
	 * <p>
	 * Getter for the field <code>coveringTrace</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
	 */
	public ExecutionTrace getCoveringTrace() {
		return coveringTrace;
	}

	/**
	 * <p>
	 * Getter for the field <code>goalVariable</code>.
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getGoalVariable() {
		return goalVariable;
	}

	/**
	 * <p>
	 * Getter for the field <code>coveringObjectId</code>.
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getCoveringObjectId() {
		return coveringObjectId;
	}

	/**
	 * <p>
	 * Getter for the field <code>goalDefinition</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.dataflow.Definition} object.
	 */
	public Definition getGoalDefinition() {
		return goalDefinition;
	}

	/**
	 * <p>
	 * Getter for the field <code>goalUse</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.dataflow.Use} object.
	 */
	public Use getGoalUse() {
		return goalUse;
	}

	/**
	 * <p>
	 * Getter for the field <code>goalUseFitness</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.TestFitnessFunction} object.
	 */
	public TestFitnessFunction getGoalUseFitness() {
		return goalUseFitness;
	}

	/**
	 * <p>
	 * Getter for the field <code>goalDefinitionFitness</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testcase.TestFitnessFunction} object.
	 */
	public TestFitnessFunction getGoalDefinitionFitness() {
		return goalDefinitionFitness;
	}

	/**
	 * <p>
	 * isInterMethodPair
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isInterMethodPair() {
		return type.equals(DefUsePairType.INTER_METHOD);
	}

	/**
	 * <p>
	 * isIntraClassPair
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isIntraClassPair() {
		return type.equals(DefUsePairType.INTRA_CLASS);
	}

	/**
	 * <p>
	 * Getter for the field <code>type</code>.
	 * </p>
	 * 
	 * @return a
	 *         {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType}
	 *         object.
	 */
	public DefUsePairType getType() {
		return type;
	}

	/**
	 * <p>
	 * isParameterGoal
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean isParameterGoal() {
		return goalDefinition == null;
	}

	// ---		Inherited from Object 			---

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer r = new StringBuffer();
		r.append(type.toString());
		r.append("-Definition-Use-Pair");
		r.append("\n\t");
		if (isParameterGoal())
			r.append("Parameter-Definition " + goalUse.getLocalVariableSlot()
			        + " for method " + goalUse.getMethodName());
		else
			r.append(goalDefinition.toString());
		r.append("\n\t");
		r.append(goalUse.toString());
		return r.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
		        + ((goalDefinition == null) ? 0 : goalDefinition.hashCode());
		result = prime * result + ((goalUse == null) ? 0 : goalUse.hashCode());
		result = prime * result + ((goalVariable == null) ? 0 : goalVariable.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/** {@inheritDoc} */
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

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof DefUseCoverageTestFitness) {
			DefUseCoverageTestFitness otherFitness = (DefUseCoverageTestFitness) other;
			// goalDefinition can be null for parameter goals
			if (goalDefinition == null || otherFitness.getGoalDefinition() == null)
				return goalUse.compareTo(otherFitness.getGoalUse());
			if (goalDefinition.compareTo(otherFitness.getGoalDefinition()) == 0) {
				return goalUse.compareTo(otherFitness.getGoalUse());
			} else {
				return goalDefinition.compareTo(otherFitness.getGoalDefinition());
			}
		}
		return compareClassName(other);
	}

	/**
	 * @return the covered
	 */
	public boolean isCovered() {
		return covered;
	}

	/**
	 * @param covered
	 *            the covered to set
	 */
	public void setCovered(boolean covered) {
		this.covered = covered;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return goalUse.getClassName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return goalUse.getMethodName();
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		DefUsePairType type = (DefUsePairType) ois.readObject();
		Integer useId = (Integer) ois.readObject();
		Integer defId = (Integer) ois.readObject();
		Use use = DefUsePool.getUseByUseId(useId);

		if (type == DefUsePairType.PARAMETER) {
			initParameterUse(use);
		} else {
			Definition def = DefUsePool.getDefinitionByDefId(defId);
			initRegularDefUse(def, use, type);
		}
	}

	/**
	 * Serialize, but need to abstract classloader away
	 * 
	 * @param oos
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(type);
		oos.writeObject(Integer.valueOf(goalUse.useId));
		if (goalDefinition != null)
			oos.writeObject(Integer.valueOf(goalDefinition.defId));
		else
			oos.writeObject(0);
	}
}
