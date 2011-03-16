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

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Andre Mis
 * 
 */
public class DefUseCoverageFactory implements TestFitnessFactory {

	private static Logger logger = Logger.getLogger(DefUseCoverageFactory.class);
	
	// TestSuiteMinimizer seems to call getCoverageGoals() a second time
	// and since analysis takes a little ...
	private static boolean called = false;
	private static List<TestFitnessFunction> goals;
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {

		// TODO replace this with Reaching-Definitions-Algorithm 
		if(called) 
			return goals;
		
		logger.info("Starting DefUse-Coverage goal generation");
		goals = new ArrayList<TestFitnessFunction>();
		
		goals.addAll(getPairsWithinMethods());
		goals.addAll(getParameterGoals());
		
		Set<Definition> freeDefs = getDefinitionsWithClearPathToMethodEnd();
		Set<Use> freeUses = getUsesWithClearPathFromMethodStart();
		for(Definition def : freeDefs)
			for(Use use : freeUses)
				if(def.getDUVariableName().equals(use.getDUVariableName())) {
					DefUseCoverageTestFitness newGoal = createGoal(def,use);
					if(!goals.contains(newGoal))
						goals.add(newGoal);
				}
		
		called = true;
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
	private DefUseCoverageTestFitness createGoal(Definition def, Use use) {

		return new DefUseCoverageTestFitness(def, use);
	}

	private Set<TestFitnessFunction> getParameterGoals() {
		Set<TestFitnessFunction> r = new HashSet<TestFitnessFunction>();
		Set<Use> parameterUses = getParameterUses();
		for(Use use : parameterUses) 
			r.add(new DefUseCoverageTestFitness(use));
		logger.info("# Parameter-Uses: "+r.size());
		return r;
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
	private Set<TestFitnessFunction> getPairsWithinMethods() {
		Set<TestFitnessFunction> r = new HashSet<TestFitnessFunction>();
		for (String className : DefUsePool.def_map.keySet()) 
			for (String methodName : DefUsePool.def_map.get(className).keySet()) 
				for (String fieldName : DefUsePool.def_map.get(className).get(methodName).keySet()) 
					for (Entry<Integer, List<Definition>> entry : DefUsePool.def_map.get(className).get(methodName).get(fieldName).entrySet()) {
						ControlFlowGraph cfg = CFGMethodAdapter.getCompleteCFG(className, methodName);
						CFGVertex v = cfg.getVertex(entry.getKey());
						if (entry.getKey() != -1 && v == null) 
							 throw new IllegalStateException("no CFG for branch "+entry.getKey()+" in method "+methodName);
						for (Definition def : entry.getValue()) {
							Set<CFGVertex> uses = cfg.getUsesForDef(def.getCFGVertex());
							for(CFGVertex use : uses)
								r.add(createGoal(def, DefUsePool.getUseByDUID(use.duID)));
						}
					}
		logger.info("# DU-Pairs within methods: "+r.size());
		return r;
	}

	/**
	 * For every definition found by the CFGMethodAdapter this Method checks,
	 * if there is a defclearpath from that definition to the end of its method.
	 * 
	 * @return A Set of all the definitions for which the above holds
	 */
	private Set<Definition> getDefinitionsWithClearPathToMethodEnd() {
		HashSet<Definition> r = new HashSet<Definition>();
		for (String className : DefUsePool.def_map.keySet())
			for (String methodName : DefUsePool.def_map.get(className).keySet()) 
				for (String varName : DefUsePool.def_map.get(className).get(methodName).keySet())
					for (Entry<Integer, List<Definition>> entry : DefUsePool.def_map.get(className).get(methodName).get(varName).entrySet()) {
						// cfg of defs method
						ControlFlowGraph cfg = CFGMethodAdapter.getCompleteCFG(className, methodName);
						if(cfg == null) 
							throw new IllegalStateException("Didnt find complete cfg for "+className+"."+methodName);
						CFGVertex v = cfg.getVertex(entry.getKey());
						if (entry.getKey() != -1 && v == null)
							 throw new IllegalStateException("no CFG for branch "+entry.getKey()+" in method "+methodName);
						for (Definition def : entry.getValue()) {
							if (cfg.hasDefClearPathToMethodEnd(def.getCFGVertex())) {
								r.add(def);
							}
						}
					}
		logger.info("# Definitions with clear path to end "+r.size());
		return r;

	}	
	
	/**
	 * For every use found by the CFGMethodAdapter this method checks,
	 * if there is a defclearpath from the beginning of the uses method to the use itself.
	 * 
	 * @return A Set of all the uses for which the above holds
	 */
	private Set<Use> getUsesWithClearPathFromMethodStart() {
		Set<Use> allUses = getAllUses();
		Set<Use> r = new HashSet<Use>();
		for (Use use : allUses) {
			ControlFlowGraph cfg = CFGMethodAdapter.getCompleteCFG(use.getClassName(), use.getMethodName());
			if (cfg.hasDefClearPathFromMethodStart(use.getCFGVertex()))
				r.add(use);
		}
		logger.info("# Uses with clear path from start "+r.size());
		return r;
	}
	
	private Set<Use> getParameterUses() {
		Set<Use> allUses = getAllUses();
		Set<Use> r = new HashSet<Use>();
		for (Use use : allUses)
			if (use.isParameterUse())
				r.add(use);
		return r;	
	}
	
	private Set<Use> getAllUses() {
		HashSet<Use> r = new HashSet<Use>();
		for (String className : DefUsePool.use_map.keySet()) 
			for (String methodName : DefUsePool.use_map.get(className).keySet()) 
				for (String varName : DefUsePool.use_map.get(className).get(methodName).keySet()) 
					for (Entry<Integer, List<Use>> entry : DefUsePool.use_map.get(className).get(methodName).get(varName).entrySet()) {
						ControlFlowGraph cfg = CFGMethodAdapter.getCompleteCFG(className, methodName);
						CFGVertex v = cfg.getVertex(entry.getKey());
						if (entry.getKey() != -1 && v == null)
							 throw new IllegalStateException("no CFG for branch "+entry.getKey()+" in method "+methodName);
						for (Use use : entry.getValue())
							r.add(use);
					}

		return r;		
	}
}
