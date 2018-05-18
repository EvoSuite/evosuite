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
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ExternalProcessGroupHandler {

    /**
     * Constant <code>logger</code>
     */
    protected static final Logger logger = LoggerFactory.getLogger(ExternalProcessHandler.class);
    /**
     * Constant <code>WAITING_FOR_DATA</code>
     */
    protected static final Object WAITING_FOR_DATA = "waiting_for_data_" + System.currentTimeMillis();

    protected Process[] processGroup;
    protected String[][] last_commands;

    protected Thread[] output_printers;
    protected Thread[] error_printers;
    protected Thread[] message_handlers;

    protected ObjectInputStream in;

    protected Object final_result;

    protected Thread[] processKillHooks;
    protected Thread clientRunningOnThread;

    protected volatile CountDownLatch[] latches;

    protected String base_dir = System.getProperty("user.dir");

    private String[] hsErrFiles;

    /**
     * <p>
     * Constructor for ExternalProcessHandler.
     * </p>
     */
    public ExternalProcessGroupHandler(int nrOfProcesses) {
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
     * @param ms amount of time to wait
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
     * @param base_dir
     *         the base directory
     */
    public void setBaseDir(String base_dir) {
        this.base_dir = base_dir;
    }

    /**
     * Starts a process for each command array of the given list. If one process fails to start, all already started
     * processes are killed.
     *
     * @param commands
     *         a list of arrays of commands to start
     * @return true iff all processes have started correctly
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

    protected boolean didClientJVMCrash(int processIndex) {
        return new File(hsErrFiles[processIndex]).exists();
    }

    protected String getAndDeleteHsErrFile(int processIndex) {
        if (!didClientJVMCrash(processIndex)) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        File file = new File(hsErrFiles[processIndex]);
        file.deleteOnExit();

        try (Scanner in = new Scanner(file);) {
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
     * @param command
     *         an array of {@link java.lang.String} objects.
     * @param population_data
     *         a {@link java.lang.Object} object.
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
     * Terminates the process with given index.
     *
     * @param processIndex
     *         index of process to kill
     */
    public void killProcess(int processIndex) {
        if (processGroup[processIndex] == null) {
            return;
        }

        try {
            Runtime.getRuntime().removeShutdownHook(processKillHooks[processIndex]);
        } catch (Exception ignored) { /* do nothing. this can happen if shutdown is in progress */
        }

        if (processGroup[processIndex] != null) {
            try {
                //be sure streamers are closed, otherwise process might hang on Windows
                processGroup[processIndex].getOutputStream().close();
                processGroup[processIndex].getInputStream().close();
                processGroup[processIndex].getErrorStream().close();
            } catch (Exception t) {
                logger.error("Failed to close process stream: " + t.toString());
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
     * @param processIndex
     *         index of process
     */
    protected void startExternalProcessPrinter(int processIndex) {

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

        if (Properties.SHOW_PROGRESS && (Properties.LOG_LEVEL == null
                || (!Properties.LOG_LEVEL.equals("info") && !Properties.LOG_LEVEL.equals("debug")
                && !Properties.LOG_LEVEL.equals("trace")))) {
            ConsoleProgressBar.startProgressBar();
        }
    }

    /**
     * <p>
     * startExternalProcessMessageHandler
     * </p>
     *
     * @param processIndex
     *         index of process
     */
    protected void startExternalProcessMessageHandler(int processIndex) {
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

                    switch (message) {
                        case Messages.FINISHED_COMPUTATION:
                            LoggingUtils.getEvoLogger().info("* Computation finished");
                            read = false;
                            killProcess(processIndex);
                            final_result = data;
                            latches[processIndex].countDown();
                            break;
                        case Messages.NEED_RESTART:
                            //now data represent the current generation
                            LoggingUtils.getEvoLogger().info("* Restarting client process");
                            killProcess(processIndex);
                            /*
                             * TODO: this will need to be changed, to take into account
                             * a possible reduced budget
                             */
                            startProcess(last_commands[processIndex], processIndex, data);
                            break;
                        default:
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
     * @param processIndex
     *         index of process
     */
    protected void startSignalHandler(int processIndex) {
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
     * @param timeout
     *         a int.
     * @return a {@link java.lang.Object} object.
     */
    public TestGenerationResult waitForResult(int timeout) {

        try {
            long start = System.currentTimeMillis();
            Set<ClientNodeRemote> clients = MasterServices.getInstance().getMasterNode().getClientsOnceAllConnected(timeout);
            
            if (clients == null) {
                logger.error("Could not access client process");
                return TestGenerationResultBuilder.buildErrorResult("Could not access client process");
            }

            for (ClientNodeRemote client : clients) {
                long passed = System.currentTimeMillis() - start;
                long remaining = timeout - passed;
                if (remaining <= 0) {
                    remaining = 1;
                }
                boolean finished = client.waitUntilFinished(remaining);

                if (!finished) {
                    /*
                     * TODO what to do here? Try to stop the the client through RMI?
                     * Or check in which state it is, and based on that decide if giving more time?
                     */
                    logger.error("Class " + Properties.TARGET_CLASS + ". Clients have not finished yet, although a timeout occurred.\n" + MasterServices.getInstance().getMasterNode().getSummaryOfClientStatuses());
                }
            }
        } catch (InterruptedException ignored) {
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

        return null;
    }
}
