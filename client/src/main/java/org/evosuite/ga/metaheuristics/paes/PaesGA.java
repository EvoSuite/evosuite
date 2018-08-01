package org.evosuite.ga.metaheuristics.paes;



import org.evosuite.Properties;
import org.evosuite.ga.*;
import org.evosuite.ga.metaheuristics.paes.analysis.AnalysisFileWriter;
import org.evosuite.ga.metaheuristics.paes.analysis.GenerationAnalysis;
import org.evosuite.rmi.ClientServices;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

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
    private static final boolean PAES_ANALYTIC_MODE = Properties.PAES_GENERATION_ANALYSIS;
    private static final Logger logger = LoggerFactory.getLogger(PaesGA.class);
    /**
     * Constructor based on the abstract class {@link AbstractPAES}
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
        //Save time for AvgTime calculation
        long start = 0;
        int archiveSize = this.archive.getChromosomes().size();
        if(PaesGA.PAES_ANALYTIC_MODE)
            start = System.currentTimeMillis();

        //Clone, mutate and calculate FitnessValue for the next generation(size=1)
        C current = this.population.get(0);
        C candidate = (C)current.clone();
        candidate.mutate();
        candidate.getFitnessValues().clear();
        this.calculateFitness(candidate);
        double candidateFit = 0.0;
        double currentFit = 0.0;
        if(PAES_ANALYTIC_MODE){
            for(FitnessFunction<?> ff : this.fitnessFunctions){
                candidateFit += FitnessFunction.normalize(candidate.getFitness(ff));
                currentFit += FitnessFunction.normalize(current.getFitness(ff));
            }
            candidateFit /= this.fitnessFunctions.size();
            currentFit /= this.fitnessFunctions.size();
        }
        List<FitnessFunction<?>> ffs = new ArrayList<>(fitnessFunctions);

        if(current.dominates(candidate, ffs)){
            if(PAES_ANALYTIC_MODE)
                this.addGenerationAnalyse(false, archiveSize,
                        GenerationAnalysis.RETURN_OPTION.CURRENT_DOMINATES_CANDIDATE, currentFit, candidateFit,start);
            return;
        }
        if(candidate.dominates(current, ffs)) {
            archive.removeDominated(candidate);
            this.population.clear();
            this.population.add(candidate);
            if(PAES_ANALYTIC_MODE)
                this.addGenerationAnalyse(true, archiveSize,
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_CURRENT, currentFit, candidateFit, start);
            return;
        }
        List<C> archivedChromosomes = archive.getChromosomes();
        boolean candidateDominates = false;
        boolean candidateIsDominated = false;
        for( C c: archivedChromosomes){
            if(candidate.dominates(c, ffs)){
                candidateDominates = true;
            } else if(c.dominates(candidate, ffs)){
                candidateIsDominated = true;
            }
        }
        assert !(candidateDominates && candidateIsDominated);
        if(candidateDominates){
            archive.add(current);
            this.population.clear();
            this.population.add(candidate);
            if(PAES_ANALYTIC_MODE)
                this.addGenerationAnalyse(true, archiveSize,
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_ARCHIVE, currentFit, candidateFit, start);
            return;
        }
        if(!candidateIsDominated) {
            if (archive.decide(candidate, current)) {
                archive.add(current);
                this.population.clear();
                this.population.add(candidate);
                if(PAES_ANALYTIC_MODE)
                    this.addGenerationAnalyse(true, archiveSize,
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CANDIDATE,currentFit, candidateFit, start);
                return;
            } else {
                archive.add(candidate);
                if(PAES_ANALYTIC_MODE)
                    this.addGenerationAnalyse(false, archiveSize,
                            GenerationAnalysis.RETURN_OPTION.ARCHIVE_DECIDES_CURRENT,currentFit, candidateFit, start);
                return;
            }
        }
        if(PAES_ANALYTIC_MODE)
            this.addGenerationAnalyse(false, archiveSize,
                    GenerationAnalysis.RETURN_OPTION.ARCHIVE_DOMINATES_CANDIDATE, currentFit, candidateFit, start);
    }

    private void addGenerationAnalyse(boolean accepted,
                                      int archiveSize,
                                      GenerationAnalysis.RETURN_OPTION return_option,
                                      double currentFitness,
                                      double candidateFitness,
                                      long startMillis){
        this.generationAnalyses.add(
                new GenerationAnalysis<C>(accepted, this.currentIteration,
                archiveSize, return_option, currentFitness, candidateFitness,
                System.currentTimeMillis()- startMillis));
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
        if(PAES_ANALYTIC_MODE){/**
            Map<GenerationAnalysis.RETURN_OPTION,Integer> returnOptionsCount = new LinkedHashMap<>();
            Map<GenerationAnalysis.RETURN_OPTION,Long> timeSums = new LinkedHashMap<>();
            int sumArchiveSize = 0;
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
                sumArchiveSize += generationAnalysis.getArchiveSize();
            }
            Map<GenerationAnalysis.RETURN_OPTION, Double> timeAvg = createAvgTimeMap(timeSums, returnOptionsCount);
            for(GenerationAnalysis.RETURN_OPTION option : GenerationAnalysis.RETURN_OPTION.values()){
                ClientServices.getInstance().getClientNode().trackOutputVariable(
                        GenerationAnalysis.RETURN_OPTION.getRuntimeVariableCount(option), option.name() +"Count: " +returnOptionsCount.get(option));
                ClientServices.getInstance().getClientNode().trackOutputVariable(
                        GenerationAnalysis.RETURN_OPTION.getRuntimeVariableAvg(option), option.name()+"Avg: "+ timeAvg.get(option));
            }
            ClientServices.getInstance().getClientNode().trackOutputVariable(
                    RuntimeVariable.AvgArchiveSize, sumArchiveSize/this.generationAnalyses.size()
            );*/
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.PAES_MAX_ARCHIVE_SIZE, Properties.POPULATION);
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.PAES_GRID_DEPTH, Properties.GRID_DEPTH);
            long seed = Randomness.getSeed();
            Properties.PaesArchiveType archiveType = super.PAES_ARCHIVE;
            int population = Properties.POPULATION;
            int gridDepth = Properties.GRID_DEPTH;
            String[] packagedClassName = RuntimeSettings.className.split("\\.");
            String className = packagedClassName[packagedClassName.length-1];
            String fileName = className + "_" + population+"_"+archiveType+"_"+gridDepth+"_"+seed;

            AnalysisFileWriter analysisFileWriter = new AnalysisFileWriter(fileName);
            analysisFileWriter.addAll(this.generationAnalyses);
            try {
                analysisFileWriter.writeToDisc();
            } catch (IOException e) {
                this.logger.warn("could not write generation analyses to disc");
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
