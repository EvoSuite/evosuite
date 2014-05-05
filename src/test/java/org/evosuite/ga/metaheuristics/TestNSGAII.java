package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.crossover.SBXCrossOver;
import org.evosuite.ga.operators.selection.TournamentSelectionCrowdedComparison;
import org.evosuite.ga.problems.OneVariableProblem;
import org.evosuite.ga.problems.Problem;
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
		Properties.POPULATION = 500;
		Properties.SEARCH_BUDGET = 250;
		Properties.CROSSOVER_RATE = 0.9;
		Properties.MUTATION_RATE = 1d / 1d;
		Properties.RANDOM_SEED = 1l;
	}

	@Test
	public void testFastNonDominatedSort()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);

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
		c1.setFitness(0.6);
		c2.setFitness(0.2);
		c3.setFitness(0.4);
		c4.setFitness(0.0);
		c5.setFitness(0.8);
		c6.setFitness(0.8);
		c7.setFitness(0.2);
		c8.setFitness(0.4);
		c9.setFitness(0.6);
		c10.setFitness(0.0);

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
	public void testCrowingDistanceAssignment()
	{
		NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);

		Problem p = new OneVariableProblem();
		List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
		ga.setFitnessFunction(fitnessFunctions.get(0));

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
		c1.setFitness(0.0);
		c2.setFitness(0.2);
		c3.setFitness(0.4);
		c4.setFitness(0.6);
		c5.setFitness(0.8);
		c6.setFitness(0.0);
		c7.setFitness(0.2);
		c8.setFitness(0.4);
		c9.setFitness(0.6);
		c10.setFitness(0.8);

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

	/**
     * Testing NSGA-II with FON Problem
     */
	@Test
    public void testFON()
    {
        
    }

	/**
     * Testing NSGA-II with OneVariable Problem
     */
	@Test
	public void testNSGAII_OneVariableProblem()
	{
	    ChromosomeFactory<?> factory = new RandomFactory(false, 1, 0.0, 0.0, 10.0, -10.0);

        GeneticAlgorithm<?> ga = new NSGAII(factory);
        TournamentSelectionCrowdedComparison ts = new TournamentSelectionCrowdedComparison();
        ts.setMaximize(false);
        ga.setSelectionFunction(ts);
        ga.setCrossOverFunction(new SBXCrossOver());

        Problem p = new OneVariableProblem();
        FitnessFunction ff = (FitnessFunction) p.getFitnessFunctions().get(0);
        ga.setFitnessFunction(ff);

        ga.generateSolution();

        Assert.assertEquals(ga.population.size(), 500);
        for (Chromosome population : ga.population)
            Assert.assertEquals(population.getFitness(), 0.0, 0.0);
	}

	/**
     * Testing NSGA-II with SCH Problem
     */
	@Test
    public void testSCH()
    {
	    
    }

	/**
     * Testing NSGA-II with ZDT4 Problem
     */
	@Test
    public void testZDT4()
    {
	    
    }
}
