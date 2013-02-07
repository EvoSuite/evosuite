package org.evosuite.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MutationHistory<T extends MutationHistoryEntry> implements Iterable<T> {

	private List<T> mutations = new ArrayList<T>();
	
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
	
	public void set(MutationHistory<T> other) {
		mutations.addAll(other.getMutations());
	}
	
}
