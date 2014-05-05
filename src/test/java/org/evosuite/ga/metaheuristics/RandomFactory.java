package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.ChromosomeFactory;

/**
 * 
 * @author José Campos
 */
public class RandomFactory implements ChromosomeFactory<NSGAChromosome>
{
	private static final long serialVersionUID = -6984639266849566298L;

	private double min;
	private double max;
	private double upperBound;
	private double lowerBound;

	private int number_of_variables;

	private boolean ZDT4 = false;

	/**
	 * 
	 * @param z are you executing ZDT4 problem?
	 * @param nv number of variables
	 * @param m min
	 * @param M max
	 * @param ub upperBound
	 * @param lb lowerBound
	 */
	public RandomFactory(boolean z, int nv, double m, double M,
			double ub, double lb) {
		this.ZDT4 = z;
		this.number_of_variables = nv;
		this.min = m;
		this.max = M;
		this.upperBound = ub;
		this.lowerBound = lb;
	}

	@Override
	public NSGAChromosome getChromosome() {
		NSGAChromosome c = new NSGAChromosome(this.ZDT4,
				this.number_of_variables,
				this.min, this.max,
				this.upperBound, this.lowerBound);
		return c;
	}
}
