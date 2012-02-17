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

import de.unisb.cs.st.evosuite.graphs.GraphPool;
import de.unisb.cs.st.evosuite.graphs.cfg.RawControlFlowGraph;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author Andre Mis
 * 
 */
public class AllDefsCoverageFactory extends AbstractFitnessFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {

		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

		Set<Definition> defs = DefUseCoverageFactory.getRegsiteredDefinitions();

		for (Definition def : defs) {
			Map<Use, DefUseCoverageTestFitness> uses = DefUseCoverageFactory
					.getRegisteredGoalsForDefinition(def);
			
			goals.add(createGoal(def,uses));
		}

		return goals;
	}

	
	private static AllDefsCoverageTestFitness createGoal(Definition def,
			Map<Use,DefUseCoverageTestFitness> uses) {

		return new AllDefsCoverageTestFitness(def,uses);
	}

}