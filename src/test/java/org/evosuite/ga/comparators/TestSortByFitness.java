package org.evosuite.ga.comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.NSGAChromosome;
import org.evosuite.ga.problems.Problem;
import org.evosuite.ga.problems.singleobjective.Booths;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author José Campos
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestSortByFitness
{
    @Test
    public void testSortByFitnessC1win()
    {
        Problem p = new Booths<NSGAChromosome>();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        FitnessFunction<NSGAChromosome> ff = fitnessFunctions.get(0);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();

        // Set Fitness
        c1.setFitness(ff, 0.7);
        c2.setFitness(ff, 0.3);

        List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
        population.add(c1);
        population.add(c2);

        SortByFitness sf = new SortByFitness(ff, true);
        Collections.sort(population, sf);

        Assert.assertTrue(population.get(0) == c1);
        Assert.assertEquals(population.get(0).getFitness(ff), 0.7, 0.0);

        Assert.assertTrue(population.get(1) == c2);
        Assert.assertEquals(population.get(1).getFitness(ff), 0.3, 0.0);
    }

    @Test
    public void testSortByFitnessC2win()
    {
        Problem p = new Booths<NSGAChromosome>();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        FitnessFunction<NSGAChromosome> ff = fitnessFunctions.get(0);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();

        // Set Fitness
        c1.setFitness(ff, 0.3);
        c2.setFitness(ff, 0.7);

        List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
        population.add(c1);
        population.add(c2);

        SortByFitness sf = new SortByFitness(ff, true);
        Collections.sort(population, sf);

        Assert.assertTrue(population.get(0) == c2);
        Assert.assertEquals(population.get(0).getFitness(ff), 0.7, 0.0);

        Assert.assertEquals(population.get(1).getFitness(ff), 0.3, 0.0);
        Assert.assertTrue(population.get(1) == c1);
    }

    @Test
    public void testSortByFitnessEqual()
    {
        Problem p = new Booths<NSGAChromosome>();
        List<FitnessFunction<NSGAChromosome>> fitnessFunctions = p.getFitnessFunctions();
        FitnessFunction<NSGAChromosome> ff = fitnessFunctions.get(0);

        NSGAChromosome c1 = new NSGAChromosome();
        NSGAChromosome c2 = new NSGAChromosome();

        // Set Fitness
        c1.setFitness(ff, 0.5);
        c2.setFitness(ff, 0.5);

        List<NSGAChromosome> population = new ArrayList<NSGAChromosome>();
        population.add(c1);
        population.add(c2);

        SortByFitness sf = new SortByFitness(ff, true);
        Collections.sort(population, sf);

        Assert.assertTrue(population.get(0) == c1);
        Assert.assertEquals(population.get(0).getFitness(ff), 0.5, 0.0);

        Assert.assertEquals(population.get(1).getFitness(ff), 0.5, 0.0);
        Assert.assertTrue(population.get(1) == c2);
    }
}
