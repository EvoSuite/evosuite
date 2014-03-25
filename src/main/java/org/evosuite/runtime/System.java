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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;

import org.evosuite.Properties;



/**
 * <p>
 * System class.
 * </p>
 * 
 * @author fraser
 */
public class System {

	private static boolean wasTimeAccessed = false;

	/**
	 * Default Java properties before we run the SUT
	 */
	private static final java.util.Properties defaultProperties  =  
			(java.util.Properties) java.lang.System.getProperties().clone();

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
	public static void restoreProperties(){
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

	public static boolean wasAnyPropertyWritten(){
		return needToRestoreProperties;
	}
	
	public static boolean handlePropertyPermission(PropertyPermission perm){
		/*
		 * we allow both writing and reading any properties. But, if SUT writes anything, then we need to re-store the values to their default. this
		 * is very important, otherwise: 1) test cases might have side effects on each other 2) SUT might change properties that are used by EvoSuite
		 */

		if(readProperties == null){
			/*
			 * this can happen when readProperties is initialized in the static body
			 * and security manager is on
			 */
			return true;
		}
		
		if (perm.getActions().contains("write")) {
			
			if(!Properties.REPLACE_CALLS){
				return false;
			}
			
			synchronized (defaultProperties) {				
				needToRestoreProperties = true;
			}
		} else {
			String var = perm.getName();
			readProperties.add(var);
		}

		return true;
	}
	
	public static Set<String> getAllPropertiesReadSoFar(){
		Set<String> copy = new LinkedHashSet<>();
		copy.addAll(readProperties);
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
	 * @param status
	 *            a int.
	 */
	public static void exit(int status) {
		wasTimeAccessed = true;

		/*
		 * TODO: Here we could handle the calls to the JVM shutdown hooks, if any is present
		 */

		throw new SystemExitException();
	}

	/** Current time returns numbers increased by 1 */
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
	
	private static Map<Integer, Integer> hashKeys = new HashMap<Integer, Integer>();
	
	public static void registerObjectForIdentityHashCode(Object o) {
		identityHashCode(o);
	}
	
	public static int identityHashCode(Object o) {
		if(o == null)
			return 0;
		
		Integer realId = java.lang.System.identityHashCode(o);
		if(!hashKeys.containsKey(realId))
			hashKeys.put(realId, hashKeys.size() + 1);
		
		return hashKeys.get(realId);
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
	 * Allow setting the time
	 * 
	 * @param time
	 *            a long.
	 */
	public static void setCurrentTimeMillis(long time) {
		currentTime = time;
	}

	/**
	 * Reset runtime to initial state
	 */
	public static void reset() {
		currentTime = 1392409281320L; // 2014-02-14, 20:21
		wasTimeAccessed = false;
		hashKeys.clear();
		restoreProperties();
		needToRestoreProperties = false;
		readProperties.clear();		
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
