package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.TestChromosome;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SumComparatorTest {

    @Test
    public void testCompareEquals() {
        double[] ch1 = {2.5, 3.0, 2.0};
        double[] ch2 = {3.0, 3.0, 1.5};

        List<Chromosome> chromosomes = getChromosomes(ch1, ch2);
        SumComparator comparator = new SumComparator();
        assertEquals(0, comparator.compare(chromosomes.get(0), chromosomes.get(1)));
    }

    @Test
    public void testCompareLess() {
        double[] ch1 = {2.0, 3.0, 2.0};
        double[] ch2 = {3.0, 4.0, 1.5};

        List<Chromosome> chromosomes = getChromosomes(ch1, ch2);
        SumComparator comparator = new SumComparator();
        assertEquals(-1, comparator.compare(chromosomes.get(0), chromosomes.get(1)));
    }

    @Test
    public void testCompareMore() {
        double[] ch1 = {2.0, 4.5, 2.0};
        double[] ch2 = {1.0, 2.0, 1.5};

        List<Chromosome> chromosomes = getChromosomes(ch1, ch2);
        SumComparator comparator = new SumComparator();
        assertEquals(1, comparator.compare(chromosomes.get(0), chromosomes.get(1)));
    }

    public static List<Chromosome> getChromosomes(double[]... ch1) {
        List<Chromosome> chromosomes = new ArrayList<>();
        for (double[] arr : ch1) {
            TestChromosome chromosome = new TestChromosome();
            for (int i = 0; i < arr.length; i++)
                chromosome.setIndicatorValues(Integer.toString(i), arr[i]);
            chromosomes.add(chromosome);
        }
        return chromosomes;
    }
}