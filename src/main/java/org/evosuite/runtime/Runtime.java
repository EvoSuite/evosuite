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

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Runtime class.
 * </p>
 * 
 * @author Gordon Fraser
 * @author Daniel Muth
 */
public class Runtime {

	private static final Logger logger = LoggerFactory.getLogger(Runtime.class);

	private static final Runtime singleton = new Runtime();

	private volatile boolean hasAddedRandom;
	private volatile boolean hasAddedSystem;
	private volatile boolean hasAddedFiles;

	protected Runtime(){		
	}

	public synchronized static Runtime getInstance(){
		return singleton;
	}

	public synchronized static void resetSingleton(){
		singleton.resetRuntime();
	}

	/**
	 * Resets all simulated classes to an initial default state (so that it
	 * seems they have never been used by previous test case executions)
	 * 
	 */
	public void resetRuntime() {

		hasAddedRandom = false;
		hasAddedSystem = false;
		hasAddedFiles = false;

		if (Properties.REPLACE_CALLS) {
			Random.reset();
			System.reset();
			Thread.reset();
		}

		if (Properties.VIRTUAL_FS) {			
			logger.debug("Resetting the VFS...");
			VirtualFileSystem.getInstance().resetSingleton();
			VirtualFileSystem.getInstance().init();
		}
	}

	/**
	 * 
	 * <p>
	 * If access to certain classes was observed at runtime, this method adds
	 * test calls to the test cluster which may lead to covering more branches.
	 * For example, if file access was observed, statements will be introduced
	 * that perform mutations on the accessed files like content modifiation.
	 * 
	 * <p>
	 * (Idea by Gordon, JavaDoc written by Daniel)
	 * 
	 * @see FileSystem
	 * @see EvoSuiteIO
	 * @see Random
	 * @see System
	 */
	public void handleRuntimeAccesses(TestCase test) {
		if (Properties.REPLACE_CALLS) {
			handleReplaceCalls();
		}

		if (Properties.VIRTUAL_FS) {
			handleVirtualFS(test);
		}
	}


	private void handleVirtualFS(TestCase test) {
		test.setAccessedFiles(new ArrayList<String>(VirtualFileSystem.getInstance().getAccessedFiles()));

		if (!hasAddedFiles && VirtualFileSystem.getInstance().getAccessedFiles().size() > 0) {
			logger.info("Adding EvoSuiteFile calls to cluster");

			hasAddedFiles = true;

			try {
				/*
				 * all methods in FileSystemHandling will be used in the search
				 */
				for(Method m : FileSystemHandling.class.getMethods()){
					TestCluster.getInstance().addTestCall(
							new GenericMethod(m,
									new GenericClass(FileSystemHandling.class)));
				}						
			} catch (Exception e) {
				logger.error("Error while handling virtual file system: "+e.getMessage(),e);
			}
		}
	}

	private void handleReplaceCalls() {

		if (!hasAddedRandom && Random.wasAccessed()) {
			hasAddedRandom = true;
			try {
				TestCluster.getInstance().addTestCall(new GenericMethod(
						Random.class.getMethod("setNextRandom",
								new Class<?>[] { int.class }),
								new GenericClass(
										Random.class)));
			} catch (SecurityException e) {
				logger.error("Error while handling Random: "+e.getMessage(),e);
			} catch (NoSuchMethodException e) {
				logger.error("Error while handling Random: "+e.getMessage(),e);
			}
		}

		if (!hasAddedSystem && System.wasAccessed()) {
			hasAddedSystem = true;
			try {
				TestCluster.getInstance().addTestCall(new GenericMethod(
						System.class.getMethod("setCurrentTimeMillis",
								new Class<?>[] { long.class }),
								new GenericClass(
										System.class)));
			} catch (SecurityException e) {
				logger.error("Error while handling System: "+e.getMessage(),e);
			} catch (NoSuchMethodException e) {
				logger.error("Error while handling System: "+e.getMessage(),e);
			}
		}
	}
}
