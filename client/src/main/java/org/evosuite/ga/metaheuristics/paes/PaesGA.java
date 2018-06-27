package org.evosuite.ga.metaheuristics.paes;



import org.evosuite.ga.*;
import org.evosuite.ga.metaheuristics.paes.analysis.GenerationAnalysis;
import org.evosuite.rmi.ClientServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        long start = System.currentTimeMillis();
        C current = this.population.get(0);
        C candidate = (C)current.clone();
        candidate.mutate();
        this.calculateFitness(candidate);
        if(current.dominates(candidate)) {
            if(PAES_ANALYTIC_MODE)
                this.addGenerationAnalyse(false, this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CURRENT_DOMINATES_CANDIDATE, start);
            return;
        }
        if(candidate.dominates(current)) {
            archive.removeDominated(candidate);
            this.population.clear();
            this.population.add(candidate);
            if(PAES_ANALYTIC_MODE)
                this.addGenerationAnalyse(true, this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_CURRENT, start);
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
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_ARCHIVE, start);
            return;
        }
        if(!candidateIsDominated) {
            if (archive.decide(candidate, current)) {
                archive.add(current);
                this.population.clear();
                this.population.add(candidate);
                if(PAES_ANALYTIC_MODE)
                    this.addGenerationAnalyse(true, this.archive.getChromosomes(),
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CANDIDATE, start);
                return;
            } else {
                archive.add(candidate);
                if(PAES_ANALYTIC_MODE)
                    this.addGenerationAnalyse(false, this.archive.getChromosomes(),
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CURRENT, start);
                return;
            }
        }
        if(PAES_ANALYTIC_MODE)
            this.addGenerationAnalyse(false, this.archive.getChromosomes(),
                    GenerationAnalysis.RETURN_OPTION.ARCHIVE_DOMINATES_CANDIDATE, start);
    }

    private void addGenerationAnalyse(boolean accepted,
                                      List<C> chromosomes,
                                      GenerationAnalysis.RETURN_OPTION return_option,
                                      long startMillis){
        this.generationAnalyses.add(new GenerationAnalysis<C>(accepted,
                chromosomes,return_option, System.currentTimeMillis()- startMillis));
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
        if(PAES_ANALYTIC_MODE){
            Map<GenerationAnalysis.RETURN_OPTION,Integer> returnOptionsCount = new LinkedHashMap<>();
            Map<GenerationAnalysis.RETURN_OPTION,Long> timeSums = new LinkedHashMap<>();
            for(GenerationAnalysis.RETURN_OPTION option : GenerationAnalysis.RETURN_OPTION.values()){
                returnOptionsCount.put(option, 0);
                timeSums.put(option, (long)0);
            }
            for(GenerationAnalysis<C> generationAnalysis : this.generationAnalyses){
                GenerationAnalysis.RETURN_OPTION option = generationAnalysis.getReturnoption();
                int count = returnOptionsCount.get(option);
                long timeSum = timeSums.get(option);
                returnOptionsCount.put(option, count + 1);
                timeSums.put(option, timeSum + generationAnalysis.getMilliSeconds());
            }
            Map<GenerationAnalysis.RETURN_OPTION, Double> timeAvg = createAvgTimeMap(timeSums, returnOptionsCount);
            for(GenerationAnalysis.RETURN_OPTION option : GenerationAnalysis.RETURN_OPTION.values()){
                ClientServices.getInstance().getClientNode().trackOutputVariable(
                        GenerationAnalysis.RETURN_OPTION.getRuntimeVariableCount(option), option.name() +": " +returnOptionsCount.get(option));
                ClientServices.getInstance().getClientNode().trackOutputVariable(
                        GenerationAnalysis.RETURN_OPTION.getRuntimeVariableAvg(option), option.name()+": "+ timeAvg.get(option));
            }
        }
        this.notifySearchFinished();
    }

    private Map<GenerationAnalysis.RETURN_OPTION, Double> createAvgTimeMap(Map<GenerationAnalysis.RETURN_OPTION,Long> timeSums,
                                                                         Map<GenerationAnalysis.RETURN_OPTION,Integer> counts){
        Map<GenerationAnalysis.RETURN_OPTION, Double> timeAvg = new LinkedHashMap<>();
        for(GenerationAnalysis.RETURN_OPTION option : GenerationAnalysis.RETURN_OPTION.values()){
            if(counts.get(option) != 0)
                timeAvg.put(option, (double)timeSums.get(option)/ (double)counts.get(option));
            else
                timeAvg.put(option, (double)-1);
        }
        return timeAvg;
    }
}
