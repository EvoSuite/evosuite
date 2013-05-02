package com.examples.with.different.packagename.testcarver;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class GenericTest {

	@Test
	public void test() {
		ObjectWrapper wrapper = new ObjectWrapper();

		Set<Long> someSet = new HashSet<Long>();
		someSet.add(42l);

		wrapper.set(someSet);
	}
}
