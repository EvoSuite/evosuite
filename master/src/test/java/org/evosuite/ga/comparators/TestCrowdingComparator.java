package org.evosuite.ga.comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.evosuite.ga.NSGAChromosome;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author José Campos
 */
public class TestCrowdingComparator
{
    @Test
    public void testCrowdingComparisonOperatorMinimize()
    {
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

        CrowdingComparator cc = new CrowdingComparator(false);
        Collections.sort(population, cc);

        // assert by Rank
        Assert.assertTrue(population.get(0).getRank() == 0);
        Assert.assertTrue(population.get(1).getRank() == 0);
        Assert.assertTrue(population.get(2).getRank() == 1);

        // assert by Distance
        Assert.assertTrue(population.get(0).getDistance() == 0.5);
        Assert.assertTrue(population.get(1).getDistance() == 0.4);
        Assert.assertTrue(population.get(2).getDistance() == 0.1);
    }

    @Test
    public void testCrowdingComparisonOperatorMaximize()
    {
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

        CrowdingComparator cc = new CrowdingComparator(true);
        Collections.sort(population, cc);

        // assert by Rank
        Assert.assertTrue(population.get(0).getRank() == 1);
        Assert.assertTrue(population.get(1).getRank() == 0);
        Assert.assertTrue(population.get(2).getRank() == 0);

        // assert by Distance
        Assert.assertTrue(population.get(0).getDistance() == 0.1);
        Assert.assertTrue(population.get(1).getDistance() == 0.5);
        Assert.assertTrue(population.get(2).getDistance() == 0.4);
    }
}
