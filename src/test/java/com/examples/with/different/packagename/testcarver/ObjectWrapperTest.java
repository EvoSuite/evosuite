package com.examples.with.different.packagename.testcarver;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ObjectWrapperTest {

	@Test
	public void test01(){
		ObjectWrapper wrapper = new ObjectWrapper();
		Assert.assertNull(wrapper.get());
				
		Set<Long> someSet = new HashSet<Long>();		
		someSet.add(42l);
		someSet.add(47l);
		someSet.remove(42l);
				
		wrapper.set(someSet);
		//wrapper.set(7l);
		Assert.assertNotNull(wrapper.get());	
		
		ObjectWrapper fortySeven = new ObjectWrapper();
		fortySeven.set(47l);
		
		Set foo = (Set) wrapper.get();

		Assert.assertTrue(foo.contains(fortySeven.get()));		
	}
}
