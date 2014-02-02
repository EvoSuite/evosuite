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
package org.evosuite.sandbox;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which controls enabling and disabling sandbox.
 * EvoSuite uses its own customized security manager.
 * 
 */
public class Sandbox {

	private static Logger logger = LoggerFactory.getLogger(Sandbox.class);

	private static MSecurityManager manager;

	/**
	 * count how often we tried to init the sandbox.
	 * 
	 * <p>
	 * Ideally, the sandbox should be init only once.
	 * Problem is, that we do compile and run the JUnit test cases (eg
	 * to see if they compile with no problems, if their assertions 
	 * are stable, etc), and those test cases do init/reset the sandbox
	 */
	private static volatile int counter;
	
	/**
	 * Create and initialize security manager for SUT
	 */
	public static void initializeSecurityManagerForSUT() {
		if (manager == null) {
			manager = new MSecurityManager();
			manager.makePriviligedAllCurrentThreads();
			manager.apply();
		} else {
			logger.warn("Sandbox can be initalized only once");
		}
		
		counter++;
	}

	public static void addPriviligedThread(Thread t) {
		if (manager != null)
			manager.addPrivilegedThread(t);
	}

	public static void resetDefaultSecurityManager() {
		
		counter--;
		
		if(counter==0){
			if (manager != null) {
				manager.restoreDefaultManager();
			}
			manager = null;
		}
	}

	public static boolean isSecurityManagerInitialized() {
		return manager != null;
	}

	public static void goingToExecuteSUTCode() {
		if (!isSecurityManagerInitialized()) {
			if(Properties.SANDBOX){
				logger.error("Sandbox is not initialized!");
			}
			return;
		}
		manager.goingToExecuteTestCase();
		PermissionStatistics.getInstance().getAndResetExceptionInfo();
		
		TestGenerationContext.getInstance().goingToExecuteSUTCode();
	}

	public static void doneWithExecutingSUTCode() {
		if (!isSecurityManagerInitialized()) {
			if(Properties.SANDBOX){
				logger.error("Sandbox is not initialized!");
			}
			return;
		}
		manager.goingToEndTestCase();
		TestGenerationContext.getInstance().doneWithExecuteingSUTCode();
	}

	public static void goingToExecuteUnsafeCodeOnSameThread() throws SecurityException,
	        IllegalStateException {
		if (!isSecurityManagerInitialized()) {
			return;
		}
		manager.goingToExecuteUnsafeCodeOnSameThread();
	}

	public static void doneWithExecutingUnsafeCodeOnSameThread()
	        throws SecurityException, IllegalStateException {
		if (!isSecurityManagerInitialized()) {
			return;
		}
		manager.doneWithExecutingUnsafeCodeOnSameThread();
	}
}
