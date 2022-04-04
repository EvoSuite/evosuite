/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>();
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>();
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>();
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>();
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>();
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>(true);
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>(true);
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>(true);
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>(true);
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

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>();
        comparator.setMaximize(true);
        double result = comparator.compare(tch1, tch2);
        assertEquals(0, result, 0.00001);
    }

    @Test
    public void testNull() {
        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());

        RankAndCrowdingDistanceComparator<TestChromosome> comparator =
                new RankAndCrowdingDistanceComparator<>();
        double value = comparator.compare(tch1, null);
        assertEquals(-1, value, 0.0001);

        value = comparator.compare(null, tch1);
        assertEquals(+1, value, 0.0001);
    }
}