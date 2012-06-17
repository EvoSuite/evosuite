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
package de.unisb.cs.st.evosuite.ma;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>Record</code> class implement a data structure which used by
 * {@link Transaction} class to save informations about current state (takes
 * only a test suite into account) of the manual editor.
 * 
 * @author Yury Pavlov
 */
public class Record {

	private final List<TCTuple> testCases = new ArrayList<TCTuple>();

	/**
	 * Create and save deep copy of a test suite.
	 * 
	 * @param testCases
	 *            - <code>List<{@link TCTuple}></code> is a test suite of the
	 *            manual editor from which we create deep copy.
	 */
	public Record(List<TCTuple> testCases) {
		// create clones of TC for every transaction
		ArrayList<TCTuple> newTuples = new ArrayList<TCTuple>();
		for (TCTuple tcTuple : testCases) {
			newTuples.add(tcTuple.clone());
		}
		this.testCases.addAll(newTuples);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("Record: " + this.hashCode() + "\n");
		for (TCTuple tmptct : testCases) {
			res.append("\t\t");
			res.append(tmptct.toString());
			res.append("\n");
		}
		return res.toString();
	}

	/**
	 * Retrieve the saved test suite.
	 * 
	 * @return testCases
	 */
	public ArrayList<TCTuple> getTestCases() {
		ArrayList<TCTuple> res = new ArrayList<TCTuple>();
		for (TCTuple tcTuple : testCases) {
			res.add(tcTuple.clone());
		}
		return res;
	}

}
