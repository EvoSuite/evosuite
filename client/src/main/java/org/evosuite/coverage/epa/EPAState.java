package org.evosuite.coverage.epa;

import java.io.IOException;
import java.io.Serializable;

public class EPAState implements Serializable {
	
	/**
	 * This singleton represents that the object state was not possible to be determined
	 */
	public static final EPAState INVALID_OBJECT_STATE = new EPAState("INVALID_OBJECT_STATE");

	
	/**
	 * This singleton represents that the object state before the object is built
	 */
	public static final EPAState INITIAL_STATE = new EPAState("_INITIAL_STATE");

	/**
	 * 
	 */
	private static final long serialVersionUID = -3701099207253080820L;

	private final String name;

	public EPAState(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "EPAState{" + "name='" + name + '\'' + '}';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EPAState other = (EPAState) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}


}
