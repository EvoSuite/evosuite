/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.comparators.CrowdingComparator;
import org.evosuite.ga.comparators.SortByFitness;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.problems.multiobjective.SCH;
import org.evosuite.ga.problems.singleobjective.Booths;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.Calculator;

/**
 * NSGA-II test
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class NSGAIISystemTest extends SystemTestBase
{
	@BeforeClass
	public static void setUp() {
		Properties.CROSSOVER_RATE = 0.9;
		Properties.RANDOM_SEED = 1l;
	}

	@Test
	public void testUnionEmptyPopulation()
	{
	    NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);

	    List<NSGAChromosome> pop = new ArrayList<NSGAChromosome>();
	    List<NSGAChromosome> off = new ArrayList<NSGAChromosome>();
	    List<NSGAChromosome> union = ga.union(pop, off);

	    Assert.assertTrue(union.isEmpty());
	}

	@Test
    public void testUnion()
    {
        NSGAII<NSGAChromosome> ga = new NSGAII<NSGAChromosome>(null);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();
        NSGAChromosome c3 = new NSGAChromosome();

        List<NSGAChromosome> pop = new ArrayList<NSGAChromosome>();
        pop.add(c1);
        pop.add(c2);

        List<NSGAChromosome> off = new ArrayList<NSGAChromosome>();
        off.add(c3);

        List<NSGAChromosome> union = ga.union(pop, off);
        Assert.assertEquals(union.size(), 3);
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

		List<List<NSGAChromosome>> fronts = ga.fastNonDominatedSort(population);

		// Total number of Fronts
		Assert.assertEquals(fronts.size(), 5);

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

		ga.crowingDistanceAssignment(population);
		Collections.sort(population, new CrowdingComparator(true));

		Assert.assertTrue(population.get(0).getDistance() == Double.POSITIVE_INFINITY);
		Assert.assertTrue(population.get(1).getDistance() == Double.POSITIVE_INFINITY);

		double epsilon = 1e-10;
		Assert.assertTrue(Math.abs(0.25 - population.get(2).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - population.get(3).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - population.get(4).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - population.get(5).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - population.get(6).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - population.get(7).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - population.get(8).getDistance()) < epsilon);
		Assert.assertTrue(Math.abs(0.25 - population.get(9).getDistance()) < epsilon);
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

        ga.crowingDistanceAssignment(population);
        Collections.sort(population, new CrowdingComparator(true));

        Assert.assertTrue(population.get(0).getDistance() == Double.POSITIVE_INFINITY);
        Assert.assertTrue(population.get(1).getDistance() == Double.POSITIVE_INFINITY);

        double epsilon = 1e-10;
        Assert.assertTrue(Math.abs(0.5 - population.get(2).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(3).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(4).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(5).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(6).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(7).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(8).getDistance()) < epsilon);
        Assert.assertTrue(Math.abs(0.5 - population.get(9).getDistance()) < epsilon);
    }

	@Test
	public void testIntegration()
	{
	    Properties.MUTATION_RATE = 1d / 1d;
	    Properties.CRITERION = new Criterion[2];
        Properties.CRITERION[0] = Criterion.RHO;
        Properties.CRITERION[1] = Criterion.AMBIGUITY;
	    Properties.ALGORITHM = Algorithm.NSGAII;

	    EvoSuite evosuite = new EvoSuite();

        String targetClass = Calculator.class.getCanonicalName();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[] {
            "-Dselection_function=BINARY_TOURNAMENT",
            "-Dminimize_values=false",
            "-Dinline=false",
            "-Dminimize=true",
            "-Dstop_zero=false",
            "-Dcoverage=false",
            "-Djunit_tests=true",
            "-Dassertions=true",
            "-Dsandbox=true",
            "-Dnew_statistics=false",
            "-generateSuite",
            "-class", targetClass
        };

        Object result = evosuite.parseCommandLine(command);
        Assert.assertNotNull(result);

        GeneticAlgorithm<?> ga = getGAFromResult(result);

        final FitnessFunction rho = ga.getFitnessFunctions().get(0);
        final FitnessFunction ag = ga.getFitnessFunctions().get(1);

        List<Chromosome> population = new ArrayList<Chromosome>(ga.getBestIndividuals());
        Collections.sort(population, new SortByFitness(rho, false));

        for (Chromosome p : population) {
            System.out.println("Rho: " + p.getFitness(rho) + ", AG: " + p.getFitness(ag) + " | Rank: " + p.getRank());

            Assert.assertEquals(0.0, p.getFitness(rho), 0.0);
            Assert.assertEquals(0.0, p.getFitness(ag), 0.0);
            Assert.assertEquals(0, p.getRank());
        }
	}
}
