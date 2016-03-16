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
package org.evosuite.ga;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.variables.DoubleVariable;
import org.evosuite.ga.variables.Variable;
import org.evosuite.utils.Randomness;

/**
 * 
 * @author José Campos
 */
public class NSGAChromosome extends Chromosome
{
	private static final long serialVersionUID = -2056801838518269049L;

	/**  */
	private List<Variable> variables = new ArrayList<Variable>();

	public NSGAChromosome() {
		// empty
	}

	public NSGAChromosome(double lowerBound, double upperBound, double ... values) {
	    for (int i = 0; i < values.length; i++) {
	        Variable v = new DoubleVariable(values[i], lowerBound, upperBound);
	        this.addVariable(v);
	    }
    }

	public NSGAChromosome(boolean ZDT4,
			int number_of_variables,
			double lowerBound, double upperBound) {
		super();

		int index = 0;
		if (ZDT4) {
			Variable v = new DoubleVariable(0.0 + Randomness.nextDouble() * (1.0 - 0.0),
					0.0, 1.0);
			this.addVariable(v);
			index++;
		}

		for (int i = index; i < number_of_variables; i++) {
			Variable v = new DoubleVariable(lowerBound + Randomness.nextDouble() * (upperBound - lowerBound),
					lowerBound, upperBound);
			this.addVariable(v);
		}
	}

	public List<Variable> getVariables() {
		return this.variables;
	}

	public Variable getVariable(int i) {
		return this.variables.get(i);
	}

	public int getNumberOfVariables() {
		return this.variables.size();
	}

	public void addVariable(Variable var) {
		this.variables.add(var);
	}

	@Override
	public Chromosome clone() {
		NSGAChromosome c = new NSGAChromosome();
		c.setFitnessValues(this.getFitnessValues());
		c.setPreviousFitnessValues(this.getPreviousFitnessValues());
		for (Variable v : this.getVariables()) {
			c.addVariable(v.clone());
		}
		c.setChanged(this.isChanged());
		c.setCoverageValues(this.getCoverageValues());
		c.setNumsOfCoveredGoals(this.getNumsOfCoveredGoals());
		c.updateAge(this.getAge());
		c.setRank(this.getRank());
		c.setDistance(this.getDistance());

		return c;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.hashCode() == obj.hashCode())
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;

		hashCode = hashCode * 37 + this.getVariables().hashCode();
	    hashCode = hashCode * 37 + this.getFitnessValues().hashCode();
	    hashCode = hashCode * 37 + this.getPreviousFitnessValues().hashCode();

	    return hashCode;
	}

	@Override
	public int compareSecondaryObjective(Chromosome o) {
		// empty
		return 0;
	}

	/**
	 *  Polynomial Mutation (for real values) - PM
	 */
	@Override
	public void mutate() {
		for (int i = 0; i < this.getNumberOfVariables(); i++) {
			Variable v = this.getVariable(i);

			if (v instanceof DoubleVariable)
				this.mutate((DoubleVariable)v);
		}
	}

	private void mutate(DoubleVariable v) {
		double ub = v.getUpperBound();
		double lb = v.getLowerBound();
		double db = ub - lb;

		double new_x = v.getValue();

		double delta1 = (new_x - lb) / db;
		double delta2 = (ub - new_x) / db;
		double deltaq;

		double distributionIndex = 10.0;
		double pow = 1.0 / (distributionIndex + 1.0);

		double r = Randomness.nextDouble();
		if (r < 0.5) {
			double aux = 2.0 * r + (1.0 - 2.0 * r) * (Math.pow(1.0 - delta1, (distributionIndex + 1.0)));
			deltaq = Math.pow(aux, pow) - 1.0;
		}
		else {
			double aux = 2.0 * (1.0 - r) + 2.0 * (r - 0.5) * (Math.pow(1.0 - delta2, (distributionIndex + 1.0)));
			deltaq = 1.0 - Math.pow(aux, pow);
		}

		new_x = new_x + deltaq * db;

		if (new_x < lb)
			new_x = lb;
		else if (new_x > ub)
			new_x = ub;

		v.setValue(new_x);
	}

	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	                throws ConstructionFailedException {
		// empty
	}

	@Override
	public boolean localSearch(
			LocalSearchObjective<? extends Chromosome> objective) {
		// empty
		return false;
	}

	@Override
	public int size() {
		// empty
		return 0;
	}
}
