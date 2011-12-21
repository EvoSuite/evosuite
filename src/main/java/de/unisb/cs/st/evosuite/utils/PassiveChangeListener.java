package de.unisb.cs.st.evosuite.utils;

import java.io.Serializable;

public class PassiveChangeListener<T> implements Listener<T>, Serializable {

	private static final long serialVersionUID = -8661407199741916844L;

	protected boolean changed = false;

	/**
	 * Returns whether the listener received any event since the last call of
	 * this method. Resets this listener such that another call of this method
	 * without receiving an event in between will return false.
	 * 
	 * @return
	 */
	public boolean hasChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}

	@Override
	public void receiveEvent(T event) {
		changed = true;
	}

}
