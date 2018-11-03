package org.evosuite.ga.comparators;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRankAndCrowdingDistanceComparator {

    @Test
    public void compareEqual() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(0);
        tch1.setDistance(0);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(0);
        tch1.setDistance(0);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator();
        double result = comparator.compare(tch1, tch2);
        assertEquals(0, result, 0.00001);
    }

    @Test
    public void compareBetterRank() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(0);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(1);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator();
        double result = comparator.compare(tch1, tch2);
        assertEquals(-1, result, 0.00001);
    }

    @Test
    public void compareHigherRank() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(1);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(0);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator();
        double result = comparator.compare(tch1, tch2);
        assertEquals(+1, result, 0.00001);
    }

    @Test
    public void compareBetterDistance() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(0);
        tch1.setDistance(1);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(0);
        tch2.setDistance(0);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator();
        double result = comparator.compare(tch1, tch2);
        assertEquals(-1, result, 0.00001);
    }

    @Test
    public void compareLowerDistance() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(0);
        tch1.setDistance(0);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(0);
        tch2.setDistance(1);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator();
        double result = comparator.compare(tch1, tch2);
        assertEquals(+1, result, 0.00001);
    }

    @Test
    public void compareBetterRank_maximization() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(0);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(1);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator(true);
        double result = comparator.compare(tch1, tch2);
        assertEquals(+1, result, 0.00001);
    }

    @Test
    public void compareHigherRank_maximization() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(1);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(0);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator(true);
        double result = comparator.compare(tch1, tch2);
        assertEquals(-1, result, 0.00001);
    }

    @Test
    public void compareBetterDistance_maximization() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(0);
        tch1.setDistance(1);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(0);
        tch2.setDistance(0);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator(true);
        double result = comparator.compare(tch1, tch2);
        assertEquals(-1, result, 0.00001);
    }

    @Test
    public void compareLowerDistance_maximization() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(0);
        tch1.setDistance(0);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(0);
        tch2.setDistance(1);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator(true);
        double result = comparator.compare(tch1, tch2);
        assertEquals(+1, result, 0.00001);
    }

    @Test
    public void compareEqual_maximization() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setRank(0);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setRank(0);

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator();
        comparator.setMaximize(true);
        double result = comparator.compare(tch1, tch2);
        assertEquals(0, result, 0.00001);
    }

    @Test
    public void testNull() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());

        RankAndCrowdingDistanceComparator comparator = new RankAndCrowdingDistanceComparator();
        double value = comparator.compare(tch1, null);
        assertEquals(-1, value, 0.0001);

        value = comparator.compare(null, tch1);
        assertEquals(+1, value, 0.0001);
    }
}