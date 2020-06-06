package org.evosuite.ga.metaheuristics.ibea;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.operators.ranking.FastNonDominatedSorting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;

public class ModificatedIBEA <T extends Chromosome> extends IBEA {
    private static final Logger logger = LoggerFactory.getLogger(ModificatedIBEA.class);

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

        union.clear();
        int numberSubFronts = this.fastNonDominatedSorting.getNumberOfSubfronts();
        // Add the individuals of this front
        addFromFront(union, this.fastNonDominatedSorting.getSubfront(0), 5);
        if(numberSubFronts>2){
            addFromFront(union, this.fastNonDominatedSorting.getSubfront(1), 5);
        }

//        logger.warn("\nUnion size finished "+union.size());
       return union;
    }

    private void addFromFront(List<T> union, List<T> front, int maxSize){
        int count  = 0 ;

        for (int k = 0; k < front.size() && count< maxSize; k++){
            union.add(front.get(k));
            count++;
        }

    }

    //not needed for now
    public T getMyBestIndividual() {
        if (population.isEmpty()) {
            return (T) this.chromosomeFactory.getChromosome();
        }

        // Assume population is sorted
        return  (T) population.get(0);
    }
}
