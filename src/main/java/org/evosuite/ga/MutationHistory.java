package org.evosuite.ga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MutationHistory<T extends MutationHistoryEntry> implements Iterable<T>,
        Serializable {

	private static final long serialVersionUID = -8543180637106924913L;

	private final List<T> mutations = new ArrayList<T>();

	public void clear() {
		mutations.clear();
	}

	public void addMutationEntry(T entry) {
		mutations.add(entry);
	}

	public List<T> getMutations() {
		return Collections.unmodifiableList(mutations);
	}

	@Override
	public Iterator<T> iterator() {
		return mutations.iterator();
	}

	public int size() {
		return mutations.size();
	}

	public boolean isEmpty() {
		return mutations.isEmpty();
	}

	public void set(MutationHistory<T> other) {
		mutations.addAll(other.getMutations());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "";
		for (T t : mutations)
			result += t.toString() + "\n";

		return result;
	}
}
