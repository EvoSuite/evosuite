package org.evosuite.performance.strategies;

import org.evosuite.testcase.TestChromosome;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
public class PerformanceDominanceSorterTest {

    private List<TestChromosome> front = new ArrayList<>();

    @Before
    @SuppressWarnings("Duplicates")
    public void setUp() {
        double[] ch1 = {10, 10, 10, 10, 10, 10};
        double[] ch2 = {1.0, 2.0, 1.5, 2.5, 3, 4};
        double[] ch3 = {8.0, 2.5, 4.5, 5.5, 3, 4.5};
        double[] ch4 = {5, 2.0, 1.5, 8.5, 3, 4};
        double[] ch5 = {2.0, 4.5, 2.0, 4.0, 2.5, 8};

        front = this.getChromosomes(ch1, ch2, ch3, ch4, ch5);
    }

    public List<TestChromosome> getChromosomes(double[]... ch1) {
        List<TestChromosome> chromosomes = new ArrayList<>();
        for (double[] arr : ch1) {
            TestChromosome chromosome = new TestChromosome();
            for (int i = 0; i < arr.length; i++) {
                chromosome.setIndicatorValues(Integer.toString(i), arr[i]);
            }
            chromosomes.add(chromosome);
        }
        return chromosomes;
    }

    @Test
    public void checkSorting() {
        int size = this.front.size();
        PerformanceDominanceSorter sorter = new PerformanceDominanceSorter();

        List<TestChromosome> sortedList = sorter.getSortedWithIndicatorsDominance(front);
        // check that the order is not the same
        assertEquals(false, this.front.equals(sortedList));
        // check that the size remains the same
        assertEquals(size, sortedList.size());
        assertEquals(0, sortedList.get(0).getPerformance_rank());
        assertEquals(2, sortedList.get(sortedList.size()-1).getPerformance_rank());
    }
}