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
package org.evosuite;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.metaheuristics.SearchListener;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;

import java.io.Serializable;

/**
 * <p>
 * ProgressMonitor class.
 * </p>
 *
 * @author gordon
 */
public class ProgressMonitor<T extends Chromosome<T>> implements SearchListener<T>, Serializable {

    private static final long serialVersionUID = -8518559681906649686L;

    private StoppingCondition<T> stoppingCondition;
    private long max;
    private int currentCoverage;

    protected int lastCoverage;
    protected int lastProgress;
    protected int iteration;
    protected ClientState state;

    public ProgressMonitor() {
        stoppingCondition = null;
        max = 1;
        currentCoverage = 0;
        lastCoverage = 0;
        lastProgress = 0;
        iteration = 0;
        state = ClientState.INITIALIZATION;
    }

    public ProgressMonitor(ProgressMonitor<T> that) {
        this.stoppingCondition = that.stoppingCondition.clone();
        this.max = that.max;
        this.currentCoverage = that.currentCoverage;
        this.lastCoverage = that.lastCoverage;
        this.lastProgress = that.lastProgress;
        this.iteration = that.iteration;
        this.state = that.state;
    }

    /**
     * <p>
     * updateStatus
     * </p>
     *
     * @param percent a int.
     */
    public void updateStatus(int percent) {
        ClientState state = ClientState.SEARCH;
        ClientStateInformation information = new ClientStateInformation(state);
        information.setCoverage(currentCoverage);
        information.setProgress(percent);
        information.setIteration(iteration);
        //LoggingUtils.getEvoLogger().info("Setting to: "+state.getNumPhase()+": "+information.getCoverage()+"/"+information.getProgress());
        ClientServices.getInstance().getClientNode().changeState(state, information);
        lastProgress = percent;
        lastCoverage = currentCoverage;
        //out.writeInt(percent);
        //out.writeInt(currentPhase);
        //out.writeInt(phases);
        //out.writeInt(currentCoverage);
        //out.writeObject(currentTask);
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#searchStarted(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        for (StoppingCondition<T> cond : algorithm.getStoppingConditions()) {
            if (cond.getLimit() == 0) // No ZeroStoppingCondition
                continue;
            stoppingCondition = cond;
            max = stoppingCondition.getLimit();
            break;
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#iteration(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void iteration(GeneticAlgorithm<T> algorithm) {
        long current = stoppingCondition.getCurrentValue();
        currentCoverage = (int) Math.floor(algorithm.getBestIndividual().getCoverage() * 100);
        updateStatus((int) (100 * current / max));
        iteration++;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchFinished(GeneticAlgorithm<T> algorithm) {
        currentCoverage = (int) Math.floor(algorithm.getBestIndividual().getCoverage() * 100);
        if (currentCoverage > lastCoverage) {
            updateStatus((int) (100 * stoppingCondition.getCurrentValue() / max));
        }
        // System.out.println("");
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#fitnessEvaluation(org.evosuite.ga.Chromosome)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void fitnessEvaluation(T individual) {
        int current = (int) ((int) (100 * stoppingCondition.getCurrentValue()) / max);
        currentCoverage = (int) Math.floor(individual.getCoverage() * 100);
        if (currentCoverage > lastCoverage || current > lastProgress)
            updateStatus(current);
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
