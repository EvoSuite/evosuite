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
package org.evosuite.statistics;

import org.evosuite.Properties;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Client-side listener that transmits data to master
 *
 * @author gordon
 */
public class StatisticsListener<T extends Chromosome<T>> implements SearchListener<T> {

    private static final long serialVersionUID = -8229756367168023616L;

    private final BlockingQueue<T> individuals;

    private volatile boolean done;

    private volatile double bestFitness;

    private volatile boolean minimizing;

    private int numFitnessEvaluations;

    private final Thread notifier;

    /**
     * When did we send an individual due to a new generation iteration?
     */
    private volatile long timeFromLastGenerationUpdate = 0;

    public StatisticsListener() {
        individuals = new LinkedBlockingQueue<>();
        done = false;
        bestFitness = Double.MAX_VALUE;
        minimizing = true;
        numFitnessEvaluations = 0;

        notifier = new Thread(() -> {
            // Wait for new element in queue
            // If there is a new element, then send it to master through RMI
            while (!done || !individuals.isEmpty()) {
                T individual;
                try {
                    individual = individuals.take();
                    StatisticsSender.sendIndividualToMaster(individual);
                } catch (InterruptedException e) {
                    done = true;
                }
            }
        });
        Sandbox.addPrivilegedThread(notifier);
        notifier.start();
    }

    public StatisticsListener(StatisticsListener<T> that) {
        this.individuals = new LinkedBlockingQueue<>(that.individuals);
        this.bestFitness = that.bestFitness;
        this.done = that.done;
        this.minimizing = that.minimizing;
        this.numFitnessEvaluations = that.numFitnessEvaluations;
        this.timeFromLastGenerationUpdate = that.timeFromLastGenerationUpdate;

        this.notifier = new Thread(that.notifier);
        Sandbox.addPrivilegedThread(this.notifier);
        this.notifier.start();
    }

    @Override
    public void iteration(GeneticAlgorithm<T> algorithm) {

        long elapsed = System.currentTimeMillis() - timeFromLastGenerationUpdate;
        if (elapsed > Properties.TIMELINE_INTERVAL) {
            /*
             * We do not send an individual to Master at each new generation, because
             * for fast CUTs we could have far too many generations.
             * As this info is only used for time interval values, there
             * is no point in sending too many
             */
            timeFromLastGenerationUpdate = System.currentTimeMillis();
            // Enqueue current best individual
            individuals.offer(algorithm.getBestIndividual());
            // send timeline variable directly
            ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.TotalExceptionsTimeline, ExceptionCoverageSuiteFitness.getMaxExceptionsCovered());
        }
    }

    @Override
    public void searchFinished(GeneticAlgorithm<T> algorithm) {

        // If the search is finished, we may want to clear the queue and just send the final element?
        //individuals.clear(); // TODO: Maybe have a check on size
        individuals.offer(algorithm.getBestIndividual());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Statements_Executed, MaxStatementsStoppingCondition.getNumExecutedStatements());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Tests_Executed, MaxTestsStoppingCondition.getNumExecutedTests());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Generations, algorithm.getAge());
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Fitness_Evaluations, numFitnessEvaluations);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.TotalExceptionsTimeline, ExceptionCoverageSuiteFitness.getMaxExceptionsCovered());

        if (algorithm.getBestIndividual() instanceof TestSuiteChromosome) {
            reportTestSuiteResult((TestSuiteChromosome) algorithm.getBestIndividual());
        }
        done = true;
        try {
            notifier.join(3000);
        } catch (InterruptedException e) {
            notifier.interrupt();
            Thread.currentThread().interrupt();//interrupted flag was reset
        }
    }

    private void reportTestSuiteResult(TestSuiteChromosome testSuite) {

    }

    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        done = false;
        if (algorithm.getFitnessFunction().isMaximizationFunction()) {
            bestFitness = 0.0;
            minimizing = false;
        } else {
            bestFitness = Double.MAX_VALUE;
            minimizing = true;
        }
    }

    @Override
    public void fitnessEvaluation(T individual) {
        numFitnessEvaluations++;
        if (!(individual instanceof TestSuiteChromosome)) {
            // Statistics expects TestSuiteChromosome individuals
            return;
        }
        double fitness = individual.getFitness();
        if (minimizing) {
            if (fitness < bestFitness) {
                bestFitness = fitness;

                individuals.offer(individual);
            }
        } else {
            if (fitness > bestFitness) {
                bestFitness = fitness;

                individuals.offer(individual);
            }
        }
    }

    @Override
    public void modification(T individual) {
        // Nothing to do
    }

}
