/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
        assertEquals("org.evosuite.shaded",PackageInfo.getShadedPackageForThirdPartyLibraries());
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