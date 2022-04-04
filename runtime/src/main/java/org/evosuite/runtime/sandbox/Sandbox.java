/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Class which controls enabling and disabling sandbox.
 * EvoSuite uses its own customized security manager.
 */
public class Sandbox {

    public enum SandboxMode {
        OFF, RECOMMENDED, IO
    }

    private static final Logger logger = LoggerFactory.getLogger(Sandbox.class);

    private static volatile MSecurityManager manager;

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

    private static boolean checkForInitialization = false;

    public static void setCheckForInitialization(boolean checkForInitialization) {
        Sandbox.checkForInitialization = checkForInitialization;
    }

    /**
     * Create and initialize security manager for SUT
     */
    public static synchronized void initializeSecurityManagerForSUT(Set<Thread> privileged) {
        if (manager == null) {
            manager = new MSecurityManager();

            if (privileged == null) {
                manager.makePrivilegedAllCurrentThreads();
            } else {
                for (Thread t : privileged) {
                    manager.addPrivilegedThread(t);
                }
            }

            manager.apply();
        } else {
            logger.warn("Sandbox can be initalized only once");
        }

        counter++;
    }

    /**
     * Create and initialize security manager for SUT
     */
    public static synchronized void initializeSecurityManagerForSUT() {
        initializeSecurityManagerForSUT(null);
    }

    public static void addPrivilegedThread(Thread t) {
        if (manager != null)
            manager.addPrivilegedThread(t);
    }

    /**
     * @return a set of the threads that were marked as privileged. This is useful
     * if then we want to reactivate the security manager with the same priviliged threads.
     */
    public static synchronized Set<Thread> resetDefaultSecurityManager() {

        Set<Thread> privileged = null;
        if (manager != null) {
            privileged = manager.getPrivilegedThreads();
        }

        counter--;

        if (counter == 0) {
            if (manager != null) {
                manager.restoreDefaultManager();
            }
            manager = null;
        }

        return privileged;
    }

    public static boolean isSecurityManagerInitialized() {
        return manager != null;
    }

    public static void goingToExecuteSUTCode() {
        if (!isSecurityManagerInitialized()) {
            if (checkForInitialization) {
                logger.error("Sandbox is not initialized!");
            }
            return;
        }
        manager.goingToExecuteTestCase();
        PermissionStatistics.getInstance().getAndResetExceptionInfo();
    }

    public static void doneWithExecutingSUTCode() {
        if (!isSecurityManagerInitialized()) {
            if (checkForInitialization) {
                logger.error("Sandbox is not initialized!");
            }
            return;
        }
        manager.goingToEndTestCase();
    }

    public static boolean isOnAndExecutingSUTCode() {
        if (!isSecurityManagerInitialized()) {
            return false;
        }
        return manager.isExecutingTestCase();
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


    public static boolean isSafeToExecuteSUTCode() {
        if (!isSecurityManagerInitialized()) {
            return false;
        }
        return manager.isSafeToExecuteSUTCode();
    }
}
