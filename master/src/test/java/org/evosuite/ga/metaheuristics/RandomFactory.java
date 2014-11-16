package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.NSGAChromosome;

/**
 * 
 * @author José Campos
 */
public class RandomFactory implements ChromosomeFactory<NSGAChromosome>
{
	private static final long serialVersionUID = -6984639266849566298L;

	private double upperBound;
	private double lowerBound;

	private int number_of_variables;

	private boolean ZDT4 = false;

	/**
	 * 
	 * @param z are you executing ZDT4 problem?
	 * @param nv number of variables
	 * @param lb lowerBound
	 * @param ub upperBound
	 */
	public RandomFactory(boolean z, int nv, double lb, double ub) {
		this.ZDT4 = z;
		this.number_of_variables = nv;
		this.lowerBound = lb;
		this.upperBound = ub;
	}

	@Override
	public NSGAChromosome getChromosome() {
		NSGAChromosome c = new NSGAChromosome(this.ZDT4,
				this.number_of_variables,
				this.lowerBound, this.upperBound);
		return c;
	}
}
