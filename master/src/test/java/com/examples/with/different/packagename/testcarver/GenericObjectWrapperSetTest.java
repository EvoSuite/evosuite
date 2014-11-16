/**
 * 
 */
package com.examples.with.different.packagename.testcarver;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class GenericObjectWrapperSetTest {
	@Test
	public void test01() {
		GenericObjectWrapper<Set<Long>> wrapper = new GenericObjectWrapper<Set<Long>>();
		Assert.assertNull(wrapper.get());

		Set<Long> someSet = new HashSet<Long>();
		someSet.add(42l);
		someSet.add(47l);
		someSet.remove(42l);
		someSet.add(48l);

		wrapper.set(someSet);
		Assert.assertNotNull(wrapper.get());

		GenericObjectWrapper<Long> fortySeven = new GenericObjectWrapper<Long>();
		fortySeven.set(47l);

		Set<Long> foo = wrapper.get();

		Assert.assertTrue(foo.contains(fortySeven.get()));
	}
}
