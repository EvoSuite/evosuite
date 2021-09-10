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

import org.evosuite.Properties.StoppingCondition;
import org.evosuite.Properties.TestFactory;
import org.evosuite.classpath.ClassPathHacker;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.runtime.util.Inputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class is used to keep track of how long EvoSuite has spent
 * in each of its different phases (eg search, minimization, assertion generation).
 *
 *
 * <p>
 * TODO: in the long run, this should replace all the occurrences of time handling in EvoSuite
 *
 * @author arcuri
 */
public class TimeController {

    private static final Logger logger = LoggerFactory.getLogger(TimeController.class);

    private static final TimeController singleton = new TimeController();

    /**
     * The current state of the client
     */
    private volatile ClientState state;

    /**
     * When the client was started
     */
    private volatile long clientStartTime;

    /**
     * When the current phase was started
     */
    private volatile long currentPhaseStartTime;

    /**
     * Leftover time
     */
    private volatile long timeLeftFromPreviousPhases;

    /**
     * Map from ClientState phase (key), to timeout (key)
     * in milliseconds for that phase
     */
    private Map<ClientState, Long> phaseTimeouts;

    /**
     * Map from ClientState phase (key), to how long (key)
     * in milliseconds EvoSuite was in that phase
     */
    private Map<ClientState, Long> timeSpentInEachPhase;


    /**
     * Main and only constructor
     */
    protected TimeController() {
        init();
    }

    private void init() {
        state = ClientState.NOT_STARTED;
        clientStartTime = 0;
        timeLeftFromPreviousPhases = 0;
        initializePhaseTimeouts();
    }

    public static void resetSingleton() {
        getInstance().init();
    }

    /**
     * Execute the given runnable synchronously, but issue a warning if it takes too long
     *
     * @param runnable
     * @param name         of the operation (will be used in the log)
     * @param warn_time_ms max time allowed for this operation before issuing a warning
     */
    public static void execute(Runnable runnable, String name, long warn_time_ms) {
        Inputs.checkNull(runnable, name);

        long start = java.lang.System.currentTimeMillis();
        runnable.run();
        long delta = java.lang.System.currentTimeMillis() - start;

        if (delta > warn_time_ms) {
            logger.warn("Operation '{}' took too long: {}ms", name, delta);
        }
    }

    private void initializePhaseTimeouts() {
        if (phaseTimeouts != null) {
            phaseTimeouts.clear();
        } else {
            phaseTimeouts = new ConcurrentHashMap<>();
        }

        // TODO: I don't understand why, but this may not have happened at this point
        Properties.getInstance();


        phaseTimeouts.put(ClientState.SEARCH, 1000L * getSearchBudgetInSeconds());
        phaseTimeouts.put(ClientState.MINIMIZATION, 1000L * Properties.MINIMIZATION_TIMEOUT);
        phaseTimeouts.put(ClientState.ASSERTION_GENERATION, 1000L * Properties.ASSERTION_TIMEOUT);
        phaseTimeouts.put(ClientState.CARVING, 1000L * Properties.CARVING_TIMEOUT);
        phaseTimeouts.put(ClientState.INITIALIZATION, 1000L * Properties.INITIALIZATION_TIMEOUT);
        phaseTimeouts.put(ClientState.JUNIT_CHECK, 1000L * Properties.JUNIT_CHECK_TIMEOUT);
        phaseTimeouts.put(ClientState.WRITING_TESTS, 1000L * Properties.WRITE_JUNIT_TIMEOUT);

        if (timeSpentInEachPhase != null) {
            timeSpentInEachPhase.clear();
        } else {
            timeSpentInEachPhase = new ConcurrentHashMap<>();
        }
    }

    /**
     * Get the singleton reference
     *
     * @return
     */
    public static TimeController getInstance() {
        return singleton;
    }


    public synchronized void updateState(ClientState newState) throws IllegalArgumentException {
        Inputs.checkNull(newState);

        if (state.equals(newState)) {
            //no change of state. do nothing
            return;
        }

        if (newState.getNumPhase() < state.getNumPhase()) {
            throw new IllegalArgumentException("Phase '" + newState + "' cannot be executed after phase '" + state + "'");
        }

        //first log the current state before changing it
        if (!state.equals(ClientState.NOT_STARTED)) {
            long elapsed = java.lang.System.currentTimeMillis() - currentPhaseStartTime;
            if (timeSpentInEachPhase.containsKey(state)) {
                logger.warn("Already entered in phase: " + state + ". This will mess up the timing calculations.");
            }
            timeSpentInEachPhase.put(state, elapsed);

            logger.debug("Phase {} lasted {} seconds", state, (elapsed / 1000));


            //check if spent too much time, eg due to bug in EvoSuite
            if (currentPhaseHasTimeout()) {
                long timeoutInMs = getCurrentPhaseTimeout();
                long left = timeoutInMs - elapsed;
                if (left < -(0.1 * timeoutInMs)) {
                    //just check if phase went over by more than 10%...
                    logger.warn("Phase " + state + " lasted too long, " + (-left / 1000) + " seconds more than allowed.");
                }
                if (Properties.REUSE_LEFTOVER_TIME) {
                    timeLeftFromPreviousPhases += left;
                    logger.info("Time left from previous phases: {}/{} -> {}, {}", left, timeoutInMs, timeLeftFromPreviousPhases, getLeftTimeBeforeEnd());
                }
            }

        }

        state = newState;
        currentPhaseStartTime = System.currentTimeMillis();

        if (state.equals(ClientState.STARTED)) {
            clientStartTime = currentPhaseStartTime;
        }

        if (currentPhaseHasTimeout()) {
            long left = getLeftTimeBeforeEnd();
            long timeout = getCurrentPhaseTimeout() + timeLeftFromPreviousPhases;
            if (left < timeout) {
                logger.warn("Current phase {} could run up to {}s, but only {}s are left",
                        state, (int) (timeout / 1000), (int) (left / 1000));
            }
        }
    }

    public static int getSearchBudgetInSeconds() {
        if (Properties.STOPPING_CONDITION == StoppingCondition.MAXTIME) {
            return (int) Properties.SEARCH_BUDGET;
        } else {
            return Properties.GLOBAL_TIMEOUT;
        }
    }

    public int calculateForHowLongClientWillRunInSeconds() {
        int time = Properties.EXTRA_TIMEOUT;

        time += Properties.INITIALIZATION_TIMEOUT;

        time += getSearchBudgetInSeconds();

        if (Properties.MINIMIZE) {
            time += Properties.MINIMIZATION_TIMEOUT;
        }
        if (Properties.ASSERTIONS) {
            time += Properties.ASSERTION_TIMEOUT;
        }
        if (Properties.TEST_FACTORY == TestFactory.JUNIT) {
            time += Properties.CARVING_TIMEOUT;
        }
        if (Properties.JUNIT_TESTS) {
            time += Properties.WRITE_JUNIT_TIMEOUT;
            if (Properties.JUNIT_CHECK == Properties.JUnitCheckValues.TRUE || (
                    Properties.JUNIT_CHECK == Properties.JUnitCheckValues.OPTIONAL && ClassPathHacker.isJunitCheckAvailable())) {
                time += Properties.JUNIT_CHECK_TIMEOUT;
            }
        }

        return time;
    }

    /**
     * Is there time to execute a test case?
     * This not only depends on which phase we are in, but
     * also on how maximum long a test case can be left run
     * before trying to kill its threads if timeout.
     *
     * @return
     */
    public synchronized boolean hasTimeToExecuteATestCase() {
        return isThereStillTimeInThisPhase(Properties.TIMEOUT);
    }

    public synchronized boolean isThereStillTimeInThisPhase() {
        return isThereStillTimeInThisPhase(1);
    }

    public synchronized boolean isThereStillTimeInThisPhase(long ms) {

        if (state.equals(ClientState.NOT_STARTED)) {
            return true;
        }

        //all time values are in milliseconds
        long left = getLeftTimeBeforeEnd();

        if (ms > left) {
            return false;
        }

        /*
         * OK, there is enough time, but have we spent too long
         * in the current phase?
         */
        if (currentPhaseHasTimeout()) {
            long timeoutInMs = getCurrentPhaseTimeout();
            long timeSincePhaseStarted = System.currentTimeMillis() - currentPhaseStartTime;
            long phaseLeft = timeoutInMs - timeSincePhaseStarted + timeLeftFromPreviousPhases;
            logger.debug("Time left for current phase {}: {}", state, phaseLeft);
            return ms <= phaseLeft;
        }

        return true;
    }

    private long getLeftTimeBeforeEnd() {
        long timeSinceStart = System.currentTimeMillis() - clientStartTime;
        long totalTimeLimit = 1000 * calculateForHowLongClientWillRunInSeconds();
        return totalTimeLimit - timeSinceStart;
    }

    /**
     * Calculate the percentage of progress in which we currently are in the phase.
     *
     * @return a value in [0,1] if the current phase has a timeout, otherwise a negative value
     */
    public double getPhasePercentage() {
        if (currentPhaseHasTimeout()) {
            long timeoutInMs = getCurrentPhaseTimeout();
            long timeSincePhaseStarted = System.currentTimeMillis() - currentPhaseStartTime;
            double ratio = (double) timeSincePhaseStarted / (double) timeoutInMs;
            assert ratio >= 0; // but could become >1 due to timer
            return Math.min(ratio, 1);
        } else {
            return -1;
        }
    }

    private long getCurrentPhaseTimeout() {
        return phaseTimeouts.get(state);
    }

    private boolean currentPhaseHasTimeout() {
        return phaseTimeouts.containsKey(state);
    }
}
