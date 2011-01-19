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


package de.unisb.cs.st.evosuite.coverage.branch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Gordon Fraser
 *
 */
public class BranchCoverageFactory implements TestFitnessFactory {

	/* (non-Javadoc)
     * @see de.unisb.cs.st.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
     */
    @Override
    public List<TestFitnessFunction> getCoverageGoals() {
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

		// Branchless methods
		String class_name = Properties.TARGET_CLASS;
		for(String method : BranchPool.branchless_methods) {
			goals.add(new BranchCoverageTestFitness(new BranchCoverageGoal(class_name, method)));
		}
		
		// Branches
		for(String className : BranchPool.branch_map.keySet()) {
			for(String methodName : BranchPool.branch_map.get(className).keySet()) {
				// Get CFG of method
//				ControlFlowGraph cfg = ExecutionTracer.getExecutionTracer().getCFG(className, methodName);
				ControlFlowGraph cfg = CFGMethodAdapter.getCFG(className, methodName);
				
				for(Entry<Integer,Integer> entry : BranchPool.branch_map.get(className).get(methodName).entrySet()) {
					// Identify vertex in CFG
					goals.add(new BranchCoverageTestFitness(new BranchCoverageGoal(entry.getValue(), entry.getKey(), true, cfg, className, methodName)));
					goals.add(new BranchCoverageTestFitness(new BranchCoverageGoal(entry.getValue(), entry.getKey(), false, cfg, className, methodName)));
				}
			}
		}
		
		return goals;
    }

}
