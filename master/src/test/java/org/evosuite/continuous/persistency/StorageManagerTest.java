package org.evosuite.continuous.persistency;

import java.io.File;

import org.evosuite.xsd.ProjectInfo;
import org.junit.Assert;

import org.junit.Test;

public class StorageManagerTest {

	@Test
	public void testDefaultProjectInfo(){
		
		StorageManager sm = new StorageManager();
		sm.clean();
		
		try{
			ProjectInfo info = sm.getDatabaseProjectInfo();
			Assert.assertNotNull(info);
		} finally {
			sm.clean();
		}
	}
	
	
	@Test
	public void extractClassNameTest(){
		String z = File.separator;
		String base = z+"some"+z+"thing"+z;
		String packageName = "foo";
		String className = "boiade";
		String full = base+packageName+z+className+".java";
		
		StorageManager storage = new StorageManager();
		String result = storage.extractClassName(new File(base), new File(full));
				
		Assert.assertEquals(packageName+"."+className, result);
	}
}
