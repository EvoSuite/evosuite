package org.evosuite.ga.ranking;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.ranking.CrowdingDistance;
import org.evosuite.testcase.TestChromosome;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CrowdingDistanceTest {

    public static List<FitnessFunction> ff;

    @BeforeClass
    public static void init(){
        // create the set of fitness functions
        FitnessFunction f1 = Mockito.mock(FitnessFunction.class);
        FitnessFunction f2 = Mockito.mock(FitnessFunction.class);
        ff = new LinkedList<>();
        ff.add(f1);
        ff.add(f2);
    }

    @Test
    public void testCrowdingDistanceAssignment() {
        // create test cases
        TestChromosome tch1 = new TestChromosome();
        tch1.setFitness(ff.get(0), 3);
        tch1.setFitness(ff.get(1), 0.5);
        TestChromosome tch2 = new TestChromosome();
        tch2.setFitness(ff.get(0), 1);
        tch2.setFitness(ff.get(1), 2);
        TestChromosome tch3 = new TestChromosome();
        tch3.setFitness(ff.get(0), 0.5);
        tch3.setFitness(ff.get(1), 3);

        // create front
        List<TestChromosome> front = new LinkedList<>();
        front.add(tch1);
        front.add(tch2);
        front.add(tch3);

        CrowdingDistance distance = new CrowdingDistance();
        distance.crowdingDistanceAssignment(front, ff);

        assertEquals(Double.POSITIVE_INFINITY, tch1.getDistance(), 0.000001);
        assertEquals(2.0, tch2.getDistance(), 0.000001);
        assertEquals(Double.POSITIVE_INFINITY, tch3.getDistance(), 0.000001);
    }

    @Test
    public void testSubvectorDominanceAssignment() {
        // create test cases
        TestChromosome tch1 = new TestChromosome();
        tch1.setFitness(ff.get(0), 3);
        tch1.setFitness(ff.get(1), 0.5);
        TestChromosome tch2 = new TestChromosome();
        tch2.setFitness(ff.get(0), 1);
        tch2.setFitness(ff.get(1), 1);
        TestChromosome tch3 = new TestChromosome();
        tch3.setFitness(ff.get(0), 0.5);
        tch3.setFitness(ff.get(1), 3);

        // create front
        List<TestChromosome> front = new LinkedList<>();
        front.add(tch1);
        front.add(tch2);
        front.add(tch3);

        CrowdingDistance distance = new CrowdingDistance();
        distance.subvectorDominanceAssignment(front, new HashSet(ff));

        assertEquals(1.0, tch1.getDistance(), 0.000001);
        assertEquals(1.0, tch2.getDistance(), 0.000001);
        assertEquals(1.0, tch3.getDistance(), 0.000001);
    }

    @Test
    public void testFastEpsilonDominanceAssignment() {
        // create test cases
        TestChromosome tch1 = new TestChromosome();
        tch1.setFitness(ff.get(0), 3);
        tch1.setFitness(ff.get(1), 0.5);
        TestChromosome tch2 = new TestChromosome();
        tch2.setFitness(ff.get(0), 1);
        tch2.setFitness(ff.get(1), 1);
        TestChromosome tch3 = new TestChromosome();
        tch3.setFitness(ff.get(0), 0.5);
        tch3.setFitness(ff.get(1), 3);

        // create front
        List<TestChromosome> front = new LinkedList<>();
        front.add(tch1);
        front.add(tch2);
        front.add(tch3);

        CrowdingDistance distance = new CrowdingDistance();
        distance.fastEpsilonDominanceAssignment(front, new HashSet(ff));

        assertEquals(2d/3d, tch1.getDistance(), 0.000001);
        assertEquals(0.0, tch2.getDistance(), 0.000001);
        assertEquals(2d/3d, tch3.getDistance(), 0.000001);
    }

    @Test
    public void testEmptyFront() {
        // create front
        List<TestChromosome> front = new LinkedList<>();
        CrowdingDistance distance = new CrowdingDistance();
        distance.crowdingDistanceAssignment(front,ff);
        distance.fastEpsilonDominanceAssignment(front, new HashSet(ff));
        distance.subvectorDominanceAssignment(front, new HashSet(ff));
    }

    @Test
    public void tesFront_OneSolution() {
        // create front with one single solution
        TestChromosome tch1 = new TestChromosome();
        tch1.setFitness(ff.get(0), 3);
        tch1.setFitness(ff.get(1), 0.5);
        List<TestChromosome> front = new LinkedList<>();
        front.add(tch1);

        CrowdingDistance distance = new CrowdingDistance();
        distance.crowdingDistanceAssignment(front,ff);
        assertEquals(Double.POSITIVE_INFINITY, tch1.getDistance(), 0.00001);

        distance.fastEpsilonDominanceAssignment(front, new HashSet(ff));
        assertEquals(0.0, tch1.getDistance(), 0.00001);

        distance.subvectorDominanceAssignment(front, new HashSet(ff));
        assertEquals(Double.POSITIVE_INFINITY, tch1.getDistance(), 0.00001);
    }

    @Test
    public void tesFront_TwoSolutions() {
        // create front with one single solution
        TestChromosome tch1 = new TestChromosome();
        tch1.setFitness(ff.get(0), 3);
        tch1.setFitness(ff.get(1), 0.5);
        TestChromosome tch2 = new TestChromosome();
        tch2.setFitness(ff.get(0), 0);
        tch2.setFitness(ff.get(1), 1);
        List<TestChromosome> front = new LinkedList<>();
        front.add(tch1); front.add(tch2);

        CrowdingDistance distance = new CrowdingDistance();
        distance.crowdingDistanceAssignment(front,ff);
        assertEquals(Double.POSITIVE_INFINITY, tch1.getDistance(), 0.00001);
        assertEquals(Double.POSITIVE_INFINITY, tch2.getDistance(), 0.00001);

        distance.fastEpsilonDominanceAssignment(front, new HashSet(ff));
        assertEquals(0.5, tch1.getDistance(), 0.00001);
        assertEquals(0.5, tch2.getDistance(), 0.00001);

        distance.subvectorDominanceAssignment(front, new HashSet(ff));
        assertEquals(1.0, tch1.getDistance(), 0.00001);
        assertEquals(1.0, tch2.getDistance(), 0.00001);
    }
}