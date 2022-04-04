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

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * <p>SocketStoppingCondition class.</p>
 *
 * @author Gordon Fraser
 */
public class SocketStoppingCondition<T extends Chromosome<T>> implements StoppingCondition<T> {

    private static final long serialVersionUID = -8260473153410290373L;

    // There should only be one instance that opens the socket -> singleton design pattern
    private static SocketStoppingCondition<?> instance = null;

    private volatile boolean interrupted = false;

    private static final Logger logger = LoggerFactory.getLogger(SocketStoppingCondition.class);

    private SocketStoppingCondition() {
        // singleton pattern
    }

    @SuppressWarnings("unchecked")
    public static <T extends Chromosome<T>> SocketStoppingCondition<T> getInstance() {
        if (instance == null) {
            instance = new SocketStoppingCondition<>();
        }

        // Unchecked cast always succeeds as long as we're not actually doing anything with a `T`
        // instance.
        return (SocketStoppingCondition<T>) instance;
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

    /**
     * <p>accept</p>
     */
    public void accept() {
        Thread t = new Thread(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(Properties.STOPPING_PORT);
                serverSocket.accept();
                LoggingUtils.getEvoLogger().info("* Stopping request received");
                interrupted = true;

            } catch (IOException e) {
                LoggingUtils.getEvoLogger().warn("Failed to create socket on port "
                        + Properties.STOPPING_PORT);
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        logger.info("Error while closing socket: " + e);
                    }
                }
            }

        });
        t.start();
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#searchStarted(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchStarted(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#iteration(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void iteration(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.SearchListener#searchFinished(org.evosuite.ga.GeneticAlgorithm)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void searchFinished(GeneticAlgorithm<T> algorithm) {
        // TODO Auto-generated method stub

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

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#forceCurrentValue(long)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void forceCurrentValue(long value) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getCurrentValue()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#getLimit()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#isFinished()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinished() {
        return interrupted;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#reset()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        interrupted = false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.ga.stoppingconditions.StoppingCondition#setLimit(long)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLimit(long limit) {
        // TODO Auto-generated method stub

    }

}
