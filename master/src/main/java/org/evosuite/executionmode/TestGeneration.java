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
package org.evosuite.executionmode;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.*;
import org.evosuite.Properties.Strategy;
import org.evosuite.classpath.ClassPathHacker;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.classpath.ResourceList;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientNodeRemote;
import org.evosuite.runtime.util.JarPathing;
import org.evosuite.runtime.util.JavaExecCmdUtil;
import org.evosuite.statistics.SearchStatistics;
import org.evosuite.utils.ExternalProcessGroupHandler;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class TestGeneration {

    private static final Logger logger = LoggerFactory.getLogger(TestGeneration.class);

    public static List<List<TestGenerationResult>> executeTestGeneration(Options options, List<String> javaOpts,
                                                                         CommandLine line) {

        Strategy strategy = getChosenStrategy(javaOpts, line);

        /** Updating properties strategy */
        Properties.STRATEGY = strategy;

        if (strategy == null) {
            strategy = Strategy.MOSUITE;
        }

        List<List<TestGenerationResult>> results = new ArrayList<>();

        if (line.getOptions().length == 0) {
            Help.execute(options);
            return results;
        }

        String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
        if (cp == null || cp.isEmpty()) {
            LoggingUtils.getEvoLogger().error("No classpath has been defined for the target project.\nOn the command line you can set it with the -projectCP option\n");
            Help.execute(options);
            return results;
        }


        if (line.hasOption("class")) {
            results.addAll(generateTests(strategy, line.getOptionValue("class"), javaOpts));
        } else if (line.hasOption("prefix")) {
            results.addAll(generateTestsPrefix(strategy, line.getOptionValue("prefix"), javaOpts));
        } else if (line.hasOption("target")) {
            String target = line.getOptionValue("target");
            results.addAll(generateTestsTarget(strategy, target, javaOpts));
        } else if (EvoSuite.hasLegacyTargets()) {
            results.addAll(generateTestsLegacy(strategy, javaOpts));
        } else {
            LoggingUtils.getEvoLogger().error(
                    "Please specify either target class ('-class' option), prefix ('-prefix' option), or " +
                            "classpath entry ('-target' option)\n");
            Help.execute(options);
        }
        return results;
    }


    private static List<List<TestGenerationResult>> generateTestsLegacy(Properties.Strategy strategy,
                                                                        List<String> args) {
        List<List<TestGenerationResult>> results = new ArrayList<>();

        ClassPathHandler.getInstance().getTargetProjectClasspath();
        LoggingUtils.getEvoLogger().info("* Using .task files in "
                + Properties.OUTPUT_DIR
                + " [deprecated]");
        File directory = new File(Properties.OUTPUT_DIR);
        String[] extensions = {"task"};
        for (File file : FileUtils.listFiles(directory, extensions, false)) {
            results.addAll(generateTests(strategy, file.getName().replace(".task", ""), args));
        }

        return results;
    }

    public static Option[] getOptions() {
        return new Option[]{
                new Option("generateSuite", "use whole suite generation."),
                new Option("generateTests", "use individual test generation (old approach for reference purposes)"),
                new Option("generateRandom", "use random test generation"),
                new Option("generateNumRandom", true, "generate fixed number of random tests"),
                new Option("generateMOSuite", "use many objective test generation (MOSA). This is the default behavior."),
                new Option("generateSuiteUsingDSE", "use Dynamic Symbolic Execution to generate test suite")
        };
    }

    private static Strategy getChosenStrategy(List<String> javaOpts, CommandLine line) {
        Strategy strategy = null;
        if (javaOpts.contains("-Dstrategy=" + Strategy.ENTBUG.name())
                && line.hasOption("generateTests")) {
            strategy = Strategy.ENTBUG;
            // TODO: Find a better way to integrate this
        } else if (javaOpts.contains("-Dstrategy=" + Strategy.NOVELTY.name())) {
            // TODO: Find a better way to integrate this
            strategy = Strategy.NOVELTY;
        } else if (javaOpts.contains("-Dstrategy=" + Strategy.MAP_ELITES.name())) {
            // TODO: Find a better way to integrate this
            strategy = Strategy.MAP_ELITES;
        } else if (line.hasOption("generateTests")) {
            strategy = Strategy.ONEBRANCH;
        } else if (line.hasOption("generateSuite")) {
            strategy = Strategy.EVOSUITE;
        } else if (line.hasOption("generateRandom")) {
            strategy = Strategy.RANDOM;
        } else if (line.hasOption("generateNumRandom")) {
            strategy = Strategy.RANDOM_FIXED;
            javaOpts.add("-Dnum_random_tests="
                    + line.getOptionValue("generateNumRandom"));
        } else if (line.hasOption("generateMOSuite")) {
            strategy = Strategy.MOSUITE;
        } else if (line.hasOption("generateSuiteUsingDSE")) {
            strategy = Strategy.DSE;
        }
        return strategy;
    }

    private static List<List<TestGenerationResult>> generateTestsPrefix(Properties.Strategy strategy, String prefix,
                                                                        List<String> args) {
        List<List<TestGenerationResult>> results = new ArrayList<>();

        String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
        Set<String> classes = new HashSet<>();

        for (String classPathElement : cp.split(File.pathSeparator)) {
            classes.addAll(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(classPathElement, prefix, false));
            try {
                ClassPathHacker.addFile(classPathElement);
            } catch (IOException e) {
                // Ignore?
            }
        }
        try {
            if (Properties.INSTRUMENT_CONTEXT || Properties.INHERITANCE_FILE.isEmpty()) {
                String inheritanceFile = EvoSuite.generateInheritanceTree(cp);
                args.add("-Dinheritance_file=" + inheritanceFile);
            }
        } catch (IOException e) {
            LoggingUtils.getEvoLogger().info("* Error while traversing classpath: " + e);
            return results;
        }
        LoggingUtils.getEvoLogger().info("* Found " + classes.size()
                + " matching classes for prefix "
                + prefix);
        for (String sut : classes) {
            try {
                if (ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isClassAnInterface(sut)) {
                    LoggingUtils.getEvoLogger().info("* Skipping interface: " + sut);
                    continue;
                }
            } catch (IOException e) {
                LoggingUtils.getEvoLogger().info("Could not load class: " + sut);
                continue;
            }
            LoggingUtils.getEvoLogger().info("* Current class: " + sut);
            results.addAll(generateTests(strategy, sut, args));
        }
        return results;
    }

    private static boolean findTargetClass(String target) {

        if (ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).hasClass(target)) {
            return true;
        }

        LoggingUtils.getEvoLogger().info("* Unknown class: " + target +
                ". Be sure its full qualifying name  is correct and the classpath is properly set with '-projectCP'");

        return false;
    }

    private static List<List<TestGenerationResult>> generateTests(Properties.Strategy strategy, String target,
                                                                  List<String> args) {

        LoggingUtils.getEvoLogger().info("* Going to generate test cases for class: " + target);

        if (!findTargetClass(target)) {
            final TestGenerationResult result = TestGenerationResultBuilder.buildErrorResult("Could not find target class");
            return Collections.singletonList(Collections.singletonList(result));
        }


        if (!BytecodeInstrumentation.checkIfCanInstrument(target)) {
            throw new IllegalArgumentException(
                    "Cannot consider "
                            + target
                            + " because it belongs to one of the packages EvoSuite cannot currently handle");
        }

        final String DISABLE_ASSERTIONS_EVO = "-da:" + PackageInfo.getEvoSuitePackage() + "...";
        final String ENABLE_ASSERTIONS_EVO = "-ea:" + PackageInfo.getEvoSuitePackage() + "...";
        final String DISABLE_ASSERTIONS_SUT = "-da:" + Properties.PROJECT_PREFIX + "...";
        final String ENABLE_ASSERTIONS_SUT = "-ea:" + Properties.PROJECT_PREFIX + "...";

        List<String> cmdLine = new ArrayList<>();
        cmdLine.add(JavaExecCmdUtil.getJavaBinExecutablePath(true)/*EvoSuite.JAVA_CMD*/);
        List<String[]> processArgs = new ArrayList<>();

        handleClassPath(cmdLine);

        if (Properties.SPAWN_PROCESS_MANAGER_PORT != null) {
            cmdLine.add("-Dspawn_process_manager_port=" + Properties.SPAWN_PROCESS_MANAGER_PORT);
        }

        if (Properties.NUM_PARALLEL_CLIENTS < 1) {
            Properties.NUM_PARALLEL_CLIENTS = 1;
        }

        LoggingUtils[] logServer = new LoggingUtils[Properties.NUM_PARALLEL_CLIENTS];
        ExternalProcessGroupHandler handler = new ExternalProcessGroupHandler(Properties.NUM_PARALLEL_CLIENTS);
        int port = handler.openServer();
        if (port <= 0) {
            throw new RuntimeException("Not possible to start RMI service");
        }
        handler.setBaseDir(EvoSuite.base_dir_path);

        cmdLine.add("-Dprocess_communication_port=" + port);
        cmdLine.add("-Dinline=true");
        if (Properties.HEADLESS_MODE) {
            cmdLine.add("-Djava.awt.headless=true");
        }
        cmdLine.add("-Dlogback.configurationFile=" + LoggingUtils.getLogbackFileName());
        cmdLine.add("-Dlog4j.configuration=SUT.log4j.properties");

        /*
         * FIXME: following 3 should be refactored, as not particularly clean.
         * First 2 does not work for master, as logback is read
         * before Properties is initialized
         */
        if (Properties.LOG_LEVEL != null) {
            cmdLine.add("-Dlog.level=" + Properties.LOG_LEVEL);
        }
        if (Properties.LOG_TARGET != null) {
            cmdLine.add("-Dlog.target=" + Properties.LOG_TARGET);
        }
        String logDir = System.getProperty("evosuite.log.folder");
        if (logDir != null) {
            // this parameter is for example used in logback-ctg.xml
            cmdLine.add(" -Devosuite.log.folder=" + logDir);
        }
        //------------------------------------------------

        cmdLine.add("-Djava.library.path=lib");
        // cmdLine.add("-Dminimize_values=true");

        if (!Properties.PROFILE.isEmpty()) {
            // enabling debugging mode to e.g. connect the eclipse remote debugger to the given port
            File agentFile = new File(Properties.PROFILE);
            if (!agentFile.exists()) {
                LoggingUtils.getEvoLogger().info("* Error: " + Properties.PROFILE + " not found");
            } else {
                cmdLine.add("-agentpath:" + Properties.PROFILE);
                LoggingUtils.getEvoLogger().info("* Using profiling agent " + Properties.PROFILE);
            }
        }


        if (Properties.JMC) {
            //FIXME: does not seem to work, at least on Mac. Looks like some RMI conflict
            cmdLine.add("-XX:+UnlockCommercialFeatures");
            cmdLine.add("-XX:+FlightRecorder");
            cmdLine.add("-Dcom.sun.management.jmxremote");
            cmdLine.add("-Dcom.sun.management.jmxremote.autodiscovery");
            cmdLine.add("-Dcom.sun.management.jmxremote.authenticate=false");
            cmdLine.add("-Dcom.sun.management.jmxremote.ssl=false");
        }
        cmdLine.add("-XX:MaxJavaStackTraceDepth=1000000");
        cmdLine.add("-XX:+StartAttachListener");

        for (String arg : args) {
            if (!arg.startsWith("-DCP=")) {
                cmdLine.add(arg);
            }
        }

        switch (strategy) {
            case EVOSUITE:
                cmdLine.add("-Dstrategy=EvoSuite");
                if (!args.stream().anyMatch(a -> a.startsWith("-Dalgorithm"))) {
                    cmdLine.add("-Dalgorithm=Monotonic_GA");
                }
                break;
            case ONEBRANCH:
                cmdLine.add("-Dstrategy=OneBranch");
                if (!args.stream().anyMatch(a -> a.startsWith("-Dalgorithm"))) {
                    cmdLine.add("-Dalgorithm=Monotonic_GA");
                }
                break;
            case RANDOM:
                cmdLine.add("-Dstrategy=Random");
                break;
            case RANDOM_FIXED:
                cmdLine.add("-Dstrategy=Random_Fixed");
                break;
            case ENTBUG:
                cmdLine.add("-Dstrategy=EntBug");
                break;
            case MOSUITE:
                cmdLine.add("-Dstrategy=MOSuite");

                // Set up defaults for MOSA if not specified by user
                boolean algorithmSet = false;
                boolean selectionSet = false;
                for (String arg : args) {
                    if (arg.startsWith("-Dalgorithm")) {
                        algorithmSet = true;
                    }
                    if (arg.startsWith("-Dselection_function")) {
                        selectionSet = true;
                    }
                }

                if (!selectionSet) {
                    cmdLine.add("-Dselection_function=RANK_CROWD_DISTANCE_TOURNAMENT");
                }

                if (!algorithmSet) {
                    cmdLine.add("-Dalgorithm=DYNAMOSA");
                }
                break;
            case DSE:
                cmdLine.add("-Dstrategy=DSE");
                break;
            case NOVELTY:
                cmdLine.add("-Dstrategy=Novelty");
                break;
            case MAP_ELITES:
                cmdLine.add("-Dstrategy=MAP_ELITES");
                break;
            default:
                throw new RuntimeException("Unsupported strategy: " + strategy);
        }
        cmdLine.add("-DTARGET_CLASS=" + target);
        if (Properties.PROJECT_PREFIX != null) {
            cmdLine.add("-DPROJECT_PREFIX=" + Properties.PROJECT_PREFIX);
        }

        for (String entry : ClassPathHandler.getInstance().getTargetProjectClasspath().split(File.pathSeparator)) {
            try {
                ClassPathHacker.addFile(entry);
            } catch (IOException e) {
                LoggingUtils.getEvoLogger().info("* Error while adding classpath entry: "
                        + entry);
            }
        }

        /*
         * TODO: here we start the client with several properties that are set through -D. These properties are not visible to the master process (ie
         * this process), when we access the Properties file. At the moment, we only need few parameters, so we can hack them
         */
        Properties.getInstance();// should force the load, just to be sure
        Properties.TARGET_CLASS = target;
        Properties.PROCESS_COMMUNICATION_PORT = port;

        for (int i = 0; i < Properties.NUM_PARALLEL_CLIENTS; i++) {
            List<String> cmdLineClone = new ArrayList<>(cmdLine);

            if (i == 0 && Properties.DEBUG) {
                // enabling debugging mode to for Client-0 e.g. connect the eclipse remote debugger to the given port
                cmdLineClone.add("-Ddebug=true");
                cmdLineClone.add("-Xdebug");
                cmdLineClone.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
                        + Properties.PORT);
                LoggingUtils.getEvoLogger().info("* Waiting for remote debugger to connect on port "
                        + Properties.PORT + "...");
            }

            cmdLineClone.add(ClientProcess.class.getName());

            if (Properties.NUM_PARALLEL_CLIENTS == 1) {
                cmdLineClone.add(ClientProcess.DEFAULT_CLIENT_NAME); //to keep functionality for non parallel runs
            } else {
                cmdLineClone.add(ClientProcess.CLIENT_PREFIX + i);
            }

            /*
             *  FIXME: refactor, and double-check if indeed correct
             *
             * The use of "assertions" in the client is pretty tricky, as those properties need to be transformed into JVM options before starting the
             * client. Furthermore, the properties in the property file might be overwritten from the commands coming from shell
             */

            String definedEAforClient = null;
            String definedEAforSUT = null;

            for (String s : cmdLineClone) {
                // first check client
                if (s.startsWith("-Denable_asserts_for_evosuite")) {
                    if (s.endsWith("false")) {
                        definedEAforClient = DISABLE_ASSERTIONS_EVO;
                    } else if (s.endsWith("true")) {
                        definedEAforClient = ENABLE_ASSERTIONS_EVO;
                    }
                }
                // then check SUT
                if (s.startsWith("-Denable_asserts_for_sut")) {
                    if (s.endsWith("false")) {
                        definedEAforSUT = DISABLE_ASSERTIONS_SUT;
                    } else if (s.endsWith("true")) {
                        definedEAforSUT = ENABLE_ASSERTIONS_SUT;
                    }
                }
            }

            /*
             * the assertions might not be defined in the command line, but they might be in the property file, or just use default values. NOTE: if those
             * are defined in the command line, then they overwrite whatever we had in the conf file
             */

            if (definedEAforSUT == null) {
                if (Properties.ENABLE_ASSERTS_FOR_SUT) {
                    definedEAforSUT = ENABLE_ASSERTIONS_SUT;
                } else {
                    definedEAforSUT = DISABLE_ASSERTIONS_SUT;
                }
            }

            if (definedEAforClient == null) {
                if (Properties.ENABLE_ASSERTS_FOR_EVOSUITE) {
                    definedEAforClient = ENABLE_ASSERTIONS_EVO;
                } else {
                    definedEAforClient = DISABLE_ASSERTIONS_EVO;
                }
            }

            /*
             * We add them in first position, after the java command To avoid confusion, we only add them if they are enabled. NOTE: this might have side
             * effects "if" in the future we have something like a generic "-ea"
             */
            if (definedEAforClient.equals(ENABLE_ASSERTIONS_EVO)) {
                cmdLineClone.add(1, definedEAforClient);
            }
            if (definedEAforSUT.equals(ENABLE_ASSERTIONS_SUT)) {
                cmdLineClone.add(1, definedEAforSUT);
            }

            if (!Properties.CLIENT_ON_THREAD) {
                /*
                 * We want to completely mute the SUT. So, we block all outputs from client, and use a remote logging
                 */
                logServer[i] = new LoggingUtils();
                boolean logServerStarted = logServer[i].startLogServer();
                if (!logServerStarted) {
                    logger.error("Cannot start the log server");
                    return null;
                }
                int logPort = logServer[i].getLogServerPort(); //
                cmdLineClone.add(1, "-Dmaster_log_port=" + logPort);
                cmdLineClone.add(1, "-Devosuite.log.appender=CLIENT");
            }

            processArgs.add(cmdLineClone.toArray(new String[0]));
        }

        if (handler.startProcessGroup(processArgs)) {

            Set<ClientNodeRemote> clients = null;
            try {
                //FIXME: timeout here should be handled by TimeController
                clients = new CopyOnWriteArraySet<>(MasterServices.getInstance().getMasterNode()
                        .getClientsOnceAllConnected(60000).values());
            } catch (InterruptedException e) {
            }
            if (clients == null) {
                logger.error("Not possible to access to clients. Clients' state:\n" + handler.getProcessStates() +
                        "Master registry port: " + MasterServices.getInstance().getRegistryPort());

            } else {
                /*
                 * The clients have started, and connected back to Master.
                 * So now we just need to tell them to start a search
                 */
                for (ClientNodeRemote client : clients) {
                    try {
                        client.startNewSearch();
                    } catch (RemoteException e) {
                        logger.error("Error in starting clients", e);
                    }
                }

                int time = TimeController.getInstance().calculateForHowLongClientWillRunInSeconds();
                handler.waitForResult(time * 1000);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            if (Properties.CLIENT_ON_THREAD) {
                handler.stopAndWaitForClientOnThread(10000);
            }

            handler.killAllProcesses();
        } else {
            LoggingUtils.getEvoLogger().info("* Could not connect to client process");
        }

        boolean hasFailed = writeStatistics();

        /*
         * FIXME: it is unclear what is the relation between TestGenerationResult and writeStatistics()
         */
        List<List<TestGenerationResult>> results = SearchStatistics.getInstance().getTestGenerationResults();
        SearchStatistics.clearInstance();

        handler.closeServer();

        if (Properties.CLIENT_ON_THREAD) {
            handler.stopAndWaitForClientOnThread(10000);
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

            for (LoggingUtils aLogServer : logServer) {
                aLogServer.closeLogServer();
            }
        }

        logger.debug("Master process has finished to wait for client");

        //FIXME: tmp hack till understood what TestGenerationResult is...
        if (hasFailed) {
            logger.error("failed to write statistics data");
            //note: cannot throw exception because would require refactoring of many SystemTests
            return new ArrayList<>();
        }

        return results;
    }

    /**
     * Writes generation statistics.
     *
     * @return
     */
    private static boolean writeStatistics() {
        boolean hasFailed = false;

        if (Properties.NEW_STATISTICS) {
            if (MasterServices.getInstance().getMasterNode() == null) {
                logger.error("Cannot write results as RMI master node is not running");
                hasFailed = true;
            } else {
                boolean written = SearchStatistics.getInstance().writeStatistics();
                hasFailed = !written;
            }
        }
        return hasFailed;
    }

    private static void handleClassPath(List<String> cmdLine) {
        String classPath = ClassPathHandler.getInstance().getEvoSuiteClassPath();
        String projectCP = ClassPathHandler.getInstance().getTargetProjectClasspath();

        if (!classPath.isEmpty() && !projectCP.isEmpty()) {
            classPath += File.pathSeparator;
        }

        if (!projectCP.isEmpty()) {
            classPath += projectCP;
        }

        cmdLine.add("-cp");
        //cmdLine.add(classPath);
        String pathingJar = JarPathing.createJarPathing(classPath);
        cmdLine.add(pathingJar);

        if (projectCP.isEmpty()) {
            projectCP = classPath;
        }

        String projectCPFilePath = ClassPathHandler.writeClasspathToFile(projectCP);
        cmdLine.add("-DCP_file_path=" + projectCPFilePath);
    }


    private static List<List<TestGenerationResult>> generateTestsTarget(Properties.Strategy strategy, String target,
                                                                        List<String> args) {
        List<List<TestGenerationResult>> results = new ArrayList<>();
        String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();

        Set<String> classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(target, false);

        LoggingUtils.getEvoLogger().info("* Found " + classes.size()
                + " matching classes in target "
                + target);
        try {
            ClassPathHacker.addFile(target);
        } catch (IOException e) {
            // Ignore?
        }
        try {
            if (Properties.INSTRUMENT_CONTEXT || Properties.INHERITANCE_FILE.isEmpty()) {
                String inheritanceFile = EvoSuite.generateInheritanceTree(cp);
                args.add("-Dinheritance_file=" + inheritanceFile);
            }
        } catch (IOException e) {
            LoggingUtils.getEvoLogger().info("* Error while traversing classpath: " + e);
            return results;
        }

        for (String sut : classes) {
            try {
                if (ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).isClassAnInterface(sut)) {
                    LoggingUtils.getEvoLogger().info("* Skipping interface: " + sut);
                    continue;
                }
            } catch (IOException e) {
                LoggingUtils.getEvoLogger().info("Could not load class: " + sut);
                continue;
            }
            LoggingUtils.getEvoLogger().info("* Current class: " + sut);
            results.addAll(generateTests(strategy, sut, args));
        }

        return results;
    }
}
