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

import org.evosuite.ClientProcess;
import org.evosuite.ConsoleProgressBar;
import org.evosuite.Properties;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientNodeRemote;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.runtime.sandbox.Sandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.*;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

/*
 * this code should be used by the main master process.
 *
 * FIXME: once RMI is stable tested, we ll need to remove all the TCP stuff, and refactor
 */

public class ExternalProcessGroupHandler {
    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(ExternalProcessGroupHandler.class);

    protected Process[] processGroup;
    protected String[][] last_commands;

    protected Thread[] output_printers;
    protected Thread[] error_printers;
    protected Thread[] message_handlers;

    protected ObjectInputStream in;

    protected Object final_result;
    /**
     * Constant <code>WAITING_FOR_DATA</code>
     */
    protected static final Object WAITING_FOR_DATA = "waiting_for_data_"
            + System.currentTimeMillis();

    protected Thread[] processKillHooks;
    protected Thread clientRunningOnThread;

    protected volatile CountDownLatch[] latches;

    protected String base_dir = System.getProperty("user.dir");

    private final String[] hsErrFiles;

    public ExternalProcessGroupHandler() {
        this(1);
    }

    /**
     * <p>
     * Constructor for ExternalProcessGroupHandler.
     * </p>
     */
    public ExternalProcessGroupHandler(final int nrOfProcesses) {
        this.processGroup = new Process[nrOfProcesses];
        this.last_commands = new String[nrOfProcesses][];

        this.output_printers = new Thread[nrOfProcesses];
        this.error_printers = new Thread[nrOfProcesses];
        this.message_handlers = new Thread[nrOfProcesses];

        this.processKillHooks = new Thread[nrOfProcesses];
        this.latches = new CountDownLatch[nrOfProcesses];
        this.hsErrFiles = new String[nrOfProcesses];
    }

    /**
     * Only for debug reasons.
     *
     * @param ms
     */
    public void stopAndWaitForClientOnThread(long ms) {

        if (clientRunningOnThread != null && clientRunningOnThread.isAlive()) {
            clientRunningOnThread.interrupt();
        }

        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) < ms) { //to avoid miss it in case of interrupt
            if (clientRunningOnThread != null && clientRunningOnThread.isAlive()) {
                try {
                    clientRunningOnThread.join(ms - (System.currentTimeMillis() - start));
                    break;
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        }

        if (clientRunningOnThread != null && clientRunningOnThread.isAlive()) {
            throw new AssertionError("clientRunningOnThread is alive even after waiting " + ms + "ms");
        }
    }


    /**
     * Sets the base directory.
     *
     * @param base_dir the base directory
     */
    public void setBaseDir(String base_dir) {
        this.base_dir = base_dir;
    }

    /**
     * <p>
     * startProcess
     * </p>
     *
     * @param commands an array of {@link java.lang.String} objects.
     * @return a boolean.
     */
    public boolean startProcess(String[] commands) {
        List<String[]> l_commands = new ArrayList<>();
        l_commands.add(commands);
        return this.startProcessGroup(l_commands);
    }

    /**
     * Starts a process for each command array of the given list. If one process fails to start, all already started
     * processes are killed.
     *
     * @param commands a list of arrays of commands to start
     * @return true iff all processes have started correctly, false otherwise
     */
    public boolean startProcessGroup(List<String[]> commands) {
        int rollbackToI = 0;

        for (int i = 0; i < commands.size(); i++) {
            String[] command = commands.get(i);

            if (!Properties.IS_RUNNING_A_SYSTEM_TEST) {
                logger.debug("Going to start process with command: " + Arrays.toString(command).replace(",", " "));
            }

            List<String> formatted = new LinkedList<>();
            for (String s : command) {
                String token = s.trim();
                if (!token.isEmpty()) {
                    formatted.add(token);
                }
            }

            hsErrFiles[i] = "hs_err_EvoSuite_client_p" + getServerPort() + "_t" + System.currentTimeMillis();
            String option = "-XX:ErrorFile=" + hsErrFiles[i];
            formatted.add(1, option); // add it after the first "java" command

            if (!startProcess(formatted.toArray(new String[0]), i, null)) {
                rollbackToI = i;
                break;
            }
        }

        if (rollbackToI > 0) {
            for (int i = 0; i < rollbackToI; i++) {
                killProcess(i);
            }
        }

        return rollbackToI == 0;
    }

    protected boolean didClientJVMCrash(final int processIndex) {
        return new File(hsErrFiles[processIndex]).exists();
    }

    protected String getAndDeleteHsErrFile(final int processIndex) {
        if (!didClientJVMCrash(processIndex)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        File file = new File(hsErrFiles[processIndex]);
        file.deleteOnExit();

        try (Scanner in = new Scanner(file)) {
            while (in.hasNextLine()) {
                String row = in.nextLine();
                //do not read the full file, just the header
                if (row.startsWith("#")) {
                    builder.append(row).append("\n");
                } else {
                    break; //end of the header
                }
            }
        } catch (FileNotFoundException e) {
            //shouldn't really happen
            logger.error("Error while reading " + file.getAbsolutePath() + ": " + e.getMessage());
            return null;
        }

        return builder.toString();
    }

    public String getProcessStates() {
        if (processGroup == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < processGroup.length; i++) {
            builder.append("process nr. ");
            builder.append(i);
            builder.append(": ");

            if (processGroup[i] == null) {
                builder.append("null\n");
            } else {
                try {
                    int exitValue = processGroup[i].exitValue();
                    builder.append("Terminated with exit status ");
                    builder.append(exitValue);
                    builder.append("\n");
                } catch (IllegalThreadStateException e) {
                    builder.append("Still running\n");
                }
            }
        }
        return builder.toString();
    }

    /**
     * <p>
     * startProcess
     * </p>
     *
     * @param command         an array of {@link java.lang.String} objects.
     * @param population_data a {@link java.lang.Object} object.
     * @return a boolean.
     */
    protected boolean startProcess(String[] command, int processIndex, Object population_data) {
        if (processGroup[processIndex] != null) {
            logger.warn("Already running an external process");
            return false;
        }

        latches[processIndex] = new CountDownLatch(1);
        final_result = WAITING_FOR_DATA;


        //the following thread is important to make sure that the external process is killed
        //when current process ends

        processKillHooks[processIndex] = new Thread() {
            @Override
            public void run() {
                killProcess(processIndex);
                closeServer();
            }
        };

        Runtime.getRuntime().addShutdownHook(processKillHooks[processIndex]);
        // now start the process

        if (!Properties.CLIENT_ON_THREAD) {
            File dir = new File(base_dir);
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(dir);
            builder.redirectErrorStream(false);

            try {
                processGroup[processIndex] = builder.start();
            } catch (IOException e) {
                logger.error("Failed to start external process", e);
                return false;
            }

            startExternalProcessPrinter(processIndex);
        } else {
            /*
             * Here we run client on a thread instead of process.
             * NOTE: this should only be done for debugging, ie in
             * JUnit files created for testing EvoSuite.
             */
            clientRunningOnThread = new Thread() {
                @Override
                public void run() {
                    /*
                     * NOTE: the handling of the parameters "-D" should be handled
                     * directly in JUnit by setting the different values in Properties
                     */
                    ClientProcess.main(new String[0]);
                }
            };
            clientRunningOnThread.setName("client");
            clientRunningOnThread.start();
            Sandbox.addPrivilegedThread(clientRunningOnThread);
        }

        startSignalHandler(processIndex);
        last_commands[processIndex] = command;

        return true;
    }

    /**
     * <p>
     * killProcess
     * </p>
     */
    public void killProcess() {
        this.killProcess(0);
    }

    /**
     * Terminates the process with given index.
     *
     * @param processIndex index of process to kill
     */
    public void killProcess(final int processIndex) {
        if (processGroup[processIndex] == null) {
            return;
        }

        try {
            Runtime.getRuntime().removeShutdownHook(processKillHooks[processIndex]);
        } catch (Exception e) { /* do nothing. this can happen if shutdown is in progress */
        }


        /*
         * TODO: use RMI to 'gracefully' stop the client
         */

        if (processGroup[processIndex] != null) {
            try {
                //be sure streamers are closed, otherwise process might hang on Windows
                processGroup[processIndex].getOutputStream().close();
                processGroup[processIndex].getInputStream().close();
                processGroup[processIndex].getErrorStream().close();
            } catch (Exception t) {
                logger.error("Failed to close process stream: " + t);
            }
            processGroup[processIndex].destroy();
        }
        processGroup[processIndex] = null;

        if (clientRunningOnThread != null && clientRunningOnThread.isAlive()) {
            clientRunningOnThread.interrupt();
        }
        clientRunningOnThread = null;

        if (output_printers[processIndex] != null && output_printers[processIndex].isAlive())
            output_printers[processIndex].interrupt();
        output_printers[processIndex] = null;

        if (error_printers[processIndex] != null && error_printers[processIndex].isAlive())
            error_printers[processIndex].interrupt();
        error_printers[processIndex] = null;

        if (message_handlers[processIndex] != null && message_handlers[processIndex].isAlive()) {
            message_handlers[processIndex].interrupt();
        }
        message_handlers[processIndex] = null;
    }

    /**
     * Terminates all running processes.
     */
    public void killAllProcesses() {
        for (int i = 0; i < processGroup.length; i++) {
            killProcess(i);
        }
    }

    /**
     * <p>
     * getServerPort
     * </p>
     *
     * @return a int.
     */
    public int getServerPort() {
        return MasterServices.getInstance().getRegistryPort();
    }

    /**
     * <p>
     * openServer
     * </p>
     *
     * @return a int.
     */
    public int openServer() {
        boolean started = MasterServices.getInstance().startRegistry();
        if (!started) {
            logger.error("Not possible to start RMI registry");
            return -1;
        }

        try {
            MasterServices.getInstance().registerServices();
        } catch (RemoteException e) {
            logger.error("Failed to start RMI services", e);
            return -1;
        }

        return MasterServices.getInstance().getRegistryPort();

		/*
		if (server == null) {
			try {
				server = new ServerSocket();
				server.setSoTimeout(10000);
				server.bind(null);
				return server.getLocalPort();
			} catch (Exception e) {
				logger.error("Not possible to start TCP server", e);
			}
		}
		return -1;
		 */
    }

    /**
     * <p>
     * closeServer
     * </p>
     */
    public void closeServer() {
        MasterServices.getInstance().stopServices();
    }

    /**
     * <p>
     * startExternalProcessPrinter
     * </p>
     *
     * @param processIndex index of process
     */
    protected void startExternalProcessPrinter(final int processIndex) {

        if (output_printers[processIndex] == null || !output_printers[processIndex].isAlive()) {
            output_printers[processIndex] = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader proc_in = new BufferedReader(
                                new InputStreamReader(processGroup[processIndex].getInputStream()));

                        int data = 0;
                        while (data != -1 && !isInterrupted()) {
                            data = proc_in.read();
                            if (data != -1 && Properties.PRINT_TO_SYSTEM) {
                                System.out.print((char) data);
                            }
                        }

                    } catch (Exception e) {
                        if (MasterServices.getInstance().getMasterNode() == null)
                            return;

                        boolean finished = true;
                        for (ClientState state : MasterServices.getInstance().getMasterNode().getCurrentState()) {
                            if (state != ClientState.DONE) {
                                finished = false;
                                break;
                            }
                        }
                        if (!finished)
                            logger.error("Exception while reading output of client process. "
                                    + e.getMessage());
                        else
                            logger.debug("Exception while reading output of client process. "
                                    + e.getMessage());
                    }
                }
            };

            output_printers[processIndex].start();
        }

        if (error_printers[processIndex] == null || !error_printers[processIndex].isAlive()) {
            error_printers[processIndex] = new Thread() {
                @Override
                public void run() {
                    try {
                        BufferedReader proc_in = new BufferedReader(
                                new InputStreamReader(processGroup[processIndex].getErrorStream()));

                        int data = 0;
                        String errorLine = "";
                        while (data != -1 && !isInterrupted()) {
                            data = proc_in.read();
                            if (data != -1 && Properties.PRINT_TO_SYSTEM) {
                                System.err.print((char) data);

                                errorLine += (char) data;
                                if ((char) data == '\n') {
                                    logger.error(errorLine);
                                    errorLine = "";
                                }
                            }
                        }

                    } catch (Exception e) {
                        if (MasterServices.getInstance().getMasterNode() == null)
                            return;

                        boolean finished = true;
                        for (ClientState state : MasterServices.getInstance().getMasterNode().getCurrentState()) {
                            if (state != ClientState.DONE) {
                                finished = false;
                                break;
                            }
                        }
                        if (!finished)
                            logger.error("Exception while reading output of client process. "
                                    + e.getMessage());
                        else
                            logger.debug("Exception while reading output of client process. "
                                    + e.getMessage());
                    }
                }
            };

            error_printers[processIndex].start();
        }

        if (Properties.SHOW_PROGRESS &&
                (Properties.LOG_LEVEL == null ||
                        (!Properties.LOG_LEVEL.equals("info")
                                && !Properties.LOG_LEVEL.equals("debug")
                                && !Properties.LOG_LEVEL.equals("trace"))
                )
        ) {
            ConsoleProgressBar.startProgressBar();
        }

    }

    /**
     * <p>
     * startExternalProcessMessageHandler
     * </p>
     *
     * @param processIndex index of process
     */
    protected void startExternalProcessMessageHandler(final int processIndex) {
        if (message_handlers[processIndex] != null && message_handlers[processIndex].isAlive())
            return;

        message_handlers[processIndex] = new Thread() {
            @Override
            public void run() {
                boolean read = true;
                while (read && !isInterrupted()) {
                    String message = null;
                    Object data = null;

                    try {
                        message = (String) in.readObject();
                        data = in.readObject();
                        logger.debug("Received msg: " + message);
                        logger.debug("Received data: " + data);
                    } catch (Exception e) {
                        /*
                         * TODO: this parts need to be improved.
                         * An exception here is most likely due to the client crashing.
                         * If there is still enough budget (this might not be trivial to check,
                         * eg it could be fine for time, but not number of fitness evaluations), then
                         * we should try to re-start based on the partial info received so far, eg
                         * the best solutions found so far which was sent to master
                         */
                        logger.error("Class "
                                + Properties.TARGET_CLASS
                                + ". Error when reading message. Likely the client has crashed. Error message: "
                                + e.getMessage());
                        message = Messages.FINISHED_COMPUTATION;
                        data = null;
                    }

                    if (message.equals(Messages.FINISHED_COMPUTATION)) {
                        LoggingUtils.getEvoLogger().info("* Computation finished");
                        read = false;
                        killProcess(processIndex);
                        final_result = data;
                        latches[processIndex].countDown();
                    } else if (message.equals(Messages.NEED_RESTART)) {
                        //now data represent the current generation
                        LoggingUtils.getEvoLogger().info("* Restarting client process");
                        killProcess(processIndex);
                        /*
                         * TODO: this will need to be changed, to take into account
                         * a possible reduced budget
                         */
                        startProcess(last_commands[processIndex], processIndex, data);
                    } else {
                        killProcess(processIndex);
                        logger.error("Class " + Properties.TARGET_CLASS
                                + ". Error, received invalid message: ", message);
                        return;
                    }
                }
            }
        };
        message_handlers[processIndex].start();
    }

    /**
     * Starts the signal handler for process with given index.
     *
     * @param processIndex index of process
     */
    protected void startSignalHandler(final int processIndex) {
        Signal.handle(new Signal("INT"), new SignalHandler() {

            private boolean interrupted = false;

            @Override
            public void handle(Signal arg0) {
                if (interrupted)
                    System.exit(0);
                try {
                    interrupted = true;
                    if (processGroup[processIndex] != null)
                        processGroup[processIndex].waitFor();
                } catch (InterruptedException e) {
                    logger.warn("", e);
                }
            }

        });
    }

    /**
     * <p>
     * waitForResult
     * </p>
     *
     * @param timeout a int.
     * @return a {@link java.lang.Object} object.
     */
    public TestGenerationResult waitForResult(int timeout) {
        try {
            long start = System.currentTimeMillis();
            Map<String, ClientNodeRemote> clients = MasterServices.getInstance()
                    .getMasterNode().getClientsOnceAllConnected(timeout);
            if (clients == null) {
                logger.error("Could not access client process");
                return TestGenerationResultBuilder.buildErrorResult("Could not access client process");
            }

            for (Entry<String, ClientNodeRemote> entry : clients.entrySet()) {
                long passed = System.currentTimeMillis() - start;
                long remaining = timeout - passed;
                if (remaining <= 0) {
                    remaining = 1;
                }
                boolean finished = false;
                ClientState clientState = MasterServices.getInstance().getMasterNode().getCurrentState(entry.getKey());
                if (clientState == null || !clientState.equals(ClientState.FINISHED)) {
                    try {
                        finished = entry.getValue().waitUntilFinished(remaining);
                    } catch (ConnectException e) {
                        logger.warn("Failed to connect to client. Client with id " + entry.getKey()
                                + " is already finished.");
                        finished = true;
                    }
                } else {
                    finished = true;
                }

                if (!finished) {
                    /*
                     * TODO what to do here? Try to stop the client through RMI?
                     * Or check in which state it is, and based on that decide if giving more time?
                     */
                    logger.error("Class " + Properties.TARGET_CLASS + ". Clients have not finished yet, although a timeout occurred.\n" + MasterServices.getInstance().getMasterNode().getSummaryOfClientStatuses());
                }
            }
        } catch (InterruptedException e) {
        } catch (RemoteException e) {

            String msg = "Class " + Properties.TARGET_CLASS + ". Lost connection with clients.\n" + MasterServices.getInstance().getMasterNode().getSummaryOfClientStatuses();

            boolean crashOccurred = false;
            for (int i = 0; i < processGroup.length; i++) {
                if (didClientJVMCrash(i)) {
                    String err = getAndDeleteHsErrFile(i);
                    String clientMsg = "The JVM of the client process crashed:\n" + err;
                    logger.error(clientMsg);
                    crashOccurred = true;
                }
            }

            if (crashOccurred) {
                logger.error(msg, e);
            }
        }

        for (int i = 0; i < processGroup.length; i++) {
            killProcess(i);
        }
        LoggingUtils.getEvoLogger().info("* Computation finished");
        return null; //TODO refactoring
		/*
		try {
			latch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.warn("Class "
			                    + Properties.TARGET_CLASS
			                    + ". Thread interrupted while waiting for results from client process",
			            e);
		}

		return final_result;
		 */
    }

}
