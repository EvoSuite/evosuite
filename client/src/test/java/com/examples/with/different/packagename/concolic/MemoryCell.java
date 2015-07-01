package com.examples.with.different.packagename.concolic;

public class MemoryCell {

	private final int intVal;

	public MemoryCell(int int0) {
		intVal = int0;
	}

	public MemoryCell anotherCell;

	public int getValue() {
		return intVal;
	}
}