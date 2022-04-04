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
package org.evosuite.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;

/**
 * Class used to handle some particular behaviors of GUI components in the
 * generated JUnit test files
 *
 * @author arcuri
 */
public class GuiSupport {

    private final static Logger logger = LoggerFactory.getLogger(GuiSupport.class);

    /**
     * Where the tests run in headless mode?
     */
    private static final boolean isDefaultHeadless = GraphicsEnvironment.isHeadless();

    private static final Field headless; // need reflection

    static {
        try {
            //AWT classes check GraphicsEnvironment for headless state
            headless = java.awt.GraphicsEnvironment.class.getDeclaredField("headless");
            headless.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException e) {
            //this should never happen. if it doesn't work, then all GUI tests would be messed up :(
            throw new RuntimeException("ERROR: failed to use reflection for AWT Headless state: " + e.getMessage(), e);
        }
    }

    /**
     * Set the JVM in headless mode
     */
    public static void setHeadless() {

        if (isDefaultHeadless) {
            //already headless: nothing to do
            return;
        }

        setHeadless(true);
    }

    public static void initialize() {

		/*
			Since trying Java 8, started to get weird behavior on a Linux cluster.
			Issue raises from GUI now trying to write on disk (ie due to Fonts...).
			However, that sometimes strangely fails, even though executed before any
			sandbox. It happens quite often on cluster experiments, but was not able
			to reproduce it to debug :(
			As workaround, we try here to load default file system (it would happen anyway when
			loading fonts in Java 8), but do not crash the test suite (ie throw exception here
			in this method, which is usually called from a @BeforeClass). Reason is that
			maybe not all tests will access GUI.
		 */
        try {
            FileSystems.getDefault();
        } catch (Throwable t) {
            logger.error("Failed to load default file system: " + t.getMessage());
            return;
        }

        /*
         * Force the loading of fonts.
         * This is needed because font loading in the JVM can take several seconds (done only once),
         * and that can mess up the JUnit test execution timeouts...
         */
        (new javax.swing.JButton()).getFontMetrics(new java.awt.Font(null));
    }


    /**
     * Restore the original headless setting of when the JVM was started.
     * This is necessary for when EvoSuite tests (which are in headless mode) are
     * run together with manual tests that are not headless.
     */
    public static void restoreHeadlessMode() {
        if (GraphicsEnvironment.isHeadless() && !isDefaultHeadless) {
            setHeadless(false);
        }
    }


    private static void setHeadless(boolean isHeadless) {

        //changing system property is not enough
        java.lang.System.setProperty("java.awt.headless", "" + isHeadless);

        try {
            headless.set(null, isHeadless);
        } catch (IllegalAccessException e) {
            //this should never happen. if it doesn't work, then all GUI tests would be messed up :(
            throw new RuntimeException("ERROR: failed to change AWT Headless state: " + e.getMessage(), e);
        }

    }
}
