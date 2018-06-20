package org.evosuite.ga.metaheuristics.paes;



import org.evosuite.Properties;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.paes.analysis.GenerationAnalysis;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
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
public class PaesGA<C extends Chromosome> extends GeneticAlgorithm<C> {
    // archive to store non-dominated solutions
    private Archive<C> archive;
    private List<GenerationAnalysis> generationAnalyses;
    private static final boolean PAES_ANALYTIC_MODE = true;
    private static final Logger logger = LoggerFactory.getLogger(PaesGA.class);
    /** Keep track of overall suite fitness functions and correspondent test fitness functions */
    protected final Map<TestSuiteFitnessFunction, Class<?>> suiteFitnessFunctions;

    /**
     * Constructor
     *
     * @param factory a {@link ChromosomeFactory} object.
     */
    public PaesGA(ChromosomeFactory<C> factory) {
        super(factory);
        this.suiteFitnessFunctions = new LinkedHashMap<>();
        for (Properties.Criterion criterion : Properties.CRITERION) {
            TestSuiteFitnessFunction suiteFit = FitnessFunctions.getFitnessFunction(criterion);
            Class<?> testFit = FitnessFunctions.getTestFitnessFunctionClass(criterion);
            this.suiteFitnessFunctions.put(suiteFit, testFit);
        }
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
                this.generationAnalyses.add(new GenerationAnalysis(false,
                        this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CURRENT_DOMINATES_CANDIDATE));
            return;
        }
        if(candidate.dominates(current)) {
            archive.add(current);
            this.population.clear();
            this.population.add(candidate);
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
            this.population.clear();
            this.population.add(candidate);
            if(PAES_ANALYTIC_MODE)
                this.generationAnalyses.add(new GenerationAnalysis(true,
                        this.archive.getChromosomes(),
                        GenerationAnalysis.RETURN_OPTION.CANDIDATE_DOMINATES_ARCHIVE));
            return;
        }
        if(!candidateIsDominated) {
            if (archive.decide(candidate, current)) {
                archive.add(current);
                this.population.clear();
                this.population.add(candidate);
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
        C first = this.chromosomeFactory.getChromosome();
        this.calculateFitness(first);
        this.population = new ArrayList<>();
        this.population.add(first);
        this.archive = new MyArchive<>(first.getCoverageValues().keySet(), 0, 1);
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
        /*debug_print(this.population.get(0).getClass().toString());
        TestSuiteChromosome testSuiteChromosome = new TestSuiteChromosome();
        testSuiteChromosome.addTest(((TestChromosome)this.population.get(0)).getTestCase());
        this.population.set(0, (C)testSuiteChromosome);*/
        // storing the time needed to reach the maximum coverage
        this.notifySearchFinished();
        //PaesGA.logger.info("executed PaesGa.generateSolution");
    }

    @Override
    public C getBestIndividual(){
        TestSuiteChromosome best = generateSuite();
        if(best.getTestChromosomes().isEmpty()) {
            for (TestSuiteFitnessFunction suiteFitness : this.suiteFitnessFunctions.keySet()) {
                best.setCoverage(suiteFitness, 0.0);
                best.setFitness(suiteFitness, 1.0);
            }
            return (C) best;
        }
        this.computeCoverageAndFitness(best);
        return (C)best;
    }

    private void computeCoverageAndFitness(TestSuiteChromosome suite){
        for (Map.Entry<TestSuiteFitnessFunction, Class<?>> entry : this.suiteFitnessFunctions
                .entrySet()) {
            TestSuiteFitnessFunction suiteFitnessFunction = entry.getKey();
            Class<?> testFitnessFunction = entry.getValue();
            int numberCoveredTargets = 0;//TODO Wert berechnen
            int numberUncoveredTargets = 0;//TODO Wert berechnen

            suite.setFitness(suiteFitnessFunction, ((double) numberUncoveredTargets));
            suite.setCoverage(suiteFitnessFunction, ((double) numberCoveredTargets)
                    / ((double) (numberCoveredTargets + numberUncoveredTargets)));
            suite.setNumOfCoveredGoals(suiteFitnessFunction, numberCoveredTargets);
            suite.setNumOfNotCoveredGoals(suiteFitnessFunction, numberUncoveredTargets);
        }
    }

    protected TestSuiteChromosome generateSuite(){
        if(this.population.isEmpty())
            this.initializePopulation();
        TestSuiteChromosome testSuiteChromosome = new TestSuiteChromosome();
        testSuiteChromosome.addTest((TestChromosome)this.population.get(0));
        for(C test : this.archive.getChromosomes())
            testSuiteChromosome.addTest((TestChromosome) test);
        return testSuiteChromosome;
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
