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