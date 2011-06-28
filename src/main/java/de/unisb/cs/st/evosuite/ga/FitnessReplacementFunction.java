/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with GA.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

/**
 * Replacement function that only looks at the fitness
 * 
 * @author Gordon Fraser
 *
 */
public class FitnessReplacementFunction extends
		ReplacementFunction {

	
	/**
	 * @param selectionFunction
	 */
	public FitnessReplacementFunction(SelectionFunction selectionFunction) {
		super(selectionFunction);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.javalanche.ga.SteadyStateReplacementFunction#keepOffspring(de.unisb.cs.st.javalanche.ga.Chromosome, de.unisb.cs.st.javalanche.ga.Chromosome, de.unisb.cs.st.javalanche.ga.Chromosome, de.unisb.cs.st.javalanche.ga.Chromosome)
	 */
	@Override
	public boolean keepOffspring(Chromosome parent1, Chromosome parent2,
			Chromosome offspring1, Chromosome offspring2) {
		
		Chromosome best_parent = getBest(parent1, parent2);
		Chromosome best_offspring = getBest(offspring1, offspring2);

		return isBetter(best_offspring, best_parent);
	}

	/* (non-Javadoc)
     * @see de.unisb.cs.st.evosuite.ga.ReplacementFunction#keepOffspring(de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome)
     */
    @Override
    public boolean keepOffspring(Chromosome parent, Chromosome offspring) {
	    return isBetter(offspring, parent);
    }

}
