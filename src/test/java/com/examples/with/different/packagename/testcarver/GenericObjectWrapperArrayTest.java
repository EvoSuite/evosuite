/**
 * 
 */
package com.examples.with.different.packagename.testcarver;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericObjectWrapperArrayTest {
	@Test
	public void test() {
		GenericObjectWrapper<Long[]> wrapper = new GenericObjectWrapper<Long[]>();
		Assert.assertNull(wrapper.get());

		Long[] someArray = new Long[] { 1l, 47l };

		wrapper.set(someArray);
		Assert.assertNotNull(wrapper.get());

		GenericObjectWrapper<Long> fortySeven = new GenericObjectWrapper<Long>();
		fortySeven.set(47l);

		Long[] foo = wrapper.get();

		Assert.assertTrue(foo[1].equals(fortySeven.get()));
	}
}
