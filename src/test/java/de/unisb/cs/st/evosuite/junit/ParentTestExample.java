package de.unisb.cs.st.evosuite.junit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.st.evosuite.junit.TestExample.MockingBird;

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
	
	@Ignore
	@Test
	public void test01() {
		MockingBird bird = MockingBird.create(needed);
		bird.executeCmd(value);
	}
}
