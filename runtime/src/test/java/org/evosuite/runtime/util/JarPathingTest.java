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
package org.evosuite.runtime.util;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 21/11/15.
 */
public class JarPathingTest {

    @Test
    public void testBase(){

        String first = "target"+File.separator+"classes";
        String second = "target"+File.separator+"test-classes";

        String classpath = first + File.pathSeparator + second;

        assertFalse(JarPathing.containsAPathingJar(classpath));

        String pathing = JarPathing.createJarPathing(classpath);
        assertTrue(JarPathing.isPathingJar(pathing));

        String back = JarPathing.extractCPFromPathingJar(pathing);

        assertTrue(back.contains(first));
        assertTrue(back.contains(second));
    }

    @Test
    public void testSpace() throws Exception{

        String name = " a file with     many spaces.jar";

        File file = new File(name);
        if(! file.exists()) {
            assertTrue(file.createNewFile());
            file.deleteOnExit();
        }

        String pathing = JarPathing.createJarPathing(file.getAbsolutePath());
        String back = JarPathing.extractCPFromPathingJar(pathing);

        assertTrue(back.contains(file.getName()));
    }
}