package org.evosuite.performance.comparator;

import org.evosuite.ga.comparators.OnlyCrowdingComparator;
import org.evosuite.testcase.TestChromosome;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MinMaxCalculatorTest {

    @Test
    public void computeIndicatorMinMaxSum() {
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        map.put("A", 0.2);
        map.put("B", 0.4);
        map.put("C", 0.4);
        map.put("D", 0.2);

        LinkedHashMap<String, Double> map2 = new LinkedHashMap<>();
        map2.put("A", 0.1);
        map2.put("B", 0.5);
        map2.put("C", 0.2);
        map2.put("D", 0.1);

        LinkedHashMap<String, Double> map3 = new LinkedHashMap<>();
        map3.put("A", 0.7);
        map3.put("B", 0.8);
        map3.put("C", 0.7);
        map3.put("D", 0.3);

        List<TestChromosome> front = Arrays.asList(new TestChromosome(), new TestChromosome(), new TestChromosome());
        front.get(0).setIndicatorValues(map);
        front.get(1).setIndicatorValues(map2);
        front.get(2).setIndicatorValues(map3);

        MinMaxCalculator<TestChromosome> adder = new MinMaxCalculator<>();
        adder.computeIndicatorMinMaxSum(front);

        for (TestChromosome solution : front)
            solution.setDistance(solution.getMinMaxSum());
        front.sort(new OnlyCrowdingComparator());

        assertEquals(true, front.get(0).getDistance() >= front.get(1).getDistance());
        assertEquals(true, front.get(1).getDistance() >= front.get(2).getDistance());

        assertEquals(3.75, front.get(0).getDistance(), 0.01);
        assertEquals(2.93, front.get(1).getDistance(), 0.01);
        assertEquals(0.0, front.get(2).getDistance(), 0.01);
    }
}