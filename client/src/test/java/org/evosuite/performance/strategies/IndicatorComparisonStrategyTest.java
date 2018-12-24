package org.evosuite.performance.strategies;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.performance.comparator.SumComparatorTest;
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
        front = SumComparatorTest.getChromosomes(ch1, ch2, ch3, ch4);
    }

    @Test
    public void sortMinMaxComparator() {
        Properties.P_COMBINATION_STRATEGY = Properties.PerformanceCombinationStrategy.MIN_MAX;
        IndicatorComparisonStrategy<Chromosome> strategy = new IndicatorComparisonStrategy<>();
        System.setProperty("performance_combination_strategy", "MIN_MAX");
        Collections.shuffle(front);
        strategy.setDistances(front, null);
        strategy.sort(front);
        for (int i = 0; i < front.size()-1; i++) {
            Assert.assertEquals(true, front.get(i).getDistance() >= front.get(i+1).getDistance());
        }
    }
}