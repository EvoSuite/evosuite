/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite;

import org.dom4j.DocumentFactory;
import org.dom4j.dom.DOMDocumentFactory;
import org.evosuite.classpath.ClassPathHacker;
import org.evosuite.junit.writer.TestSuiteWriterUtils;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.MasterNodeRemote;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.classhandling.JDKClassResetter;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.sandbox.MSecurityManager;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.SpawnProcessKeepAliveChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * ClientProcess class.
 * </p>
 * 
 * @author Gordon Fraser
 * @author Andrea Arcuri
 */
public class ClientProcess {

	private static Logger logger = LoggerFactory.getLogger(ClientProcess.class);

	public static TestGenerationResult result;

	/**
	 * <p>
	 * run
	 * </p>
	 */
	public void run() {
		Properties.getInstance();
		setupRuntimeProperties();
		handleShadingSpecialCases();
		JDKClassResetter.init();
		Sandbox.setCheckForInitialization(Properties.SANDBOX);
		MockFramework.enable();

		if (TestSuiteWriterUtils.needToUseAgent() && Properties.JUNIT_CHECK) {
			initializeToolJar();
		}

		MSecurityManager.setupMasterNodeRemoteHandling(MasterNodeRemote.class);

		LoggingUtils.getEvoLogger().info("* Connecting to master process on port "
				+ Properties.PROCESS_COMMUNICATION_PORT);

		boolean registered = ClientServices.getInstance().registerServices();

		if (!registered) {
			result = TestGenerationResultBuilder.buildErrorResult("Could not connect to master process on port "
					+ Properties.PROCESS_COMMUNICATION_PORT);
			throw new RuntimeException("Could not connect to master process on port "
					+ Properties.PROCESS_COMMUNICATION_PORT);
		}

		if(Properties.SPAWN_PROCESS_MANAGER_PORT != null){
			SpawnProcessKeepAliveChecker.getInstance().registerToRemoteServerAndDieIfFails(
					Properties.SPAWN_PROCESS_MANAGER_PORT
			);
		}

		/*
		 * Now the client node is registered with RMI.
		 * The master will control this node directly.
		 */

		ClientServices.getInstance().getClientNode().waitUntilDone();
		ClientServices.getInstance().stopServices();
		SpawnProcessKeepAliveChecker.getInstance().unRegister();
	}

	private void initializeToolJar() {

		ClassPathHacker.initializeToolJar();

		/*
		 * We load the agent although we "might" not use it.
		 * Reason is that when we compile the generated test cases to debug
		 * EvoSuite, those will/should use the agent.
		 * But for some arcane reason, the loading there fails.
		 * For example, there were/are issues with double-loading
		 * of libraries in different classloaders, eg the static
		 * initializer of BsdVirtualMachine does a  System.loadLibrary("attach"),
		 * and for some reason that is executed twice if the agent is loaded
		 * later in the search. Note: this does not affect the generated test
		 * cases when run from Eclipse (for example). 
		 */

        /*
        	TODO: tmp disabled to understand what the hack is happening on Jenkins.
        	however, it does not seem necessary any more, after quite a few refactoring/changes
        	in how agents are used (but don't know why...)
         */
		//AgentLoader.loadAgent();
	}

	private static void setupRuntimeProperties(){
		RuntimeSettings.useVFS = Properties.VIRTUAL_FS;
		RuntimeSettings.mockJVMNonDeterminism = Properties.REPLACE_CALLS;
		RuntimeSettings.mockSystemIn = Properties.REPLACE_SYSTEM_IN;
		RuntimeSettings.sandboxMode = Properties.SANDBOX_MODE;
        RuntimeSettings.maxNumberOfThreads = Properties.MAX_STARTED_THREADS;
        RuntimeSettings.maxNumberOfIterationsPerLoop = Properties.MAX_LOOP_ITERATIONS;
        RuntimeSettings.useVNET = Properties.VIRTUAL_NET;
        RuntimeSettings.useSeparateClassLoader = Properties.USE_SEPARATE_CLASSLOADER;
		RuntimeSettings.className = Properties.TARGET_CLASS;
		RuntimeSettings.useJEE = Properties.JEE;
		RuntimeSettings.applyUIDTransformation = true;
		RuntimeSettings.isRunningASystemTest = Properties.IS_RUNNING_A_SYSTEM_TEST;
        MethodCallReplacementCache.resetSingleton();
    }


	private static void handleShadingSpecialCases(){

		String shadePrefix = PackageInfo.getShadedPackageForThirdPartyLibraries()+".";

		if(! DocumentFactory.class.getName().startsWith(shadePrefix)){
			//if not shaded (eg in system tests), then nothing to do
			return;
		}

		String defaultFactory = System.getProperty("org.dom4j.factory", "org.dom4j.DocumentFactory");
		String defaultDomSingletonClass= System.getProperty(
				"org.dom4j.dom.DOMDocumentFactory.singleton.strategy", "org.dom4j.util.SimpleSingleton");
		String defaultSingletonClass = System.getProperty(
				"org.dom4j.DocumentFactory.singleton.strategy", "org.dom4j.util.SimpleSingleton");

		System.setProperty("org.dom4j.factory" , shadePrefix + defaultFactory);
		System.setProperty("org.dom4j.dom.DOMDocumentFactory.singleton.strategy" ,
				shadePrefix + defaultDomSingletonClass);
		System.setProperty("org.dom4j.DocumentFactory.singleton.strategy" ,
				shadePrefix + defaultSingletonClass);

		DocumentFactory.getInstance(); //force loading
		DOMDocumentFactory.getInstance();

		//restore in case SUT uses its own dom4j
		System.setProperty("org.dom4j.factory" ,defaultFactory);
		System.setProperty("org.dom4j.dom.DOMDocumentFactory.singleton.strategy",
				defaultDomSingletonClass);
		System.setProperty("org.dom4j.DocumentFactory.singleton.strategy",
				defaultSingletonClass);
	}

	/**
	 * <p>
	 * main
	 * </p>
	 * 
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {

		/*
		 * important to have it in a variable, otherwise 
		 * might be issues with following System.exit if successive
		 * threads change it if this thread is still running
		 */
		boolean onThread = Properties.CLIENT_ON_THREAD;

		try {
			LoggingUtils.getEvoLogger().info("* Starting client");
			ClientProcess process = new ClientProcess();
			TimeController.resetSingleton();
			process.run();
			if (!onThread) {
				/*
				 * If we we are in debug mode in which we run client on separated thread,
				 * then do not kill the JVM
				 */
				System.exit(0);
			}
		} catch (Throwable t) {
			logger.error("Error when generating tests for: " + Properties.TARGET_CLASS
					+ " with seed " + Randomness.getSeed()+". Configuration id : "+Properties.CONFIGURATION_ID, t);
			t.printStackTrace();

			//sleep 1 sec to be more sure that the above log is recorded
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			if (!onThread) {
				System.exit(1);
			}
		}
	}
}
