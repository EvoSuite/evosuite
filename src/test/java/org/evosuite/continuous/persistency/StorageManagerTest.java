package org.evosuite.continuous.persistency;

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
}
