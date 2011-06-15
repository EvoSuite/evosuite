package de.unisb.cs.st.evosuite.mutation.HOM;

/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Javalanche is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * Javalanche. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.javalanche.mutation.properties.ConfigurationLocator;

/**
 * Class used by the mutation test driver at runtime to report the testcases
 * that cover a mutation.
 * 
 * 
 */
public class HOMObserver {

	private static Logger logger = Logger.getLogger(HOMObserver.class);

	/**
	 * Set of all mutations that where touched during this run.
	 */
	private static Set<Long> touchedMutations = new HashSet<Long>();

	public static final int LIMIT = ConfigurationLocator.getJavalancheConfiguration().getTimeoutInSeconds() * 1000;

	/**
	 * See public static void touch(long mutationID)
	 */
	public static final String NAME_OF_TOUCH_METHOD = "touch";

	public static List<Long> getTouched() {
		return new ArrayList<Long>(touchedMutations);
	}

	public static void resetTouched() {
		touchedMutations.clear();
	}

	// Notice that the name of this method must be saved in NAME_OF_TOUCH_METHOD
	/**
	 * This method is called by statements that are added to the mutated code.
	 * It is called every time the mutated statements get executed.
	 * 
	 * @param mutationID
	 *            the id of the mutation that is executed
	 */
	public static void touch(long mutationID) {
		touchedMutations.add(mutationID);

		logger.trace("Touched mutation: " + mutationID + " Thread " + Thread.currentThread()
				+ " loaded by class loader " + HOMObserver.class.getClassLoader());
		// + "Trace " + Util.getStackTraceString());

	}

	public static boolean wasTouched(Long id) {
		return touchedMutations.contains(id);
	}

}
