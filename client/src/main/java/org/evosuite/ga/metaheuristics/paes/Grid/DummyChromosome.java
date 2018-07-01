package org.evosuite.ga.metaheuristics.paes.Grid;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.localsearch.LocalSearchObjective;

public class DummyChromosome extends Chromosome {


    @Override
    public Chromosome clone() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public <T extends Chromosome> int compareSecondaryObjective(T o) {
        return 0;
    }

    @Override
    public void mutate() {

    }

    @Override
    public void crossOver(Chromosome other, int position1, int position2) throws ConstructionFailedException {

    }

    @Override
    public boolean localSearch(LocalSearchObjective<? extends Chromosome> objective) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }
}
