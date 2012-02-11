package de.unisb.cs.st.evosuite.ma;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>Transactions</code> class implements the whole logic to manage
 * {@link Record}s of the manual editor.
 * 
 * @author Yury Pavlov
 */
public class Transactions {

	// To save old states of the manual editor (for reDo)
	private List<Record> records = new ArrayList<Record>();

	// For unDo
	private List<Record> alternativeRecords = new ArrayList<Record>();

	/**
	 * Creates instance with initial and unmodifiable test suite.
	 * 
	 * @param testCases
	 *            <code>List<{@link TCTupel}></code>
	 */
	public Transactions(List<TCTuple> testCases) {
		// We work here only with deep copy
		records.add(new Record(testCases));
	}

	/**
	 * Push new test suite in the transaction instance and clear alternative
	 * records.
	 * 
	 * @param testCases
	 *            <code>List<{@link TCTupel}></code>
	 */
	public void push(List<TCTuple> testCases) {
		records.add(new Record(testCases));
		alternativeRecords.clear();
	}

	/**
	 * Returns and removes last record and adds to alternativeRecords
	 * 
	 * @return <code>List<{@link TCTupel}></code>
	 */
	public List<TCTuple> prev() {
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
	 * If the {@code alternativeRecords} is't empty, then return a last
	 * {@link Record} from it to {@code records}.
	 * 
	 * @return <code>List<{@link TCTupel}></code>
	 */
	public List<TCTuple> next() {
		if (alternativeRecords.size() > 0) {
			// return alternative last record to current and restore it state
			Record res = alternativeRecords.remove(alternativeRecords.size() - 1);
			records.add(res);
		}
		return records.get(records.size() - 1).getTestCases();
	}

	/**
	 * Reset records to initial {@link Record}.
	 * 
	 * @return <code>List<{@link TCTupel}></code> - first {@link Record}
	 */
	public List<TCTuple> reset() {
		alternativeRecords = new ArrayList<Record>(records.subList(1, records.size()));
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
