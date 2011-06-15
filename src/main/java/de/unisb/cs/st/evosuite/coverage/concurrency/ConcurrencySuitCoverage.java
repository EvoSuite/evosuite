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

package de.unisb.cs.st.evosuite.coverage.concurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.CFGPool;
import de.unisb.cs.st.evosuite.cfg.ControlFlowEdge;
import de.unisb.cs.st.evosuite.cfg.RawControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

public class ConcurrencySuitCoverage extends TestSuiteFitnessFunction {

	private static Logger logger = Logger.getLogger(TestSuiteFitnessFunction.class);

	public final int total_branches = BranchPool.getBranchCounter();

	public final int branchless_methods = BranchPool.getBranchlessMethods().size();

	public final int total_methods = TestCluster.getInstance().num_defined_methods;

	public int covered_branches = 0;

	public int covered_methods = 0;

	public double best_fitness = Double.MAX_VALUE;

	public final int total_goals = 2 * total_branches + branchless_methods;

	public ConcurrencySuitCoverage() {
		logger.info("Total goals: " + total_goals);
		logger.info("Total branches: " + total_branches);
	}

	/**
	 * Execute all tests and count covered branches
	 */
	@Override
	public double getFitness(Chromosome individual) {
		logger.trace("Calculating concurrent fitness");
		long start = System.currentTimeMillis();

		TestSuiteChromosome suite = (TestSuiteChromosome) individual;
		long estart = System.currentTimeMillis();
		List<ExecutionResult> results = runTestSuite(suite);
		long eend = System.currentTimeMillis();
		double fitness = 0.0;
		Map<Integer, Double> true_distance = new HashMap<Integer, Double>();
		Map<Integer, Double> false_distance = new HashMap<Integer, Double>();
		Map<Integer, Integer> predicate_count = new HashMap<Integer, Integer>();
		Map<String, Integer> call_count = new HashMap<String, Integer>();
		Set<List<SchedulingDecisionTuple>> schedules = new HashSet<List<SchedulingDecisionTuple>>();

		for (ExecutionResult result : results) {
			assert (result != null);
			assert (result.getTrace() != null);
			assert (result.getTrace().concurrencyTracer != null);
			assert (result.getTrace().concurrencyTracer.getTrace() != null);
			schedules.add(result.getTrace().concurrencyTracer.getTrace());

			if (hasTimeout(result)) {
				updateIndividual(individual, total_branches * 2 + total_methods);
				suite.setCoverage(0.0);
				logger.info("Test case has timed out, setting fitness to max value "
				        + (total_branches * 2 + total_methods));
				return total_branches * 2 + total_methods;
			}

			for (Entry<String, Integer> entry : result.getTrace().covered_methods.entrySet()) {
				if (!call_count.containsKey(entry.getKey()))
					call_count.put(entry.getKey(), entry.getValue());
				else {
					call_count.put(entry.getKey(),
					               call_count.get(entry.getKey()) + entry.getValue());
				}
			}
			for (Entry<Integer, Integer> entry : result.getTrace().covered_predicates.entrySet()) {
				if (!predicate_count.containsKey(entry.getKey()))
					predicate_count.put(entry.getKey(), entry.getValue());
				else {
					predicate_count.put(entry.getKey(),
					                    predicate_count.get(entry.getKey())
					                            + entry.getValue());
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().true_distances.entrySet()) {
				if (!true_distance.containsKey(entry.getKey()))
					true_distance.put(entry.getKey(), entry.getValue());
				else {
					true_distance.put(entry.getKey(),
					                  Math.min(true_distance.get(entry.getKey()),
					                           entry.getValue()));
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().false_distances.entrySet()) {
				if (!false_distance.containsKey(entry.getKey()))
					false_distance.put(entry.getKey(), entry.getValue());
				else {
					false_distance.put(entry.getKey(),
					                   Math.min(false_distance.get(entry.getKey()),
					                            entry.getValue()));
				}
			}
		}

		int num_covered = 0;
		int uncovered = 0;
		//logger.info("Got data for predicates: " + predicate_count.size()+"/"+total_branches);
		for (Integer key : predicate_count.keySet()) {
			//logger.info("Key: "+key);
			if (!true_distance.containsKey(key) || !false_distance.containsKey(key))
				continue;
			int num_executed = predicate_count.get(key);
			double df = true_distance.get(key);
			double dt = false_distance.get(key);
			if (num_executed == 1) {

				fitness += 1.0; // + normalize(df) + normalize(dt);
			} else {
				fitness += normalize(df) + normalize(dt);
			}
			if (df == 0.0)
				num_covered++;
			else
				uncovered++;
			if (dt == 0.0)
				num_covered++;
			else
				uncovered++;
		}
		//logger.info("Fitness after branch distances: "+fitness);
		//for(String call : call_count.keySet()) {
		//	logger.info("  "+call+": "+call_count.get(call));
		//}
		/*
		if(call_count.size() < total_methods) { // +1 for the call of the test case
			//logger.info("Missing calls: "+(total_methods - call_count.size())+"/"+total_methods);
			fitness += total_methods - call_count.size();
		}
		 */
		//		logger.info("Method calls : "+(total_methods - call_count.size())+"/"+total_methods+" ("+CFGMethodAdapter.methods.size()+")");
		int missing_methods = 0;
		for (String e : CFGMethodAdapter.methods) {
			if (!call_count.containsKey(e)) {
				logger.debug("Missing method: " + e);
				fitness += 1.0;
				missing_methods += 1;
			}
		}
		for (String method : call_count.keySet()) {
			logger.debug("Got method: " + method);
		}

		//logger.info("Fitness after missing methods: "+fitness);

		// How many branches are there in total?
		//fitness += 2 * total_branches - num;
		fitness += 2 * (total_branches - predicate_count.size());
		//logger.info("Missing branches: "+(2*(total_branches - predicate_count.size()))+"/"+(2*total_branches));
		//logger.info("Missing methods: "+missing_methods+"/"+total_methods);
		//logger.info("Uncovered branches: "+uncovered);
		//logger.info("Fitness after missing branches: "+fitness);
		//logger.info("Got data for "+predicate_count.size()+" predicates, covered "+num_covered+" total "+(total_branches*2)+", covered "+call_count.size()+" methods out of "+total_methods);

		if (num_covered > covered_branches) {
			covered_branches = Math.max(covered_branches, num_covered);
			logger.info("(Branches) Best individual covers " + covered_branches + "/"
			        + (total_branches * 2) + " branches and "
			        + (total_methods - missing_methods) + "/" + total_methods
			        + " methods");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.length());
		}
		//		if(call_count.size() > covered_methods) {
		if ((total_methods - missing_methods) > covered_methods) {
			logger.info("(Methods) Best individual covers " + covered_branches + "/"
			        + (total_branches * 2) + " branches and "
			        + (total_methods - missing_methods) + "/" + total_methods
			        + " methods");
			covered_methods = (total_methods - missing_methods);
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.length());

		}
		if (fitness < best_fitness) {
			logger.info("(Fitness) Best individual covers " + covered_branches + "/"
			        + (total_branches * 2) + " branches and "
			        + (total_methods - missing_methods) + "/" + total_methods
			        + " methods");
			best_fitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.length());

		}

		double coverage = num_covered;
		for (String e : BranchPool.getBranchlessMethods()) {
			if (call_count.keySet().contains(e))
				coverage += 1.0;

		}

		//if(fitness==0.0){
		//#FIXME steenbuck RUNTIME
		double concurrentFitness = 0.0;
		try {
			assert (LockRuntime.threadIDs.size() > 1) : "We should expect the LockRuntime to know more than 0 threads. But apparently it only knows "
			        + LockRuntime.threadIDs.size();
			long t1 = System.currentTimeMillis();
			Set<SchedulingDecisionList> goalSchedules = getSchedules(4,
			                                                         LockRuntime.threadIDs);
			logger.info("We generated " + goalSchedules.size()
			        + " schedules which should be covered. In : "
			        + (System.currentTimeMillis() - t1));
			assert (goalSchedules.size() > 0) : "it appears odd, that zero goals were generated";
			for (SchedulingDecisionList goal : goalSchedules) {
				logger.trace("testing for schedule: " + goal.toString());
				double min = Double.MAX_VALUE;
				for (List<SchedulingDecisionTuple> seen : schedules) {
					double distance = getDistance(goal, seen);
					min = Math.min(min, distance / 1000000);
				}
				concurrentFitness += min;
			}
		} catch (Throwable t) {
			logger.fatal("why?", t);
			System.exit(1);
			throw new Error();
		}
		//System.exit(1);
		if (concurrentFitness > 0.5) {
			concurrentFitness = 0.5;
		}
		fitness += concurrentFitness;
		//assert(fitness<0.1);
		//System.out.println("kicken it " + fitness);
		//}else{
		//System.out.println("not so kicken it " + fitness);
		//}
		logger.info("fitness " + fitness);
		//if(fitness==0.0)System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + fitness);

		//		covered_methods  = Math.max(covered_methods,  call_count.size());

		//logger.info("Fitness: "+fitness+", size: "+suite.size()+", length: "+suite.length());
		updateIndividual(individual, fitness);

		long end = System.currentTimeMillis();
		if (end - start > 1000) {
			logger.info("Executing tests took    : " + (eend - estart) + "ms");
			logger.info("Calculating fitness took: " + (end - start) + "ms");
		}

		suite.setCoverage(coverage / total_goals);

		return fitness;
	}

	//#TODO currently we only generate schedules, which can be reached in one run. In one method (that depends on the CFG for multi method stuff) If we allow arbitrarily many calls between reaching a scheduling point, what would change?
	//generates all schedules which should be covered
	private Set<SchedulingDecisionList> s = null;

	private final Set<SchedulingDecisionList> getSchedules(
	        final int synchPointsToConsider, final Set<Integer> threadIDs) {
		assert (synchPointsToConsider > 0);
		if (s != null)
			return s;
		Set<SchedulingDecisionList> schedules = new HashSet<SchedulingDecisionList>();

		//init
		for (Integer fieldAccessId : LockRuntime.fieldAccessIDToCFGBranch.keySet()) {
			for (Integer threadID : threadIDs) {
				SchedulingDecisionList s = new SchedulingDecisionList();
				s.add(new SchedulingDecisionTuple(threadID, fieldAccessId));
				schedules.add(s);
			}
		}

		for (int i = 1; i < synchPointsToConsider; i++) {

			Set<SchedulingDecisionList> nextRound = new HashSet<SchedulingDecisionList>();
			for (SchedulingDecisionList schedule : schedules) {
				nextRound.addAll(getNextSteps(schedule, threadIDs));

			}
			schedules = nextRound;
		}
		s = schedules;
		return schedules;
	}

	private final Set<SchedulingDecisionList> getNextSteps(
	        final SchedulingDecisionList history, final Set<Integer> threadIDs) {

		Set<SchedulingDecisionList> result = new HashSet<SchedulingDecisionList>();
		for (SchedulingDecisionTuple nextTuple : getAllTuples()) {
			assert (LockRuntime.fieldAccToConcInstr.containsKey(nextTuple.scheduleID));
			String className = LockRuntime.fieldAccToConcInstr.get(nextTuple.scheduleID).getClassName();
			String methodName = LockRuntime.fieldAccToConcInstr.get(nextTuple.scheduleID).getMethodName();
			RawControlFlowGraph completeCFG = CFGPool.getRawCFG(className, methodName);
			if (isAfter(nextTuple, history, completeCFG)) {
				SchedulingDecisionList newList = history.clone();
				newList.add(nextTuple);
				result.add(newList);
			}

		}
		if (result.size() == 0) {
			result.add(history);
		}
		return result;
	}

	/**
	 * Returns true, if tuple is, considering the same thread as tuple, after
	 * the last fieldAccessID in history. 'is after' is defined by isBefore(...)
	 * 
	 * @param tuple
	 * @param history
	 * @return
	 */
	private boolean isAfter(SchedulingDecisionTuple tuple,
	        SchedulingDecisionList history, final RawControlFlowGraph completeCFG) {
		for (int i = (history.size() - 1); i >= 0; i--) {
			assert (history.size() > i);
			SchedulingDecisionTuple searchFront = history.get(i);
			if (searchFront.threadID == tuple.threadID) {
				if (isBefore(searchFront.scheduleID, tuple.scheduleID, completeCFG)) {
					return true;
				} else {
					return false; //isBefore is transitive
				}
			}
		}

		return true; //the history doesn't contain this thread
	}

	private Set<SchedulingDecisionTuple> allTuples = null;

	/**
	 * Generates a list of all possible scheduling decision tuples #TODO cache
	 * 
	 * @return
	 */
	private Set<SchedulingDecisionTuple> getAllTuples() {
		if (allTuples != null)
			return allTuples;

		Set<Integer> threadIDs = LockRuntime.threadIDs;
		Set<Integer> fieldAccessIDs = LockRuntime.fieldAccessIDToCFGBranch.keySet();

		allTuples = new HashSet<SchedulingDecisionTuple>();

		for (Integer fieldAccessID : fieldAccessIDs) {
			for (Integer threadID : threadIDs) {
				allTuples.add(new SchedulingDecisionTuple(threadID, fieldAccessID));
			}
		}

		return allTuples;
	}

	//#TODO move to CFG
	private final Map<Integer, Map<Integer, Map<RawControlFlowGraph, Boolean>>> isC = new HashMap<Integer, Map<Integer, Map<RawControlFlowGraph, Boolean>>>();

	/**
	 * Tests if scheduleID1 is before scheduleID2. That is: branchID2 can be
	 * reached after branchID1 was reached. Notice that before(int, int, graph)
	 * is not a partial order. As while(true) //branch 1 if(true) //branch 2 ...
	 * 
	 * note: if branchID1==branchID2, this function will return false;
	 * 
	 * //#TODO check that this works with while as expected //#TODO this should
	 * be in the CFGgraph class
	 * 
	 * @param branchID1
	 * @param branchID2
	 * @param minimizedCFG
	 * @return
	 */
	private final boolean isBefore(final int scheduleID1, final int scheduleID2,
	        RawControlFlowGraph completeCFG) {
		if (isC.containsKey(scheduleID1)) {
			if (isC.get(scheduleID1).containsKey(scheduleID2)) {
				if (isC.get(scheduleID1).get(scheduleID2).containsKey(completeCFG)) {
					return isC.get(scheduleID1).get(scheduleID2).get(completeCFG);
				}
			} else {
				isC.get(scheduleID1).put(scheduleID2,
				                         new HashMap<RawControlFlowGraph, Boolean>());
			}
		} else {
			isC.put(scheduleID1,
			        new HashMap<Integer, Map<RawControlFlowGraph, Boolean>>());
			isC.get(scheduleID1).put(scheduleID2,
			                         new HashMap<RawControlFlowGraph, Boolean>());
		}
		assert (LockRuntime.fieldAccessIDToCFGVertex.containsKey(scheduleID1));
		assert (LockRuntime.fieldAccessIDToCFGVertex.containsKey(scheduleID1));
		assert (completeCFG != null);
		assert (completeCFG.containsVertex(LockRuntime.fieldAccessIDToCFGVertex.get(scheduleID1)));
		assert (completeCFG.containsVertex(LockRuntime.fieldAccessIDToCFGVertex.get(scheduleID2)));

		if (scheduleID1 == scheduleID2) {
			isC.get(scheduleID1).get(scheduleID2).put(completeCFG, false);
			return false;
		}

		BytecodeInstruction start = LockRuntime.fieldAccessIDToCFGVertex.get(scheduleID1);
		BytecodeInstruction goal = LockRuntime.fieldAccessIDToCFGVertex.get(scheduleID2);
		Set<BytecodeInstruction> seen = new HashSet<BytecodeInstruction>();
		List<BytecodeInstruction> searchFront = new LinkedList<BytecodeInstruction>();
		searchFront.add(start);
		while (searchFront.size() > 0) {
			BytecodeInstruction current = searchFront.remove(0);
			if (current.equals(goal)) {
				isC.get(scheduleID1).get(scheduleID2).put(completeCFG, true);
				return true;
			} else {
				for (ControlFlowEdge e : completeCFG.outgoingEdgesOf(current)) {
					BytecodeInstruction toCheck = completeCFG.getEdgeTarget(e);
					if (!seen.contains(toCheck)) {
						seen.add(toCheck);
						searchFront.add(toCheck);
					}
				}
			}
		}
		isC.get(scheduleID1).get(scheduleID2).put(completeCFG, false);
		return false;
	}

	public static String printList(List<SchedulingDecisionTuple> l) {
		StringBuilder b = new StringBuilder();
		for (SchedulingDecisionTuple o : l) {
			b.append(o.scheduleID);
			b.append(":");
			b.append(o.threadID);
			b.append(" - ");
		}
		return b.toString();
	}

	public int getDistance(List<SchedulingDecisionTuple> target,
	        List<SchedulingDecisionTuple> seen) {
		assert (seen != null);
		assert (target != null);

		List<SchedulingDecisionTuple> seen2 = new ArrayList<SchedulingDecisionTuple>();
		for (SchedulingDecisionTuple s : seen) {
			if (target.contains(s)) {
				seen2.add(s);
			}
		}

		return ConcurrencyTracer.computeDistance(target, seen2);

	}
}
