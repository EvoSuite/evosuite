package org.exsyst.genetics;

import org.evosuite.testcase.ExecutableChromosome;
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
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> chromosome) {
		int size = chromosome.totalLengthOfTestCases();
		
		double baseFitness = this.baseFunction.getFitness(chromosome);
		double fitness = baseFitness * (Math.pow(size, this.sizeExponent) / size);
		
		updateIndividual(chromosome, fitness);
		return fitness;
	}

}
