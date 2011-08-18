/**
 * 
 */
package de.unisb.cs.st.evosuite.sandbox;

import java.io.FilePermission;
import java.security.Permission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Gordon Fraser
 * 
 */
public class PermissionStatistics {

	private static PermissionStatistics instance;

	private final Map<String, Map<String, Integer>> allowedCount;

	private final Map<String, Map<String, Integer>> deniedCount;
	
	private final Set<String> recentAccess;

	// Private constructor
	private PermissionStatistics() {
		allowedCount = new HashMap<String, Map<String, Integer>>();
		deniedCount = new HashMap<String, Map<String, Integer>>();
		recentAccess = new HashSet<String>();
	}

	public static PermissionStatistics getInstance() {
		if (instance == null) {
			instance = new PermissionStatistics();
		}
		return instance;
	}

	public String[] getRecentFileReadPermissions(){
		return recentAccess.toArray(new String[0]);
	}
	
	public void resetRecentStatistic(){
		recentAccess.clear();
	}
	
	private void rememberRecentReadFilePermissions(Permission permission){
		try {
			FilePermission fp = (FilePermission)permission;
			if(!fp.getActions().equals("read"))
				return;
			recentAccess.add(fp.getName());
		}catch(Exception e){
			return;
		}
	}
	
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

	public void permissionDenied(Permission permission) {
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

	public boolean hasDeniedPermissions() {
		return !deniedCount.isEmpty();
	}

	public void printStatistics() {

		if (hasDeniedPermissions()) {
			System.out.println("* Permissions denied during test execution: ");
			for (String name : deniedCount.keySet()) {
				System.out.println("  - " + name + ": ");
				for (String type : deniedCount.get(name).keySet()) {
					System.out.println("         " + type + ": "
					        + deniedCount.get(name).get(type));
				}
			}
		}
	}

}
