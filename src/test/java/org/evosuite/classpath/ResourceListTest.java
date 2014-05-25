package org.evosuite.classpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.classpath.ResourceList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.classpath.Foo;
import com.examples.with.different.packagename.classpath.subp.SubPackageFoo;

public class ResourceListTest {

	private static final String basePrefix = "com.examples.with.different.packagename.classpath";
	
	@BeforeClass
	public static void initClass(){
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
	} 
		
	@Before
	public void resetCache(){
		ResourceList.resetCache();
	}
	
	//-------------------------------------------------------------------------------------------------

	@Test
	public void testGetPackageName(){
		Assert.assertEquals("", ResourceList.getParentPackageName(""));
		Assert.assertEquals("", ResourceList.getParentPackageName("foo"));
		Assert.assertEquals("foo", ResourceList.getParentPackageName("foo.bar"));
		Assert.assertEquals("bar.foo", ResourceList.getParentPackageName("bar.foo.evo"));
	}
	
	@Test
	public void testStreamFromFolder() throws Exception{
		File localFolder = new File("local_test_data"+File.separator+"aCpEntry");
		Assert.assertTrue("ERROR: file "+localFolder+" should be avaialable on local file system",localFolder.exists());
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(localFolder.getAbsolutePath());
		
		String className = "foo.ExternalClass";
		InputStream stream = ResourceList.getClassAsStream(className);
		Assert.assertNotNull(stream);
		stream.close();
	}
	
	
	@Test
	public void testStreamFromJar() throws Exception{
		File localJar = new File("local_test_data"+File.separator+"water-simulator.jar");
		Assert.assertTrue("ERROR: file "+localJar+" should be avaialable on local file system",localJar.exists());
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(localJar.getAbsolutePath());
		
		String className = "simulator.DAWN";
		InputStream stream = ResourceList.getClassAsStream(className);
		Assert.assertNotNull(stream);
		stream.close();
	}
	
	@Test
	public void testHandleUnKnownJarFile(){
		
		File localJar = new File("local_test_data"+File.separator+"water-simulator.jar");
		Assert.assertTrue("ERROR: file "+localJar+" should be avaialable on local file system",localJar.exists());
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(localJar.getAbsolutePath());
		
		String prefix = "simulator";
		String target = prefix + ".DAWN"; 
		
		Assert.assertTrue("Missing: "+target,ResourceList.hasClass(target));
		
		Collection<String> classes = ResourceList.getAllClasses(
				ClassPathHandler.getInstance().getTargetProjectClasspath(), prefix, false);
		Assert.assertTrue(classes.contains(target));
	}
	
	@Test
	public void testHandleKnownJarFile(){
		
		File localJar = new File("local_test_data"+File.separator+"asm-all-4.2.jar");
		Assert.assertTrue("ERROR: file "+localJar+" should be avaialable on local file system",localJar.exists());
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(localJar.getAbsolutePath());
		
		// we use one class among the jars EvoSuite depends on
		String target = org.objectweb.asm.util.ASMifier.class.getName();
		String prefix = org.objectweb.asm.util.ASMifier.class.getPackage().getName();
		
		Assert.assertTrue("Missing: "+target,ResourceList.hasClass(target));
		
		Collection<String> classes = ResourceList.getAllClasses(
				ClassPathHandler.getInstance().getTargetProjectClasspath(), prefix, false);
		Assert.assertTrue(classes.contains(target));
	}
	
	@Test
	public void testHasClass(){
		Assert.assertTrue(ResourceList.hasClass(Foo.class.getName()));
		Assert.assertTrue(ResourceList.hasClass(SubPackageFoo.class.getName()));		
	}
	
	
	@Test
	public void testSubPackage(){
		Collection<String> classes = ResourceList.getAllClasses(
				ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix, false);
		Assert.assertTrue(classes.contains(Foo.class.getName()));
		Assert.assertTrue(classes.contains(SubPackageFoo.class.getName()));

		classes = ResourceList.getAllClasses(
				ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix+".subp", false);
		Assert.assertTrue(! classes.contains(Foo.class.getName()));
		Assert.assertTrue(classes.contains(SubPackageFoo.class.getName()));
	}
	
	@Test
	public void testGatherClassNoAnonymous(){
		Collection<String> classes = ResourceList.getAllClasses(
				ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix, false);
		Assert.assertTrue(classes.contains(Foo.class.getName()));
		Assert.assertTrue( ! classes.contains(Foo.InternalFooClass.class.getName()));
	}
	
	@Test
	public void testGatherClassWithAnonymous(){
		Collection<String> classes = ResourceList.getAllClasses(
				ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix, true);
		Assert.assertTrue(classes.contains(Foo.class.getName()));
		Assert.assertTrue(""+Arrays.toString(classes.toArray()),classes.contains(Foo.InternalFooClass.class.getName()));
	}
	
	

	@Test
	public void testLoadOfEvoSuiteTestClassesAsStream() throws IOException {
		String className = ResourceListFoo.class.getName();
		InputStream res = ResourceList.getClassAsStream(className);
		Assert.assertNotNull(res);
		res.close();
	}

	

	private class ResourceListFoo {
	};

}
