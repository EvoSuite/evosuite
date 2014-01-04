package org.evosuite.mock.java.io;

import java.io.File;

import org.junit.Assert;

import org.junit.Test;

public class MockFileTest {

	@Test
	public void testSamePath(){
		
		String name = "foo.txt";
		File real = new File(name);
		MockFile mock = new MockFile(name);
		
		Assert.assertEquals(real.toString(), mock.toString());
		Assert.assertEquals(real.getPath(), mock.getPath());
	}
}
