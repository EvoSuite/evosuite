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

import java.util.Map;

import de.unisb.cs.st.evosuite.coverage.statement.StatementCoverageTestFitness;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;


/**
 * Evaluate fitness of a single test case with respect to one Definition-Use
 * pair
 * 
 * For more information look at the comment from method getDistance()
 * 
 * @author Andre Mis
 */
public class AllDefsCoverageTestFitness extends TestFitnessFunction {
	
	private static final long serialVersionUID = 1L;
	public static long singleFitnessTime = 0l;
	
	
	private Definition targetDef;
	private final TestFitnessFunction goalDefinitionFitness;
	private Map<Use,DefUseCoverageTestFitness> uses;
	

	public AllDefsCoverageTestFitness(Definition def, Map<Use,DefUseCoverageTestFitness> uses) {
		this.targetDef = def;
		this.goalDefinitionFitness = new StatementCoverageTestFitness(def);
		this.uses = uses;
	}


	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		
		double defFitness = goalDefinitionFitness.getFitness(individual, result);
		
		if(defFitness>0)
			return 1+normalize(defFitness);
		
		// TODO: filter all objects
		// TODO: compute minimum over all use-fitnesses
		// TODO: return that minimum after normalization, stop once a use-fitness is 0
		
		// first inefficient version:
		double min = Double.MAX_VALUE;
		for(Use use : uses.keySet()) {
			double useFitness = uses.get(use).getFitness(individual, result);
			if(useFitness == 0)
				return 0;
			if(useFitness<min)
				min = useFitness;
		}
		
		return min;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((targetDef == null) ? 0 : targetDef.hashCode());
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
		AllDefsCoverageTestFitness other = (AllDefsCoverageTestFitness) obj;
		if (targetDef == null) {
			if (other.targetDef != null)
				return false;
		} else if (!targetDef.equals(other.targetDef))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "AllDef-Goal "+targetDef.toString();
	}

}
