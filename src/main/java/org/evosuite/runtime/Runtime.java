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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EvoSuiteIO;

/**
 * <p>
 * Runtime class.
 * </p>
 * 
 * @author Gordon Fraser
 * @author Daniel Muth
 */
public class Runtime {

	/**
	 * maps from the name of a FileSystem method to a class array containing its parameter types
	 */
	private static Map<String, Class<?>[]> fileOperations;

	/**
	 * the set of file operation selectors that shall be used to select FileSystem methods
	 */
	private static Set<FileOperationSelector> fileOperationSelectors;

	private static Logger logger = LoggerFactory.getLogger(Runtime.class);

	/**
	 * Resets all simulated classes to an initial default state (so that it seems they have never been used by previous test case executions)
	 * <p>
	 * (Idea by Gordon, JavaDoc written by Daniel)
	 */
	public static void resetRuntime() {
		if (Properties.REPLACE_CALLS) {
			Random.reset();
			System.reset();
		}

		if (Properties.VIRTUAL_FS) {
			logger.info("Resetting the VFS...");
			FileSystem.reset();
			logger.info("Enabling the VFS...");
			EvoSuiteIO.enableVFS();
		}
	}

	/**
	 * 
	 * <p>
	 * If access to certain classes was observed at runtime, this method adds test calls to the test cluster which may lead to covering more branches.
	 * For example, if file access was observed, statements will be introduced that perform mutations on the accessed files like content modifiation.
	 * 
	 * <p>
	 * (Idea by Gordon, JavaDoc written by Daniel)
	 * 
	 * @see FileSystem
	 * @see EvoSuiteIO
	 * @see Random
	 * @see System
	 */
	public static void handleRuntimeAccesses(TestCase test) {
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

		if (Properties.VIRTUAL_FS) {
			EvoSuiteIO.disableVFS();
			test.setAccessedFiles(new ArrayList<String>(EvoSuiteIO
					.getFilesAccessedByCUT()));

			if (EvoSuiteIO.filesWereAccessedByCUT()) {
				logger.info("Adding EvoSuiteFile calls to cluster");

				if (fileOperations == null) {
					fileOperations = new HashMap<String, Class<?>[]>();
					fileOperations.put("setFileContent", new Class<?>[] {
							EvoSuiteFile.class, String.class });
					fileOperations.put("setReadPermission", new Class<?>[] {
							EvoSuiteFile.class, boolean.class });
					fileOperations.put("setWritePermission", new Class<?>[] {
							EvoSuiteFile.class, boolean.class });
					fileOperations.put("setExecutePermission", new Class<?>[] {
							EvoSuiteFile.class, boolean.class });
					fileOperations.put("deepDelete",
							new Class<?>[] { EvoSuiteFile.class });
					fileOperations.put("createFile",
							new Class<?>[] { EvoSuiteFile.class });
					fileOperations.put("createDirectory",
							new Class<?>[] { EvoSuiteFile.class });
					fileOperations.put("createAndFillDirectory",
							new Class<?>[] { EvoSuiteFile.class });
					fileOperations.put("createParent",
							new Class<?>[] { EvoSuiteFile.class });
					fileOperations.put("deepDeleteParent",
							new Class<?>[] { EvoSuiteFile.class });
				}

				if (fileOperationSelectors == null) {
					fileOperationSelectors = new HashSet<FileOperationSelector>();
					fileOperationSelectors
							.add(FileOperationSelectors.FILE_CONTENT_MODIFICATION);
					fileOperationSelectors
							.add(FileOperationSelectors.CREATION_AND_DELETION);
					fileOperationSelectors
							.add(FileOperationSelectors.PARENT_CREATION_AND_DELETION);
					fileOperationSelectors
							.add(FileOperationSelectors.PERMISSION_MODIFICATION);
					fileOperationSelectors
							.add(FileOperationSelectors.DIRECTORY_CONTENT_MODIFICATION);
				}

				try {
					for (String method : fileOperations.keySet()) {
						for (FileOperationSelector fileOperationSelector : fileOperationSelectors) {
							if (fileOperationSelector.select(method)) {
								TestCluster.getInstance().addTestCall(
										FileSystem.class.getMethod(method,
												fileOperations.get(method)));
								break;
							}
						}
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
