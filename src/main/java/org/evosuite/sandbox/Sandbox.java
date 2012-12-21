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

import java.util.ArrayList;

import org.evosuite.Properties;
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.core.util.ThreadSafePropertyEditor;

/**
 * Class which controls enabling and disabling sandbox.
 * 
 * 
 * Note: for the moment it sets a custom security manager and a mocking framework.
 * But this latter is not really used now (but we might want to use it in the future once fixed).
 * So, for the time being, this class is just a wrapper over the security manager
 * 
 * @author Andrey Tarasevich
 */
public class Sandbox {

	private static Logger logger = LoggerFactory.getLogger(Sandbox.class);
	
	/** Mock controller. */
	private static Mocks mocks = new Mocks();

	/** Array of files accessed during test generation */
	private static ArrayList<EvosuiteFile> accessedFiles = new ArrayList<EvosuiteFile>();

	/** Constant <code>lastAccessedFile</code> */
	public static EvosuiteFile lastAccessedFile = null;;

	private static MSecurityManager manager;
	
	
	/**
	 * Create and initialize security manager for SUT
	 */
	public static void initializeSecurityManagerForSUT(){
		if(manager==null){
			manager = new MSecurityManager();
			manager.makePriviligedAllCurrentThreads();			
			manager.apply();
		} else {
			logger.warn("Sandbox can be initalized only once");
		}
	}
	
	public static void addPriviligedThread(Thread t) {
		if(manager != null)
			manager.addPrivilegedThread(t);
	}
	
	public static void resetDefaultSecurityManager() {
		if(manager!=null){
			manager.restoreDefaultManager();
		}
		manager = null;
	}
	
	
	public static boolean isSecurityManagerInitialized(){
		return manager!=null; 
	}
	
	public static void goingToExecuteSUTCode(){
		if(!isSecurityManagerInitialized()){return;}
		manager.goingToExecuteTestCase();
	}
	
	public static void doneWithExecutingSUTCode(){
		if(!isSecurityManagerInitialized()){return;}
		manager.goingToEndTestCase();
	}
	
	
	public static void goingToExecuteUnsafeCodeOnSameThread() throws SecurityException, IllegalStateException {
		if(!isSecurityManagerInitialized()){return;}
		manager.goingToExecuteUnsafeCodeOnSameThread();
	}

	public static void doneWithExecutingUnsafeCodeOnSameThread() throws SecurityException, IllegalStateException {
		if(!isSecurityManagerInitialized()){return;}
		manager.doneWithExecutingUnsafeCodeOnSameThread();
	}
	

	/**
	 * Set up mocks, if mock property is true
	 */
	@Deprecated
	public static void setUpMocks() {
		if (Properties.MOCKS) {
			mocks.setUpMocks();
			accessedFiles.clear();
		}	
	}

	/**
	 * Disable all active mocks
	 */
	@Deprecated
	public static void tearDownMocks() {		
		if (Properties.MOCKS) {
			mocks.tearDownMocks();
			for (String s : PermissionStatistics.getInstance().getRecentFileReadPermissions()) {
				EvosuiteFile a = new EvosuiteFile(s, "default content");
				accessedFiles.add(a);
				lastAccessedFile = a;
			}
		}

		PermissionStatistics.getInstance().resetRecentStatistic();
	}

	/**
	 * Disable mocks. This method is used sometimes
	 * just for the sake of simplicity.
	 */
	@Deprecated
	public static void tearDownEverything() {
		tearDownMocks();
	}

	/**
	 * Checks if class is currently replaced by its mock.
	 * 
	 * @param clazz
	 *            class to check
	 * @return true if class is mocked, false otherwise
	 */
	public static boolean isClassMocked(Class<?> clazz) {
		return mocks.getClassesMocked().contains(clazz);
	}

	/**
	 * <p>
	 * canUseFileContentGeneration
	 * </p>
	 * 
	 * @return a boolean.
	 */
	@Deprecated
	public static boolean canUseFileContentGeneration() {
		if (Properties.MOCKS && Properties.SANDBOX)
			return !accessedFiles.isEmpty();
		return false;
	}

	/**
	 * <p>
	 * generateFileContent
	 * </p>
	 * 
	 * @param file
	 *            a {@link org.evosuite.sandbox.EvosuiteFile} object.
	 * @param content
	 *            a {@link java.lang.String} object.
	 */
	public static void generateFileContent(EvosuiteFile file, String content) {
		if (file == null)
			return;
		if (content == null)
			Utils.writeFile(file.getContent(), file.getFileName());
		else
			Utils.writeFile(content, file.getFileName());
	}
}
