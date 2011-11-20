package de.unisb.cs.st.evosuite.utils;

public class PassiveChangeListener<T> implements Listener<T> {

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
