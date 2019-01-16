package org.evosuite.performance.strategies;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.performance.PerformanceScore;
import org.evosuite.performance.comparator.MinMaxCalculator;
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
        double[] ch1 = {2.0, 4.5, 2.0, 4.0, 2.5, 8};
        double[] ch2 = {1.0, 2.0, 1.5, 2.5, 3, 4};
        double[] ch3 = {5, 2.0, 1.5, 8.5, 3, 4};
        double[] ch4 = {8.0, 2.0, 4.5, 2.5, 3, 4.5};
        MinMaxCalculator calculator = new MinMaxCalculator<>();
        front = createChromosomesForTest(ch1, ch2, ch3, ch4);
        calculator.computeIndicatorMinMaxSum(front);
    }

    @Test
    public void sortMinMaxComparator() {
        PerformanceScore score = new PerformanceScore();
        Properties.P_COMBINATION_STRATEGY = Properties.PerformanceCombinationStrategy.MIN_MAX;
        IndicatorComparisonStrategy<Chromosome> strategy = new IndicatorComparisonStrategy<>();
        System.setProperty("performance_combination_strategy", "MIN_MAX");
        Collections.shuffle(front);
        score.assignPerformanceScore(front);
        strategy.setDistances(front, null);
        strategy.sort(front);

        double total = 0;
        double totalPerformanceScore = 0;

        for (int i = 0; i < front.size()-1; i++) {
            Assert.assertEquals(true, front.get(i).getDistance() >= front.get(i+1).getDistance());
            total += front.get(i).getDistance();
            totalPerformanceScore += front.get(i).getPerformanceScore();
        }

        total += front.get(front.size()-1).getDistance();
        totalPerformanceScore += front.get(front.size()-1).getPerformanceScore();
        // checking that the distance is not 0 for the entire population
        Assert.assertNotEquals(0, total);
        // checking that the total of the distances is equal to the total of the performance scores
        Assert.assertEquals(total, totalPerformanceScore, 0.0);
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