package org.evosuite.mock.javax.swing;

import java.io.File;

import javax.swing.JFileChooser;

import org.evosuite.Properties;
import org.evosuite.mock.java.io.MockFile;
import org.evosuite.runtime.Runtime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MockJFileChooserTest {

	private static final boolean VFS = Properties.VIRTUAL_FS;

	@Before
	public void init(){		
		Properties.VIRTUAL_FS = true;
		Runtime.getInstance().resetRuntime();		
	}
	
	@After
	public void restoreProperties(){
		Properties.VIRTUAL_FS = VFS;
	}
	
	@Test
	public void testGetCurrentDirectory(){
		
		JFileChooser chooser = new MockJFileChooser();
		File dir = chooser.getCurrentDirectory();
		
		Assert.assertTrue(dir.exists());
		Assert.assertTrue(dir instanceof MockFile);
	}

}
