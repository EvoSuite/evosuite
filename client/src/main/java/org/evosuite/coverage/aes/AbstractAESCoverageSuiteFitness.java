package org.evosuite.coverage.aes;

import java.util.List;
import java.io.*;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public abstract class AbstractAESCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 5184507726269266351L;

	public static enum Metric { AES, DTR, VDDU,VMDDU,VRDDU,VCDDU,VCMDDU1,VCMDDU2};		//New enum added 1
	private Metric metric;

	public AbstractAESCoverageSuiteFitness(Metric metric) {
		this.metric = metric;
	}

	public double getMetric(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> results = runTestSuite(suite);
		Spectrum spectrum = getSpectrum(results);
		return getMetric(spectrum);
	}

	public double getBasicCoverage(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> results = runTestSuite(suite);
		Spectrum spectrum = getSpectrum(results);
		return spectrum.basicCoverage();
	}

	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		double metric_value = getMetric(suite);
		double fitness = metricToFitness(metric_value);

		updateIndividual(this, suite, fitness);
		suite.setCoverage(this, metric_value);

		return fitness;
	}

	protected abstract Spectrum getSpectrum(List<ExecutionResult> results);

	public double getMetric(Spectrum spectrum) {
		switch (this.metric) {
		case DTR:
			return spectrum.getDistinctTransactionsRho() * spectrum.getAmbiguity();

		case VDDU:
			return spectrum.getVrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VMDDU:
			return spectrum.getVMrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VCDDU:
			return spectrum.getVCrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VCMDDU1:
			return spectrum.getVCMrho1() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VCMDDU2:
			return spectrum.getVCMrho2() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VRDDU:
			return spectrum.getVRrho();
		case AES:
		default:
			 return spectrum.getRho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();                   
		}
	}

	public static double metricToFitness(double metric) {
		return Math.abs(0.5d - metric);
	}

}
