package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.problems.multiobjective.SCH;
import org.evosuite.ga.problems.singleobjective.Booths;
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

		Problem p = new Booths<NSGAChromosome>();
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

		/*List<List<NSGAChromosome>> fronts = ga.fastNonDominatedSort(population, Properties.POPULATION);

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
		Assert.assertTrue(fronts.get(4).get(1).getFitness() == 0.8);*/
	}

	@Test
	public void testCrowingDistanceAssignment_OneVariable()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);

		Problem p = new Booths();
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

		/*List<NSGAChromosome> ret = ga.crowingDistanceAssignment(population);
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
		Assert.assertTrue(ret.get(8).getDistance() == 0.0);*/
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

        /*List<NSGAChromosome> ret = ga.crowingDistanceAssignment(population);
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
        Assert.assertTrue(ret.get(8).getDistance() == 0.0);*/
    }

	@Test
    public void testCrowdedComparisonOperator()
    {
	    NSGAII<NSGAChromosome> ga = new NSGAII(null);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();
        NSGAChromosome c3 = new NSGAChromosome();

        // Set Rank
        c1.setRank(1);
        c2.setRank(0);
        c3.setRank(0);

        // Set Distance
        c1.setDistance(0.1);
        c2.setDistance(0.5);
        c3.setDistance(0.4);

        List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
        population.add(c1);
        population.add(c2);
        population.add(c3);

        //ga.crowdedComparisonOperator(population);

        // assert by Rank
        Assert.assertTrue(population.get(0).getRank() == 0);
        Assert.assertTrue(population.get(1).getRank() == 0);
        Assert.assertTrue(population.get(2).getRank() == 1);

        // assert by Distance
        Assert.assertTrue(population.get(0).getDistance() == 0.5);
        Assert.assertTrue(population.get(1).getDistance() == 0.4);
        Assert.assertTrue(population.get(2).getDistance() == 0.1);
    }
}
