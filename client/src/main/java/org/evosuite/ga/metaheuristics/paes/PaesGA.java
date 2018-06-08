package org.evosuite.ga.metaheuristics.paes;



import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.paes.analysis.GenerationAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Class represents a Pareto Archived Evolution Strategy.
 * Each generation the current [{@link Chromosome} is cloned
 * and mutated to generate a new candidate in order to
 * check whether the candidate is a better solution.
 *
 * Created by Sebastian on 10.04.2018.
 */
public class PaesGA<C extends Chromosome> extends GeneticAlgorithm<C> {
    // archive to store non-dominated solutions
    private Archive<C> archive;
    private List<GenerationAnalysis> generationAnalyses;
    private static final boolean PAES_ANALYTIC_MODE = true;

    /**
     * Constructor
     *
     * @param factory a {@link ChromosomeFactory} object.
     */
    public PaesGA(ChromosomeFactory<C> factory) {
        super(factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void evolve() {
        C current = super.population.get(0);
        C candidate = (C)current.clone();
        candidate.mutate();
        if(current.dominates(candidate)) {
            if(PAES_ANALYTIC_MODE)
                this.generationAnalyses.add(new GenerationAnalysis(false,
                        this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CURRENT_DOMINATES_CANDIDATE));
            return;
        }
        if(candidate.dominates(current)) {
            archive.add(current);
            super.population = new ArrayList<>();
            super.population.add(candidate);
            if(PAES_ANALYTIC_MODE)
                this.generationAnalyses.add(new GenerationAnalysis(true,
                        this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_CURRENT));
            return;
        }
        List<C> archivedChromosomes = archive.getChromosomes();
        boolean candidateDominates = false;
        boolean candidateIsDominated = false;
        for( C c: archivedChromosomes){
            if(candidate.dominates(c)){
                candidateDominates = true;
                break;
            } else if(c.dominates(candidate)){
                candidateIsDominated = true;
                break;
            }
        }
        if(candidateDominates){
            archive.add(current);
            super.population = new ArrayList<>();
            super.population.add(candidate);
            if(PAES_ANALYTIC_MODE)
                this.generationAnalyses.add(new GenerationAnalysis(true,
                        this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_ARCHIVE));
            return;
        }
        if(!candidateIsDominated) {
            if (archive.decide(candidate, current)) {
                archive.add(current);
                super.population = new ArrayList<>();
                super.population.add(candidate);
                if(PAES_ANALYTIC_MODE)
                    this.generationAnalyses.add(new GenerationAnalysis(true,
                            this.archive.getChromosomes(),
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CANDIDATE));
                return;
            } else {
                archive.add(candidate);
                if(PAES_ANALYTIC_MODE)
                    this.generationAnalyses.add(new GenerationAnalysis(false,
                            this.archive.getChromosomes(),
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CURRENT));
                return;
            }
        }
        if(PAES_ANALYTIC_MODE)
            this.generationAnalyses.add(new GenerationAnalysis(false,
                    this.archive.getChromosomes(),
                    GenerationAnalysis.RETURN_OPTION.ARCHIVE_DOMINATES_CANDIDATE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializePopulation() {
        if(PAES_ANALYTIC_MODE)
            this.generationAnalyses = new ArrayList<>();
        C first = super.chromosomeFactory.getChromosome();
        super.population = new ArrayList<>();
        super.population.add(first);
        this.archive = new MyArchive<>(first.getCoverageValues().keySet(), 0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateSolution() {}
}
