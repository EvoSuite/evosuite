package org.evosuite.ga.metaheuristics.paes;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.AbstractMOSuiteGA;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian on 20.06.2018.
 */
public abstract class AbstractPAES<C extends Chromosome> extends AbstractMOSuiteGA<C> {

    // archive to store non-dominated solutions
    protected PaesArchiveInterface<C> archive;

    /**
     * Constructor
     *
     * @param factory a {@link ChromosomeFactory} object.
     */
    public AbstractPAES(ChromosomeFactory<C> factory) {
        super(factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializePopulation() {
        C first = this.chromosomeFactory.getChromosome();
        this.calculateFitness(first);
        this.population = new ArrayList<>();
        this.population.add(first);
        this.archive = new PaesArchive<C>(first.getFitnessValues().keySet(), 0, 1);
    }

    @Override
    public List<C> getNonDominatedSolutions(List<C> solutions){
        List<C> nonDominatedSolutions = archive.getChromosomes();
        nonDominatedSolutions.add(this.population.get(0));
        return nonDominatedSolutions;
    }
}
