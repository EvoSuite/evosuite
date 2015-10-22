package org.evosuite;

import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Andrea Arcuri on 22/10/15.
 */
public class PackageInfoTest {

    //these are valid only when running tests on un-shaded version

    @Test
    public void testGetEvoSuitePackage() throws Exception {
        assertEquals("org.evosuite",PackageInfo.getEvoSuitePackage());
    }

    @Test
    public void testGetEvoSuitePackageWithSlash() throws Exception {
        assertEquals("org/evosuite",PackageInfo.getEvoSuitePackageWithSlash());
    }

    @Test
    public void testGetShadedPackage() throws Exception {
        assertEquals("org.evosuite.shaded",PackageInfo.getShadedPackage());
    }

    @Test
    public void testGetNameWithSlash() throws Exception {
        assertEquals("org/evosuite/PackageInfo",PackageInfo.getNameWithSlash(PackageInfo.class));
    }

    @Test
    public void testGetNameWithSlashForInnerClass() throws Exception {
        assertEquals("org/evosuite/PackageInfoTest$Foo",PackageInfo.getNameWithSlash(PackageInfoTest.Foo.class));
    }

    public static class Foo{}

}