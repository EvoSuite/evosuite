/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Andre Mis
 * 
 */
public class DefUseCoverageFactory implements TestFitnessFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {

		// TODO replace this with Reaching-Definitions-Algorithm 
		
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

		Set<CFGVertex> freeDefs = getDefsWithClearPathToMethodEnd();
		Set<CFGVertex> freeUses = getUsesWithClearPathFromMethodStart();
		
		System.out.println("#defs with clear path to end "+freeDefs.size());
		System.out.println("#uses with clear path from start "+freeUses.size());

		for(CFGVertex def : freeDefs)
			for(CFGVertex use : freeUses)
				if(def.getDUVariableName().equals(use.getDUVariableName())) {
					goals.add(createGoal(def,use));
				}
				
		goals.addAll(getPairsWithinMethods());
		
		return goals;
	}

	/**
	 * Given a definition and a use, this method creates a DefUseCoverageGoal
	 * for this DefUsePair.
	 * 
	 * 
	 * @param def The definition of the goal
	 * @param use The use of the goal
	 * @return The created DefUseCoverageGoal
	 */
	private TestFitnessFunction createGoal(CFGVertex def, CFGVertex use) {

		
		return new DefUseCoverageTestFitness(def, use);
	}

	/**
	 * For every definition found by the CFGMethodAdapter this Method checks,
	 * what uses there are in the same method and for the same field of that definition.
	 * 
	 * If there is a defclearpath from the definition to the use, a DefUseCoverageGoal
	 * for this pair is created.
	 * 
	 * @return A list of all the DefUseCoverageGoals created this way
	 */
	private List<TestFitnessFunction> getPairsWithinMethods() {
		
		ArrayList<TestFitnessFunction> r = new ArrayList<TestFitnessFunction>();
		
		for (String className : CFGMethodAdapter.def_map.keySet()) 
			for (String methodName : CFGMethodAdapter.def_map.get(className).keySet()) 
				for (String fieldName : CFGMethodAdapter.def_map.get(className).get(methodName).keySet()) 
					for (Entry<Integer, List<CFGVertex>> entry : CFGMethodAdapter.def_map.get(className).get(methodName).get(fieldName).entrySet()) {
						
						ControlFlowGraph cfg = ExecutionTracer.getExecutionTracer().getCompleteCFG(className, methodName);
						CFGVertex v = cfg.getVertex(entry.getKey());

						if (entry.getKey() != -1 && v == null) 
							 throw new IllegalStateException("no CFG for branch "+entry.getKey()+" in method "+methodName);

						for (CFGVertex def : entry.getValue()) {
							
							List<CFGVertex> uses = cfg.getUsesForDef(def);
							for(CFGVertex use : uses) {
								r.add(createGoal(def, use));
							}
						}
					}
//		System.out.println("#IntraMethod Pairs: "+r.size());
		return r;
	}

	/**
	 * For every use found by the CFGMethodAdapter this method checks,
	 * if there is a defclearpath from the beginning of the uses method to the use itself.
	 * 
	 * @return A Set of all the uses for which the above holds
	 */
	private Set<CFGVertex> getUsesWithClearPathFromMethodStart() {

		HashSet<CFGVertex> r = new HashSet<CFGVertex>();
		
		for (String className : CFGMethodAdapter.use_map.keySet()) 
			for (String methodName : CFGMethodAdapter.use_map.get(className).keySet()) 
				for (String varName : CFGMethodAdapter.use_map.get(className).get(methodName).keySet()) 
					for (Entry<Integer, List<CFGVertex>> entry : CFGMethodAdapter.use_map.get(className).get(methodName).get(varName).entrySet()) {

						ControlFlowGraph cfg = ExecutionTracer.getExecutionTracer().getCompleteCFG(className, methodName);
						CFGVertex v = cfg.getVertex(entry.getKey());

						if (entry.getKey() != -1 && v == null)
							 throw new IllegalStateException("no CFG for branch "+entry.getKey()+" in method "+methodName);

						for (CFGVertex use : entry.getValue()) {
							if (cfg.hasDefClearPathFromMethodStart(use))
								r.add(use);
						}
					}

		return r;
	}

	/**
	 * For every definition found by the CFGMethodAdapter this Method checks,
	 * if there is a defclearpath from that definition to the end of its method.
	 * 
	 * @return A Set of all the definitions for which the above holds
	 */
	private Set<CFGVertex> getDefsWithClearPathToMethodEnd() {

		HashSet<CFGVertex> r = new HashSet<CFGVertex>();
		
		for (String className : CFGMethodAdapter.def_map.keySet())
			for (String methodName : CFGMethodAdapter.def_map.get(className).keySet()) 
				for (String varName : CFGMethodAdapter.def_map.get(className).get(methodName).keySet())
					for (Entry<Integer, List<CFGVertex>> entry : CFGMethodAdapter.def_map.get(className).get(methodName).get(varName).entrySet()) {

						// cfg of defs method
						ControlFlowGraph cfg = ExecutionTracer.getExecutionTracer().getCompleteCFG(className, methodName);
						if(cfg == null) 
							throw new IllegalStateException("Didnt find complete cfg for "+className+"."+methodName);
						
						CFGVertex v = cfg.getVertex(entry.getKey());

						if (entry.getKey() != -1 && v == null) {
							 throw new IllegalStateException("no CFG for branch "+entry.getKey()+" in method "+methodName);
						}

						for (CFGVertex def : entry.getValue()) {
							if (cfg.hasDefClearPathToMethodEnd(def)) {
								r.add(def);
							}

						}
					}

		return r;

	}
	
}
