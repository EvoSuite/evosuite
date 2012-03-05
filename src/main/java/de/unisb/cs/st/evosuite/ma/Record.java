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
