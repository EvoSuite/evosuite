package org.evosuite.mock.javax.swing;

import java.io.File;

import javax.swing.JFileChooser;


import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.java.io.MockFile;
import org.evosuite.runtime.mock.javax.swing.MockJFileChooser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MockJFileChooserTest {

	private static final boolean VFS = RuntimeSettings.useVFS;

	@Before
	public void init(){		
		RuntimeSettings.useVFS = true;
		Runtime.getInstance().resetRuntime();		
	}
	
	@After
	public void restoreProperties(){
		RuntimeSettings.useVFS = VFS;
	}
	
	@Test
	public void testGetCurrentDirectory(){
		
		JFileChooser chooser = new MockJFileChooser();
		File dir = chooser.getCurrentDirectory();
		
		Assert.assertTrue(dir.exists());
		Assert.assertTrue(dir instanceof MockFile);
	}

}
