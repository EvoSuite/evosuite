package de.unisb.cs.st.evosuite.junit;

import org.junit.Before;
import org.junit.BeforeClass;

public class ParentTestExample {
	protected String needed = null;
	protected static Integer value = 0;

	@BeforeClass
	public static void initializeValue() {
		value = 5;
	}

	@Before
	public void setupNeeded() {
		needed = "escape";
	}
}
