package org.evosuite.setup;

import java.io.File;

import org.evosuite.Properties;
import org.evosuite.runtime.RuntimeSettings;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestClusterGeneratorTest {

	private static final boolean defaultVFS = RuntimeSettings.useVFS;
	
	@After
	public void tearDown(){
		RuntimeSettings.useVFS = defaultVFS;
	}
	
	@Test
	public void test_checkIfCanUse_noVFS(){
		
		RuntimeSettings.useVFS = false;
		boolean canUse = TestClusterGenerator.checkIfCanUse(File.class.getCanonicalName());
		Assert.assertTrue(canUse);
	}

	@Test
	public void test_checkIfCanUse_withVFS(){
		
		RuntimeSettings.useVFS = true;
		boolean canUse = TestClusterGenerator.checkIfCanUse(File.class.getCanonicalName());
		Assert.assertFalse(canUse);
	}
}
