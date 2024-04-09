package org.evosuite.ga.metaheuristics.mosa;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.metaheuristics.mosa.structural.MultiCriteriaManager;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.fuzzsearch.TestSuiteFuzzSearch;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvoFuzz extends DynaMOSA {

    private static final long serialVersionUID = 146182080947267628L;

    private static final Logger logger = LoggerFactory.getLogger(EvoFuzz.class);

    public EvoFuzz(ChromosomeFactory<TestChromosome> factory) {
        super(factory);
    }

    @Override
    public void generateSolution() {
        logger.debug("executing generateSolution function");
        long evolutionStartInMillis = System.currentTimeMillis();
        this.goalsManager = new MultiCriteriaManager(this.fitnessFunctions);
        if (this.population.isEmpty()) {
            this.initializePopulation();
        }

        this.rankingFunction.computeRankingAssignment(this.population, this.goalsManager.getCurrentGoals());
        for (int i = 0; i < this.rankingFunction.getNumberOfSubfronts(); i++) {
            this.distance.fastEpsilonDominanceAssignment(this.rankingFunction.getSubfront(i), this.goalsManager.getCurrentGoals());
        }

        this.evofuzz(evolutionStartInMillis);
    }

    private void evofuzz(long evolutionStartInMillis) {
        if(evolveUntilEvoFinished(evolutionStartInMillis)) {
            if(!fuzzUntilFinished()) {
                evolveUntilFinished();
            }
        }
       this.notifySearchFinished();
    }

    private boolean evolveUntilEvoFinished(long evolutionStartInMillis) {
        while (this.goalsManager.getUncoveredGoals().size() > 0 && !isEvoFinished()) {
            this.evolve();
            this.notifyIteration();
        }
        LoggingUtils.getEvoLogger().info("* {} seconds taken for evosuite", (System.currentTimeMillis() - evolutionStartInMillis) / 1000);
        return !isFinished();
    }

    private void evolveUntilFinished() {
        while (this.goalsManager.getUncoveredGoals().size() > 0 && !isFinished()) {
            this.evolve();
            this.notifyIteration();
        }
    }

    private boolean fuzzUntilFinished() {
        if (!TestSuiteFuzzSearch.getInstance().prepare(this.goalsManager.getUncoveredGoals())) {
            return false;
        }
        long fuzzStartInMillis = System.currentTimeMillis();
        while (this.goalsManager.getUncoveredGoals().size() > 0 && !isFinished()) {
            TestSuiteFuzzSearch.getInstance().search();
            notifyIteration();
            if ((System.currentTimeMillis() - fuzzStartInMillis) / 1000 >= Properties.FUZZ_BUDGET) {
                break;
            }
        }
        LoggingUtils.getEvoLogger().info("* {} seconds taken for fuzzing", (System.currentTimeMillis() - fuzzStartInMillis) / 1000);
        TestSuiteFuzzSearch.getInstance().finish();
        return true;
    }

    @Override
    public TestSuiteChromosome generateSuite() {
        TestSuiteChromosome solution = Archive.getArchiveInstance().getMinimizedSuite();
        return solution != null ? solution : super.generateSuite();
    }

    public boolean isEvoFinished() {
        return stoppingConditions.stream().anyMatch(c->
                c.getLimit() != 0 ? (
                        ((double)c.getCurrentValue()) / c.getLimit() >= Properties.FUZZ_START_PERCENT)
                        : false);
    }

}
