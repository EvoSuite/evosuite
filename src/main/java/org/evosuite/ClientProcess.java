/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite;

import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.rmi.ClientServices;
import org.evosuite.utils.ExternalProcessUtilities;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
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

	static {
		LoggingUtils.checkAndSetLogLevel();
	}

	private static Logger logger = LoggerFactory.getLogger(ClientProcess.class);

	//private final ExternalProcessUtilities util = new ExternalProcessUtilities();

	/** Constant <code>geneticAlgorithmStatus</code> */
	public static GeneticAlgorithm<?> geneticAlgorithmStatus;

	/**
	 * <p>
	 * run
	 * </p>
	 */
	public void run() {
		Properties.getInstance();
		LoggingUtils.getEvoLogger().info("* Connecting to master process on port "
		                                         + Properties.PROCESS_COMMUNICATION_PORT);
	
		boolean registered = ClientServices.getInstance().registerServices();
	
		//if (!util.connectToMainProcess()) {
		if (!registered) {
			throw new RuntimeException("Could not connect to master process on port "
                    + Properties.PROCESS_COMMUNICATION_PORT);
		}
		
		/*
		 * Now the client node is registered with RMI.
		 * The master will control this node directly.
		 */
		
		ClientServices.getInstance().getClientNode().waitUntilDone();
		ClientServices.getInstance().stopServices();
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
		try {
			LoggingUtils.getEvoLogger().info("* Starting client");
			ClientProcess process = new ClientProcess();
			process.run();
			if (!Properties.CLIENT_ON_THREAD) {
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

			if (!Properties.CLIENT_ON_THREAD) {
				System.exit(1);
			}
		}
	}
}
