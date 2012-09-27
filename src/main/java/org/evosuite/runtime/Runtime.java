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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.evosuite.io.IOWrapper;

/**
 * <p>
 * Runtime class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class Runtime {

	/**
	 * maps from the name of a FileSystem method to a class array containing its parameter types
	 */
	private static Map<String, Class<?>[]> fileOperations;

	public static Set<FileOperationSelector> fileOperationSelectors;

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
			if (!IOWrapper.isInitialized())
				IOWrapper.initialize(Properties.PROJECT_PREFIX,
						Properties.READ_ONLY_FROM_SANDBOX_FOLDER, new File(
								Properties.SANDBOX_FOLDER, "read")
								.getAbsoluteFile());
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
					fileOperations.put("createFolder",
							new Class<?>[] { EvoSuiteFile.class });
					fileOperations.put("fillFolder",
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

					// TODO test calls concerning permissions do not work with evosuite-io v0.3 - enable them with evosuite-io v0.4
					// fileOperationSelectors.add(FileOperationSelectors.PERMISSION_MODIFICATION);

					// TODO lead to bad performance - commented out until reason found
					// fileOperationSelectors.add(FileOperationSelectors.FOLDER_CONTENT_MODIFICATION);
					// fileOperationSelectors.add(FileOperationSelectors.PARENT_CREATION_AND_DELETION);
				}

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

				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("setFileContent",
				// EvoSuiteFile.class, String.class));

				// TODO test calls concerning permissions do not work with evosuite-io v0.3 - enable them with evosuite-io v0.4
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("setReadPermission",
				// EvoSuiteFile.class, boolean.class));
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("setWritePermission",
				// EvoSuiteFile.class, boolean.class));
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("setExecutePermission",
				// EvoSuiteFile.class, boolean.class));
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("deepDelete",
				// EvoSuiteFile.class));
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("createFile",
				// EvoSuiteFile.class));
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("createFolder",
				// EvoSuiteFile.class));
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("fillFolder",
				// EvoSuiteFile.class));
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("createParent",
				// EvoSuiteFile.class));
				// TestCluster.getInstance().addTestCall(
				// FileSystem.class.getMethod("deepDeleteParent",
				// EvoSuiteFile.class));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}
}
