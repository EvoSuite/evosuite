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
package org.evosuite.utils;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * EvoSuite can run out of resources: eg out of memory, or too many threads that
 * are stalled and cannot be killed.
 * <p>
 * There can be several ways to handle these cases. The simplest is to to just
 * stop the search. Note: stopping the search when EvoSuite is close to run of
 * memory is important because, if it does actually run out of memory, when it
 * will not be able to write down the results obtained so far!
 *
 * @author Gordon Fraser
 */
public class ResourceController<T extends Chromosome<T>> implements SearchListener<T>,
        StoppingCondition<T>, Serializable {

    private static final long serialVersionUID = -4459807323163275506L;

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private GeneticAlgorithm<T> ga;
    private boolean stopComputation;

    public ResourceController() {
        // empty default constructor
    }

    public ResourceController(ResourceController<T> that) {
        this.ga = that.ga; // no deep copy
        this.stopComputation = that.stopComputation;
    }

    @Override
    public ResourceController<T> clone() {
        return new ResourceController<>(this);
    }

    private boolean hasExceededResources() {

        if (TestCaseExecutor.getInstance().getNumStalledThreads() >= Properties.MAX_STALLED_THREADS) {
            logger.info("* Too many stalled threads: "
                    + TestCaseExecutor.getInstance().getNumStalledThreads() + " / "
                    + Properties.MAX_STALLED_THREADS);
            return true;
        }

        Runtime runtime = Runtime.getRuntime();

        long freeMem = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();

        if (freeMem < Properties.MIN_FREE_MEM) {
            logger.trace("* Running out of memory, calling GC with memory left: "
                    + freeMem + " / " + runtime.maxMemory());
            System.gc();
            freeMem = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();

            if (freeMem < Properties.MIN_FREE_MEM) {
                logger.info("* Running out of memory, giving up: " + freeMem + " / "
                        + runtime.maxMemory() + " - need " + Properties.MIN_FREE_MEM);
                return true;
            } else {
                logger.trace("* Garbage collection recovered sufficient memory: "
                        + freeMem + " / " + runtime.maxMemory());
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        ga = algorithm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void iteration(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchFinished(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fitnessEvaluation(T individual) {
        if (hasExceededResources()) {
            /*
             * TODO: for now, we just stop the search. in case of running out of memory, other options could
             * be to reduce the population size, eg by using "removeWorstIndividuals". but before that,
             * "calculateFitness" need to be-refactored
             */
            stopComputation = true;
            ga.addStoppingCondition(this);
            logger.warn("Shutting down the search due to running out of computational resources");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modification(T individual) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forceCurrentValue(long value) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinished() {
        return stopComputation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        stopComputation = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLimit(long limit) {
        // TODO Auto-generated method stub

    }

}
