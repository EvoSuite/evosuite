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
import org.evosuite.ClientProcess;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHacker;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientNodeRemote;
import org.evosuite.runtime.util.JavaExecCmdUtil;
import org.evosuite.utils.ExternalProcessGroupHandler;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WriteDependencies {

    private static final Logger logger = LoggerFactory.getLogger(WriteDependencies.class);

    public static final String NAME = "writeDependencies";

    public static Option getOption() {
        return new Option(NAME, true, "write the dependencies of a target class to file");
    }

    public static Object execute(Options options, List<String> javaOpts, CommandLine line) {
        if (line.hasOption("class"))
            writeDependencies(line.getOptionValue(NAME), line.getOptionValue("class"), javaOpts);
        else {
            LoggingUtils.getEvoLogger().error("Please specify target class ('-class' option) to list class dependencies");
            Help.execute(options);
        }
        return null;
    }

    private static void writeDependencies(String targetFile, String targetClass,
                                          List<String> args) {

        if (!BytecodeInstrumentation.checkIfCanInstrument(targetClass)) {
            throw new IllegalArgumentException(
                    "Cannot consider "
                            + targetClass
                            + " because it belongs to one of the packages EvoSuite cannot currently handle");
        }
        String classPath = ClassPathHandler.getInstance().getEvoSuiteClassPath();
        String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
        classPath += File.pathSeparator + cp;

        ExternalProcessGroupHandler handler = new ExternalProcessGroupHandler();
        int port = handler.openServer();
        List<String> cmdLine = new ArrayList<>();
        cmdLine.add(JavaExecCmdUtil.getJavaBinExecutablePath(true)/*EvoSuite.JAVA_CMD*/);
        cmdLine.add("-cp");
        cmdLine.add(classPath);
        cmdLine.add("-Dprocess_communication_port=" + port);
        cmdLine.add("-Djava.awt.headless=true");
        cmdLine.add("-Dlogback.configurationFile=" + LoggingUtils.getLogbackFileName());
        cmdLine.add("-Djava.library.path=lib");
        cmdLine.add("-DCP=" + cp);
        // cmdLine.add("-Dminimize_values=true");

        for (String arg : args) {
            if (!arg.startsWith("-DCP=")) {
                cmdLine.add(arg);
            }
        }

        cmdLine.add("-DTARGET_CLASS=" + targetClass);
        if (Properties.PROJECT_PREFIX != null) {
            cmdLine.add("-DPROJECT_PREFIX=" + Properties.PROJECT_PREFIX);
        }

        cmdLine.add("-Dclassloader=true");
        cmdLine.add(ClientProcess.class.getName());

        /*
         * TODO: here we start the client with several properties that are set through -D. These properties are not visible to the master process (ie
         * this process), when we access the Properties file. At the moment, we only need few parameters, so we can hack them
         */
        Properties.getInstance();// should force the load, just to be sure
        Properties.TARGET_CLASS = targetClass;
        Properties.PROCESS_COMMUNICATION_PORT = port;

        LoggingUtils logUtils = new LoggingUtils();

        if (!Properties.CLIENT_ON_THREAD) {
            /*
             * We want to completely mute the SUT. So, we block all outputs from client, and use a remote logging
             */
            boolean logServerStarted = logUtils.startLogServer();
            if (!logServerStarted) {
                logger.error("Cannot start the log server");
                return;
            }
            int logPort = logUtils.getLogServerPort(); //
            cmdLine.add(1, "-Dmaster_log_port=" + logPort);
            cmdLine.add(1, "-Devosuite.log.appender=CLIENT");
        }

        String[] newArgs = cmdLine.toArray(new String[cmdLine.size()]);
        for (String entry : ClassPathHandler.getInstance().getClassPathElementsForTargetProject()) {
            try {
                ClassPathHacker.addFile(entry);
            } catch (IOException e) {
                LoggingUtils.getEvoLogger().info("* Error while adding classpath entry: "
                        + entry);
            }
        }

        handler.setBaseDir(EvoSuite.base_dir_path);
        if (handler.startProcess(newArgs)) {
            Set<ClientNodeRemote> clients = null;
            try {
                clients = new CopyOnWriteArraySet<>(MasterServices.getInstance().getMasterNode()
                        .getClientsOnceAllConnected(10000).values());
            } catch (InterruptedException e) {
            }
            if (clients == null) {
                logger.error("Not possible to access to clients");
            } else {
                /*
                 * The clients have started, and connected back to Master.
                 * So now we just need to tell them to start a search
                 */
                for (ClientNodeRemote client : clients) {
                    try {
                        client.doDependencyAnalysis(targetFile);
                    } catch (RemoteException e) {
                        logger.error("Error in starting clients", e);
                    }
                }

                handler.waitForResult((Properties.GLOBAL_TIMEOUT
                        + Properties.MINIMIZATION_TIMEOUT + Properties.EXTRA_TIMEOUT) * 1000); // FIXXME: search
            }
            // timeout plus
            // 100 seconds?
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

            handler.killProcess();
        } else {
            LoggingUtils.getEvoLogger().info("* Could not connect to client process");
        }

        handler.closeServer();

        if (!Properties.CLIENT_ON_THREAD) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            logUtils.closeLogServer();
        }
    }

}
