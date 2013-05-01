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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which controls enabling and disabling sandbox.
 * 
 * 
 * Note: for the moment it sets a custom security manager and a mocking
 * framework. But this latter is not really used now (but we might want to use
 * it in the future once fixed). So, for the time being, this class is just a
 * wrapper over the security manager
 * 
 * @author Andrey Tarasevich
 */
public class Sandbox {

	private static Logger logger = LoggerFactory.getLogger(Sandbox.class);

	private static MSecurityManager manager;

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
	}

	public static void addPriviligedThread(Thread t) {
		if (manager != null)
			manager.addPrivilegedThread(t);
	}

	public static void resetDefaultSecurityManager() {
		if (manager != null) {
			manager.restoreDefaultManager();
		}
		manager = null;
	}

	public static boolean isSecurityManagerInitialized() {
		return manager != null;
	}

	public static void goingToExecuteSUTCode() {
		if (!isSecurityManagerInitialized()) {
			return;
		}
		manager.goingToExecuteTestCase();
		PermissionStatistics.getInstance().getAndResetExceptionInfo();
	}

	public static void doneWithExecutingSUTCode() {
		if (!isSecurityManagerInitialized()) {
			return;
		}
		manager.goingToEndTestCase();
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
