package de.unisb.cs.st.evosuite.utils;

import java.util.ArrayList;
import java.util.Collection;

public class SimpleListenable<T> implements Listenable<T> {

	protected final Collection<Listener<T>> listeners = new ArrayList<Listener<T>>();

	@Override
	public void addListener(Listener<T> listener) {
		listeners.add(listener);
	}

	@Override
	public void deleteListener(Listener<T> listener) {
		listeners.remove(listener);
	}

	public void fireEvent(T event) {
		for (Listener<T> listener : listeners) {
			listener.receiveEvent(event);
		}
	}

}
