package de.unisb.cs.st.evosuite.utils;

import java.io.Serializable;

/**
 * 
 * Since the {@link java.util.Listenable} is a class rather than an Interface,
 * one cannot use it in any case. Therefore we need our own implementation.
 * 
 * @author roessler
 * 
 * @param <T>
 *            The type of the event.
 */
public interface Listener<T> extends Serializable {
	void receiveEvent(T event);
}
