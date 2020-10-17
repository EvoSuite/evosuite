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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.evosuite.*;
import org.evosuite.classpath.ClassPathHacker;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.classpath.ResourceList;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientNodeRemote;
import org.evosuite.runtime.util.JavaExecCmdUtil;
import org.evosuite.statistics.SearchStatistics;
import org.evosuite.utils.ExternalProcessGroupHandler;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasureCoverage {

	private static final Logger logger = LoggerFactory.getLogger(MeasureCoverage.class);

	public static final String NAME = "measureCoverage";

	public static Option getOption(){
		return new Option(NAME, "measure coverage on existing test cases");
	}

	public static Object execute(Options options, List<String> javaOpts,
			CommandLine line) {
		if (line.hasOption("class")) {
			measureCoverageClass(line.getOptionValue("class"), javaOpts);
		} else if (line.hasOption("target")) {
			measureCoverageTarget(line.getOptionValue("target"), javaOpts);
		} else {
			LoggingUtils.getEvoLogger().error("Please specify target class ('-class' option)");
			Help.execute(options);
		}
		return SearchStatistics.getInstance();
	}

	private static void measureCoverageClass(String targetClass, List<String> args) {

		if (!ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).hasClass(targetClass)) {
			LoggingUtils.getEvoLogger().error("* Unknown class: " + targetClass
							+ ". Be sure its full qualifying name is correct and the classpath is properly set with '-projectCP'");
		}

		if (!BytecodeInstrumentation.checkIfCanInstrument(targetClass)) {
			throw new IllegalArgumentException(
			        "Cannot consider "
			                + targetClass
			                + " because it belongs to one of the packages EvoSuite cannot currently handle");
		}

		measureCoverage(targetClass, args);
	}

	private static void measureCoverageTarget(String target, List<String> args) {

		Set<String> classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(target, false);
		LoggingUtils.getEvoLogger().info("* Found " + classes.size() + " matching classes in target " + target);

		measureCoverage(target, args);
	}

	private static void measureCoverage(String targetClass, List<String> args) {

		String classPath = ClassPathHandler.getInstance().getEvoSuiteClassPath();
		String projectCP = ClassPathHandler.getInstance().getTargetProjectClasspath();

		classPath += !classPath.isEmpty() ? File.pathSeparator + projectCP : projectCP;

		ExternalProcessGroupHandler handler = new ExternalProcessGroupHandler();
		int port = handler.openServer();
		List<String> cmdLine = new ArrayList<>();
		cmdLine.add(JavaExecCmdUtil.getJavaBinExecutablePath(true)/*EvoSuite.JAVA_CMD*/);
		cmdLine.add("-cp");
		cmdLine.add(classPath);
		cmdLine.add("-Dprocess_communication_port=" + port);
		if(Properties.HEADLESS_MODE) {
			cmdLine.add("-Djava.awt.headless=true");
		}
		cmdLine.add("-Dlogback.configurationFile="+LoggingUtils.getLogbackFileName());
		cmdLine.add("-Djava.library.path=lib");
		cmdLine.add(projectCP.isEmpty() ? "-DCP=" + classPath : "-DCP=" + projectCP);

		for (String arg : args) {
			if (!arg.startsWith("-DCP=")) {
				cmdLine.add(arg);
			}
		}

		cmdLine.add("-DTARGET_CLASS=" + targetClass);
		cmdLine.add("-Djunit=" + Properties.JUNIT);
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
						client.doCoverageAnalysis();
					} catch (RemoteException e) {
						logger.error("Error in starting clients", e);
					}
				}
				int time = TimeController.getInstance().calculateForHowLongClientWillRunInSeconds();
				handler.waitForResult(time * 1000);
			}
			// timeout plus
			// 100 seconds?
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			if (Properties.NEW_STATISTICS) {
				if(MasterServices.getInstance().getMasterNode() == null) {
					logger.error("Cannot write results as RMI master node is not running");
				} else {
					LoggingUtils.getEvoLogger().info("* Writing statistics");

					SearchStatistics.getInstance().writeStatisticsForAnalysis();
				}
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
