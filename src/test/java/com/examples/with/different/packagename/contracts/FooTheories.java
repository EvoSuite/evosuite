package com.examples.with.different.packagename.contracts;

import org.junit.Assert;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class FooTheories {

	@Theory
	public void theory(Foo foo) {
		Assert.assertTrue(foo.getX() >= 0);
	}
}
