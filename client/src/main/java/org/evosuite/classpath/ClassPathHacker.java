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

import org.apache.commons.lang3.SystemUtils;
import org.evosuite.Properties;
import org.evosuite.runtime.agent.ToolsJarLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>ClassPathHacker class.</p>
 *
 * @author fraser
 */
public class ClassPathHacker {

    private static final Logger logger = LoggerFactory.getLogger(ClassPathHacker.class);

    private static final Class<?>[] parameters = new Class[]{URL.class};

    private static boolean junitCheckAvailable = true;

    private static String cause = "";

    private static ClassLoader continuousClassLoader = null;

    /**
     * Locate and add to classpath the tools.jar.
     * It is important that tools.jar ends up in the classpath of the <emp>system</emp> classloader,
     * otherwise exceptions in EvoSuite classes using tools.jar
     *
     * <p>
     * If we need to activate JavaAgent (eg to handle environment in generated tests), we need
     * to be sure we can use tools.jar
     */
    public static void initializeToolJar() throws RuntimeException {
        Integer javaVersion = Integer.valueOf(SystemUtils.JAVA_VERSION.split("\\.")[0]);
        if (javaVersion >= 9) {
			/*junitCheckAvailable = false;
			cause = "Running the junit tests on Java >8 is not available yet";
			return; */
            // running junit tests only causes errors when executing the jar in IntelliJ
            return;
        }
        ToolsJarLocator locator = new ToolsJarLocator(Properties.TOOLS_JAR_LOCATION);
        locator.getLoaderForToolsJar();
        if (locator.getLocationNotOnClasspath() != null) {
            try {
                logger.info("Using JDK libraries at: " + locator.getLocationNotOnClasspath());
                addFile(locator.getLocationNotOnClasspath());  //FIXME needs refactoring
            } catch (IOException e) {
                cause = "Failed to add " + locator.getLocationNotOnClasspath() + " to system classpath";
                junitCheckAvailable = false;
                //throw new RuntimeException("Failed to add " + locator.getLocationNotOnClasspath() + " to system classpath");
                return;
            }
        }

    }

    public static String getCause() {
        return cause;
    }

    public static boolean isJunitCheckAvailable() {
        return junitCheckAvailable;
    }

    /**
     * <p>addFile</p>
     *
     * @param s a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }//end method

    /**
     * <p>addFile</p>
     *
     * @param f a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    public static void addFile(File f) throws IOException {
        //addURL(f.toURL());
        addURL(f.toURI().toURL());
    }//end method

    /**
     * <p>addURL</p>
     *
     * @param u a {@link java.net.URL} object.
     * @throws java.io.IOException if any.
     */
    public static void addURL(URL u) throws IOException {
        logger.info("Trying to add URL to class path:" + u.toString());
        ClassLoader sysloader = ClassLoader.getSystemClassLoader();
        if (sysloader instanceof URLClassLoader) {
            try {
                Class<?> sysclass = URLClassLoader.class;
                Method method = sysclass.getDeclaredMethod("addURL", parameters);
                method.setAccessible(true);
                method.invoke(sysloader, u);
            } catch (Throwable t) {
                throw new IOException("Error, could not add URL to system classloader");
            }
            logger.info("Successfully added " + u + " to class path");
        } else {
            logger.info("Did not add " + u + ", because system class loader is no URLClassLoader");
        }
    }


    public static void setupContinuousClassLoader(String cp) throws IOException {
        setupContinuousClassLoader(cp.split(File.pathSeparator));
    }

    public static void setupContinuousClassLoader(String[] cpEntries) throws IOException {
        List<URL> list = new ArrayList<>();
        for (String cpEntry : cpEntries) {
            File file = new File(cpEntry);
            URI toURI = file.toURI();
            URL toURL = toURI.toURL();
            list.add(toURL);
        }
        URL[] urls = new URL[list.size()];
        URL[] urlArray = list.toArray(urls);
        ClassLoader sysloader = ClassLoader.getSystemClassLoader();
        if (sysloader instanceof URLClassLoader) {
            try {
                for (URL url : urlArray) {
                    Class<?> sysclass = URLClassLoader.class;
                    Method method = sysclass.getDeclaredMethod("addURL", parameters);
                    method.setAccessible(true);
                    method.invoke(sysloader, url);
                }

                continuousClassLoader = sysloader;
            } catch (Throwable t) {
                throw new IOException("Error, could not add URL to system classloader");
            }
        } else {
            URLClassLoader urlClassLoader = new URLClassLoader(urlArray, sysloader);
            continuousClassLoader = urlClassLoader;
        }
    }

    /**
     * get a classLoader that can load the cuts for continuous integration
     */
    public static ClassLoader getContinuousClassLoader() {
        return continuousClassLoader != null ? continuousClassLoader : Thread.currentThread().getContextClassLoader();
    }
}
