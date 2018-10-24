package org.evosuite.ga.ranking;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.ranking.FastNonDominatedSorting;
import org.evosuite.testcase.TestChromosome;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FastNonDominatedSortingTest {

    public static Set<FitnessFunction> ff;
    public static List<TestChromosome> front;

    @BeforeClass
    public static void init(){
        // create the set of fitness functions
        FitnessFunction f1 = Mockito.mock(FitnessFunction.class);
        FitnessFunction f2 = Mockito.mock(FitnessFunction.class);
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
        FastNonDominatedSorting sorting = new FastNonDominatedSorting();
        sorting.computeRankingAssignment(front,ff);

        assertEquals(2, sorting.getNumberOfSubfronts());

        assertEquals(2, sorting.getSubfront(0).size());
        assertEquals(front.get(0), sorting.getSubfront(0).get(0));
        assertEquals(front.get(2), sorting.getSubfront(0).get(1));

        assertEquals(1, sorting.getSubfront(1).size());
        assertEquals(front.get(1), sorting.getSubfront(1).get(0));
    }

}