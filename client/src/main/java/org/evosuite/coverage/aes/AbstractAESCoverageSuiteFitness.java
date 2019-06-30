package org.evosuite.coverage.aes;

//import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
//import org.evosuite.Properties;
import org.evosuite.coverage.aes.branch.BranchDetails;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
//import java.util.Map;

import static java.lang.Math.abs;

//mycode starts
//import org.nd4j.linalg.io.ClassPathResource;
//mycode ends

public abstract class AbstractAESCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 5184507726269266351L;
	private static long iteration = 0; //mycode
    final double THRESHOLD = 0.000001;
//	private static String model_path;
//    MultiLayerNetwork model;


	public static enum Metric { AES, DTR, VDDU,VMDDU,VRDDU,VCDDU,VCMDDU1,VCMDDU2};		//New enum added 1
	private Metric metric;

	public AbstractAESCoverageSuiteFitness(Metric metric) {
		this.metric = metric;
//        BufferedReader br = null;
//        File file = new File("/tmp/model_path7.txt");
//        try {
//                br = new BufferedReader(new FileReader(file));
//            } catch (FileNotFoundException e) {
//                System.out.println("File doesn't exist");
//                e.printStackTrace();
//
//            }
//            try {
//                model_path = br.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

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
    protected abstract Map<Integer,Double> getWeights();    //mycode, returns the likelihood weights
    protected abstract double getSumWeights();              //mycode, returns the sum of likelihood weights

    public void appendStrToFile(String fileName,
                                String str)
    {
        try {

            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, true));
            out.write(str);
            out.close();
        }
        catch (IOException e) {
            System.out.println("exception occoured" + e);
        }
    }

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
//    public double[] getPCA(double[][] mySpectrum)
//    {
//        double[] features = new double[10];
//        for(int i=0;i<10;i++)
//            features[i] = 0;
//        INDArray activity_mtrx = Nd4j.create(mySpectrum);
//        long[] shape = activity_mtrx.shape();
//        if(shape[1] == 1)
//            return features;
//
//        //  center and scale the data
//        INDArray mean = activity_mtrx.mean(1);
//        INDArray std_dev = activity_mtrx.std(1);
//
//        for(int i=0;i<shape[1];i++)
//        {
//            for(int j=0;j<shape[0];j++)
//            {
//                double val = activity_mtrx.getDouble(j,i);
//                if(std_dev.getDouble(i) == 0.0)
//                    activity_mtrx.putScalar(new int[] {j,i},0.0);
//                else
//                    activity_mtrx.putScalar(new int[] {j,i},((val - mean.getDouble(i))/std_dev.getDouble(i)));
//
//            }
//
//        }
//
//        PCA mypca = new PCA(activity_mtrx);
//        INDArray cov =  mypca.covarianceMatrix(activity_mtrx);
//        INDArray principalC = principalComponents(cov);
//        for(int i=0;i<10;i++)
//            features[i] = principalC.getDouble(1,i)
//
//
//    }





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
//        double mean = 2.702036492668407064e-01;
//        double std_dev = 2.602917479375488896e-01;
//        double max_val = 1.6190573910443273;
//        double min_val = -3.1802215920491417;
        double[][] ochiai = spectrum.compute_ochiai();
        if(ochiai == null)
            return Double.MAX_VALUE;
        int components = spectrum.getNumComponents();
        double[] ochiai_mean = new double[components];
        for(int i=0;i<components;i++)
            ochiai_mean[i] = compute_mean(ochiai[i],components);
        return compute_mean(ochiai_mean,components);
////        result =  ((result - mean) / std_dev);
//        result = (result - min_val) / (max_val - min_val);
//        if(result<0d)
//            return 0d;
//        if(result>1d)
//            return 1d;
//        return result;
    }

    public double one_hot_dist_mean_metric(Spectrum spectrum)
    {
//        double mean = 3.890780293711493254e+00;
//        double std_dev = 1.809356985849612354e+00;
//        double max_val = 2.287275214843024;
//        double min_val = -2.876011406839377;
        double[][] ochiai = spectrum.compute_ochiai();
        if(ochiai == null)
            return Double.MAX_VALUE;
        int components = spectrum.getNumComponents();
        double[] one_hot_vec_dists = compute_dist_one_hot_vector(ochiai,components);
        double result = compute_mean(one_hot_vec_dists,components);
//        return result / (components-1);
        return result;
//        result =  ((result - mean) / std_dev);
//        result =  (result - min_val) / (max_val - min_val);
//        if(result<0d)
//            return 0d;
//        if(result>1d)
//            return 1d;
//        return result;

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


//    public void normalize_weights(Map<Integer,Double> A)
//    {
//        Double sum = 0d;
//        for(Map.Entry<Integer,Double> entry : A.entrySet())
//            sum = sum + entry.getValue();
//        for(Map.Entry<Integer,Double> entry : A.entrySet())
//            A.put(entry.getKey(),(entry.getValue()/sum));
//
//    }


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
                avg_val[i] = (ones / (components - 1));
            }
        }
        double sumWeights = 0d;
        if(weights == null)
            return compute_mean(avg_val,components);


        double sum = 0d;
        for(int i=0;i<components;i++) {

//            String s = String.valueOf(weights.get(i)) + "," +String.valueOf(avg_val[i]); //temp
            sumWeights = sumWeights + weights.get(i);
            avg_val[i] = avg_val[i] * weights.get(i);
//            s = s + "," +String.valueOf(avg_val[i]) + "\n"; //temp
//            if(iteration == 30) //temp
//                appendStrToFile("/tmp/printing.csv", s);
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
        //Changed VMDDU flag to return VDDU * branch_coverage
        //case VMDDU:
        //return spectrum.getVMrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
        case VMDDU:{
            //mycode starts
            //lambda implementation

//            iteration++;
//            Aj aj = spectrum.getVrho2();
//            double rho_component = aj.getvcd();
//            double rho_transaction = aj.getvrd();
//
//
//            //double result =  ((0.0294493 * spectrum.basicCoverage()) + (( -0.82273926) * spectrum.getRho()) + (0.10903152 * (1 - spectrum.getSimpson())) + ((-0.39320752) * spectrum.getAmbiguity()) +
//            //        ((-0.33318195) * rho_transaction) + ((-1.00889138) * rho_component) +  (0.73490905));
//
//
//            double[] mydata = new double[6];
//            mydata[0] = spectrum.basicCoverage();
//            mydata[1] = spectrum.getRho();
//            mydata[2] = (1 - spectrum.getSimpson());
//            mydata[3] = spectrum.getAmbiguity();
//            mydata[4] = rho_transaction;
//            mydata[5] = rho_component;
//            int matrix_size = spectrum.getNumTransactions();
//
//
//            String data_dump = "";
//            data_dump = String.valueOf(iteration) + "," + String.valueOf(mydata[0]) + "," + String.valueOf(mydata[1]) + "," + String.valueOf(mydata[2]) + ","
//                    + String.valueOf(mydata[3]) + "," + String.valueOf(mydata[4]) + "," + String.valueOf(mydata[5]) + ","
//                    + String.valueOf(matrix_size);
//
//            //normalize the features
//            mydata[0] = (mydata[0] - 6.528579662598990030e-01) / 3.091870017158382389e-01;
//            mydata[1] = (mydata[1] - 2.830355552505142147e-01) / 2.559463383939822312e-01;
//            mydata[2] = (mydata[2] - 8.741161545340463412e-01) / 2.935632591125947877e-01;
//            mydata[3] = (mydata[3] - 2.417415102720089359e-01) / 1.476727560690869467e-01;
//            mydata[4] = (mydata[4] - 9.075997634198873509e-01) / 7.665414057345444621e-02;
//            mydata[5] = (mydata[5] - 6.171747989139511059e-01) / 2.047115140756818608e-01;
//
//
//
//            INDArray test_data = Nd4j.create(1, 6);
//            INDArray myrow = Nd4j.create(mydata);
//            test_data.putRow(0, myrow);
//            String model_path = "";
//            MultiLayerNetwork model;
//            BufferedReader br = null;
//            File file = new File("/tmp/model_path6.txt");
//
//
//            try {
//                br = new BufferedReader(new FileReader(file));
//            } catch (FileNotFoundException e) {
//                System.out.println("File doesn't exist");
//                e.printStackTrace();
//                //return;
//            }
//            try {
//                model_path = br.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            try {
//
//                model = KerasModelImport.importKerasSequentialModelAndWeights(model_path);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            double result = model.output(test_data).getDouble(0);
//
//
//            if (result < 0)
//                result = 0;
//            else if (result > 1)
//                result = 1;
//
//
//            double lambda = 1 - Math.min(1, (((double) iteration) / 10000));
//
//            double new_result = (lambda * spectrum.basicCoverage()) + ((1 - lambda) * (1 - result));
//
//
//            data_dump = "Start" + "," + data_dump + "," + String.valueOf(result) + "," + String.valueOf(new_result) + "," +"end" + "\n";
//            appendStrToFile("/tmp/feature_dump_Time_VMDDU.csv", data_dump);
//
//            return (0.5 * new_result);
            //mycode ends
            return spectrum.getVrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity() * spectrum.basicCoverage();
        }

        case VCDDU:{


            //mycode starts
            //switching implementation

//            iteration++;
//
//            Aj aj = spectrum.getVrho2();
//            double rho_component = aj.getvcd();
//            double rho_transaction = aj.getvrd();
//
//
//            //double result =  ((0.0294493 * spectrum.basicCoverage()) + (( -0.82273926) * spectrum.getRho()) + (0.10903152 * (1 - spectrum.getSimpson())) + ((-0.39320752) * spectrum.getAmbiguity()) +
//            //        ((-0.33318195) * rho_transaction) + ((-1.00889138) * rho_component) +  (0.73490905));
//
//
//            double[] mydata = new double[6];
//            mydata[0] = spectrum.basicCoverage();
//            mydata[1] = spectrum.getRho();
//            mydata[2] = (1 - spectrum.getSimpson());
//            mydata[3] = spectrum.getAmbiguity();
//            mydata[4] = rho_transaction;
//            mydata[5] = rho_component;
//            int matrix_size = spectrum.getNumTransactions();
//
//
//            String data_dump = "";
//            data_dump = String.valueOf(iteration) + "," + String.valueOf(mydata[0]) + "," + String.valueOf(mydata[1]) + "," + String.valueOf(mydata[2]) + ","
//                    + String.valueOf(mydata[3]) + "," + String.valueOf(mydata[4]) + "," + String.valueOf(mydata[5]) + ","
//                    + String.valueOf(matrix_size);
//
//
//            mydata[0] = (mydata[0] - 6.528579662598990030e-01) / 3.091870017158382389e-01;
//            mydata[1] = (mydata[1] - 2.830355552505142147e-01) / 2.559463383939822312e-01;
//            mydata[2] = (mydata[2] - 8.741161545340463412e-01) / 2.935632591125947877e-01;
//            mydata[3] = (mydata[3] - 2.417415102720089359e-01) / 1.476727560690869467e-01;
//            mydata[4] = (mydata[4] - 9.075997634198873509e-01) / 7.665414057345444621e-02;
//            mydata[5] = (mydata[5] - 6.171747989139511059e-01) / 2.047115140756818608e-01;
//
//
//            //if iteration number is less than c then return branch coverage metric, else return nn prediction
//            if (iteration <= 1000 && (spectrum.basicCoverage() <= 0.6)) {
//                data_dump = "Start" + "," + data_dump + "," + String.valueOf(spectrum.basicCoverage()) + "," + "0" + "end" + "\n";
//                appendStrToFile("/tmp/feature_dump_VCDDU.csv", data_dump);
//                //data_dump = "";
//                return (0.5 * spectrum.basicCoverage());
//            }
//
//            INDArray test_data = Nd4j.create(1, 6);
//            INDArray myrow = Nd4j.create(mydata);
//            test_data.putRow(0, myrow);
//            String model_path = "";
//            MultiLayerNetwork model;
//            BufferedReader br = null;
//            File file = new File("/tmp/model_path5.txt");
//            //File file = new File("/home/abhijit/Thesis/repos/evo_vddu/evo_iteration_dump/iteration_number.txt");
//
//            try {
//                br = new BufferedReader(new FileReader(file));
//            } catch (FileNotFoundException e) {
//                System.out.println("File doesn't exist");
//                e.printStackTrace();
//                //return;
//            }
//            try {
//                model_path = br.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                //String simpleMlp = new ClassPathResource("/home/abhijit/Desktop/test_model.h5").getFile().getPath();
//                // String simpleMlp = "/home/abhijit/Desktop/test_model.h5";
//                // System.out.println(simpleMlp);
//                model = KerasModelImport.importKerasSequentialModelAndWeights(model_path);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            double result = model.output(test_data).getDouble(0);
//
//
//            if (result < 0)
//                result = 0;
//            else if (result > 1)
//                result = 1;
//            //return (0.5*spectrum.basicCoverage())*(0.5 - (0.5 * result));
//
//
//            data_dump = "Start" + "," + data_dump + "," + String.valueOf(result) + "," + "1" + "end" + "\n";
//            appendStrToFile("/tmp/feature_dump_VCDDU.csv", data_dump);
//            //data_dump = "";
//            return (0.5 - (0.5 * result));
             return spectrum.getVCrho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
        }
            //mycode ends

		case VCMDDU1: {
//////            return 0.5d - (0.5d * mean_mean_metric(spectrum));
//            iteration++;
//            double max_min_val = max_mean_metric(spectrum);
//            double coverage = spectrum.basicCoverage();
//            double density = spectrum.getRho();
//            double diversity = (1 - spectrum.getSimpson());
//            double uniqueness = spectrum.getAmbiguity();
//            String txttoprint = String.valueOf(iteration) + "," + String.valueOf(coverage) + "," + String.valueOf(density) + "," + String.valueOf(diversity) +
//                    "," + String.valueOf(uniqueness) + "," + String.valueOf(max_min_val) + "\n";
//            appendStrToFile("/tmp/ff3_val.txt", txttoprint);
//            if(((iteration % 500) == 1) && (spectrum.getActivityMatrix() != null))
//                printActivityMatrix(spectrum.getActivityMatrix(), "/home/ubuntu/abhijitc/activity_matrices/",iteration,"FF3",spectrum.getNumTransactions(),spectrum.getNumComponents(),"/tmp/FF3_activity_matrix_index.txt");
//            return 0.5d * (1 - max_min_val);
//        }
            //mycode starts
//            iteration++;
//            //distance feature added
//            Aj aj = spectrum.getVrho2();
//            double rho_component = aj.getvcd();
//            double rho_transaction = aj.getvrd();
//            double[] distances = spectrum.getDistances();
//
//            double[] mydata = new double[9];
//            mydata[0] = spectrum.basicCoverage();
//            mydata[1] = 1 - abs(1 - (2 * spectrum.getRho()));
//            mydata[2] = (1 - spectrum.getSimpson());
//            mydata[3] = spectrum.getAmbiguity();
//            mydata[4] = rho_transaction;
//            mydata[5] = rho_component;
//            mydata[6] = distances[0];
//            mydata[7] = distances[1];
//            mydata[8] = distances[2];
//
//            //normalise the data
//            ArrayList<String> means = getMean("/tmp/mean_VCMDDU1");
//            ArrayList<String> std_devs = getStd_dev("/tmp/std_dev_VCMDDU1");
//
//            for(int i=0;i<means.size();i++)
//            {
//                double std_dev  = Double.parseDouble(std_devs.get(i));
//                if (Math.abs(std_dev)<THRESHOLD)
//                    mydata[i] = 0.0;
//                else
//                    mydata[i] = (mydata[i] - (Double.parseDouble(means.get(i)))) / std_dev;
//            }
//
//            INDArray test_data = Nd4j.create(1, 9);
//            INDArray myrow = Nd4j.create(mydata);
//            test_data.putRow(0, myrow);
//            String model_path = null;
//            MultiLayerNetwork model;
//            BufferedReader br = null;
//            File file = new File("/tmp/model_path7.txt");
//            try {
//                br = new BufferedReader(new FileReader(file));
//            } catch (FileNotFoundException e) {
//                System.out.println("File doesn't exist");
//                e.printStackTrace();
//            }
//            try {
//                model_path = br.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            try {
//
//                model = KerasModelImport.importKerasSequentialModelAndWeights(model_path);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            double result = model.output(test_data).getDouble(0);
//
//
//            if (result < 0)
//                result = 0d;
//            else if (result > 1)
//                result = 1d;
//
//            double lambda = (1 - Math.min(1, (((double) iteration) / 10000)));
//
//            double new_result = (lambda * spectrum.basicCoverage()) + ((1-lambda) * (1 - result));
//
//
//            String txttodump = String.valueOf(iteration) + "," + String.valueOf(result) + "," + String.valueOf(spectrum.basicCoverage())
//            +","+String.valueOf(spectrum.getRho())+"," + String.valueOf(1 - spectrum.getSimpson())+ "," + String.valueOf(spectrum.getAmbiguity())+
//            ","+ String.valueOf(new_result) + "," + "\n";
//
//            appendStrToFile("/tmp/nn_val.txt",txttodump);
//            return (0.5 * new_result);
            return spectrum.getVCMrho1() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
        }
        //mycode ends

		case VCMDDU2: {
            iteration++;
//		    return 0.5d - (0.5d * one_hot_dist_mean_metric(spectrum));
            double ff_val = number_of_1s_metric(spectrum,getWeights());


            double coverage = spectrum.basicCoverage();
            double density =  spectrum.getRho();
            double diversity = (1 - spectrum.getSimpson());
            double uniqueness = spectrum.getAmbiguity();
            Aj aj = spectrum.getVrho2();
            double rho_component = aj.getvcd();
            double rho_transaction = aj.getvrd();
            String filename = System.getenv("FEATURE_DUMP_LOC") + "/feature_dump.csv";
            String txttoprint = String.valueOf(iteration) + "," + String.valueOf(coverage) + "," + String.valueOf(density) + "," + String.valueOf(diversity) +
                    "," + String.valueOf(uniqueness) + "," +String.valueOf(rho_transaction) + "," + String.valueOf(rho_component)+ ","
                    + String.valueOf(ff_val) + "\n";
            appendStrToFile(filename, txttoprint);

            if(((iteration % 100) == 1) && (spectrum.getActivityMatrix() != null))
                printActivityMatrix(spectrum.getActivityMatrix(), spectrum.getNumTransactions(),spectrum.getNumComponents(),System.getenv("SPEC_DUMP_LOC"));
            return  (0.5d - (0.5d * ff_val));
        }
//		{
//            //mycode starts
//            iteration++;
//            //feature power set implementation
//            Aj aj = spectrum.getVrho2();
//            double rho_component = aj.getvcd();
//            double rho_transaction = aj.getvrd();
//            double[] distances = spectrum.getDistances();
//
//
//            double[] mydata = new double[9];
//            mydata[0] = spectrum.basicCoverage();
//            mydata[1] = 1 - abs(1 - (2 * spectrum.getRho()));
//            mydata[2] = (1 - spectrum.getSimpson());
//            mydata[3] = spectrum.getAmbiguity();
//            mydata[4] = rho_transaction;
//            mydata[5] = rho_component;
//            mydata[6] = distances[0];
//            mydata[7] = distances[1];
//            mydata[8] = distances[2];
//            //int matrix_size = spectrum.getNumTransactions();
//
//            //first normalise the 9 features
//            ArrayList<String> means1 = getMean("/tmp/mean1_VCMDDU2");
//            ArrayList<String> std_devs1 = getStd_dev("/tmp/std_dev1_VCMDDU2");
//
//            for(int i=0;i<means1.size();i++)
//            {
//                double std_dev  = Double.parseDouble(std_devs1.get(i));
//                if(std_dev == 0)
//                    mydata[i] = 0.0;
//                else
//                    mydata[i] = (mydata[i] - (Double.parseDouble(means1.get(i)))) / std_dev;
//            }
//
//            //compute the quad set
//            double quad_feature_set[] = compute_quadset(mydata,9);
//
//            //normalise the quad set
//            ArrayList<String> means2 = getMean("/tmp/mean2_VCMDDU2");
//            ArrayList<String> std_devs2 = getStd_dev("/tmp/std_dev2_VCMDDU2");
//
//            for(int i=0;i<means2.size();i++)
//            {
//                double std_dev  = Double.parseDouble(std_devs2.get(i));
//                if(std_dev == 0)
//                    quad_feature_set[i] = 0.0;
//                else
//                    quad_feature_set[i] = (quad_feature_set[i] - (Double.parseDouble(means2.get(i)))) / std_dev;
//            }
//
//
//            INDArray test_data = Nd4j.create(1, 45);
//            INDArray myrow = Nd4j.create(quad_feature_set);
//            test_data.putRow(0, myrow);
//            String model_path = "";
//            MultiLayerNetwork model;
//            BufferedReader br = null;
//            File file = new File("/tmp/model_path8.txt");
//            //File file = new File("/home/abhijit/Thesis/repos/evo_vddu/evo_iteration_dump/iteration_number.txt");
//
//            try {
//                br = new BufferedReader(new FileReader(file));
//            } catch (FileNotFoundException e) {
//                System.out.println("File doesn't exist");
//                e.printStackTrace();
//                //return;
//            }
//            try {
//                model_path = br.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            try {
//
//                model = KerasModelImport.importKerasSequentialModelAndWeights(model_path);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//            double result = model.output(test_data).getDouble(0);
//
//
//
//            if (result < 0)
//                result = 0;
//            else if (result > 1)
//                result = 1;
//
//            double lambda = 1 - Math.min(1, (((double) iteration) / 10000));
//
//            double new_result = (lambda * spectrum.basicCoverage()) + ((1 - lambda) * (1 - result));
//
//
//            return (0.5 * new_result);
//        }
        //mycode ends
			//return spectrum.getVCMrho2() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
		case VRDDU:
		    return spectrum.getVRrho();
		case AES:
		default: {
//            iteration++;
            return spectrum.getRho() * (1.0 - spectrum.getSimpson()) * spectrum.getAmbiguity();
//            double coverage = spectrum.basicCoverage();
//            double density =  spectrum.getRho();
//            double diversity = (1 - spectrum.getSimpson());
//            double uniqueness = spectrum.getAmbiguity();
//            Aj aj = spectrum.getVrho2();
//            double rho_component = aj.getvcd();
//            double rho_transaction = aj.getvrd();
//            double ff4 = number_of_1s_metric(spectrum,getWeights());
//            String filename = "/tmp/feature_dump_d14_ddu.csv";
//            String txttoprint = String.valueOf(iteration) + "," + String.valueOf(coverage) + "," + String.valueOf(density) + "," + String.valueOf(diversity) +
//                    "," + String.valueOf(uniqueness) + "," +String.valueOf(rho_transaction) + "," + String.valueOf(rho_component)+ ","
//                    + String.valueOf(ff4) + "," + String.valueOf(ff_val) + "\n";
//            appendStrToFile(filename, txttoprint);
//
////            String txtToPrint = Properties.OUTPUT_DIR + "\n";
////            appendStrToFile("/tmp/temp_dump.csv", txtToPrint);
////            if(((iteration % 500) == 1) && (spectrum.getActivityMatrix() != null))
////                printActivityMatrix(spectrum.getActivityMatrix(), "/home/ubuntu/abhijitc/activity_matrices/",iteration,"DDU",spectrum.getNumTransactions(),spectrum.getNumComponents(),"/tmp/DDU_activity_matrix_index.txt");
//            return ff_val;
        }
		}
	}

	public static double metricToFitness(double metric) {
		return abs(0.5d - metric);
	}


//	private void printmyhashmap(Map<Integer,Double> A)
//    {
//        if(A == null)
//            return;
//        BufferedWriter out = null;
//
//        for(Map.Entry<Integer,Double> entry : A.entrySet())
//        {
//            try {
//                // Open given file in append mode.
//                String str = String.valueOf(entry.getKey()) + "," + String.valueOf(entry.getValue()) + "\n";
//                out = new BufferedWriter(
//                        new FileWriter("/tmp/weights_normalised.csv", true));
//                out.write(str);
//                out.close();
//            }
//            catch (IOException e) {
//                System.out.println("exception occoured" + e);
//            }
//        }
//
//
//    }

}
