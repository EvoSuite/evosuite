package org.evosuite.Paes;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.paes.Grid.DummyChromosome;
import org.evosuite.ga.metaheuristics.paes.Grid.DummyFitnessFunction;
import org.evosuite.ga.metaheuristics.paes.Grid.GridLocation;
import org.evosuite.ga.metaheuristics.paes.Grid.GridNode;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestPaesArchiveGrid {

    @Test
    public void testStructure(){
        DummyFitnessFunction ff1 = new DummyFitnessFunction();
        DummyFitnessFunction ff2 = new DummyFitnessFunction();
        DummyFitnessFunction ff3 = new DummyFitnessFunction();
        Map<FitnessFunction<?>,Double> upperBounds = new LinkedHashMap<>();
        Map<FitnessFunction<?>,Double> lowerBounds = new LinkedHashMap<>();
        upperBounds.put(ff1,1.0);
        upperBounds.put(ff2,1.0);
        upperBounds.put(ff3,1.0);
        lowerBounds.put(ff1,0.0);
        lowerBounds.put(ff2,0.0);
        lowerBounds.put(ff3,0.0);
        GridNode<DummyChromosome> node = new GridNode<>(lowerBounds,upperBounds,3,null);
        DummyChromosome c1 = new DummyChromosome();
        DummyChromosome c2 = new DummyChromosome();
        DummyChromosome c3 = new DummyChromosome();
        c1.setFitness(ff1, 1);
        c1.setFitness(ff2, 0.1);
        c1.setFitness(ff3, 0.3);
        c2.setFitness(ff1, 0.99);
        c2.setFitness(ff2, 0.11);
        c2.setFitness(ff3, 0.31);
        c3.setFitness(ff1, 0.5);
        c3.setFitness(ff2, 0.75);
        c3.setFitness(ff3, 0.25);
        node.add(c1);
        node.add(c2);
        node.add(c3);
        GridLocation<DummyChromosome> l1 = node.region(c1);
        GridLocation<DummyChromosome> l2 = node.region(c1);
        GridLocation<DummyChromosome> l3 = node.region(c1);
        assertEquals(l1,l2);
        assertNotEquals(l1,l3);
    }
}
