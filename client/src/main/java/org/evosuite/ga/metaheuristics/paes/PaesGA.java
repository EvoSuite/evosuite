package org.evosuite.ga.metaheuristics.paes;



import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.paes.analysis.GenerationAnalysis;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
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
            /**if(PAES_ANALYTIC_MODE)
                this.generationAnalyses.add(new GenerationAnalysis(false,
                        this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CURRENT_DOMINATES_CANDIDATE));*/
            return;
        }
        if(candidate.dominates(current)) {
            archive.add(current);
            this.population.clear();
            this.population.add(candidate);
            /**if(PAES_ANALYTIC_MODE)
                this.generationAnalyses.add(new GenerationAnalysis(true,
                        this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_CURRENT));*/
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
            this.population.clear();
            this.population.add(candidate);
            /**if(PAES_ANALYTIC_MODE)
                this.generationAnalyses.add(new GenerationAnalysis(true,
                        this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_ARCHIVE));*/
            return;
        }
        if(!candidateIsDominated) {
            if (archive.decide(candidate, current)) {
                archive.add(current);
                this.population.clear();
                this.population.add(candidate);
                /**if(PAES_ANALYTIC_MODE)
                    this.generationAnalyses.add(new GenerationAnalysis(true,
                            this.archive.getChromosomes(),
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CANDIDATE));*/
                return;
            } else {
                archive.add(candidate);
                /**if(PAES_ANALYTIC_MODE)
                    this.generationAnalyses.add(new GenerationAnalysis(false,
                            this.archive.getChromosomes(),
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CURRENT));*/
                return;
            }
        }
        /**if(PAES_ANALYTIC_MODE)
            this.generationAnalyses.add(new GenerationAnalysis(false,
                    this.archive.getChromosomes(),
                    GenerationAnalysis.RETURN_OPTION.ARCHIVE_DOMINATES_CANDIDATE));*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateSolution() {
        if(this.population.isEmpty())
            initializePopulation();
        debug_print(Double.toString(this.population.get(0).getFitness()));
        while(!this.isFinished()){
            this.evolve();
            this.notifyIteration();
            debug_print(Double.toString(this.population.get(0).getFitness()));
        }
        for(StoppingCondition s : this.stoppingConditions){
            debug_print("Stopping Condition: " + s.toString());
        }
        this.notifySearchFinished();
    }

    private void debug_print(String msg){
        String time = LocalTime.now() + " " + LocalDate.now();
        try {
            FileWriter fileWriter = new FileWriter("C:\\Users\\Sebastian\\Desktop\\paesLog.txt", true);
            fileWriter.write(time + ": " + msg +"\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
