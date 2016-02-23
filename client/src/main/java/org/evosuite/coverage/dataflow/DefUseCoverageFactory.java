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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness.DefUsePairType;
import org.evosuite.coverage.dataflow.analysis.AllUsesAnalysis;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.ccfg.ClassControlFlowGraph;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.JdkPureMethodsList;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * DefUseCoverageFactory class.
 * </p>
 * 
 * @author Andre Mis
 */
public class DefUseCoverageFactory extends
        AbstractFitnessFactory<DefUseCoverageTestFitness> {

	private static final Logger logger = LoggerFactory.getLogger(DefUseCoverageFactory.class);

	// TestSuiteMinimizer seems to call getCoverageGoals() a second time
	// and since analysis takes a little ...
	private static boolean called = false;
	private static List<DefUseCoverageTestFitness> duGoals; // TODO: What's the difference to goals?
	private static List<DefUseCoverageTestFitness> goals;

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
	public List<DefUseCoverageTestFitness> getCoverageGoals() {
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
		

		// XXX testing purposes
		/*for(String methodInCCFG : GraphPool.getInstance(TestGenerationContext.getClassLoader()).getRawCFGs(Properties.TARGET_CLASS).keySet()) {
			if(GraphPool.getInstance(TestGenerationContext.getClassLoader()).getCCFG(Properties.TARGET_CLASS).isPure(methodInCCFG))
				LoggingUtils.getEvoLogger().debug("PURE method:\t"+methodInCCFG);
			else
				LoggingUtils.getEvoLogger().debug("IMPURE method:\t"+methodInCCFG);
		} */

		long start = System.currentTimeMillis();
		LoggingUtils.getEvoLogger().info("starting DefUse-Coverage goal generation");
		duGoals = new ArrayList<DefUseCoverageTestFitness>();
		if(!GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).canMakeCCFGForClass(Properties.TARGET_CLASS)) {
			goals = new ArrayList<DefUseCoverageTestFitness>();
			logger.info("Have no CFGs, is this an interface?");
			return;
		}

		LoggingUtils.getEvoLogger().info("* Creating DefUse-Pairs from CCFG...");
		duGoals.addAll(getCCFGPairs());
		LoggingUtils.getEvoLogger().info("  ..created " + getIntraMethodGoalsCount()
		                                         + " intra-method-, "
		                                         + getInterMethodGoalsCount()
		                                         + " inter-method- and "
		                                         + getIntraClassGoalsCount()
		                                         + " intra-class-pairs");

		LoggingUtils.getEvoLogger().info("  "+duGoals.toString());

		LoggingUtils.getEvoLogger().info("* Creating parameter goals...");
		duGoals.addAll(getParameterGoals());
		LoggingUtils.getEvoLogger().info("  created " + getParamGoalsCount()
		                                         + " parameter goals");

		called = true;
		goals = new ArrayList<DefUseCoverageTestFitness>();
		goals.addAll(duGoals);
		long end = System.currentTimeMillis();
		goalComputationTime = end - start;
		LoggingUtils.getEvoLogger().info("* Goal computation took: "
		                                         + goalComputationTime + "ms");

		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.IntraMethodPairs,
		                                                                 getIntraMethodGoalsCount());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.InterMethodPairs,
		                                                                 getInterMethodGoalsCount());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.ParameterPairs,
		                                                                 getParamGoalsCount());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.IntraClassPairs,
		                                                                 getIntraClassGoalsCount());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.DefUsePairs,
		                                                                 goals.size());
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
	 */
	private static void categorizeFieldMethodCalls() {
		Set<BytecodeInstruction> fieldMethodCalls = DefUsePool.retrieveFieldMethodCalls();

		LoggingUtils.getEvoLogger().info("Categorizing field method calls: "
		                                         + fieldMethodCalls.size());

		for (BytecodeInstruction fieldMethodCall : fieldMethodCalls) {
			if (GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).canMakeCCFGForClass(fieldMethodCall.getCalledMethodsClass())) {
				ClassControlFlowGraph ccfg = GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getCCFG(fieldMethodCall.getCalledMethodsClass());
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
			} else {
				String toAnalyze = fieldMethodCall.getCalledMethodsClass() + "."
				        + fieldMethodCall.getCalledMethodName();

				if (toAnalyze != null && toAnalyze.startsWith("java.")) {

					Type[] parameters = org.objectweb.asm.Type.getArgumentTypes(fieldMethodCall.getMethodCallDescriptor());
					String newParams = "";
					if (parameters.length != 0) {
						for (Type i : parameters) {
							newParams = newParams + "," + i.getClassName();
						}
						newParams = newParams.substring(1, newParams.length());
					}
					toAnalyze = fieldMethodCall.getCalledMethodsClass() + "."
					        + fieldMethodCall.getCalledMethodName() + "(" + newParams
					        + ")";
					//System.out.println(toAnalyze);

					if (JdkPureMethodsList.instance.checkPurity(toAnalyze)) {
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
				} else {
					if (!DefUsePool.addAsUse(fieldMethodCall))
						throw new IllegalStateException(
						        "unable to register field method call as a use "
						                + fieldMethodCall.toString());
				}
			}
		}
	}

	private static Set<DefUseCoverageTestFitness> getCCFGPairs() {
		ClassControlFlowGraph ccfg = GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getCCFG(Properties.TARGET_CLASS);
		AllUsesAnalysis aua = new AllUsesAnalysis(ccfg);
		Set<DefUseCoverageTestFitness> r = aua.determineDefUsePairs();

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

		//		LoggingUtils.getEvoLogger().info(goal.toString());
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
		//		LoggingUtils.getEvoLogger().debug("# Parameter-Uses: " + r.size());
		return r;
	}

	/**
	 * <p>
	 * getRegsiteredDefinitions
	 * </p>
	 * 
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<Definition> getRegisteredDefinitions() {
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

	public static void clear() {
		if (called) {
			called = false;
			duGoals.clear();
			goals.clear();
			goalMap.clear();
			goalCounts.clear();
		}
	}

	public static boolean detectAliasingGoals(List<ExecutionResult> results) {

		if (!Properties.DEFUSE_ALIASES)
			return false;

		Set<DefUseCoverageTestFitness> aliasingGoals = new HashSet<DefUseCoverageTestFitness>();

		for (ExecutionResult result : results) {
			aliasingGoals.addAll(detectAliasingGoals(result));
		}

		// Need to add here to avoid concurrent access
		if (!aliasingGoals.isEmpty()) {
			goals.addAll(aliasingGoals);
			duGoals.addAll(aliasingGoals);
		}

		return !aliasingGoals.isEmpty();
	}

	private static Set<DefUseCoverageTestFitness> detectAliasingGoals(
	        ExecutionResult result) {
		Map<String, HashMap<Integer, HashMap<Integer, Object>>> passedDefsObject = result.getTrace().getDefinitionDataObjects();
		Map<String, HashMap<Integer, HashMap<Integer, Object>>> passedUsesObject = result.getTrace().getUseDataObjects();

		Map<String, HashMap<Integer, HashMap<Integer, Integer>>> passedDefs = result.getTrace().getDefinitionData();
		Map<String, HashMap<Integer, HashMap<Integer, Integer>>> passedUses = result.getTrace().getUseData();

		Set<DefUseCoverageTestFitness> aliasingGoals = new HashSet<DefUseCoverageTestFitness>();

		for (String goalVariable : passedUsesObject.keySet()) {
			for (Integer objectId : passedUsesObject.get(goalVariable).keySet()) {
				for (Object o1 : passedUsesObject.get(goalVariable).get(objectId).values()) {
					for (String otherGoalVariable : passedDefsObject.keySet()) {
						for (Integer otherObjectId : passedDefsObject.get(otherGoalVariable).keySet()) {
							for (Object o2 : passedDefsObject.get(otherGoalVariable).get(otherObjectId).values()) {
								if (o1 != null && o1 == o2 && objectId == otherObjectId
								        && !goalVariable.equals(otherGoalVariable)) {
									Map<Integer, Integer> currentDefMap = passedDefs.get(otherGoalVariable).get(objectId);
									Map<Integer, Integer> currentUseMap = passedUses.get(goalVariable).get(objectId);

									List<Integer> duCounterTrace = new ArrayList<Integer>(
									        currentDefMap.keySet());
									duCounterTrace.addAll(currentUseMap.keySet());
									//				System.out.println(duCounterTrace.size()); oO for ncs.Bessj these can be up to 50k entries big
									Collections.sort(duCounterTrace);
									int traceLength = duCounterTrace.size();
									Integer[] sortedDefDUTrace = duCounterTrace.toArray(new Integer[traceLength]);

									int activeDef = -1;
									for (int i = 0; i < traceLength; i++) {
										int currentDUCounter = sortedDefDUTrace[i];

										if (currentDefMap.containsKey(currentDUCounter)) {
											activeDef = currentDefMap.get(currentDUCounter);
										} else if (activeDef != -1) {
											int currentUse = currentUseMap.get(currentDUCounter);
											DefUseCoverageTestFitness currentGoal = DefUseCoverageFactory.retrieveGoal(activeDef,
											                                                                           currentUse);
											if (currentGoal == null) {
												logger.info("New alias found: Variable defined as "
												        + otherGoalVariable
												        + " appeared in use as "
												        + goalVariable);
												Definition def = DefUsePool.getDefinitionByDefId(activeDef);
												Use use = DefUsePool.getUseByUseId(currentUse);

												for (DefUseCoverageTestFitness defUse : duGoals) {
													// Find all other defs of the variable in the use
													if (defUse.getGoalUse().equals(use)) {
														Map<Use, DefUseCoverageTestFitness> defUseMap = getRegisteredGoalsForDefinition(defUse.getGoalDefinition());
														for (Use otherUse : defUseMap.keySet()) {
															//   For each defuse pair, add new defuse pair with alternative def
															DefUseCoverageTestFitness goal = DefUseCoverageFactory.createGoal(def,
															                                                                  otherUse,
															                                                                  defUseMap.get(otherUse).getType());
															if (goal != null) {
																logger.info("Created new defuse pair: "
																        + goal
																        + " of type "
																        + goal.getType());
																aliasingGoals.add(goal);
															}
														}
													}
												}

											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return aliasingGoals;
	}

}
