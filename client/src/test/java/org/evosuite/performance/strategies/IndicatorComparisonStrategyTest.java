package org.evosuite.performance.strategies;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IndicatorComparisonStrategyTest {

    private List<Chromosome> front = new ArrayList<>();

    @Before
    public void setUp() {
    }

    @Test
    public void sortMinMaxComparator() {

    }

    @SuppressWarnings("Duplicates")
    public List<Chromosome> createChromosomesForTest(double[]... ch1) {
        List<Chromosome> chromosomes = new ArrayList<>();
        for (double[] arr : ch1) {
            Chromosome chromosome = new TestChromosome();
            for (int i = 0; i < arr.length; i++)
                chromosome.setIndicatorValues(Integer.toString(i), arr[i]);
            chromosomes.add(chromosome);
        }
        return chromosomes;
    }
}