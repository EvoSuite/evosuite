package org.evosuite.mock.java.io;

import java.io.File;

import org.junit.Assert;

import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.mock.java.io.ExtendingFile;

public class ParentReplacementTest {

	private static final boolean USING_VFS = Properties.VIRTUAL_FS;
	
	@After
	public void reset(){
		Properties.VIRTUAL_FS = USING_VFS;
	}
	
	@Before
	public void init(){
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
	}
	
	@Test
	public void testNoVFS() throws ClassNotFoundException{
		
		Properties.VIRTUAL_FS = false;
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		Class<?> clazz = cl.loadClass(ExtendingFile.class.getCanonicalName());
		
		Class<?> parent = clazz.getSuperclass();
		Assert.assertEquals(File.class.getCanonicalName(), parent.getCanonicalName());
	}

	@Test
	public void testWithVFS() throws ClassNotFoundException{
		
		Properties.VIRTUAL_FS = true;
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		Class<?> clazz = cl.loadClass(ExtendingFile.class.getCanonicalName());
		
		Class<?> parent = clazz.getSuperclass();
		Assert.assertEquals(MockFile.class.getCanonicalName(), parent.getCanonicalName());
	}

}
