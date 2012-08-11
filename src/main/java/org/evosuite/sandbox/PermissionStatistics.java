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
package org.evosuite.sandbox;

import java.io.FilePermission;
import java.security.Permission;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.evosuite.utils.LoggingUtils;


/**
 * <p>PermissionStatistics class.</p>
 *
 *
 * 
 * <p> FIXME: This class seem directly used by the SUT, when its method check the security manager. This can lead to concurrency issues when the
 * SUT is multi-threaded. Some re-factoring might be needed, but that would need some discussions first regarding its use/goals<p> 
 *
 *
 * @author Gordon Fraser
 */
public class PermissionStatistics {

	private static PermissionStatistics instance;

	private final Map<String, Map<String, Integer>> allowedCount;
	private final Map<String, Map<String, Integer>> deniedCount;
	private final Map<Class<?>, Integer> deniedClassCount;
	private final Set<String> recentAccess;

	// Private constructor
	private PermissionStatistics() {
		allowedCount = new ConcurrentHashMap<String, Map<String, Integer>>();
		deniedCount = new ConcurrentHashMap<String, Map<String, Integer>>();
		deniedClassCount = new ConcurrentHashMap<Class<?>, Integer>();
		recentAccess = Collections.synchronizedSet(new HashSet<String>());
	}

	/**
	 * <p>Getter for the field <code>instance</code>.</p>
	 *
	 * @return a {@link org.evosuite.sandbox.PermissionStatistics} object.
	 */
	public static PermissionStatistics getInstance() {
		if (instance == null) {
			instance = new PermissionStatistics();
		}
		return instance;
	}

	/**
	 * <p>getRecentFileReadPermissions</p>
	 *
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getRecentFileReadPermissions() {
		return recentAccess.toArray(new String[0]);
	}

	/**
	 * <p>resetRecentStatistic</p>
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
	 * <p>permissionAllowed</p>
	 *
	 * @param permission a {@link java.security.Permission} object.
	 */
	public void permissionAllowed(Permission permission) {
		rememberRecentReadFilePermissions(permission);
		String name = permission.getClass().getName();
		String type = permission.getName();
		if (!allowedCount.containsKey(name)) {
			allowedCount.put(name, new HashMap<String, Integer>());
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

	/**
	 * <p>permissionDenied</p>
	 *
	 * @param permission a {@link java.security.Permission} object.
	 */
	public void permissionDenied(Permission permission) {
		incCurrentCount(permission.getClass());
		rememberRecentReadFilePermissions(permission);
		String name = permission.getClass().getName();
		String type = permission.getName();
		if (!deniedCount.containsKey(name)) {
			deniedCount.put(name, new HashMap<String, Integer>());
		}

		if (deniedCount.get(name).containsKey(type)) {
			deniedCount.get(name).put(type, deniedCount.get(name).get(type) + 1);
		} else {
			deniedCount.get(name).put(type, 1);
		}

	}

	/**
	 * Retrieve the number of times a particular permission was denied
	 *
	 * @param permission a {@link java.security.Permission} object.
	 * @return a int.
	 */
	public int getPermissionDeniedCount(Permission permission) {
		String name = permission.getClass().getName();
		String type = permission.getName();
		if (deniedCount.containsKey(name)) {
			if (deniedCount.get(name).containsKey(type)) {
				return deniedCount.get(name).get(type);
			}
		}
		return 0;
	}

	/**
	 * <p>getNumAllPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumAllPermission() {
		return getCurrentCount(java.security.AllPermission.class);
	}

	/**
	 * <p>getNumSecurityPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumSecurityPermission() {
		return getCurrentCount(java.security.SecurityPermission.class);
	}

	/**
	 * <p>getNumUnresolvedPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumUnresolvedPermission() {
		return getCurrentCount(java.security.UnresolvedPermission.class);
	}

	/**
	 * <p>getNumAWTPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumAWTPermission() {
		return getCurrentCount(java.awt.AWTPermission.class);
	}

	/**
	 * <p>getNumFilePermission</p>
	 *
	 * @return a int.
	 */
	public int getNumFilePermission() {
		return getCurrentCount(java.io.FilePermission.class);
	}

	/**
	 * <p>getNumSerializablePermission</p>
	 *
	 * @return a int.
	 */
	public int getNumSerializablePermission() {
		return getCurrentCount(java.io.SerializablePermission.class);
	}

	/**
	 * <p>getNumReflectPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumReflectPermission() {
		return getCurrentCount(java.lang.reflect.ReflectPermission.class);
	}

	/**
	 * <p>getNumRuntimePermission</p>
	 *
	 * @return a int.
	 */
	public int getNumRuntimePermission() {
		return getCurrentCount(java.lang.RuntimePermission.class);
	}

	/**
	 * <p>getNumNetPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumNetPermission() {
		return getCurrentCount(java.net.NetPermission.class);
	}

	/**
	 * <p>getNumSocketPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumSocketPermission() {
		return getCurrentCount(java.net.SocketPermission.class);
	}

	/**
	 * <p>getNumSQLPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumSQLPermission() {
		return getCurrentCount(java.sql.SQLPermission.class);
	}

	/**
	 * <p>getNumPropertyPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumPropertyPermission() {
		return getCurrentCount(java.util.PropertyPermission.class);
	}

	/**
	 * <p>getNumLoggingPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumLoggingPermission() {
		return getCurrentCount(java.util.logging.LoggingPermission.class);
	}

	/**
	 * <p>getNumSSLPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumSSLPermission() {
		return getCurrentCount(javax.net.ssl.SSLPermission.class);
	}

	/**
	 * <p>getNumAuthPermission</p>
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
	 * <p>getNumAudioPermission</p>
	 *
	 * @return a int.
	 */
	public int getNumAudioPermission() {
		return getCurrentCount(javax.sound.sampled.AudioPermission.class);
	}

	/**
	 * <p>getNumOtherPermission</p>
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
	 * <p>hasDeniedPermissions</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasDeniedPermissions() {
		return !deniedCount.isEmpty();
	}

	/**
	 * <p>printStatistics</p>
	 */
	public void printStatistics() {

		if (hasDeniedPermissions()) {
			LoggingUtils.getEvoLogger().info("* Permissions denied during test execution: ");
			for (String name : deniedCount.keySet()) {
				LoggingUtils.getEvoLogger().info("  - " + name + ": ");
				if (deniedCount.get(name).size() <= 3) {
					for (String type : deniedCount.get(name).keySet()) {
						LoggingUtils.getEvoLogger().info("         " + type + ": "
						        + deniedCount.get(name).get(type));
					}
				}
			}
		}
	}

	private int maxThreads = 1;

	/**
	 * <p>countThreads</p>
	 *
	 * @param numThreads a int.
	 */
	public void countThreads(int numThreads) {
		maxThreads = Math.max(maxThreads, numThreads);
	}

	/**
	 * <p>Getter for the field <code>maxThreads</code>.</p>
	 *
	 * @return a int.
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

}
