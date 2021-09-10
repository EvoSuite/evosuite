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

import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class TestPreferenceSortingComparator {

    @Test
    public void compareEqual() {
        BranchCoverageGoal goal = Mockito.mock(BranchCoverageGoal.class);
        BranchCoverageTestFitness fitness = new BranchCoverageTestFitness(goal);

        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setFitness(fitness, 1);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setFitness(fitness, 1);

        PreferenceSortingComparator comparator = new PreferenceSortingComparator(fitness);
        double value = comparator.compare(tch1, tch2);
        assertEquals(0.0, value, 0.0001);
    }

    @Test
    public void compareLarger() {
        BranchCoverageGoal goal = Mockito.mock(BranchCoverageGoal.class);
        BranchCoverageTestFitness fitness = new BranchCoverageTestFitness(goal);

        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setFitness(fitness, 2);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setFitness(fitness, 1);

        PreferenceSortingComparator comparator = new PreferenceSortingComparator(fitness);
        double value = comparator.compare(tch1, tch2);
        assertEquals(+1, value, 0.0001);
    }

    @Test
    public void compareSmaller() {
        BranchCoverageGoal goal = Mockito.mock(BranchCoverageGoal.class);
        BranchCoverageTestFitness fitness = new BranchCoverageTestFitness(goal);

        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setFitness(fitness, 0);

        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.setFitness(fitness, 1);

        PreferenceSortingComparator comparator = new PreferenceSortingComparator(fitness);
        double value = comparator.compare(tch1, tch2);
        assertEquals(-1, value, 0.0001);
    }

    @Test
    public void testNull() {
        BranchCoverageGoal goal = Mockito.mock(BranchCoverageGoal.class);
        BranchCoverageTestFitness fitness = new BranchCoverageTestFitness(goal);

        TestChromosome tch1 = new TestChromosome();
        tch1.setTestCase(new DefaultTestCase());
        tch1.setFitness(fitness, 0);

        PreferenceSortingComparator comparator = new PreferenceSortingComparator(fitness);
        double value = comparator.compare(tch1, null);
        assertEquals(-1, value, 0.0001);

        value = comparator.compare(null, tch1);
        assertEquals(+1, value, 0.0001);
    }

}