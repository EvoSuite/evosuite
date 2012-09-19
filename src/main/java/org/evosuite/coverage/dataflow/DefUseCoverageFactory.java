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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.ccfg.ClassControlFlowGraph;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * DefUseCoverageFactory class.
 * </p>
 * 
 * @author Andre Mis
 */
public class DefUseCoverageFactory extends AbstractFitnessFactory {

	private static Logger logger = LoggerFactory.getLogger(DefUseCoverageFactory.class);

	// TestSuiteMinimizer seems to call getCoverageGoals() a second time
	// and since analysis takes a little ...
	private static boolean called = false;
	private static List<DefUseCoverageTestFitness> duGoals;
	private static List<TestFitnessFunction> goals;

	// map of all NON-parameter-goals
	private static Map<Definition, Map<Use, DefUseCoverageTestFitness>> goalMap = new HashMap<Definition, Map<Use, DefUseCoverageTestFitness>>();

	private static Map<DefUseCoverageTestFitness.DefUsePairType, Integer> goalCounts = new HashMap<DefUseCoverageTestFitness.DefUsePairType, Integer>();

	/**
	 * <p>
	 * getDUGoals
	 * </p>
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public static List<DefUseCoverageTestFitness> getDUGoals() {
		if (!called)
			computeGoals();
		return duGoals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		if (!called)
			computeGoals();

		return goals;
	}

	/**
	 * Determines all goals that need to get covered in order to fulfill
	 * DefUseCoverage
	 * 
	 * Those are the following: - for each parameterUse this method creates a
	 * goal trying to cover i - for each duPair with a definition clear path
	 * inside the methods of the CUT a goal is created - for each definition in
	 * the CUT with a clear path to an exit of its method and each use with a
	 * clear path from its methods entry a goal is created
	 */
	public static void computeGoals() {

		categorizeFieldMethodCalls();

		// TODO remove the following lines once purity analysis is implemented
		// they are just for testing purposes
		for (String methodInCCFG : GraphPool.getInstance(TestCluster.classLoader).getRawCFGs(Properties.TARGET_CLASS).keySet()) {
			if (GraphPool.getInstance(TestCluster.classLoader).getCCFG(Properties.TARGET_CLASS).isPure(methodInCCFG))
				LoggingUtils.getEvoLogger().info("PURE method:\t" + methodInCCFG);
			else
				LoggingUtils.getEvoLogger().info("IMPURE method:\t" + methodInCCFG);
		}

		long start = System.currentTimeMillis();
		logger.trace("starting DefUse-Coverage goal generation");
		duGoals = new ArrayList<DefUseCoverageTestFitness>();

		System.out.println("* Creating DefUse-Pairs from CCFG...");
		duGoals.addAll(getCCFGPairs());
		System.out.println("..created " + getIntraMethodGoalsCount() + " intra-method-, "
		        + getInterMethodGoalsCount() + " inter-method- and "
		        + getIntraClassGoalsCount() + " intra-class-pairs");

		System.out.print("* Creating parameter goals...");
		duGoals.addAll(getParameterGoals());
		System.out.println(" created " + getParamGoalsCount() + " parameter goals");

		called = true;
		goals = new ArrayList<TestFitnessFunction>();
		goals.addAll(duGoals);
		long end = System.currentTimeMillis();
		goalComputationTime = end - start;
		System.out.println("* Goal computation took: " + goalComputationTime + "ms");
	}

	/**
	 * Determines for all method calls on fields of the CUT whether the call is
	 * to a pure or impure method. For these calls Uses and Definitions are
	 * created respectively.
	 * 
	 * Since purity analysis is used here and requires all classes along the
	 * call tree to be completely analyzed this part of the CUT analysis can not
	 * be done in the CFGMethodAdapter like the rest of it.
	 * 
	 * WORK IN PROGRESS
	 */
	private static void categorizeFieldMethodCalls() {
		Set<BytecodeInstruction> fieldMethodCalls = DefUsePool.retrieveFieldMethodCalls();

		LoggingUtils.getEvoLogger().info("Categorizing field method calls: "
		                                         + fieldMethodCalls.size());

		for (BytecodeInstruction fieldMethodCall : fieldMethodCalls) {
			if (!GraphPool.getInstance(TestCluster.classLoader).canMakeCCFGForClass(fieldMethodCall.getCalledMethodsClass())) {
				// classes in java.* can not be analyzed for purity yet. for now just ignore them
				continue;
			}

			ClassControlFlowGraph ccfg = GraphPool.getInstance(TestCluster.classLoader).getCCFG(fieldMethodCall.getCalledMethodsClass());
			if (ccfg.isPure(fieldMethodCall.getCalledMethod())) {
				if (!DefUsePool.addAsUse(fieldMethodCall))
					throw new IllegalStateException(
					        "unable to register field method call as a use "
					                + fieldMethodCall.toString());
			} else {
				if (!DefUsePool.addAsDefinition(fieldMethodCall))
					throw new IllegalStateException(
					        "unable to register field method call as a definition "
					                + fieldMethodCall.toString());
			}
		}
	}

	private static Set<DefUseCoverageTestFitness> getCCFGPairs() {
		ClassControlFlowGraph ccfg = GraphPool.getInstance(TestCluster.classLoader).getCCFG(Properties.TARGET_CLASS);
		Set<DefUseCoverageTestFitness> r = ccfg.determineDefUsePairs();

		return r;
	}

	/**
	 * Given a definition and a use, this method creates a DefUseCoverageGoal
	 * for this DefUsePair.
	 * 
	 * @param def
	 *            The definition of the goal
	 * @param use
	 *            The use of the goal
	 * @return The created DefUseCoverageGoal
	 * @param type
	 *            a
	 *            {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType}
	 *            object.
	 */
	public static DefUseCoverageTestFitness createGoal(Definition def, Use use,
	        DefUseCoverageTestFitness.DefUsePairType type) {

		DefUseCoverageTestFitness goal = new DefUseCoverageTestFitness(def, use, type);

		if (registerGoal(goal))
			return goal;
		else {
			// System.out.println("Discarding goal: "+goal.toString());
			return null;
		}
	}

	/**
	 * Convenience method that retrieves the Definition and Use object for the
	 * given BytecodeInstructions from the DefUsePool and calls
	 * createGoal(Definition,Use)
	 * 
	 * @param def
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @param use
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @param type
	 *            a
	 *            {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType}
	 *            object.
	 * @return a
	 *         {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness}
	 *         object.
	 */
	public static DefUseCoverageTestFitness createGoal(BytecodeInstruction def,
	        BytecodeInstruction use, DefUseCoverageTestFitness.DefUsePairType type) {
		if (def == null)
			throw new IllegalArgumentException("null given as def");
		if (use == null)
			throw new IllegalArgumentException("null given as use");

		Definition definition = DefUsePool.getDefinitionByInstruction(def);
		Use usee = DefUsePool.getUseByInstruction(use);
		if (definition == null || usee == null) // can happen in (very, very)
		                                        // weird cases, ignoring that
		                                        // for now
			return null;

		return createGoal(definition, usee, type);
	}

	private static boolean registerGoal(DefUseCoverageTestFitness goal) {
		if (!goalMap.containsKey(goal.getGoalDefinition()))
			goalMap.put(goal.getGoalDefinition(),
			            new HashMap<Use, DefUseCoverageTestFitness>());
		if (goalMap.get(goal.getGoalDefinition()).containsKey(goal.getGoalUse()) /* && goal.isInterMethodPair() */)
			// when intra-goal DUs also have free paths to method start and end
			// it can be declared both an intra-goal and an inter-goal. in this
			// case we declare the goal to be intra
			return false;

		goalMap.get(goal.getGoalDefinition()).put(goal.getGoalUse(), goal);
		countGoal(goal);
		return true;
	}

	private static void countGoal(DefUseCoverageTestFitness goal) {
		if (goalCounts.get(goal.getType()) == null)
			goalCounts.put(goal.getType(), 0);
		goalCounts.put(goal.getType(), goalCounts.get(goal.getType()) + 1);

		LoggingUtils.getEvoLogger().info(goal.toString());
	}

	/**
	 * <p>
	 * retrieveGoal
	 * </p>
	 * 
	 * @param defId
	 *            a int.
	 * @param useId
	 *            a int.
	 * @return a
	 *         {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness}
	 *         object.
	 */
	public static DefUseCoverageTestFitness retrieveGoal(int defId, int useId) {

		Definition def = DefUsePool.getDefinitionByDefId(defId);
		Use use = DefUsePool.getUseByUseId(useId);

		return retrieveGoal(def, use);
	}

	/**
	 * <p>
	 * retrieveGoal
	 * </p>
	 * 
	 * @param def
	 *            a {@link org.evosuite.coverage.dataflow.Definition} object.
	 * @param use
	 *            a {@link org.evosuite.coverage.dataflow.Use} object.
	 * @return a
	 *         {@link org.evosuite.coverage.dataflow.DefUseCoverageTestFitness}
	 *         object.
	 */
	public static DefUseCoverageTestFitness retrieveGoal(Definition def, Use use) {
		if (!goalMap.containsKey(def))
			return null;
		if (!goalMap.get(def).containsKey(use))
			return null;

		return goalMap.get(def).get(use);
	}

	/**
	 * For each parameterUse in the CUT this method creates a
	 * DefUseCoverageTestFitness that tries to cover that use
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<DefUseCoverageTestFitness> getParameterGoals() {
		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();
		Set<Use> parameterUses = DefUsePool.retrieveRegisteredParameterUses();
		for (Use use : parameterUses) {
			DefUseCoverageTestFitness goal = new DefUseCoverageTestFitness(use);
			r.add(goal);
			countGoal(goal);
		}
		// paramGoalsCount = r.size();
		logger.info("# Parameter-Uses: " + r.size());
		return r;
	}

	/**
	 * <p>
	 * getRegsiteredDefinitions
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<Definition> getRegsiteredDefinitions() {
		if (!called)
			computeGoals();
		return new HashSet<Definition>(goalMap.keySet());
	}

	/**
	 * <p>
	 * getRegisteredGoalsForDefinition
	 * </p>
	 * 
	 * @param def
	 *            a {@link org.evosuite.coverage.dataflow.Definition} object.
	 * @return a {@link java.util.Map} object.
	 */
	public static Map<Use, DefUseCoverageTestFitness> getRegisteredGoalsForDefinition(
	        Definition def) {
		if (!called)
			computeGoals();
		return goalMap.get(def);
	}

	// Getter

	/**
	 * <p>
	 * getParamGoalsCount
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int getParamGoalsCount() {
		Integer r = goalCounts.get(DefUsePairType.PARAMETER);
		if (r == null)
			return 0;
		return r;
	}

	/**
	 * <p>
	 * getIntraMethodGoalsCount
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int getIntraMethodGoalsCount() {
		Integer r = goalCounts.get(DefUsePairType.INTRA_METHOD);
		if (r == null)
			return 0;
		return r;
	}

	/**
	 * <p>
	 * getInterMethodGoalsCount
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int getInterMethodGoalsCount() {
		Integer r = goalCounts.get(DefUsePairType.INTER_METHOD);
		if (r == null)
			return 0;
		return r;
	}

	/**
	 * <p>
	 * getIntraClassGoalsCount
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int getIntraClassGoalsCount() {
		Integer r = goalCounts.get(DefUsePairType.INTRA_CLASS);
		if (r == null)
			return 0;
		return r;
	}

	// public static Set<DefUseCoverageTestFitness> getInterMethodPairs() {
	//
	// Set<DefUseCoverageTestFitness> r = new
	// HashSet<DefUseCoverageTestFitness>();
	//
	// // System.out.print("* Searching for clear Defs...");
	// Set<Definition> freeDefs = getDefinitionsWithClearPathToMethodEnd();
	// // System.out.println(" found " + freeDefs.size());
	//
	// // System.out.print("* Searching for clear Uses...");
	// Set<Use> freeUses = getUsesWithClearPathFromMethodStart();
	// // System.out.println(" found " + freeUses.size());
	//
	// for (Definition def : freeDefs)
	// for (Use use : freeUses)
	// if (def.getDUVariableName().equals(use.getDUVariableName())) {
	// DefUseCoverageTestFitness newGoal = createGoal(def, use,
	// DefUseCoverageTestFitness.DefUsePairType.INTRA_CLASS);
	// if (newGoal != null)
	// r.add(newGoal);
	// }
	//
	// // interGoalsCount = r.size();
	// return r;
	// }

	// /**
	// * For every definition found by the CFGMethodAdapter this Method checks,
	// * what uses there are in the same method and for the same field of that
	// * definition.
	// *
	// * If there is a definition clear path from the definition to the use, a
	// * DefUseCoverageGoal for this pair is created.
	// *
	// * @return A list of all the DefUseCoverageGoals created this way
	// */
	// public static Set<DefUseCoverageTestFitness> getIntraMethodPairs() {
	// Set<DefUseCoverageTestFitness> r = new
	// HashSet<DefUseCoverageTestFitness>();
	//
	// for (Definition def : DefUsePool.retrieveRegisteredDefinitions()) {
	//
	// String className = def.getClassName();
	// String methodName = def.getMethodName();
	//
	// RawControlFlowGraph cfg = GraphPool
	// .getRawCFG(className, methodName);
	// if (cfg == null)
	// throw new IllegalStateException("Expect CFG to exist for "
	// + methodName);
	//
	// Set<Use> uses = cfg.getUsesForDef(def);
	// logger.debug("Found " + uses.size() + " Uses for Def "
	// + def.getDefId() + " in " + def.getMethodName());
	//
	// for (Use use : uses) {
	// DefUseCoverageTestFitness newGoal = createGoal(def,
	// DefUsePool.getUseByDefUseId(use.getDefUseId()),
	// DefUseCoverageTestFitness.DefUsePairType.INTRA_METHOD);
	// if (newGoal == null)
	// throw new IllegalStateException(
	// "expect intra-method-pair creation to always succeed");
	// r.add(newGoal);
	// }
	// }
	// // intraMethodGoalsCount = r.size();
	// logger.info("# DU-Pairs within methods: " + r.size());
	// return r;
	// }
	//
	// /**
	// * For every definition found by the CFGMethodAdapter this Method checks,
	// if
	// * there is a definition clear path from that definition to an exit of its
	// * method.
	// *
	// * @return A Set of all the definitions for which the above holds
	// */
	// public static Set<Definition> getDefinitionsWithClearPathToMethodEnd() {
	// HashSet<Definition> r = new HashSet<Definition>();
	// for (Definition def : DefUsePool.retrieveRegisteredDefinitions()) {
	//
	// String className = def.getClassName();
	// String methodName = def.getMethodName();
	//
	// RawControlFlowGraph cfg = GraphPool
	// .getRawCFG(className, methodName);
	// if (cfg == null)
	// throw new IllegalStateException("Expect CFG to exist for "
	// + methodName);
	//
	// if (cfg.hasDefClearPathToMethodExit(def))
	// r.add(def);
	// else
	// logger.debug("no defclearpath to method end for Def "
	// + def.getDefId());
	// }
	//
	// logger.info("# Definitions with clear path to method exit " + r.size());
	// return r;
	// }
	//
	// /**
	// * For every use found by the CFGMethodAdapter this method checks, if
	// there
	// * is a definition clear path from an entry of the uses method to the use
	// * itself.
	// *
	// * @return A Set of all the uses for which the above holds
	// */
	// public static Set<Use> getUsesWithClearPathFromMethodStart() {
	// Set<Use> r = new HashSet<Use>();
	//
	// Set<Use> allUses = DefUsePool.retrieveRegisteredUses();
	// for (Use use : allUses) {
	// RawControlFlowGraph cfg = GraphPool.getRawCFG(use.getClassName(),
	// use.getMethodName());
	// if (cfg == null)
	// throw new IllegalStateException("no cfg for method "
	// + use.getMethodName());
	// if (cfg.hasDefClearPathFromMethodEntry(use))
	// r.add(use);
	// else
	// logger.debug("no defclearpath from method start for Use "
	// + use.getUseId());
	// }
	// logger.info("# Uses with clear path from method entry " + r.size());
	// return r;
	// }

}
