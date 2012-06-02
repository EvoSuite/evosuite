/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.unisb.cs.st.evosuite.utils.ExternalProcessHandler;
import de.unisb.cs.st.evosuite.utils.LoggingUtils;

/**
 * @author Gordon Fraser
 * @author Andrea Arcuri
 * 
 */
public class MasterProcess {

	private static final boolean logLevelSet = LoggingUtils.checkAndSetLogLevel();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExternalProcessHandler handler = new ExternalProcessHandler();
		handler.openServer();
		int port = handler.getServerPort();
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.addAll(Arrays.asList(args));
		cmdLine.add(cmdLine.size() - 1, "-Dprocess_communication_port=" + port);
		String[] newArgs = cmdLine.toArray(new String[cmdLine.size()]);
		if (handler.startProcess(newArgs)) {
			handler.waitForResult((Properties.GLOBAL_TIMEOUT
			        + Properties.MINIMIZATION_TIMEOUT + 120) * 1000); // FIXXME: search timeout plus 100 seconds?			
		} else {
			LoggingUtils.getEvoLogger().info("* Could not connect to client process");
		}
		System.exit(0);
	}
}
