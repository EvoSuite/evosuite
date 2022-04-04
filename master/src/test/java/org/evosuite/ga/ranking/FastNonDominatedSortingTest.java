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
package org.evosuite.ga.ranking;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.ranking.FastNonDominatedSorting;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FastNonDominatedSortingTest {

    public static Set<FitnessFunction<TestChromosome>> ff;
    public static List<TestChromosome> front;

    @BeforeClass
    public static void init() {
        // create the set of fitness functions
        FitnessFunction<TestChromosome> f1 = Mockito.mock(TestFitnessFunction.class);
        FitnessFunction<TestChromosome> f2 = Mockito.mock(TestFitnessFunction.class);
        ff = new HashSet<>();
        ff.add(f1);
        ff.add(f2);

        // create test cases
        TestChromosome tch1 = new TestChromosome();
        tch1.setFitness(f1, 1);
        tch1.setFitness(f2, 0.5);
        TestChromosome tch2 = new TestChromosome();
        tch2.setFitness(f1, 1);
        tch2.setFitness(f2, 2);
        TestChromosome tch3 = new TestChromosome();
        tch3.setFitness(f1, 0.5);
        tch3.setFitness(f2, 1);

        // create front
        front = new LinkedList<>();
        front.add(tch1);
        front.add(tch2);
        front.add(tch3);
    }

    @Test
    public void testComputeRankingAssignment() {
        FastNonDominatedSorting<TestChromosome> sorting = new FastNonDominatedSorting<>();
        sorting.computeRankingAssignment(front, ff);

        assertEquals(2, sorting.getNumberOfSubfronts());

        assertEquals(2, sorting.getSubfront(0).size());
        assertEquals(front.get(0), sorting.getSubfront(0).get(0));
        assertEquals(front.get(2), sorting.getSubfront(0).get(1));

        assertEquals(1, sorting.getSubfront(1).size());
        assertEquals(front.get(1), sorting.getSubfront(1).get(0));
    }

}