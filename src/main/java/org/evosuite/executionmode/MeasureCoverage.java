package org.evosuite.executionmode;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.javaagent.InstrumentingClassLoader;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientNodeRemote;
import org.evosuite.utils.ClassPathHacker;
import org.evosuite.utils.ExternalProcessHandler;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasureCoverage {

	private static Logger logger = LoggerFactory.getLogger(MeasureCoverage.class);
	
	public static final String NAME = "measureCoverage";
	
	public static Option getOption(){
		return new Option(NAME, "measure coverage on existing test cases");
	}
	
	public static Object execute(Options options, List<String> javaOpts,
			CommandLine line, String cp) {
		if (line.hasOption("class")){
			measureCoverage(line.getOptionValue("class"), line.getOptionValue("junit"), javaOpts, cp);
		} else {
			LoggingUtils.getEvoLogger().error("Please specify target class ('-class' option)");
			Help.execute(options);
		}
		return null;
	}
	
	private static void measureCoverage(String targetClass, String junitPrefix,
	        List<String> args, String cp) {
		if (!InstrumentingClassLoader.checkIfCanInstrument(targetClass)) {
			throw new IllegalArgumentException(
			        "Cannot consider "
			                + targetClass
			                + " because it belongs to one of the packages EvoSuite cannot currently handle");
		}
		String classPath = System.getProperty("java.class.path");
		if (!EvoSuite.evosuiteJar.equals("")) {
			classPath += File.pathSeparator + EvoSuite.evosuiteJar;
		}

		classPath += File.pathSeparator + cp;
		ExternalProcessHandler handler = new ExternalProcessHandler();
		int port = handler.openServer();
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(EvoSuite.JAVA_CMD);
		cmdLine.add("-cp");
		cmdLine.add(classPath);
		cmdLine.add("-Dprocess_communication_port=" + port);
		cmdLine.add("-Djava.awt.headless=true");
		cmdLine.add("-Dlogback.configurationFile=logback-evosuite.xml");
		cmdLine.add("-Djava.library.path=lib");
		cmdLine.add("-DCP=" + cp);
		// cmdLine.add("-Dminimize_values=true");

		for (String arg : args) {
			if (!arg.startsWith("-DCP=")) {
				cmdLine.add(arg);
			}
		}

		cmdLine.add("-DTARGET_CLASS=" + targetClass);
		cmdLine.add("-Djunit_prefix=" + junitPrefix);
		if (Properties.PROJECT_PREFIX != null) {
			cmdLine.add("-DPROJECT_PREFIX=" + Properties.PROJECT_PREFIX);
		}

		cmdLine.add("-Dclassloader=true");
		cmdLine.add("org.evosuite.ClientProcess");

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
		for (String entry : Properties.CP.split(File.pathSeparator)) {
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
				clients = MasterServices.getInstance().getMasterNode().getClientsOnceAllConnected(10000);
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
