/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.sandbox;

import java.io.FilePermission;
import java.security.Permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestRunnable;

/**
 * Mocked Security Manager, which forbids any access to I/O, network, etc.
 * 
 * @author Andrey Tarasevich
 * 
 */
class MSecurityManager extends SecurityManager {
	/** package under test */
	private final String testPackage = Properties.PROJECT_PREFIX;

	/** indicates if mocks are enabled */
	private final boolean mocksEnabled = Properties.MOCKS;

	private static Logger logger = LoggerFactory.getLogger(MSecurityManager.class);

	private final PermissionStatistics statistics = PermissionStatistics.getInstance();

	/**
	 * Overridden method for checking permissions for any operation.
	 */
	@Override
	public void checkPermission(Permission perm) {
		// check access  
		if (!allowPermission(perm)) {
			statistics.permissionDenied(perm);
			String stack = "\n";
			for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
				stack += e + "\n";
			}
			logger.info("Security manager blocks permission " + perm + stack);
			throw new SecurityException("Security manager blocks " + perm + stack);
		} else {
			statistics.permissionAllowed(perm);
		}

		return;
	}

	/**
	 * Method for checking if requested access, specified by the given
	 * permission, is permitted.
	 * 
	 * @param perm
	 *            permission for which the security manager is asked
	 * @return false if access is forbidden, true otherwise
	 */
	private boolean allowPermission(Permission perm) {
		// get all elements of the stack trace for the current thread 
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

		// false if "executeTestCase" method wasn't in a stack trace, true otherwise
		boolean testExec = false;

		// iterate through all elements and check if name of the calling class contains 
		// the name of the class under test or "executeTestCase" method call.
		// Also check for few special cases, when permission should be granted
		for (int elementCounter = 0; elementCounter < stackTraceElements.length; elementCounter++) {
			StackTraceElement e = stackTraceElements[elementCounter];

			String[] packageNameSplit = e.getClassName().split("\\.");
			String packageName = "";
			if (packageNameSplit.length > 1) {
				packageName = packageNameSplit[0];
				for (int i = 1; i < packageNameSplit.length - 1; i++)
					packageName += "." + packageNameSplit[i];
			}

			if (e.getMethodName().equals("executeTestCase")
			        || e.getMethodName().equals("call")
			        || (packageName.equals(testPackage) && !testPackage.equals(""))
			        || e.getClassName().equals(TestRunnable.class.getName())) {
				testExec = true;
				break;
			}

			if (e.getMethodName().equals("setSecurityManager")) {
				if (stackTraceElements[elementCounter + 1].getMethodName().equals("tearDownMockedSecurityManager")
				        || stackTraceElements[elementCounter + 1].getMethodName().equals("setUpMockedSecurityManager"))
					return true;
				else if (stackTraceElements[elementCounter + 1].getMethodName().equals("call"))
					return true;
			}

			if (e.getMethodName().equals("setOut") || e.getMethodName().equals("setErr"))
				if (stackTraceElements[elementCounter + 1].getMethodName().equals("execute"))
					return true;
			if (e.getClassName().contains("MockingBridge") && this.mocksEnabled)
				return true;
		}

		// if permission was asked during test case execution, then check permission itself
		if (testExec) {
			String permName = perm.getClass().getCanonicalName();

			PermissionStatistics.getInstance().countThreads(Thread.currentThread().getThreadGroup().activeCount());
			// Check for allowed permissions.
			// Done with chunk of ugly "if-case" code, since it switch statement does not
			// support Strings as parameters. Doing it through Enum is also not an option,
			// since java cannot guarantee the unique values returned by hashCode() method.
			if (permName.equals("java.lang.reflect.ReflectPermission"))
				return true;
			if (permName.equals("java.util.PropertyPermission"))
				if (perm.getActions().equals("read"))
					return true;
			if (perm.getClass().equals(java.util.logging.LoggingPermission.class)) {
				return true;
			}
			if (perm instanceof RuntimePermission) {
				if (perm.getName().startsWith("loadLibrary."))
					return true;
				if (perm.getName().equals("loadLibrary.awt"))
					return true;
				if (perm.getName().equals("loadLibrary.net"))
					return true;
			}

			//TODO: -------------------- NEED TO FIND BETTER SOLUTION ----------------------- 
			// At the moment this is the only way to allow classes under test define and load 
			// other classes, but the way it is done seriously damages security of the program.
			//
			// Oracle explains risks here
			// http://download.oracle.com/javase/6/docs/technotes/guides/security/permissions.html
			if (permName.equals("java.lang.RuntimePermission")) {
				if (perm.getName().equals("getClassLoader")
				        || perm.getName().equals("createClassLoader")
				        || perm.getName().contains("accessClassInPackage")
				        || perm.getName().equals("setContextClassLoader"))
					return true;
				if (perm.getName().equals("accessDeclaredMembers"))
					return true;
			}

			if (permName.equals("java.io.FilePermission")) {

				// check if we try to access sandbox folder. In that case allow.
				//TODO: -------------------- VERY VERY V-E-R-Y DANGEROUS -----------------------
				// Assume some malicious code was written to the sandbox folder, then it was compiled 
				// and executed during test execution. Then we have problems. Once again. PROBLEMS!!!
				// But I leave this as temporary solution since test generated inside chroot environment
				// and let's hope nothing will go wrong.  
				FilePermission fp = (FilePermission) perm;
				if (fp.getName().contains(Properties.SANDBOX_FOLDER))
					return true;

				if (perm.getActions().equals("read")
				        && fp.getName().endsWith(".properties"))
					return true;

				if (perm.getActions().equals("read"))
					for (StackTraceElement e : stackTraceElements) {
						if (e.getClassName().startsWith("java.net.URLClassLoader"))
							return true;
						if (e.getClassName().startsWith("java.lang.ClassLoader"))
							return true;
					}
			}

			return false;
		}
		return true;
	}
}
