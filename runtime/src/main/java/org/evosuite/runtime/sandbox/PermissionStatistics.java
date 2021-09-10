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

import java.io.FilePermission;
import java.security.Permission;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * PermissionStatistics class.
 * </p>
 *
 *
 *
 * <p>
 * FIXME: This class seem directly used by the SUT, when its methods check the
 * security manager. This can lead to concurrency issues when the SUT is
 * multi-threaded. Some re-factoring might be needed, but that would need some
 * discussions first regarding its use/goals
 * <p>
 *
 * @author Gordon Fraser
 */
public class PermissionStatistics {

    private static final PermissionStatistics instance = new PermissionStatistics();

    private final Map<String, Map<String, Integer>> allowedCount;

    /**
     * Keep track of the denied exceptions. Key -> name of the permission class
     * Value -> a map from type (name+action) to counter of times it was thrown
     */
    private final Map<String, Map<String, Integer>> deniedCount;

    private final Map<Class<?>, Integer> deniedClassCount;
    private final Set<String> recentAccess;
    private int maxThreads;

    private boolean hasNewExceptions = false;


    private String threadGroupToMonitor;


    // Private constructor
    private PermissionStatistics() {
        allowedCount = new ConcurrentHashMap<>();
        deniedCount = new ConcurrentHashMap<>();
        deniedClassCount = new ConcurrentHashMap<>();
        recentAccess = Collections.synchronizedSet(new HashSet<>());
        maxThreads = 1;
    }


    /**
     * <p>
     * Getter for the field <code>instance</code>.
     * </p>
     *
     * @return a {@link org.evosuite.runtime.sandbox.PermissionStatistics} object.
     */
    public static PermissionStatistics getInstance() {
        return instance;
    }

    /**
     * <p>
     * getRecentFileReadPermissions
     * </p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getRecentFileReadPermissions() {
        return recentAccess.toArray(new String[0]);
    }

    /**
     * <p>
     * resetRecentStatistic
     * </p>
     */
    public void resetRecentStatistic() {
        recentAccess.clear();
    }

    private void rememberRecentReadFilePermissions(Permission permission) {
        try {
            FilePermission fp = (FilePermission) permission;
            if (!fp.getActions().equals("read"))
                return;
            recentAccess.add(fp.getName());
        } catch (Exception e) {
            return;
        }
    }

    /**
     * <p>
     * permissionAllowed
     * </p>
     *
     * @param permission a {@link java.security.Permission} object.
     */
    public void permissionAllowed(Permission permission) {
        rememberRecentReadFilePermissions(permission);
        String name = permission.getClass().getName();
        String type = getPermissionType(permission);
        if (!allowedCount.containsKey(name)) {
            allowedCount.put(name, new HashMap<>());
        }

        if (allowedCount.get(name).containsKey(type)) {
            allowedCount.get(name).put(type, allowedCount.get(name).get(type) + 1);
        } else {
            allowedCount.get(name).put(type, 1);
        }
    }

    private int getCurrentCount(Class<?> permissionClass) {
        if (!deniedClassCount.containsKey(permissionClass))
            deniedClassCount.put(permissionClass, 0);

        return deniedClassCount.get(permissionClass);
    }

    private void incCurrentCount(Class<?> permissionClass) {
        deniedClassCount.put(permissionClass, getCurrentCount(permissionClass) + 1);
    }

    private String getPermissionType(Permission permission) {
        String name = permission.getName();
        String actions = permission.getActions();
        String type = "";
        if (actions != null && !actions.isEmpty()) {
            type += actions + " ";
        }
        if (name != null && !name.isEmpty()) {
            type += name;
        }
        return type;
    }

    /**
     * <p>
     * permissionDenied
     * </p>
     *
     * @param permission a {@link java.security.Permission} object.
     */
    public void permissionDenied(Permission permission) {
        incCurrentCount(permission.getClass());
        rememberRecentReadFilePermissions(permission);

        String permissionClassName = permission.getClass().getName();
        String type = getPermissionType(permission);

        if (!deniedCount.containsKey(permissionClassName)) {
            deniedCount.put(permissionClassName, new HashMap<>());
        }

        if (deniedCount.get(permissionClassName).containsKey(type)) {
            deniedCount.get(permissionClassName).put(type,
                    deniedCount.get(permissionClassName).get(type) + 1);
        } else {
            deniedCount.get(permissionClassName).put(type, 1);
        }
        hasNewExceptions = true;
    }

    /**
     * Retrieve the number of times a particular permission was denied
     *
     * @param permission a {@link java.security.Permission} object.
     * @return a int.
     */
    public int getPermissionDeniedCount(Permission permission) {
        String name = permission.getClass().getName();
        String type = getPermissionType(permission);
        if (deniedCount.containsKey(name)) {
            if (deniedCount.get(name).containsKey(type)) {
                return deniedCount.get(name).get(type);
            }
        }
        return 0;
    }

    /**
     * <p>
     * getNumAllPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumAllPermission() {
        return getCurrentCount(java.security.AllPermission.class);
    }

    /**
     * <p>
     * getNumSecurityPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumSecurityPermission() {
        return getCurrentCount(java.security.SecurityPermission.class);
    }

    /**
     * <p>
     * getNumUnresolvedPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumUnresolvedPermission() {
        return getCurrentCount(java.security.UnresolvedPermission.class);
    }

    /**
     * <p>
     * getNumAWTPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumAWTPermission() {
        return getCurrentCount(java.awt.AWTPermission.class);
    }

    /**
     * <p>
     * getNumFilePermission
     * </p>
     *
     * @return a int.
     */
    public int getNumFilePermission() {
        return getCurrentCount(java.io.FilePermission.class);
    }

    /**
     * <p>
     * getNumSerializablePermission
     * </p>
     *
     * @return a int.
     */
    public int getNumSerializablePermission() {
        return getCurrentCount(java.io.SerializablePermission.class);
    }

    /**
     * <p>
     * getNumReflectPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumReflectPermission() {
        return getCurrentCount(java.lang.reflect.ReflectPermission.class);
    }

    /**
     * <p>
     * getNumRuntimePermission
     * </p>
     *
     * @return a int.
     */
    public int getNumRuntimePermission() {
        return getCurrentCount(java.lang.RuntimePermission.class);
    }

    /**
     * <p>
     * getNumNetPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumNetPermission() {
        return getCurrentCount(java.net.NetPermission.class);
    }

    /**
     * <p>
     * getNumSocketPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumSocketPermission() {
        return getCurrentCount(java.net.SocketPermission.class);
    }

    /**
     * <p>
     * getNumSQLPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumSQLPermission() {
        return getCurrentCount(java.sql.SQLPermission.class);
    }

    /**
     * <p>
     * getNumPropertyPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumPropertyPermission() {
        return getCurrentCount(java.util.PropertyPermission.class);
    }

    /**
     * <p>
     * getNumLoggingPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumLoggingPermission() {
        return getCurrentCount(java.util.logging.LoggingPermission.class);
    }

    /**
     * <p>
     * getNumSSLPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumSSLPermission() {
        return getCurrentCount(javax.net.ssl.SSLPermission.class);
    }

    /**
     * <p>
     * getNumAuthPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumAuthPermission() {
        return getCurrentCount(javax.security.auth.AuthPermission.class)
                + getCurrentCount(javax.security.auth.PrivateCredentialPermission.class)
                + getCurrentCount(javax.security.auth.kerberos.DelegationPermission.class)
                + getCurrentCount(javax.security.auth.kerberos.ServicePermission.class);
    }

    /**
     * <p>
     * getNumAudioPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumAudioPermission() {
        return getCurrentCount(javax.sound.sampled.AudioPermission.class);
    }

    /**
     * <p>
     * getNumOtherPermission
     * </p>
     *
     * @return a int.
     */
    public int getNumOtherPermission() {
        int sum = getNumAllPermission() + getNumSecurityPermission()
                + getNumUnresolvedPermission() + getNumAWTPermission()
                + getNumFilePermission() + getNumSerializablePermission()
                + getNumReflectPermission() + getNumRuntimePermission()
                + getNumNetPermission() + getNumSocketPermission()
                + getNumSQLPermission() + getNumPropertyPermission()
                + getNumLoggingPermission() + getNumSSLPermission()
                + getNumAuthPermission() + getNumAudioPermission();

        int total = 0;
        for (int i : deniedClassCount.values())
            total += i;

        return total - sum;
    }

    /**
     * <p>
     * hasDeniedPermissions
     * </p>
     *
     * @return a boolean.
     */
    public boolean hasDeniedPermissions() {
        return !deniedCount.isEmpty();
    }

    /**
     * <p>
     * printStatistics
     * </p>
     */
    public void printStatistics(Logger inputLog) {
        forcePermissionInit();
        if (hasDeniedPermissions()) {
            inputLog.info("* Permissions denied during test execution: ");
            for (String name : deniedCount.keySet()) {
                inputLog.info("  - " + name + ": ");

                /*
                 * We don't want to print all the exceptions if they are too many
                 */
                final int MAX_TO_PRINT = 4;
                int counter = 0;
                int total = deniedCount.get(name).keySet().size();
                boolean printAll = (total <= MAX_TO_PRINT);
                for (String type : deniedCount.get(name).keySet()) {
                    inputLog.info("         "
                            + type
                            + ": "
                            + deniedCount.get(name).get(type));
                    counter++;
                    if (!printAll && counter >= (MAX_TO_PRINT - 1)) {
                        break;
                    }
                }
                int remaining = total - counter;
                if (remaining > 1) {
                    inputLog.info("         and other "
                            + remaining
                            + " cases of action/name for this exception class");
                }
            }
        }
    }

    /**
     * This is needed due to caching
     */
    private void forcePermissionInit() {
        getNumAllPermission();
        getNumSecurityPermission();
        getNumUnresolvedPermission();
        getNumAWTPermission();
        getNumFilePermission();
        getNumSerializablePermission();
        getNumReflectPermission();
        getNumRuntimePermission();
        getNumNetPermission();
        getNumSocketPermission();
        getNumSQLPermission();
        getNumPropertyPermission();
        getNumLoggingPermission();
        getNumSSLPermission();
        getNumAuthPermission();
        getNumAudioPermission();
        getNumOtherPermission();
        getMaxThreads();
    }


    /**
     * Check how many threads are active, and store the maximum value seen so
     * far. Note: this is used to check if the SUT is multi-threading, but it is
     * not a 100% bullet-proof solution, as this method is only called any now
     * and then.
     *
     * @param numThreads a int.
     */
    public void countThreads(int numThreads) {
        if (threadGroupToMonitor != null && Thread.currentThread().getThreadGroup().getName().equals(threadGroupToMonitor)) {
            maxThreads = Math.max(maxThreads, numThreads);
        }
    }

    /**
     * <p>
     * Getter for the field <code>maxThreads</code>.
     * </p>
     *
     * @return a int.
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    public boolean getAndResetExceptionInfo() {
        if (hasNewExceptions) {
            hasNewExceptions = false;
            return true;
        }
        return false;
    }

    public void setThreadGroupToMonitor(String threadGroupToMonitor) {
        this.threadGroupToMonitor = threadGroupToMonitor;
    }

}
