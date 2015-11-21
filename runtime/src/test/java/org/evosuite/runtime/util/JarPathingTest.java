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