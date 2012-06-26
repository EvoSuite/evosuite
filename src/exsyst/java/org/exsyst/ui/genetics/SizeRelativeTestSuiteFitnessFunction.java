package org.exsyst.ui.genetics;

import org.evosuite.ga.Chromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;


public class SizeRelativeTestSuiteFitnessFunction extends TestSuiteFitnessFunction {
	private static final long serialVersionUID = 1L;
	private TestSuiteFitnessFunction baseFunction;
	private double sizeExponent;

	public SizeRelativeTestSuiteFitnessFunction(TestSuiteFitnessFunction baseFunction,
			double sizeExponent) {
		this.baseFunction = baseFunction;
		this.sizeExponent = sizeExponent;
	}
	
	public SizeRelativeTestSuiteFitnessFunction(TestSuiteFitnessFunction baseFunction) {
		this(baseFunction, 1.01);
	}
	
	@Override
	public double getFitness(Chromosome individual) {
		AbstractTestSuiteChromosome<?> chromosome = (AbstractTestSuiteChromosome<?>) individual;
		int size = chromosome.totalLengthOfTestCases();
		
		double baseFitness = this.baseFunction.getFitness(individual);
		double fitness = baseFitness * (Math.pow(size, this.sizeExponent) / size);
		
		updateIndividual(individual, fitness);
		return fitness;
	}

}
