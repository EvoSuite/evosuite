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


/**
 * Class which controls enabling and disabling sandbox.
 * 
 * @author Andrey Tarasevich
 * 
 */
public class Sandbox {

	/** Mocked security manager. */
	private static SecurityManager evilManager = new MSecurityManager();

	/** Old security manager. */
	private static SecurityManager kindManager = System.getSecurityManager();

	/** Mock controller. */
	private static Mocks mocks = new Mocks();

	/** Array of files accessed during test generation */
	private static ArrayList<EvosuiteFile> accessedFiles = new ArrayList<EvosuiteFile>();

	public static EvosuiteFile lastAccessedFile = null;;

	private static PermissionStatistics statistics = PermissionStatistics.getInstance();

	/**
	 * Set up mocked security manager if sandbox property is true.
	 */
	public static void setUpMockedSecurityManager() {
		if (Properties.SANDBOX)
			System.setSecurityManager(evilManager);
	}

	/**
	 * Bring back old security manager.
	 */
	public static void tearDownMockedSecurityManager() {
		System.setSecurityManager(kindManager);
	}

	/**
	 * Set up mocks, if mock property is true
	 */
	public static void setUpMocks() {
		if (Properties.MOCKS)
			mocks.setUpMocks();
	}

	/**
	 * Disable all active mocks
	 */
	public static void tearDownMocks() {
		mocks.tearDownMocks();
		for (String s : statistics.getRecentFileReadPermissions()) {
			EvosuiteFile a = new EvosuiteFile(s, "default content");
			accessedFiles.add(a);
			lastAccessedFile = a;
		}

		statistics.resetRecentStatistic();
	}

	/**
	 * Disable mocks and mocked security manager. This method is used sometimes
	 * just for the sake of simplicity.
	 */
	public static void tearDownEverything() {
		tearDownMockedSecurityManager();
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

	public static boolean canUseFileContentGeneration() {
		if (Properties.MOCKS && Properties.SANDBOX)
			return !accessedFiles.isEmpty();
		return false;
	}

	public static void generateFileContent(EvosuiteFile file, String content) {
		if (file == null)
			return;
		if (content == null)
			Utils.writeFile(file.getContent(), file.getFileName());
		else
			Utils.writeFile(content, file.getFileName());
	}
}
