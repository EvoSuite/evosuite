package org.evosuite.statistics;

public class OutputVariable<T> {
	
	private final String name;
	
	private final T value;
	
	public OutputVariable(String name, T value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return name+": "+value;
	}
}