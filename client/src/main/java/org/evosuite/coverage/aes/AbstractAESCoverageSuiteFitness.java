package org.evosuite.coverage.aes;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;

public abstract class AbstractAESCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 5184507726269266351L;
	private static long iteration = 0;
    final double THRESHOLD = 0.000001;

	public static enum Metric { AES, DTR, VDDU,VMDDU,VRDDU,VCDDU,VCMDDU1,VCMDDU2};		//New enum added 1
	private Metric metric;

	public AbstractAESCoverageSuiteFitness(Metric metric) {
		this.metric = metric;
	}

	public double getMetric(TestSuiteChromosome suite) {
		List<ExecutionResult> results = runTestSuite(suite);
		Spectrum spectrum = getSpectrum(results);
		return getMetric(spectrum);
	}

	public double getBasicCoverage(TestSuiteChromosome suite) {
		List<ExecutionResult> results = runTestSuite(suite);
		Spectrum spectrum = getSpectrum(results);
		return spectrum.basicCoverage();
	}

	@Override
	public double getFitness(TestSuiteChromosome suite) {
		double metric_value = getMetric(suite);
		double fitness = metricToFitness(metric_value);

		updateIndividual(suite, fitness);
		suite.setCoverage(this, metric_value);

		return fitness;
	}

	protected abstract Spectrum getSpectrum(List<ExecutionResult> results);

    public double compute_mean(double[] A, int components)
    {
        double sum = 0d;
        for(int i=0;i<components;i++)
            sum = sum + A[i];
        return (sum / components);
    }

    public double number_of_1s_metric(Spectrum spectrum, Map<Integer,Double> weights)
    {
        double[][] ochiai = spectrum.compute_ochiai();
        if(ochiai == null)
            return 1d;
        int components = spectrum.getNumComponents();
        double[] avg_val = new double[components];

        for(int i=0;i<components;i++)
        {
            double ones = 0d;
            if(Math.abs(ochiai[i][i])<THRESHOLD)
                avg_val[i] = 1d;
            else
            {
                for (int j = 0; j < components; j++)
                {
                    if ((i != j) && (Math.abs((ochiai[i][j])-1d)<THRESHOLD))
                        ones++;
                }
                if(components < 2)
                    avg_val[i] = (ones / (components - 1));
            }
        }
        double sumWeights = 0d;
        if(weights == null)
            return compute_mean(avg_val,components);

        double sum = 0d;
        for(int i=0;i<components;i++) {
            sumWeights = sumWeights + weights.get(i);
            avg_val[i] = avg_val[i] * weights.get(i);
            sum = sum + avg_val[i];

        }
        return sum/(sumWeights);

    }


    public double getMetric(Spectrum spectrum) {
		switch (this.metric) {
        case DTR:
            return spectrum.getDistinctTransactionsRho() * spectrum.getAmbiguity();

		case VCMDDU2: {
            double ff_val = number_of_1s_metric(spectrum,null);
            return  (0.5d - (0.5d * ff_val));
        }

		case VRDDU: {
            return 0.5d * spectrum.basicCoverage();
        }
		case AES:
		default: {
            double density =  spectrum.getRho();
            double diversity = (1 - spectrum.getSimpson());
            double uniqueness = spectrum.getAmbiguity();
            return density * diversity * uniqueness;
        }
		}
	}

	public static double metricToFitness(double metric) {
		return abs(0.5d - metric);
	}
}
