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


import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.java.lang.MockThread;
import org.evosuite.runtime.mock.java.util.MockLocale;
import org.evosuite.runtime.mock.java.util.MockTimeZone;
import org.evosuite.runtime.thread.ThreadCounter;
import org.evosuite.runtime.vfs.VirtualFileSystem;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Runtime class.
 * </p>
 * 
 * @author Gordon Fraser
 * @author Daniel Muth
 */
public class Runtime {

	private static final Logger logger = LoggerFactory.getLogger(Runtime.class);

	private static final Runtime singleton = new Runtime();


	protected Runtime(){		
	}

	public synchronized static Runtime getInstance(){
		return singleton;
	}

	public synchronized static void resetSingleton(){
		singleton.resetRuntime();
	}

	/**
	 * Resets all simulated classes to an initial default state (so that it
	 * seems they have never been used by previous test case executions)
	 * 
	 */
	public void resetRuntime() {

		MockFramework.enable();

		/*
		 * TODO: If the setting of mockJVMNonDeterminism changes
		 *       at runtime, then the MethodCallReplacementCache
		 *       would need to be reset.
		 */
		if (RuntimeSettings.mockJVMNonDeterminism) {			
			Random.reset();
			System.resetRuntime();
            MockThread.reset();
            ThreadCounter.getInstance().resetSingleton();
            MockTimeZone.reset();
            MockLocale.reset();
        }

		if (RuntimeSettings.useVFS) {
			VirtualFileSystem.getInstance().resetSingleton();
			VirtualFileSystem.getInstance().init();
		}

        if(RuntimeSettings.useVNET){
            VirtualNetwork.getInstance().reset();
            VirtualNetwork.getInstance().init();
        }
	}

}
