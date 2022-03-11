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

package org.evosuite.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * <p>
 * System class.
 * </p>
 *
 * @author fraser
 */
public class System {

    private static final Logger logger = LoggerFactory.getLogger(System.class);

    private static boolean wasTimeAccessed = false;

    /**
     * Default Java properties before we run the SUT
     */
    private static final java.util.Properties defaultProperties;

    private static final Set<String> systemProperties;

    static {

        // We have to allow some system properties like "java.io.tmpdir",  "user.home",  "user.name"
        // as otherwise tests using the VFS at runtime might break
        // We are not including "user.dir" because that will break Jacoco and other instrumentation tools
        systemProperties = new HashSet<>(Arrays.asList("java.version", "java.vendor", "java.vendor.url", "java.home", "java.vm.specification.version", "java.vm.specification.vendor",
                "java.vm.specification.name", "java.vm.version", "java.vm.vendor", "java.vm.name", "java.specification.version", "java.specification.vendor",
                "java.specification.name", "java.class.version", "java.class.path", "java.library.path", "java.compiler", "java.ext.dirs",
                "os.name", "os.arch", "os.version", "file.separator", "path.separator", "line.separator", "java.endorsed.dirs",
                "awt.toolkit", "java.awt.graphicsenv", "java.awt.printerjob", "java.vm.info", "java.runtime.version", "java.runtime.name"));

        java.util.Properties prop = null;
        try {
            prop = (java.util.Properties) java.lang.System.getProperties().clone();
        } catch (Exception e) {
            logger.error("Error while initializing System: " + e.getMessage());
        }
        defaultProperties = prop;
    }


    /**
     * If SUT changed some properties, we need to re-set the default values
     */
    private static volatile boolean needToRestoreProperties;

    /**
     * Keep track of which System properties were read
     */
    private static final Set<String> readProperties = new LinkedHashSet<>();

    /**
     * Restore to their original values all the properties that have
     * been modified during test execution
     */
    public static void restoreProperties() {
        /*
         * The synchronization is used to avoid (if possible) a SUT thread to modify a property just immediately after we restore them. this could
         * actually happen if this method is called while a SUT thread is executing a permission check
         */
        synchronized (defaultProperties) {
            if (needToRestoreProperties) {
                java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
                needToRestoreProperties = false;
            }
        }
    }

    public static boolean wasAnyPropertyWritten() {
        return needToRestoreProperties;
    }

    public static boolean isSystemProperty(String property) {
        return systemProperties.contains(property);
    }

    public static boolean handlePropertyPermission(PropertyPermission perm) {
        /*
         * we allow both writing and reading any properties. But, if SUT writes anything, then we need to re-store the values to their default. this
         * is very important, otherwise: 1) test cases might have side effects on each other 2) SUT might change properties that are used by EvoSuite
         */

        if (readProperties == null) {
            /*
             * this can happen when readProperties is initialized in the static body
             * and security manager is on
             */
            return true;
        }

        if (perm.getActions().contains("write")) {

            if (!RuntimeSettings.mockJVMNonDeterminism) {
                // We cannot restore these properties to ensure cross-OS compatibility, so they can't be written to
                return !isSystemProperty(perm.getName());
            } else {
                // As we do not restore these properties to ensure cross-OS compatibility they should not be written to
                // ...but that would break compatibility with exiting test sets, so ignoring it for now.
                // This risks potentially unstable tests.
                //
                // if(isSystemProperty(perm.getName())) {
                //	return false;
                // }
            }

            synchronized (defaultProperties) {
                needToRestoreProperties = true;
            }
        } else {
            String var = perm.getName();
            // We cannot restore these properties to ensure cross-OS compatibility, so they can't be written to
            if (!isSystemProperty(var)) {
                synchronized (readProperties) {
                    readProperties.add(var);
                }
            }
        }

        return true;
    }

    public static Set<String> getAllPropertiesReadSoFar() {
        Set<String> copy;

        synchronized (readProperties) {
            copy = new LinkedHashSet<>(readProperties);
        }

        return copy;
    }

    /**
     * <p >
     * This exception tells the test execution that it should stop at this point
     * </p>
     *
     * <p>
     * Note that it extends {@code Error}, as we need something that is
     * unchecked
     * </p>
     */
    public static class SystemExitException extends Error {

        private static final long serialVersionUID = 1L;

    }

    /**
     * Replacement function for System.exit
     *
     * @param status a int.
     */
    public static void exit(int status) {
        wasTimeAccessed = true;

        /*
         * TODO: Here we could handle the calls to the JVM shutdown hooks, if any is present
         */

        throw new SystemExitException();
    }

    /**
     * Current time returns numbers increased by 1
     */
    // Initialised to 2014-02-14, 20:21
    private static long currentTime = 1392409281320L;

    /**
     * Replacement function for System.currentTimeMillis
     *
     * @return a long.
     */
    public static long currentTimeMillis() {
        wasTimeAccessed = true;
        return currentTime; //++;
    }

    /**
     * Get time without modifying whether the time was accessed.
     * This is important as otherwise the use of VFS would always
     * mark the time as accessed
     *
     * @return
     */
    public static long getCurrentTimeMillisForVFS() {
        //wasTimeAccessed = true;
        return currentTime; //++;
    }

    private static final Map<Integer, Integer> hashKeys = new HashMap<>();

    public static void registerObjectForIdentityHashCode(Object o) {
        identityHashCode(o);
    }

    public static int identityHashCode(Object o) {
        if (o == null)
            return 0;

        synchronized (hashKeys) {
            Integer realId = java.lang.System.identityHashCode(o);
            if (!hashKeys.containsKey(realId))
                hashKeys.put(realId, hashKeys.size() + 1);

            return hashKeys.get(realId);
        }
    }

    public static String toString(Object o) {
        if (o == null)
            throw new NullPointerException();

        return o.getClass().getName() + "@" + String.format("%010d", identityHashCode(o));
    }

    /**
     * Replacement function for System.currentTimeMillis
     *
     * @return a long.
     */
    public static long nanoTime() {
        wasTimeAccessed = true;
        return currentTime * 1000; //++;
    }

    /**
     * Replacement function for for Runtime.freeMemory()
     *
     * @return
     */
    public static long freeMemory() {
        return 0L;
    }

    /**
     * Replacement function for for Runtime.maxMemory()
     *
     * @return
     */
    public static long maxMemory() {
        return 0L;
    }

    /**
     * Replacement function for for Runtime.totalMemory()
     *
     * @return
     */
    public static long totalMemory() {
        return 0L;
    }

    /**
     * Replacement function for for Runtime.availableProcessors()
     *
     * @return
     */
    public static int availableProcessors() {
        return 0;
    }


    /**
     * Allow setting the time
     *
     * @param time a long.
     */
    public static void setCurrentTimeMillis(long time) {
        currentTime = time;
    }

    /**
     * TODO: Reflection and class initialisation may cause setSecurityManager to be called by the SUT.
     * Until this is resolved, we avoid this using instrumentation
     *
     * @param manager
     */
    public static void setSecurityManager(SecurityManager manager) {
        throw new SecurityException("Permission Denied");
    }

    /**
     * Reset runtime to initial state
     */
    public static void resetRuntime() {
        currentTime = 1392409281320L; // 2014-02-14, 20:21
        wasTimeAccessed = false;
        hashKeys.clear();
        restoreProperties();
        needToRestoreProperties = false;
        //readProperties.clear(); //we cannot reset read properties here
    }

    /**
     * Fully reset the state, not only the one related to
     * latest test case execution
     */
    public static void fullReset() {
        resetRuntime();

        /*
         * System properties have to be treated very specially because
         * we want to keep track of them when they are accessed in
         * static blocks, and also from non-instrumented classes
         */
        synchronized (readProperties) {
            readProperties.clear();
        }
    }

    /**
     * Getter to check whether the runtime replacement for time was accessed during test
     * execution
     *
     * @return a boolean.
     */
    public static boolean wasTimeAccessed() {
        return wasTimeAccessed;
    }
}
