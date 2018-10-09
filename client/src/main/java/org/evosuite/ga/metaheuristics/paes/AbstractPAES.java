package org.evosuite.ga.metaheuristics.paes;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.AbstractMOSuiteGA;

import java.util.*;

/**
 * Created by Sebastian on 20.06.2018.
 */
public abstract class AbstractPAES<C extends Chromosome> extends AbstractMOSuiteGA<C> {

    // archive to store non-dominated solutions
    protected PaesArchiveInterface<C> archive;
    //stores last updated Set of objectives of the archive
    protected Set<FitnessFunction<?>> archiveObjectives = null;
    protected static final org.evosuite.Properties.PaesArchiveType PAES_ARCHIVE = org.evosuite.Properties.PAES_ARCHIVE_TYPE;

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
        Set<FitnessFunction<?>> ffs = new LinkedHashSet<>();
        ffs.addAll(this.fitnessFunctions);
        archiveObjectives = ffs;
        if(PAES_ARCHIVE == org.evosuite.Properties.PaesArchiveType.GRID)
            this.archive = new PaesArchive<>(ffs, 0, 1);
        else if(PAES_ARCHIVE == org.evosuite.Properties.PaesArchiveType.DISTANCE)
            this.archive = new PaesDistanceArchive<C>(this.fitnessFunctions);
        else
            throw new IllegalStateException("Unknown Archive Type");
    }

    @Override
    public List<C> getNonDominatedSolutions(List<C> solutions){
        List<C> nonDominatedSolutions = archive.getChromosomes();
        nonDominatedSolutions.add(this.population.get(0));
        return nonDominatedSolutions;
    }
}
