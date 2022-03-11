/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.localsearch;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * <p>
 * This singleton class provides support for updating the amount of LS budget
 * that was consumed during the current application of LS.
 * </p>
 *
 * @author Gordon Fraser
 */
public class LocalSearchBudget<T extends Chromosome<T>> implements SearchListener<T>, Serializable {

    private static final long serialVersionUID = 9152147170303160131L;

    private final static Logger logger = LoggerFactory.getLogger(LocalSearchBudget.class);

    // Singleton instance
    private static LocalSearchBudget<?> instance = null;

    protected int fitnessEvaluations = 0;
    protected int tests = 0;
    protected long executedStart = 0L;
    protected int suites = 0;
    protected long startTime = 0L;
    protected long endTime = 0L;

    protected GeneticAlgorithm<?> ga = null;

    // Private constructor because of singleton type
    private LocalSearchBudget() {

    }

    // Singleton accessor
    @SuppressWarnings("unchecked")
    public static <T extends Chromosome<T>> LocalSearchBudget<T> getInstance() {
        if (instance == null)
            instance = new LocalSearchBudget<>();

        return (LocalSearchBudget<T>) instance;
    }

    /**
     * <p>
     * isFinished
     * </p>
     *
     * @return a boolean.
     */
    public boolean isFinished() {
        // If the global search is finished then we don't want local search either
        if (ga != null && ga.isFinished())
            return true;

        boolean isFinished = false;

        switch (Properties.LOCAL_SEARCH_BUDGET_TYPE) {
            case FITNESS_EVALUATIONS:
                isFinished = fitnessEvaluations >= Properties.LOCAL_SEARCH_BUDGET;
                break;
            case SUITES:
                isFinished = suites >= Properties.LOCAL_SEARCH_BUDGET;
                break;
            case STATEMENTS:
                isFinished = MaxStatementsStoppingCondition.getNumExecutedStatements() > executedStart + Properties.LOCAL_SEARCH_BUDGET;
                break;
            case TESTS:
                isFinished = tests >= Properties.LOCAL_SEARCH_BUDGET;
                break;
            case TIME:
                isFinished = System.currentTimeMillis() > endTime;
                break;
            default:
                throw new RuntimeException("Unknown budget type: "
                        + Properties.LOCAL_SEARCH_BUDGET_TYPE);
        }
        if (isFinished) {
            logger.info("Local search budget used up; type: " + Properties.LOCAL_SEARCH_BUDGET_TYPE);
        }
        return isFinished;
    }

    /**
     * Reports that a fitness evaluation was consumed
     */
    public void countFitnessEvaluation() {
        fitnessEvaluations++;
    }

    /**
     * Reports that Local search on an specific test has been concluded.
     */
    public void countLocalSearchOnTest() {
        tests++;
    }

    /**
     * Reports that local search on a whole test suite has been finished.
     */
    public void countLocalSearchOnTestSuite() {
        suites++;
    }

    /**
     * Indicates that the application of LS to the current population has started.
     */
    public void localSearchStarted() {
        startTime = System.currentTimeMillis();
        endTime = startTime + Properties.LOCAL_SEARCH_BUDGET * 1000;
        tests = 0;
        suites = 0;
        fitnessEvaluations = 0;
        executedStart = MaxStatementsStoppingCondition.getNumExecutedStatements();
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#searchStarted(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        ga = algorithm;
        tests = 0;
        suites = 0;
        fitnessEvaluations = 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#iteration(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void iteration(GeneticAlgorithm<T> algorithm) {
        tests = 0;
        suites = 0;
        fitnessEvaluations = 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchFinished(GeneticAlgorithm<T> algorithm) {
        ga = null;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#fitnessEvaluation(org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void fitnessEvaluation(T individual) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#modification(org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void modification(T individual) {
        // TODO Auto-generated method stub

    }

}
