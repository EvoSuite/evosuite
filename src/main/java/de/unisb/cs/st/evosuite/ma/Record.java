/**
 * 
 */
package de.unisb.cs.st.evosuite.ma;

import java.util.ArrayList;

/**
 * @author Yury Pavlov
 *
 */
public class Record {

	private final ArrayList<TCTuple> testCases = new ArrayList<TCTuple>();
	
	public Record(ArrayList<TCTuple> testCases) {
		// create clones of TC for every transaction
		ArrayList<TCTuple> newTuples = new ArrayList<TCTuple>();
		for (TCTuple tcTuple : testCases) {
			newTuples.add(tcTuple.clone());
		}
		this.testCases.addAll(newTuples);
	}
	
	/* (non-Javadoc)
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
	 * @return the testCases
	 */
	public ArrayList<TCTuple> getTestCases() {
		return testCases;
	}
	
}
