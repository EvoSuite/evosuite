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
package org.evosuite.rmi.service;

import org.evosuite.Properties;
import org.evosuite.*;
import org.evosuite.Properties.NoSuchParameterException;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.ClassStatisticsPrinter;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.stoppingconditions.RMIStoppingCondition;
import org.evosuite.junit.CoverageAnalysis;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.runtime.sandbox.PermissionStatistics;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.utils.FileIOUtils;
import org.evosuite.utils.Listener;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.*;

public class ClientNodeImpl<T extends Chromosome<T>>
        implements ClientNodeLocal<T>, ClientNodeRemote<T> {

    private static final Logger logger = LoggerFactory.getLogger(ClientNodeImpl.class);
    private static final long serialVersionUID = 485858845631346580L;

    /**
     * The current state/phase in which this client process is (eg, search or assertion generation)
     */
    private volatile ClientState state;

    /**
     * RMI reference used to communicate with the master node
     */
    private MasterNodeRemote masterNode;

    /**
     * A unique identifier for this client process (needed if running several clients in parallel)
     */
    private String clientRmiIdentifier;

    /**
     * A latch used to wait till the test generation is done
     */
    protected volatile CountDownLatch doneLatch;

    /**
     * A latch used to wait for this process to be fully finished
     */
    protected volatile CountDownLatch finishedLatch;


    protected Registry registry;

    protected final Collection<Listener<Set<T>>> listeners = Collections.synchronizedList(
            new ArrayList<>());

    protected final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();

    private final BlockingQueue<OutputVariable> outputVariableQueue = new LinkedBlockingQueue<>();

    private Collection<Set<T>> bestSolutions;

    private Thread statisticsThread;

    //only for testing
    protected ClientNodeImpl() {
    }

    public ClientNodeImpl(Registry registry, String identifier) {
        this.registry = registry;
        state = ClientState.NOT_STARTED;
        clientRmiIdentifier = identifier;
        doneLatch = new CountDownLatch(1);
        finishedLatch = new CountDownLatch(1);
        this.bestSolutions = Collections.synchronizedList(new ArrayList<>(Properties.NUM_PARALLEL_CLIENTS));
    }

    private static class OutputVariable {
        public RuntimeVariable variable;
        public Object value;

        public OutputVariable(RuntimeVariable variable, Object value) {
            super();
            this.variable = variable;
            this.value = value;
        }
    }

    @Override
    public void startNewSearch() throws RemoteException, IllegalStateException {
        if (!state.equals(ClientState.NOT_STARTED)) {
            throw new IllegalStateException("Search has already been started");
        }

        /*
         * Needs to be done on separated thread, otherwise the master will block on this
         * function call until end of the search, even if it is on remote process
         */
        searchExecutor.submit(() -> {
            changeState(ClientState.STARTED);

            //Before starting search, let's activate the sandbox
            if (Properties.SANDBOX) {
                Sandbox.initializeSecurityManagerForSUT();
            }
            List<TestGenerationResult> results = new ArrayList<>();

            try {
                // Starting a new search
                TestSuiteGenerator generator = new TestSuiteGenerator();
                results.add(generator.generateTestSuite());
                // TODO: Why?
                // GeneticAlgorithm<?> ga = generator.getEmployedGeneticAlgorithm();

                masterNode.evosuite_collectTestGenerationResult(clientRmiIdentifier, results);
            } catch (Throwable t) {
                logger.error("Error when generating tests for: "
                        + Properties.TARGET_CLASS + " with seed "
                        + Randomness.getSeed() + ". Configuration id : "
                        + Properties.CONFIGURATION_ID, t);
                results.add(TestGenerationResultBuilder.buildErrorResult("Error when generating tests for: "
                        + Properties.TARGET_CLASS + ": " + t));
            }

            changeState(ClientState.DONE);

            if (Properties.SANDBOX) {
                /*
                 * Note: this is mainly done for debugging purposes, to simplify how test cases are run/written
                 */
                Sandbox.resetDefaultSecurityManager();
            }

            /*
             * System is special due to the handling of properties
             *
             *  TODO: re-add it once we save JUnit code in the
             *  best individual. Otherwise, we wouldn't
             *  be able to properly create the JUnit files in the
             *  system test cases after the search
             */
            //org.evosuite.runtime.System.fullReset();
        });
    }

    @Override
    public void cancelCurrentSearch() throws RemoteException {
        if (this.state == ClientState.INITIALIZATION) {
            System.exit(1);
        }
        RMIStoppingCondition.getInstance().stop();
    }

    @Override
    public boolean waitUntilFinished(long timeoutInMs) throws RemoteException, InterruptedException {
        return finishedLatch.await(timeoutInMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void waitUntilDone() {
        try {
            doneLatch.await();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void emigrate(Set<T> immigrants) {
        try {
            logger.debug(ClientProcess.getPrettyPrintIdentifier() + "Sending " + immigrants.size() + " immigrants");
            masterNode.evosuite_migrate(clientRmiIdentifier, immigrants);
        } catch (RemoteException e) {
            logger.error(ClientProcess.getPrettyPrintIdentifier() + "Cannot send immigrating individuals to master", e);
        }
    }

    @Override
    public void sendBestSolution(Set<T> solutions) {
        try {
            logger.debug(ClientProcess.getPrettyPrintIdentifier() + "sending best solutions to " + ClientProcess.DEFAULT_CLIENT_NAME);
            masterNode.evosuite_collectBestSolutions(clientRmiIdentifier, solutions);
        } catch (RemoteException e) {
            logger.error(ClientProcess.getPrettyPrintIdentifier() + "Cannot send best solution to master", e);
        }
    }

    @Override
    public void changeState(ClientState state) {
        changeState(state, new ClientStateInformation(state));
    }

    @Override
    public void changeState(ClientState state, ClientStateInformation information) {
        if (this.state != state) {
            logger.info(ClientProcess.getPrettyPrintIdentifier() + "Client changing state from " + this.state + " to " + state);
        }

        this.state = state;

        TimeController.getInstance().updateState(state);

        try {
            masterNode.evosuite_informChangeOfStateInClient(clientRmiIdentifier, state, information);
        } catch (RemoteException e) {
            logger.error("Cannot inform master of change of state", e);
        }

        if (this.state.equals(ClientState.DONE)) {
            doneLatch.countDown();
        }

        if (this.state.equals(ClientState.FINISHED)) {
            finishedLatch.countDown();
        }
    }

    @Override
    public void updateStatistics(T individual) {
        logger.info("Sending current best individual to master process");

        try {
            masterNode.evosuite_collectStatistics(clientRmiIdentifier, individual);
        } catch (RemoteException e) {
            logger.error("Cannot inform master of change of state", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void flushStatisticsForClassChange() {
        logger.info("Flushing output variables to master process");

        try {
            masterNode.evosuite_flushStatisticsForClassChange(clientRmiIdentifier);
        } catch (RemoteException e) {
            logger.error("Cannot inform master of change of state", e);
        }
    }

    @Override
    public void updateProperty(String propertyName, Object value) {
        logger.info("Updating property '" + propertyName + "' with value '" + value + "' on master process");

        try {
            masterNode.evosuite_updateProperty(clientRmiIdentifier, propertyName, value);
        } catch (RemoteException | IllegalArgumentException | IllegalAccessException | NoSuchParameterException e) {
            logger.error("Cannot inform master of change of state", e);
        }
    }

    @Override
    public void trackOutputVariable(RuntimeVariable variable, Object value) {
        logger.info("Sending output variable to master process: " + variable + " = " + value);

        /*
         * As this code might be called from unsafe blocks, we just put the values
         * on a queue, and have a privileged thread doing the RMI connection to master
         */
        outputVariableQueue.offer(new OutputVariable(variable, value));
    }

    @Override
    public void publishPermissionStatistics() {

        trackOutputVariable(RuntimeVariable.AllPermission,
                PermissionStatistics.getInstance().getNumAllPermission());
        trackOutputVariable(RuntimeVariable.SecurityPermission,
                PermissionStatistics.getInstance().getNumSecurityPermission());
        trackOutputVariable(RuntimeVariable.UnresolvedPermission,
                PermissionStatistics.getInstance().getNumUnresolvedPermission());
        trackOutputVariable(RuntimeVariable.AWTPermission,
                PermissionStatistics.getInstance().getNumAWTPermission());
        trackOutputVariable(RuntimeVariable.FilePermission,
                PermissionStatistics.getInstance().getNumFilePermission());
        trackOutputVariable(RuntimeVariable.SerializablePermission,
                PermissionStatistics.getInstance().getNumSerializablePermission());
        trackOutputVariable(RuntimeVariable.ReflectPermission,
                PermissionStatistics.getInstance().getNumReflectPermission());
        trackOutputVariable(RuntimeVariable.RuntimePermission,
                PermissionStatistics.getInstance().getNumRuntimePermission());
        trackOutputVariable(RuntimeVariable.NetPermission,
                PermissionStatistics.getInstance().getNumNetPermission());
        trackOutputVariable(RuntimeVariable.SocketPermission,
                PermissionStatistics.getInstance().getNumSocketPermission());
        trackOutputVariable(RuntimeVariable.SQLPermission,
                PermissionStatistics.getInstance().getNumSQLPermission());
        trackOutputVariable(RuntimeVariable.PropertyPermission,
                PermissionStatistics.getInstance().getNumPropertyPermission());
        trackOutputVariable(RuntimeVariable.LoggingPermission,
                PermissionStatistics.getInstance().getNumLoggingPermission());
        trackOutputVariable(RuntimeVariable.SSLPermission,
                PermissionStatistics.getInstance().getNumSSLPermission());
        trackOutputVariable(RuntimeVariable.AuthPermission,
                PermissionStatistics.getInstance().getNumAuthPermission());
        trackOutputVariable(RuntimeVariable.AudioPermission,
                PermissionStatistics.getInstance().getNumAudioPermission());
        trackOutputVariable(RuntimeVariable.OtherPermission,
                PermissionStatistics.getInstance().getNumOtherPermission());
        trackOutputVariable(RuntimeVariable.Threads,
                PermissionStatistics.getInstance().getMaxThreads());

    }

    public void stop() {
        if (statisticsThread != null) {
            statisticsThread.interrupt();
            List<OutputVariable> vars = new ArrayList<>();
            outputVariableQueue.drainTo(vars);
            for (OutputVariable ov : vars) {
                try {
                    masterNode.evosuite_collectStatistics(clientRmiIdentifier, ov.variable, ov.value);
                } catch (RemoteException e) {
                    logger.error("Error when exporting statistics: " + ov.variable + "=" + ov.value, e);
                    break;
                }
            }

            try {
                statisticsThread.join(3000);
            } catch (InterruptedException e) {
                logger.error("Failed to stop statisticsThread in time");
            }
            statisticsThread = null;
        }

        changeState(ClientState.FINISHED);
    }

    @Override
    public boolean init() {
        try {
            masterNode = (MasterNodeRemote) registry.lookup(MasterNodeRemote.RMI_SERVICE_NAME);
            masterNode.evosuite_registerClientNode(clientRmiIdentifier);
            masterNode.evosuite_informChangeOfStateInClient(clientRmiIdentifier, state,
                    new ClientStateInformation(state));

            statisticsThread = new Thread() {
                @Override
                public void run() {
                    while (!this.isInterrupted()) {
                        OutputVariable ov = null;
                        try {
                            ov = outputVariableQueue.take(); //this is blocking
                            masterNode.evosuite_collectStatistics(clientRmiIdentifier, ov.variable, ov.value);
                        } catch (InterruptedException e) {
                            break;
                        } catch (RemoteException e) {
                            logger.error("Error when exporting statistics: " + ov.variable + "=" + ov.value, e);
                            break;
                        }
                    }
                }
            };
            statisticsThread.setName("Statistics sender in client process");
            Sandbox.addPrivilegedThread(statisticsThread);
            statisticsThread.start();

        } catch (Exception e) {
            logger.error("Error when connecting to master via RMI", e);
            return false;
        }
        return true;
    }

    public String getClientRmiIdentifier() {
        return clientRmiIdentifier;
    }

    @Override
    public void doCoverageAnalysis() throws RemoteException {
        if (!state.equals(ClientState.NOT_STARTED)) {
            throw new IllegalArgumentException("Search has already been started");
        }

        /*
         * Needs to be done on separated thread, otherwise the master will block on this
         * function call until end of the search, even if it is on remote process
         */
        searchExecutor.submit(new Runnable() {
            @Override
            public void run() {
                changeState(ClientState.STARTED);
                //Before starting search, let's activate the sandbox
                if (Properties.SANDBOX) {
                    Sandbox.initializeSecurityManagerForSUT();
                }

                try {
                    // We have to deactivate the archive when measuring coverage
                    // otherwise it would complain about unknown goals
                    boolean archive = Properties.TEST_ARCHIVE;
                    Properties.TEST_ARCHIVE = false;
                    CoverageAnalysis.analyzeCoverage();
                    Properties.TEST_ARCHIVE = archive;

                } catch (Throwable t) {
                    logger.error("Error when analysing coverage for: "
                            + Properties.TARGET_CLASS + " with seed "
                            + Randomness.getSeed() + ". Configuration id : "
                            + Properties.CONFIGURATION_ID, t);
                }

                changeState(ClientState.DONE);
                if (Properties.SANDBOX) {
                    /*
                     * Note: this is mainly done for debugging purposes, to simplify how test cases are run/written
                     */
                    Sandbox.resetDefaultSecurityManager();
                }

            }
        });
    }

    @Override
    public void doDependencyAnalysis(final String fileName) throws RemoteException {
        if (!state.equals(ClientState.NOT_STARTED)) {
            throw new IllegalArgumentException("Search has already been started");
        }

        /*
         * Needs to be done on separated thread, otherwise the master will block on this
         * function call until end of the search, even if it is on remote process
         */
        searchExecutor.submit(() -> {
            changeState(ClientState.STARTED);
            Sandbox.goingToExecuteSUTCode();
            TestGenerationContext.getInstance().goingToExecuteSUTCode();
            Sandbox.goingToExecuteUnsafeCodeOnSameThread();

            try {
                LoggingUtils.getEvoLogger().info("* Analyzing classpath (dependency analysis)");
                DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS,
                        Arrays.asList(ClassPathHandler.getInstance().getClassPathElementsForTargetProject()));
                StringBuffer fileNames = new StringBuffer();
                for (Class<?> clazz : TestCluster.getInstance().getAnalyzedClasses()) {
                    fileNames.append(clazz.getName());
                    fileNames.append("\n");
                }
                LoggingUtils.getEvoLogger().info("* Writing class dependencies to file " + fileName);
                FileIOUtils.writeFile(fileNames.toString(), fileName);
            } catch (Throwable t) {
                logger.error("Error when analysing coverage for: "
                        + Properties.TARGET_CLASS + " with seed "
                        + Randomness.getSeed() + ". Configuration id : "
                        + Properties.CONFIGURATION_ID, t);
            } finally {
                Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
                Sandbox.doneWithExecutingSUTCode();
                TestGenerationContext.getInstance().doneWithExecutingSUTCode();
            }

            changeState(ClientState.DONE);
        });
    }

    /* (non-Javadoc)
     * @see org.evosuite.rmi.service.ClientNodeRemote#printClassStatistics()
     */
    @Override
    public void printClassStatistics() throws RemoteException {
        if (!state.equals(ClientState.NOT_STARTED)) {
            throw new IllegalArgumentException("Search has already been started");
        }

        /*
         * Needs to be done on separated thread, otherwise the master will block on this
         * function call until end of the search, even if it is on remote process
         */
        searchExecutor.submit(new Runnable() {
            @Override
            public void run() {
                changeState(ClientState.STARTED);
                if (Properties.SANDBOX) {
                    Sandbox.initializeSecurityManagerForSUT();
                }

                try {
                    ClassStatisticsPrinter.printClassStatistics();

                } catch (Throwable t) {
                    logger.error("Error when analysing coverage for: "
                            + Properties.TARGET_CLASS + " with seed "
                            + Randomness.getSeed() + ". Configuration id : "
                            + Properties.CONFIGURATION_ID, t);
                }

                changeState(ClientState.DONE);
            }
        });
    }

    @Override
    public void immigrate(Set<T> migrants) throws RemoteException {
        logger.debug(ClientProcess.getPrettyPrintIdentifier() + "receiving "
                + (migrants != null ? migrants.size() : 0) + " immigrants");
        fireEvent(migrants);
    }

    @Override
    public void collectBestSolutions(Set<T> solutions) throws RemoteException {
        logger.debug(ClientProcess.getPrettyPrintIdentifier() + "added solution to set");
        bestSolutions.add(solutions);
    }

    @Override
    public void addListener(Listener<Set<T>> listener) {
        listeners.add(listener);
    }

    @Override
    public void deleteListener(Listener<Set<T>> listener) {
        listeners.remove(listener);
    }

    /**
     * Fires event to all registered listeners.
     *
     * @param event the event to fire
     */
    private void fireEvent(Set<T> event) {
        for (Listener<Set<T>> listener : listeners) {
            listener.receiveEvent(event);
        }
    }

    /**
     * Returns collected solutions of all clients other than client 0. This method blocks until all solutions are
     * collected.
     *
     * @return the list of collected best solutions or null if there is a timeout
     */
    public Set<Set<T>> getBestSolutions() {
        do {
            if (bestSolutions.size() == (Properties.NUM_PARALLEL_CLIENTS - 1)) {
                return new HashSet<>(bestSolutions);
            }
        } while (finishedLatch.getCount() != 0);

        return null;
    }
}
