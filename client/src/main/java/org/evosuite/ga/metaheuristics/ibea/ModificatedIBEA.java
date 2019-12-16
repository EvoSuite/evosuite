package org.evosuite.ga.metaheuristics.ibea;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.ranking.FastNonDominatedSorting;

import java.util.LinkedHashSet;
import java.util.List;

public class ModificatedIBEA <T extends Chromosome> extends IBEA {

    FastNonDominatedSorting fastNonDominatedSorting = new FastNonDominatedSorting();
    /**
     * Constructor
     *
     * @param factory
     */
    public ModificatedIBEA(ChromosomeFactory factory) {
        super(factory);
    }

    @Override
    public void applyModification(List union) {
       fastNonDominatedSorting.computeRankingAssignment(union, new LinkedHashSet<FitnessFunction<T>>(this.getFitnessFunctions()));
    }
}
