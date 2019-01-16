package org.evosuite.performance.strategies;

import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class PreferenceCriterionStrategyTest {

    private List<Chromosome> front = new ArrayList<>();

    @Before
    public void setUp() {
        double[] ch2 = {1.0, 4.5, 2.0, 4.0, 2.5, 8};
        double[] ch1 = {1.0, 2.5, 2.5, 2.5, 3, 4};
        double[] ch3 = {5, 2.0, 1.5, 8, 3, 5};
        double[] ch4 = {8.0, 2.0, 4.5, 2.5, 4.5, 5.5};
        front = this.createChromosomesForTest(ch1, ch2, ch3, ch4);
        front.stream().forEach(ch -> ch.setDistance(new Random().nextDouble()));
    }

    @Test
    public void sort() {
        PreferenceCriterionStrategy strategy = new PreferenceCriterionStrategy();
        Collections.shuffle(front);
        strategy.sort(front);
        for (int i = 0; i < front.size()-1; i++) {
            assertEquals(true, front.get(i).getDistance() >= front.get(i+1).getDistance());
        }
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