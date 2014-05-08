package org.evosuite.ga.metaheuristics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.crossover.SBXCrossover;
import org.evosuite.ga.operators.selection.TournamentSelectionCrowdedComparison;
import org.evosuite.ga.problems.FON;
import org.evosuite.ga.problems.OneVariableProblem;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.problems.SCH;
import org.evosuite.ga.problems.ZDT4;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * NSGA-II test
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestNSGAII
{
	@BeforeClass
	public static void setUp() {
	    Properties.POPULATION = 100;
		Properties.SEARCH_BUDGET = 250;
		Properties.CROSSOVER_RATE = 0.9;
		Properties.RANDOM_SEED = 1l;
	}

	@Test
	public void testFastNonDominatedSort()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);

		Problem p = new OneVariableProblem<NSGAChromosome>();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        ga.addFitnessFunctions(fitnessFunctions);

		NSGAChromosome c1 = new NSGAChromosome();
		NSGAChromosome c2 = new NSGAChromosome();
		NSGAChromosome c3 = new NSGAChromosome();
		NSGAChromosome c4 = new NSGAChromosome();
		NSGAChromosome c5 = new NSGAChromosome();
		NSGAChromosome c6 = new NSGAChromosome();
		NSGAChromosome c7 = new NSGAChromosome();
		NSGAChromosome c8 = new NSGAChromosome();
		NSGAChromosome c9 = new NSGAChromosome();
		NSGAChromosome c10 = new NSGAChromosome();

		// Set Fitness
		c1.setFitness(fitnessFunctions.get(0), 0.6);
		c2.setFitness(fitnessFunctions.get(0), 0.2);
		c3.setFitness(fitnessFunctions.get(0), 0.4);
		c4.setFitness(fitnessFunctions.get(0), 0.0);
		c5.setFitness(fitnessFunctions.get(0), 0.8);
		c6.setFitness(fitnessFunctions.get(0), 0.8);
		c7.setFitness(fitnessFunctions.get(0), 0.2);
		c8.setFitness(fitnessFunctions.get(0), 0.4);
		c9.setFitness(fitnessFunctions.get(0), 0.6);
		c10.setFitness(fitnessFunctions.get(0), 0.0);

		List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
		population.add(c1);
		population.add(c2);
		population.add(c3);
		population.add(c4);
		population.add(c5);
		population.add(c6);
		population.add(c7);
		population.add(c8);
		population.add(c9);
		population.add(c10);

		List<List<NSGAChromosome>> fronts = ga.fastNonDominatedSort(population, Properties.POPULATION);

		// Front 0
		Assert.assertTrue(fronts.get(0).get(0).getFitness() == 0.0);
		Assert.assertTrue(fronts.get(0).get(1).getFitness() == 0.0);

		// Front 1
		Assert.assertTrue(fronts.get(1).get(0).getFitness() == 0.2);
		Assert.assertTrue(fronts.get(1).get(1).getFitness() == 0.2);

		// Front 2
		Assert.assertTrue(fronts.get(2).get(0).getFitness() == 0.4);
		Assert.assertTrue(fronts.get(2).get(1).getFitness() == 0.4);

		// Front 3
		Assert.assertTrue(fronts.get(3).get(0).getFitness() == 0.6);
		Assert.assertTrue(fronts.get(3).get(1).getFitness() == 0.6);

		// Front 4
		Assert.assertTrue(fronts.get(4).get(0).getFitness() == 0.8);
		Assert.assertTrue(fronts.get(4).get(1).getFitness() == 0.8);
	}

	@Test
	public void testCrowingDistanceAssignment_OneVariable()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);

		Problem p = new OneVariableProblem();
		List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
		ga.addFitnessFunctions(fitnessFunctions);

		NSGAChromosome c1 = new NSGAChromosome();
		NSGAChromosome c2 = new NSGAChromosome();
		NSGAChromosome c3 = new NSGAChromosome();
		NSGAChromosome c4 = new NSGAChromosome();
		NSGAChromosome c5 = new NSGAChromosome();
		NSGAChromosome c6 = new NSGAChromosome();
		NSGAChromosome c7 = new NSGAChromosome();
		NSGAChromosome c8 = new NSGAChromosome();
		NSGAChromosome c9 = new NSGAChromosome();
		NSGAChromosome c10 = new NSGAChromosome();

		// Set Fitness
		c1.setFitness(fitnessFunctions.get(0), 0.0);
		c2.setFitness(fitnessFunctions.get(0), 0.2);
		c3.setFitness(fitnessFunctions.get(0), 0.4);
		c4.setFitness(fitnessFunctions.get(0), 0.6);
		c5.setFitness(fitnessFunctions.get(0), 0.8);
		c6.setFitness(fitnessFunctions.get(0), 0.0);
		c7.setFitness(fitnessFunctions.get(0), 0.2);
		c8.setFitness(fitnessFunctions.get(0), 0.4);
		c9.setFitness(fitnessFunctions.get(0), 0.6);
		c10.setFitness(fitnessFunctions.get(0), 0.8);

		List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
		population.add(c1);
		population.add(c2);
		population.add(c3);
		population.add(c4);
		population.add(c5);
		population.add(c6);
		population.add(c7);
		population.add(c8);
		population.add(c9);
		population.add(c10);

		List<NSGAChromosome> ret = ga.crowingDistanceAssignment(population);
		Assert.assertTrue(ret.get(0).getDistance() == Double.MAX_VALUE);
		Assert.assertTrue(ret.get(ret.size() - 1).getDistance() == Double.MAX_VALUE);

		double epsilon = 1e-10;		
		Assert.assertTrue(Math.abs(0.25 - ret.get(1).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - ret.get(2).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - ret.get(3).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - ret.get(4).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - ret.get(5).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - ret.get(6).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - ret.get(7).getDistance()) < epsilon);
		Assert.assertTrue(ret.get(8).getDistance() == 0.0);
	}

	@Test
    public void testCrowingDistanceAssignment_SeveralVariables()
	{
        NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);

        Problem p = new SCH();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        ga.addFitnessFunctions(fitnessFunctions);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();
        NSGAChromosome c3 = new NSGAChromosome();
        NSGAChromosome c4 = new NSGAChromosome();
        NSGAChromosome c5 = new NSGAChromosome();
        NSGAChromosome c6 = new NSGAChromosome();
        NSGAChromosome c7 = new NSGAChromosome();
        NSGAChromosome c8 = new NSGAChromosome();
        NSGAChromosome c9 = new NSGAChromosome();
        NSGAChromosome c10 = new NSGAChromosome();

        // Set Fitness 1
        c1.setFitness(fitnessFunctions.get(0), 0.0);
        c2.setFitness(fitnessFunctions.get(0), 0.2);
        c3.setFitness(fitnessFunctions.get(0), 0.4);
        c4.setFitness(fitnessFunctions.get(0), 0.6);
        c5.setFitness(fitnessFunctions.get(0), 0.8);
        c6.setFitness(fitnessFunctions.get(0), 0.0);
        c7.setFitness(fitnessFunctions.get(0), 0.2);
        c8.setFitness(fitnessFunctions.get(0), 0.4);
        c9.setFitness(fitnessFunctions.get(0), 0.6);
        c10.setFitness(fitnessFunctions.get(0), 0.8);

        // Set Fitness 2
        c1.setFitness(fitnessFunctions.get(1), 0.1);
        c2.setFitness(fitnessFunctions.get(1), 0.3);
        c3.setFitness(fitnessFunctions.get(1), 0.5);
        c4.setFitness(fitnessFunctions.get(1), 0.7);
        c5.setFitness(fitnessFunctions.get(1), 0.9);
        c6.setFitness(fitnessFunctions.get(1), 0.1);
        c7.setFitness(fitnessFunctions.get(1), 0.3);
        c8.setFitness(fitnessFunctions.get(1), 0.5);
        c9.setFitness(fitnessFunctions.get(1), 0.7);
        c10.setFitness(fitnessFunctions.get(1), 0.9);

        List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
        population.add(c1);
        population.add(c2);
        population.add(c3);
        population.add(c4);
        population.add(c5);
        population.add(c6);
        population.add(c7);
        population.add(c8);
        population.add(c9);
        population.add(c10);

        List<NSGAChromosome> ret = ga.crowingDistanceAssignment(population);
        Assert.assertTrue(ret.get(0).getDistance() == Double.MAX_VALUE);
        Assert.assertTrue(ret.get(ret.size() - 1).getDistance() == Double.MAX_VALUE);
        double epsilon = 0.000000000000001;
        Assert.assertTrue(Math.abs(0.5 - ret.get(1).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - ret.get(2).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - ret.get(3).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - ret.get(4).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - ret.get(5).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - ret.get(6).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - ret.get(7).getDistance()) < epsilon);
        Assert.assertTrue(ret.get(8).getDistance() == 0.0);
    }

	/**
     * Testing NSGA-II with FON Problem
     * 
     * @throws IOException 
     * @throws NumberFormatException 
     */
	@Test
    public void testFON() throws NumberFormatException, IOException
    {
	    Properties.MUTATION_RATE = 1d / 3d; // 3 because, FON problem has 3 variables

        ChromosomeFactory<?> factory = new RandomFactory(false, 3, -4.0, 4.0);

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        TournamentSelectionCrowdedComparison ts = new TournamentSelectionCrowdedComparison();
        ts.setMaximize(false);
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new FON();
        final FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        final FitnessFunction f2 = (FitnessFunction) p.getFitnessFunctions().get(1);
        ga.addFitnessFunction(f1);
        ga.addFitnessFunction(f2);

        // execute
        ga.generateSolution();

        List<Chromosome> chromosomes = (List<Chromosome>) ga.population;
        Collections.sort(chromosomes, new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome arg0, Chromosome arg1) {
                return Double.compare(arg0.getFitness(f1), arg1.getFitness(f1));
            }
        });

        // load Pareto Front
        double[] pareto_f1 = new double[Properties.POPULATION];
        double[] pareto_f2 = new double[Properties.POPULATION];
        int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("Fonseca.pf").getPath()));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            String[] split = sCurrentLine.split("\t");
            pareto_f1[index] = Double.valueOf(split[0]);
            pareto_f2[index] = Double.valueOf(split[1]);
            index++;
        }
        br.close();

        // test
        index = 0;
        for (Chromosome chromosome : chromosomes)
        {
            System.out.printf("%f,%f\n", chromosome.getFitness(f1), chromosome.getFitness(f2));

            //Assert.assertEquals(chromosome.getFitness(f1), pareto_f1[index], 0.05);
            //Assert.assertEquals(chromosome.getFitness(f2), pareto_f2[index], 0.05);
            index++;
        }
    }

	/**
     * Testing NSGA-II with OneVariable Problem
     */
	@Test
	public void testOneVariable()
	{
	    Properties.MUTATION_RATE = 1d / 1d; // 1 because, OneVariable problem has 1 variable

	    ChromosomeFactory<?> factory = new RandomFactory(false, 1, -10.0, 10.0);

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        TournamentSelectionCrowdedComparison ts = new TournamentSelectionCrowdedComparison();
        ts.setMaximize(false);
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new OneVariableProblem();
        FitnessFunction ff = (FitnessFunction) p.getFitnessFunctions().get(0);
        ga.addFitnessFunction(ff);
        ga.generateSolution();

        Assert.assertEquals(ga.population.size(), Properties.POPULATION);
        for (Chromosome population : ga.population)
            Assert.assertEquals(population.getFitness(), 0.0, 0.0);
	}

	/**
     * Testing NSGA-II with SCH Problem
     * 
	 * @throws IOException 
	 * @throws NumberFormatException 
     */
	@Test
    public void testSCH() throws NumberFormatException, IOException
    {
	    Properties.MUTATION_RATE = 1d / 1d; // 1 because, SCH problem has 1 variable

	    ChromosomeFactory<?> factory = new RandomFactory(false, 1, Math.pow(-10.0, 3), Math.pow(10.0, 3));

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        TournamentSelectionCrowdedComparison ts = new TournamentSelectionCrowdedComparison();
        ts.setMaximize(false);
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new SCH();
        final FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        final FitnessFunction f2 = (FitnessFunction) p.getFitnessFunctions().get(1);
        ga.addFitnessFunction(f1);
        ga.addFitnessFunction(f2);

        // execute
        ga.generateSolution();

        List<Chromosome> chromosomes = (List<Chromosome>) ga.population;
        Collections.sort(chromosomes, new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome arg0, Chromosome arg1) {
                return Double.compare(arg0.getFitness(f1), arg1.getFitness(f1));
            }
        });

        // load Pareto Front
        /*double[] pareto_f1 = new double[Properties.POPULATION];
        double[] pareto_f2 = new double[Properties.POPULATION];
        int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("Schaffer.pf").getPath()));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            String[] split = sCurrentLine.split("\t");
            pareto_f1[index] = Double.valueOf(split[0]);
            pareto_f2[index] = Double.valueOf(split[1]);
            index++;
        }
        br.close();

        // test
        index = 0;*/
        for (Chromosome chromosome : chromosomes)
        {
            System.out.printf("%f,%f\n", chromosome.getFitness(f1), chromosome.getFitness(f2));

            /*Assert.assertEquals(chromosome.getFitness(f1), pareto_f1[index], 0.06);
            Assert.assertEquals(chromosome.getFitness(f2), pareto_f2[index], 0.06);
            index++;*/
        }
    }

	/**
     * Testing NSGA-II with ZDT4 Problem
     * 
     * @throws IOException 
     * @throws NumberFormatException 
     */
	@Test
    public void testZDT4() throws NumberFormatException, IOException
    {
	    Properties.MUTATION_RATE = 1d / 10d; // 10 because, ZDT4 problem has 10 variable
	    Properties.TOURNAMENT_SIZE = 2;

        ChromosomeFactory<?> factory = new RandomFactory(true, 10, -5.0, 5.0);

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        TournamentSelectionCrowdedComparison ts = new TournamentSelectionCrowdedComparison();
        ts.setMaximize(false);
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossover());

        Problem p = new ZDT4();
        final FitnessFunction f1 = (FitnessFunction) p.getFitnessFunctions().get(0);
        final FitnessFunction f2 = (FitnessFunction) p.getFitnessFunctions().get(1);
        ga.addFitnessFunction(f1);
        ga.addFitnessFunction(f2);

        // execute
        ga.generateSolution();

        List<Chromosome> chromosomes = (List<Chromosome>) ga.population;
        Collections.sort(chromosomes, new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome arg0, Chromosome arg1) {
                return Double.compare(arg0.getFitness(f1), arg1.getFitness(f1));
            }
        });

        // load Pareto Front
        /*double[] pareto_f1 = new double[Properties.POPULATION];
	    double[] pareto_f2 = new double[Properties.POPULATION];
	    int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(ClassLoader.getSystemResource("ZDT4.pf").getPath()));
        String sCurrentLine;
        while ((sCurrentLine = br.readLine()) != null) {
            String[] split = sCurrentLine.split("\t");
            pareto_f1[index] = Double.valueOf(split[0]);
            pareto_f2[index] = Double.valueOf(split[1]);
            index++;
        }
        br.close();

        // test
        index = 0;*/
        for (Chromosome chromosome : chromosomes)
        {
            System.out.printf("%f,%f\n", chromosome.getFitness(f1), chromosome.getFitness(f2));

            /*Assert.assertEquals(chromosome.getFitness(f1), pareto_f1[index], 0.05);
            Assert.assertEquals(chromosome.getFitness(f2), pareto_f2[index], 0.05);
            index++;*/
        }
    }
}
