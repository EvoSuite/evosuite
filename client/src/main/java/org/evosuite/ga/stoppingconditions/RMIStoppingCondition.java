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
package org.evosuite.ga.stoppingconditions;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;

/**
 * @author Gordon Fraser
 */
public class RMIStoppingCondition<T extends Chromosome<T>> implements StoppingCondition<T> {

    private static final long serialVersionUID = 3073266508021896691L;

    private static RMIStoppingCondition<?> instance = null;

    private boolean isStopped = false;

    private RMIStoppingCondition() {
        // empty default constructor
    }

    @SuppressWarnings("unchecked")
    public static <T extends Chromosome<T>> RMIStoppingCondition<T> getInstance() {
        if (instance == null) {
            instance = new RMIStoppingCondition<>();
        }

        // Cast always succeeds because RMIStoppingCondition doesn't actually do anything with a
        // `T` instance.
        return (RMIStoppingCondition<T>) instance;
    }

    /**
     * Always throws an {@code UnsupportedOperationException} when called. Singletons cannot be
     * cloned.
     *
     * @return never returns, always fails
     * @throws UnsupportedOperationException always
     */
    @Override
    public StoppingCondition<T> clone() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("cannot clone singleton");
    }

    public void stop() {
        isStopped = true;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#searchStarted(org.evosuite.ga.GeneticAlgorithm)
     */
    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#iteration(org.evosuite.ga.GeneticAlgorithm)
     */
    @Override
    public void iteration(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
     */
    @Override
    public void searchFinished(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#fitnessEvaluation(org.evosuite.ga.Chromosome)
     */
    @Override
    public void fitnessEvaluation(T individual) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#modification(org.evosuite.ga.Chromosome)
     */
    @Override
    public void modification(T individual) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#forceCurrentValue(long)
     */
    @Override
    public void forceCurrentValue(long value) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getCurrentValue()
     */
    @Override
    public long getCurrentValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getLimit()
     */
    @Override
    public long getLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#isFinished()
     */
    @Override
    public boolean isFinished() {
        return isStopped;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#reset()
     */
    @Override
    public void reset() {
        isStopped = false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#setLimit(long)
     */
    @Override
    public void setLimit(long limit) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RMIStoppingCondition";
    }
}
