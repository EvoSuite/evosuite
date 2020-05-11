package org.evosuite.coverage.aes;

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

		updateIndividual(suite, fitness);
		suite.setCoverage(this, metric_value);

		return fitness;
	}

	protected abstract Spectrum getSpectrum(List<ExecutionResult> results);
    protected abstract Map<Integer,Double> getWeights();    //mycode, returns the likelihood weights
    protected abstract double getSumWeights();              //mycode, returns the sum of likelihood weights

    public ArrayList<String> getMean(String file_path)
    {
        ArrayList<String> means = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    file_path));
            String line = reader.readLine();
            while (line != null) {
                means.add(line);
                // read next line
                line = reader.readLine();

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return means;

    }

    public ArrayList<String> getStd_dev(String file_path)
    {
        ArrayList<String> std_devs = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    file_path));
            String line = reader.readLine();
            while (line != null) {
                std_devs.add(line);
                // read next line
                line = reader.readLine();

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return std_devs;

    }



    public double[] powerset(double[] features,int size)
    {
        int pow_set_size = ((int)Math.pow(2, size));
        double powset[] = new double[pow_set_size-1];
        int pow_index = 0;
        for(int counter=1;counter<pow_set_size;counter++)
        {
            double temp = 1.0;
            for(int j=0;j<size;j++)
            {
                if((counter & (1 << j)) > 0)
                {
                    temp = temp * features[j];
                }

            }
            powset[pow_index] = temp;
            pow_index++;
        }

        return powset;

    }

    public double[] compute_quadset(double[] features,int size)
    {
        int quad_set_size = size + ((size * (size -1)) / 2);
        double quadset[] = new double[quad_set_size];

        //copy the feature set to quadset first
        for(int i=0;i<size;i++)
            quadset[i] = features[i];
        int quad_index = size;
        //compute the pairwise multiplication and add it to the quadset
        for(int i=0;i<size;i++)
        {
            for(int j=i+1;j<size;j++)
            {
                quadset[quad_index] = quadset[i] * quadset[j];
                quad_index++;
            }

        }

        return quadset;

    }

    public double compute_euclid_dist(double[] A, double[] B, int size)
    {
        double result = 0d;
        for(int i=0;i<size;i++)
        {
            result = result + Math.pow(Math.abs(A[i] - B[i]),2);
        }
        return Math.sqrt(result);
    }


    public double[] compute_dist_one_hot_vector(double[][] ochiai, int components)
    {
        double[] one_hot_vec = new double[components];
        double[] result = new double[components];

        for(int i=0;i<components;i++)
        {
            one_hot_vec[i] = 1d;
            result[i] = compute_euclid_dist(one_hot_vec,ochiai[i],components);
            // printer(one_hot_vec,components);
            one_hot_vec[i] = 0d;
        }

        return result;

    }

    public double compute_mean(double[] A, int components)
    {
        double sum = 0d;
        for(int i=0;i<components;i++)
            sum = sum + A[i];
        return (sum / components);
    }


    public double mean_mean_metric(Spectrum spectrum)
    {
        double[][] ochiai = spectrum.compute_ochiai();
        if(ochiai == null)
            return Double.MAX_VALUE;
        int components = spectrum.getNumComponents();
        double[] ochiai_mean = new double[components];
        for(int i=0;i<components;i++)
            ochiai_mean[i] = compute_mean(ochiai[i],components);
        return compute_mean(ochiai_mean,components);
    }

    public double one_hot_dist_mean_metric(Spectrum spectrum)
    {
        double[][] ochiai = spectrum.compute_ochiai();
        if(ochiai == null)
            return Double.MAX_VALUE;
        int components = spectrum.getNumComponents();
        double[] one_hot_vec_dists = compute_dist_one_hot_vector(ochiai,components);
        double result = compute_mean(one_hot_vec_dists,components);
        return result;

    }

    public double compute_log(double[] A, int size)
    {
        double sum = 0d;
        for(int i=0;i<size;i++)
        {
            if(A[i] > 0)
                sum += Math.log(A[i]);
        }
        return Math.exp(sum/size);
    }

    public double max_mean_metric(Spectrum spectrum)
    {
        double[][] ochiai = spectrum.compute_ochiai();
        if(ochiai == null)
            return 1d;
        int components = spectrum.getNumComponents();
        double[] max_mean = new double[components];
        for(int i=0;i<components;i++)
        {
            if(Math.abs(ochiai[i][i])<THRESHOLD)
                max_mean[i] = 1d;
            else
            {
                double max_val = -1d;
                for(int j=0;j<components;j++)
                {
                    if( (j!=i) && (ochiai[i][j] > max_val))
                        max_val = ochiai[i][j];
                }
                max_mean[i] = max_val;
            }
        }
        return compute_mean(max_mean,components);
    }

    public double compute_FF4(Spectrum spectrum, Map<Integer,Double> weights)
    {

        double[] wweVec = spectrum.compute_WWE_vec();
        if(wweVec == null)
            return 1d;
        int components = spectrum.getNumComponents();

        if(weights == null)
            return compute_mean(wweVec,components);

        double sumWeights = 0d;
        double sum = 0d;
        for(int i=0;i<components;i++) {
            sumWeights = sumWeights + weights.get(i);
            wweVec[i] = wweVec[i] * weights.get(i);
            sum = sum + wweVec[i];
        }
        return sum/(sumWeights);

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

    public static int getActivityMatrixIndex(String fileName)
    {
        BufferedReader reader;
        int val = 0;
        try {
            reader = new BufferedReader(new FileReader(
                    fileName));
            val = Integer.parseInt(reader.readLine());
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, false));
            out.write(String.valueOf(val+1));
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
        return val;

    }
    public static void printActivityMatrix(ArrayList<BitSet> A,  int rowsize, int colsize,String filelocation)
    {
        String toprint = "";
        for (BitSet transaction : A)
        {
            for(int j=0;j<colsize;j++)
            {
                if(transaction.get(j))
                    toprint += "1" + " ";

                else
                    toprint += "0" + " ";

            }
            toprint = toprint.substring(0, toprint.length() - 1);
            toprint += "\n";
        }
        String fileName = filelocation + "/activity_matrix"+"_"+ String.valueOf(iteration);
        try {


            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, false));
            out.write(toprint);
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }


    public double getMetric(Spectrum spectrum) {
		switch (this.metric) {
        case DTR:
            return spectrum.getDistinctTransactionsRho() * spectrum.getAmbiguity();

        case VDDU:
            return spectrum.getVrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();

        case VMDDU:{
            return 1.0 - spectrum.getSecondOrderDiversity();
        }

        case VCDDU:{

            double ff_val = compute_FF4(spectrum,getWeights());
            return 0.5d * spectrum.basicCoverage() * (1 - ff_val);
        }

		case VCMDDU1: {
            double ff_val = number_of_1s_metric(spectrum,null);
            return spectrum.getRho() * (1.0 - spectrum.getSimpson()) * (1.0 - ff_val);
        }

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
