package org.evosuite.ga.metaheuristics.paes;



import org.evosuite.ga.*;
import org.evosuite.ga.metaheuristics.paes.analysis.GenerationAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class PaesGA<C extends Chromosome> extends AbstractPAES<C> {
    private List<GenerationAnalysis> generationAnalyses;
    private static final boolean PAES_ANALYTIC_MODE = true;
    private static final Logger logger = LoggerFactory.getLogger(PaesGA.class);
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
        C current = this.population.get(0);
        C candidate = (C)current.clone();
        candidate.mutate();
        this.calculateFitness(candidate);
        if(current.dominates(candidate)) {
            if(PAES_ANALYTIC_MODE)
                this.addGenerationAnalyse(false, this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CURRENT_DOMINATES_CANDIDATE);
            return;
        }
        if(candidate.dominates(current)) {
            archive.removeDominated(candidate);
            this.population.clear();
            this.population.add(candidate);
            if(PAES_ANALYTIC_MODE)
                this.addGenerationAnalyse(true, this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_CURRENT);
            return;
        }
        List<C> archivedChromosomes = archive.getChromosomes();
        boolean candidateDominates = false;
        boolean candidateIsDominated = false;
        for( C c: archivedChromosomes){
            if(candidate.dominates(c)){
                candidateDominates = true;
            } else if(c.dominates(candidate)){
                candidateIsDominated = true;
            }
        }
        assert !(candidateDominates && candidateIsDominated);
        if(candidateDominates){
            archive.add(current);
            this.population.clear();
            this.population.add(candidate);
            if(PAES_ANALYTIC_MODE)
                this.addGenerationAnalyse(true, this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_ARCHIVE);
            return;
        }
        if(!candidateIsDominated) {
            if (archive.decide(candidate, current)) {
                archive.add(current);
                this.population.clear();
                this.population.add(candidate);
                if(PAES_ANALYTIC_MODE)
                    this.addGenerationAnalyse(true, this.archive.getChromosomes(),
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CANDIDATE);
                return;
            } else {
                archive.add(candidate);
                if(PAES_ANALYTIC_MODE)
                    this.addGenerationAnalyse(false, this.archive.getChromosomes(),
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CURRENT);
                return;
            }
        }
        if(PAES_ANALYTIC_MODE)
            this.addGenerationAnalyse(false, this.archive.getChromosomes(),
                    GenerationAnalysis.RETURN_OPTION.ARCHIVE_DOMINATES_CANDIDATE);
    }

    private void addGenerationAnalyse(boolean accepted,
                                      List<C> chromosomes,
                                      GenerationAnalysis.RETURN_OPTION return_option){
        this.generationAnalyses.add(new GenerationAnalysis<C>(accepted,
                chromosomes,return_option));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateSolution() {
        if(this.population.isEmpty())
            initializePopulation();
        if(PAES_ANALYTIC_MODE){
            this.generationAnalyses = new ArrayList<>();
        }
        while(!this.isFinished()){
            this.evolve();
            ++this.currentIteration;
            this.notifyIteration();
        }
        this.notifySearchFinished();
    }
}
