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
    public List applyModification(List union) {
       fastNonDominatedSorting.computeRankingAssignment(union, new LinkedHashSet<FitnessFunction<T>>(this.getFitnessFunctions()));

        int remain = union.size();
        int index = 0;
        List<T> front = null;
        union.clear();
        front = this.fastNonDominatedSorting.getSubfront(index);

        while ((remain > 0) && (remain >= front.size())) {
            // Add the individuals of this front
            for (int k = 0; k < front.size(); k++)
                union.add(front.get(k));

            remain = remain - front.size();

            // Obtain the next front
            index++;
            if (remain > 0)
                front = this.fastNonDominatedSorting.getSubfront(index);

        }
       return union;
    }

    @Override
    public T getBestIndividual() {
        if (population.isEmpty()) {
            return (T) this.chromosomeFactory.getChromosome();
        }

        // Assume population is sorted
        return  (T) population.get(0);
    }
}
