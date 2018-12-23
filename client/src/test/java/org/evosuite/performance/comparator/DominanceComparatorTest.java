package org.evosuite.performance.comparator;

import org.evosuite.ga.Chromosome;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DominanceComparatorTest {

    @Test
    public void testCompareEquals() {
        double[] ch1 = {3.0, 3.0, 2.0};
        double[] ch2 = {3.0, 3.0, 2.0};

        List<Chromosome> chromosomes = SumComparatorTest.getChromosomes(ch1, ch2);
        Comparator<Chromosome> comparator = new SumComparator();
        assertEquals(0, comparator.compare(chromosomes.get(0), chromosomes.get(1)));
    }

    @Test
    public void testCompareLess() {
        double[] ch1 = {2.0, 3.0, 1.0};
        double[] ch2 = {3.0, 4.0, 1.5};

        List<Chromosome> chromosomes = SumComparatorTest.getChromosomes(ch2, ch1);
        Chromosome best = chromosomes.get(1);
        Comparator<Chromosome> comparator = new DominanceComparator();
        chromosomes.sort(comparator);
        assertEquals(best, chromosomes.get(0));
    }

    @Test
    public void testCompareMore() {
        double[] ch1 = {2.0, 4.5, 2.0};
        double[] ch2 = {1.0, 2.0, 1.5};

        List<Chromosome> chromosomes = SumComparatorTest.getChromosomes(ch1, ch2);
        Comparator<Chromosome> comparator = new DominanceComparator();
        assertEquals(1, comparator.compare(chromosomes.get(0), chromosomes.get(1)));
    }

    @Test
    public void testNonDominance() {
        double[] ch1 = {2.0, 1.0, 1.0};
        double[] ch2 = {1.0, 2.0, 2.0};

        List<Chromosome> chromosomes = SumComparatorTest.getChromosomes(ch1, ch2);
        Comparator<Chromosome> comparator = new DominanceComparator();
        assertEquals(0, comparator.compare(chromosomes.get(0), chromosomes.get(1)));

        double[] ch3 = {1.0, 2.0, 1.0};
        double[] ch4 = {2.0, 1.0, 2.0};
        chromosomes = SumComparatorTest.getChromosomes(ch3, ch4);
        assertEquals(0, comparator.compare(chromosomes.get(0), chromosomes.get(1)));

        double[] ch5 = {1.0, 1.0, 2.0};
        double[] ch6 = {2.0, 2.0, 1.0};
        chromosomes = SumComparatorTest.getChromosomes(ch5, ch6);
        assertEquals(0, comparator.compare(chromosomes.get(0), chromosomes.get(1)));
    }

}