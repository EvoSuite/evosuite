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
package org.evosuite.runtime;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.io.IOWrapper;

/**
 * <p>
 * Runtime class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class Runtime {

	private static Logger logger = LoggerFactory.getLogger(Runtime.class);

	/**
	 * <p>
	 * resetRuntime
	 * </p>
	 */
	public static void resetRuntime() {
		if (Properties.REPLACE_CALLS) {
			Random.reset();
			System.reset();
		}

		if (Properties.VIRTUAL_FS) {
			IOWrapper.initialize(Properties.PROJECT_PREFIX); // actually only needs to be executed once before the very first test run - if you read
																// this comment and know a nice place for this, feel free to move it there; for now
																// I'm leaving it here for code coherence since runtime is negligible
			FileSystem.reset();
		}
	}

	/**
	 * <p>
	 * handleRuntimeAccesses
	 * </p>
	 */
	public static void handleRuntimeAccesses() {
		if (Properties.REPLACE_CALLS) {

			if (Random.wasAccessed()) {
				try {
					TestCluster.getInstance().addTestCall(
							Random.class.getMethod("setNextRandom",
									new Class<?>[] { int.class }));
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (System.wasAccessed()) {
				try {
					TestCluster.getInstance().addTestCall(
							System.class.getMethod("setCurrentTimeMillis",
									new Class<?>[] { long.class }));
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (Properties.VIRTUAL_FS && FileSystem.wasAccessed()) {
			try {
				logger.info("Adding EvoSuiteFile calls to cluster");
				TestCluster.getInstance().addTestCall(
						FileSystem.class.getMethod("setFileContent",
								new Class<?>[] { EvoSuiteFile.class,
										String.class }));
				// TODO: Add other methods (setFilePermission, etc)

			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}
}
