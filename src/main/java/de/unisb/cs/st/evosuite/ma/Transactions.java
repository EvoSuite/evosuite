/**
 * 
 */
package de.unisb.cs.st.evosuite.ma;

import java.util.ArrayList;

/**
 * @author Yury Pavlov
 * 
 */
public class Transactions {

	private ArrayList<Record> records = new ArrayList<Record>();
	
	private ArrayList<Record> alternativeRecords = new ArrayList<Record>();

	/**
	 * Create instance with unchangeable init values
	 * 
	 * @param testCases
	 * @param currentTC
	 */
	public Transactions(ArrayList<TCTuple> testCases, TCTuple currentTC) {
		// work here only with deep copy
		records.add(new Record(testCases));
	}

	/**
	 * Push new value in transactions and clear alternative records
	 * 
	 * @param testCases
	 * @param currTCTuple
	 */
	public void push(ArrayList<TCTuple> testCases, TCTuple currTCTuple) {
		records.add(new Record(testCases));
		alternativeRecords.clear();
	}

	/**
	 * Get prev (last) from the records and add it to alternativeRecords
	 * 
	 * @return
	 */
	public ArrayList<TCTuple> prev() {
		Record res = null;
		if (records.size() > 1) {
			// current record to alternative records
			res = records.remove(records.size() - 1);
			alternativeRecords.add(res);
			// and restore state befor last changes
			res = records.get(records.size() - 1);
		} else {
			res = records.get(0);
		}
		return res.getTestCases();
	}

	/**
	 * If alternativeRecords is't leer, then return last element to records and
	 * so on
	 * 
	 * @return
	 */
	public ArrayList<TCTuple> next() {
		if (alternativeRecords.size() > 0) {
			// return alternative last record to current and restore it state
			Record res = alternativeRecords
					.remove(alternativeRecords.size() - 1);
			records.add(res);
		}
		return records.get(records.size() - 1).getTestCases();
	}

	/**
	 * Reset records to init values. Can be undone.
	 * 
	 * @return
	 */
	public ArrayList<TCTuple> reset() {
		alternativeRecords = new ArrayList<Record>(records.subList(1,
				records.size()));
		records = new ArrayList<Record>(records.subList(0, 1));
		return records.get(0).getTestCases();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("\n--=== Begin of transactions ===--\n");
		for (Record tmprec : records) {
			res.append("\t");
			res.append(tmprec.toString());
		}
		res.append("\n -- Alternative transaction --\n");
		for (Record tmpwrec : alternativeRecords) {
			res.append("\t");
			res.append(tmpwrec.toString());
		}
		return res.toString();
	}
	
}
