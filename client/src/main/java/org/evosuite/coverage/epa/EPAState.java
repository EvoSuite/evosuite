package org.evosuite.coverage.epa;

public class EPAState {
	final private String name;

	public EPAState(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "EPAState{" +
				"name='" + name + '\'' +
				'}';
	}
}
