package org.evosuite.junit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class JUnitAnalyzerTest {

	@Test
	public void testCreationOfTmpDir() throws IOException{
		
		File dir = JUnitAnalyzer.createNewTmpDir();
		Assert.assertNotNull(dir);
		Assert.assertTrue(dir.exists());
		
		FileUtils.deleteDirectory(dir);
		Assert.assertFalse(dir.exists());
	}
}
