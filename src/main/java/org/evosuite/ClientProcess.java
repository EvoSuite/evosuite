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

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.SearchListener;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.utils.ExternalProcessUtilities;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>ClientProcess class.</p>
 *
 * @author Gordon Fraser
 * @author Andrea Arcuri
 */
public class ClientProcess {

	private static final boolean logLevelSet = LoggingUtils.checkAndSetLogLevel();

	private static Logger logger = LoggerFactory.getLogger(ClientProcess.class);

	private final ExternalProcessUtilities util = new ExternalProcessUtilities();

	/** Constant <code>geneticAlgorithmStatus</code> */
	public static GeneticAlgorithm geneticAlgorithmStatus;

	/**
	 * <p>run</p>
	 */
	public void run() {

		LoggingUtils.getEvoLogger().info("* Connecting to master process on port "
		                                         + Properties.PROCESS_COMMUNICATION_PORT);
		if (!util.connectToMainProcess()) {
			LoggingUtils.getEvoLogger().error("* Could not connect to master process on port "
			                                          + Properties.PROCESS_COMMUNICATION_PORT);
			System.exit(1);
		}

		TestSuiteGenerator generator = null;
		Object instruction = util.receiveInstruction();
		/*
		 * for now, we ignore the instruction (originally was meant to support several client in parallel and
		 * restarts, but that will be done in RMI)
		 */

		// Starting a new search
		generator = new TestSuiteGenerator();
		generator.generateTestSuite();

		GeneticAlgorithm ga = generator.getEmployedGeneticAlgorithm();
		
		if (Properties.CLIENT_ON_THREAD) {
			/*
			 * this is done when the client is run on same JVM, to avoid
			 * problems of serializing ga
			 */
			geneticAlgorithmStatus = ga;
		}
		
		util.informSearchIsFinished(ga);
	}

	

	/**
	 * <p>main</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		try {
			LoggingUtils.getEvoLogger().info("* Starting client");
			ClientProcess process = new ClientProcess();
			process.run();
			if (!Properties.CLIENT_ON_THREAD) {
				System.exit(0);
			}
		} catch (Throwable t) {
			logger.error("Error when generating tests for: " + Properties.TARGET_CLASS
			        + " with seed " + Randomness.getSeed(), t);
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
