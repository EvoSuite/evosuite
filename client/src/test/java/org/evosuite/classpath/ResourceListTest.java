/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.classpath;

import com.examples.with.different.packagename.classpath.Foo;
import com.examples.with.different.packagename.classpath.subp.SubPackageFoo;
import org.evosuite.TestGenerationContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

public class ResourceListTest {

    private static final String basePrefix = "com.examples.with.different.packagename.classpath";

    @BeforeClass
    public static void initClass() {
        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
    }

    @Before
    public void resetCache() {
        ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).resetCache();
    }

    //-------------------------------------------------------------------------------------------------

    @Test
    public void testGetPackageName() {
        Assert.assertEquals("", ResourceList.getParentPackageName(""));
        Assert.assertEquals("", ResourceList.getParentPackageName("foo"));
        Assert.assertEquals("foo", ResourceList.getParentPackageName("foo.bar"));
        Assert.assertEquals("bar.foo", ResourceList.getParentPackageName("bar.foo.evo"));
    }

    @Test
    public void testStreamFromFolder() throws Exception {
        File localFolder = new File("local_test_data" + File.separator + "aCpEntry");
        Assert.assertTrue("ERROR: file " + localFolder + " should be available on local file system", localFolder.exists());
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(localFolder.getAbsolutePath());

        String className = "foo.ExternalClass";
        InputStream stream = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClassAsStream(className);
        Assert.assertNotNull(stream);
        stream.close();
    }


    @Test
    public void testStreamFromJar() throws Exception {
        File localJar = new File("local_test_data" + File.separator + "water-simulator.jar");
        Assert.assertTrue("ERROR: file " + localJar + " should be avaialable on local file system", localJar.exists());
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(localJar.getAbsolutePath());

        String className = "simulator.DAWN";
        InputStream stream = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClassAsStream(className);
        Assert.assertNotNull(stream);
        stream.close();
    }

    @Test
    public void testHandleUnKnownJarFile() {

        File localJar = new File("local_test_data" + File.separator + "water-simulator.jar");
        Assert.assertTrue("ERROR: file " + localJar + " should be avaialable on local file system", localJar.exists());
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(localJar.getAbsolutePath());

        String prefix = "simulator";
        String target = prefix + ".DAWN";

        Assert.assertTrue("Missing: " + target, ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).hasClass(target));

        Collection<String> classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(
                ClassPathHandler.getInstance().getTargetProjectClasspath(), prefix, false);
        Assert.assertTrue(classes.contains(target));
    }

    @Test
    public void testHandleKnownJarFile() {

        File localJar = new File("local_test_data" + File.separator + "asm-all-4.2.jar");
        Assert.assertTrue("ERROR: file " + localJar + " should be avaialable on local file system", localJar.exists());
        ClassPathHandler.getInstance().addElementToTargetProjectClassPath(localJar.getAbsolutePath());

        // we use one class among the jars EvoSuite depends on
        String target = org.objectweb.asm.util.ASMifier.class.getName();
        String prefix = org.objectweb.asm.util.ASMifier.class.getPackage().getName();

        Assert.assertTrue("Missing: " + target, ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).hasClass(target));

        Collection<String> classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(
                ClassPathHandler.getInstance().getTargetProjectClasspath(), prefix, false);
        Assert.assertTrue(classes.contains(target));
    }

    @Test
    public void testHasClass() {
        Assert.assertTrue(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).hasClass(Foo.class.getName()));
        Assert.assertTrue(ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).hasClass(SubPackageFoo.class.getName()));
    }


    @Test
    public void testSubPackage() {
        Collection<String> classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(
                ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix, false);
        Assert.assertTrue(classes.contains(Foo.class.getName()));
        Assert.assertTrue(classes.contains(SubPackageFoo.class.getName()));

        classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(
                ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix + ".subp", false);
        Assert.assertFalse(classes.contains(Foo.class.getName()));
        Assert.assertTrue(classes.contains(SubPackageFoo.class.getName()));
    }

    @Test
    public void testGatherClassNoInternal() {
        Collection<String> classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(
                ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix, false);
        Assert.assertTrue(classes.contains(Foo.class.getName()));
        Assert.assertFalse(classes.contains(Foo.InternalFooClass.class.getName()));
        Assert.assertEquals(2, classes.size()); //there is also SubPFoo
    }

    @Test
    public void testGatherClassWithInternalButNoAnonymous() {
        Collection<String> classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(
                ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix, true);
        Assert.assertTrue(classes.contains(Foo.class.getName()));
        Assert.assertTrue("" + Arrays.toString(classes.toArray()), classes.contains(Foo.InternalFooClass.class.getName()));
        Assert.assertEquals(3, classes.size());//there is also SubPFoo
    }

    @Test
    public void testGatherClassWithInternalIncludingAnonymous() {
        Collection<String> classes = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(
                ClassPathHandler.getInstance().getTargetProjectClasspath(), basePrefix, true, false);
        Assert.assertTrue(classes.contains(Foo.class.getName()));
        Assert.assertTrue("" + Arrays.toString(classes.toArray()), classes.contains(Foo.InternalFooClass.class.getName()));
        Assert.assertEquals(4, classes.size());//there is also SubPFoo
    }


    @Test
    public void testLoadOfEvoSuiteTestClassesAsStream() throws IOException {
        String className = ResourceListFoo.class.getName();
        InputStream res = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClassAsStream(className);
        Assert.assertNotNull(res);
        res.close();
    }


    private class ResourceListFoo {
    }

    ;

}
