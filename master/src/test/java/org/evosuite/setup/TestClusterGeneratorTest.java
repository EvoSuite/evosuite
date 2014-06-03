package org.evosuite.setup;

import java.io.File;

import org.evosuite.Properties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestClusterGeneratorTest {

	private static final boolean defaultVFS = Properties.VIRTUAL_FS;
	
	@After
	public void tearDown(){
		Properties.VIRTUAL_FS = defaultVFS;
	}
	
	@Test
	public void test_checkIfCanUse_noVFS(){
		
		Properties.VIRTUAL_FS = false;
		boolean canUse = TestClusterGenerator.checkIfCanUse(File.class.getCanonicalName());
		Assert.assertTrue(canUse);
	}

	@Test
	public void test_checkIfCanUse_withVFS(){
		
		Properties.VIRTUAL_FS = true;
		boolean canUse = TestClusterGenerator.checkIfCanUse(File.class.getCanonicalName());
		Assert.assertFalse(canUse);
	}
}
