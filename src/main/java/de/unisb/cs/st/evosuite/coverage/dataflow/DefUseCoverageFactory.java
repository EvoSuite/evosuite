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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.cfg.RawControlFlowGraph;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author Andre Mis
 * 
 */
public class DefUseCoverageFactory extends AbstractFitnessFactory {

	private static Logger logger = LoggerFactory
			.getLogger(DefUseCoverageFactory.class);

	// TestSuiteMinimizer seems to call getCoverageGoals() a second time
	// and since analysis takes a little ...
	private static boolean called = false;
	private static List<DefUseCoverageTestFitness> duGoals;
	private static List<TestFitnessFunction> goals;

	// map of all NON-parameter-goals
	private static Map<Definition, Map<Use, DefUseCoverageTestFitness>> goalMap = new HashMap<Definition, Map<Use, DefUseCoverageTestFitness>>();

	private static int paramGoalsCount = -1;
	private static int intraGoalsCount = -1;
	private static int interGoalsCount = -1;

	public static List<DefUseCoverageTestFitness> getDUGoals() {
		if (!called)
			computeGoals();
		return duGoals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
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
		// DONTDO replace this with Reaching-Definitions-Algorithm
		long start = System.currentTimeMillis();
		logger.trace("starting DefUse-Coverage goal generation");
		duGoals = new ArrayList<DefUseCoverageTestFitness>();

		System.out.print("* Creating parameter goals...");
		duGoals.addAll(getParameterGoals());
		System.out.println(" created " + paramGoalsCount);

		System.out.print("* Creating intra-method goals...");
		duGoals.addAll(getIntraMethodPairs());
		System.out.println(" created " + intraGoalsCount);

		System.out.print("* Creating inter-method goals...");
		duGoals.addAll(getInterMethodPairs());
		System.out.println(" created " + interGoalsCount);

		called = true;
		goals = new ArrayList<TestFitnessFunction>();
		goals.addAll(duGoals);
		long end = System.currentTimeMillis();
		System.out.println("* Goal computation took: " + (end - start) + "ms");
		goalComputationTime = end - start;
	}

	public static Set<DefUseCoverageTestFitness> getInterMethodPairs() {

		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();

		// System.out.print("* Searching for clear Defs...");
		Set<Definition> freeDefs = getDefinitionsWithClearPathToMethodEnd();
		// System.out.println(" found " + freeDefs.size());

		// System.out.print("* Searching for clear Uses...");
		Set<Use> freeUses = getUsesWithClearPathFromMethodStart();
		// System.out.println(" found " + freeUses.size());

		for (Definition def : freeDefs)
			for (Use use : freeUses)
				if (def.getDUVariableName().equals(use.getDUVariableName())) {
					DefUseCoverageTestFitness newGoal = createGoal(def, use,
							true);
					if(newGoal != null)
						r.add(newGoal);
				}

		interGoalsCount = r.size();
		return r;
	}

	/**
	 * Given a definition and a use, this method creates a DefUseCoverageGoal
	 * for this DefUsePair.
	 * 
	 * 
	 * @param def
	 *            The definition of the goal
	 * @param use
	 *            The use of the goal
	 * @return The created DefUseCoverageGoal
	 */
	private static DefUseCoverageTestFitness createGoal(Definition def,
			Use use, boolean interMethod) {

		DefUseCoverageTestFitness goal = new DefUseCoverageTestFitness(def,
				use, interMethod);

		if(registerGoal(goal))
			return goal;
		else
			return null;
	}

	private static boolean registerGoal(
			DefUseCoverageTestFitness goal) {
		if (!goalMap.containsKey(goal.getGoalDefinition()))
			goalMap.put(goal.getGoalDefinition(),
					new HashMap<Use, DefUseCoverageTestFitness>());
		if (goalMap.get(goal.getGoalDefinition())
				.containsKey(goal.getGoalUse())
				&& goal.isInterMethodPair())
			// when intra-goal DUs also have free paths to method start and end
			// it can be declared both an intra-goal and an inter-goal. in this
			// case we declare the goal to be intra
			return false;

		goalMap.get(goal.getGoalDefinition()).put(goal.getGoalUse(), goal);

		return true;
	}

	public static DefUseCoverageTestFitness retrieveGoal(int defId, int useId) {

		Definition def = DefUsePool.getDefinitionByDefId(defId);
		Use use = DefUsePool.getUseByUseId(useId);

		return retrieveGoal(def, use);
	}

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
	 */
	public static Set<DefUseCoverageTestFitness> getParameterGoals() {
		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();
		Set<Use> parameterUses = DefUsePool.retrieveRegisteredParameterUses();
		for (Use use : parameterUses)
			r.add(new DefUseCoverageTestFitness(use));
		paramGoalsCount = r.size();
		logger.info("# Parameter-Uses: " + r.size());
		return r;
	}

	/**
	 * For every definition found by the CFGMethodAdapter this Method checks,
	 * what uses there are in the same method and for the same field of that
	 * definition.
	 * 
	 * If there is a definition clear path from the definition to the use, a
	 * DefUseCoverageGoal for this pair is created.
	 * 
	 * @return A list of all the DefUseCoverageGoals created this way
	 */
	public static Set<DefUseCoverageTestFitness> getIntraMethodPairs() {
		Set<DefUseCoverageTestFitness> r = new HashSet<DefUseCoverageTestFitness>();

		for (Definition def : DefUsePool.retrieveRegisteredDefinitions()) {

			String className = def.getClassName();
			String methodName = def.getMethodName();

			RawControlFlowGraph cfg = CFGPool.getRawCFG(className, methodName);
			if (cfg == null)
				throw new IllegalStateException("Expect CFG to exist for "
						+ methodName);

			Set<Use> uses = cfg.getUsesForDef(def);
			logger.debug("Found " + uses.size() + " Uses for Def "
					+ def.getDefId() + " in " + def.getMethodName());

			for (Use use : uses) {
				DefUseCoverageTestFitness newGoal = createGoal(def, DefUsePool.getUseByDefUseId(use
						.getDefUseId()), false);
				if(newGoal == null)
					throw new IllegalStateException("expect intra-method-pair creation to always succeed");
				r.add(newGoal);
			}
		}
		intraGoalsCount = r.size();
		logger.info("# DU-Pairs within methods: " + r.size());
		return r;
	}

	/**
	 * For every definition found by the CFGMethodAdapter this Method checks, if
	 * there is a definition clear path from that definition to an exit of its
	 * method.
	 * 
	 * @return A Set of all the definitions for which the above holds
	 */
	public static Set<Definition> getDefinitionsWithClearPathToMethodEnd() {
		HashSet<Definition> r = new HashSet<Definition>();
		for (Definition def : DefUsePool.retrieveRegisteredDefinitions()) {

			String className = def.getClassName();
			String methodName = def.getMethodName();

			RawControlFlowGraph cfg = CFGPool.getRawCFG(className, methodName);
			if (cfg == null)
				throw new IllegalStateException("Expect CFG to exist for "
						+ methodName);

			if (cfg.hasDefClearPathToMethodExit(def))
				r.add(def);
			else
				logger.debug("no defclearpath to method end for Def "
						+ def.getDefId());
		}

		logger.info("# Definitions with clear path to method exit " + r.size());
		return r;
	}

	/**
	 * For every use found by the CFGMethodAdapter this method checks, if there
	 * is a definition clear path from an entry of the uses method to the use
	 * itself.
	 * 
	 * @return A Set of all the uses for which the above holds
	 */
	public static Set<Use> getUsesWithClearPathFromMethodStart() {
		Set<Use> r = new HashSet<Use>();

		Set<Use> allUses = DefUsePool.retrieveRegisteredUses();
		for (Use use : allUses) {
			RawControlFlowGraph cfg = CFGPool.getRawCFG(use.getClassName(), use
					.getMethodName());
			if (cfg == null)
				throw new IllegalStateException("no cfg for method "
						+ use.getMethodName());
			if (cfg.hasDefClearPathFromMethodEntry(use))
				r.add(use);
			else
				logger.debug("no defclearpath from method start for Use "
						+ use.getUseId());
		}
		logger.info("# Uses with clear path from method entry " + r.size());
		return r;
	}

	// Getter

	public static int getParamGoalsCount() {
		return paramGoalsCount;
	}

	public static int getIntraGoalsCount() {
		return intraGoalsCount;
	}

	public static int getInterGoalsCount() {
		return interGoalsCount;
	}

}
