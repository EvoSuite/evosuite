package com.examples.with.different.packagename.testcarver;

import org.junit.Assert;
import org.junit.Test;

public class ObjectWrapperArrayTest {

	@Test
	public void test() {
		ObjectWrapper wrapper = new ObjectWrapper();
		Assert.assertNull(wrapper.get());

		Long[] someArray = new Long[] { 1l, 47l };

		wrapper.set(someArray);
		Assert.assertNotNull(wrapper.get());

		ObjectWrapper fortySeven = new ObjectWrapper();
		fortySeven.set(47l);

		Long[] foo = (Long[]) wrapper.get();

		Assert.assertTrue(foo[1].equals(fortySeven.get()));
	}

}
