package org.evosuite.ga.ranking;

import org.evosuite.Properties;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.ranking.RankBasedPreferenceSorting;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.numeric.BooleanPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RankBasedPreferenceSortingTest {

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
        tch1.setTestCase(new DefaultTestCase());
        tch1.getTestCase().addStatement(new BooleanPrimitiveStatement(tch1.getTestCase(),true));
        tch1.setFitness(f1, 1);
        tch1.setFitness(f2, 0.5);
        TestChromosome tch2 = new TestChromosome();
        tch2.setTestCase(new DefaultTestCase());
        tch2.getTestCase().addStatement(new IntPrimitiveStatement(tch2.getTestCase(),2));
        tch2.setFitness(f1, 0.6);
        tch2.setFitness(f2, 0.7);
        TestChromosome tch3 = new TestChromosome();
        tch3.setTestCase(new DefaultTestCase());
        tch3.getTestCase().addStatement(new IntPrimitiveStatement(tch3.getTestCase(),1));
        tch3.setFitness(f1, 0.7);
        tch3.setFitness(f2, 0.6);
        TestChromosome tch4 = new TestChromosome();
        tch4.setTestCase(new DefaultTestCase());
        tch4.getTestCase().addStatement(new BooleanPrimitiveStatement(tch4.getTestCase(),false));
        tch4.setFitness(f1, 0.5);
        tch4.setFitness(f2, 2);

        // create front
        front = new LinkedList<>();
        front.add(tch1);
        front.add(tch2);
        front.add(tch3);
        front.add(tch4);
    }

    @Test
    public void testComputeRankingAssignment() {
        RankBasedPreferenceSorting sorting =  new RankBasedPreferenceSorting();
        sorting.computeRankingAssignment(front, ff);
        Properties.POPULATION = 4;

        assertEquals(2, sorting.getNumberOfSubfronts());

        assertEquals(2, sorting.getSubfront(0).size());
        assertTrue(front.get(0)== sorting.getSubfront(0).get(0) || front.get(0)== sorting.getSubfront(0).get(1));
        assertTrue(front.get(0)== sorting.getSubfront(0).get(0) || front.get(0)== sorting.getSubfront(0).get(1));


        assertEquals(2, sorting.getSubfront(1).size());
        assertEquals(front.get(1), sorting.getSubfront(1).get(0));
        assertEquals(front.get(2), sorting.getSubfront(1).get(1));
        assertEquals(0,sorting.getSubfront(2).size());
    }

    @Test
    public void testComputeRankingAssignment_smallPopulation() {
        RankBasedPreferenceSorting sorting =  new RankBasedPreferenceSorting();
        sorting.computeRankingAssignment(front, ff);
        Properties.POPULATION = 2;

        assertEquals(2, sorting.getNumberOfSubfronts());

        assertEquals(2, sorting.getSubfront(0).size());
        assertTrue(front.get(0)== sorting.getSubfront(0).get(0) || front.get(0)== sorting.getSubfront(0).get(1));
        assertTrue(front.get(0)== sorting.getSubfront(0).get(0) || front.get(0)== sorting.getSubfront(0).get(1));


        assertEquals(2, sorting.getSubfront(1).size());
        assertEquals(front.get(1), sorting.getSubfront(1).get(0));
        assertEquals(front.get(2), sorting.getSubfront(1).get(1));
    }

    @Test
    public void getNumberOfSubfronts() {
    }
}