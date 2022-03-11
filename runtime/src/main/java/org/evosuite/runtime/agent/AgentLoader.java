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
package org.evosuite.runtime.agent;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.evosuite.runtime.util.JarPathing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


/**
 * This class is responsible to load the jar with the agent
 * definition (in its manifest) and then hook it to the current
 * running JVM
 *
 * @author arcuri
 */
public class AgentLoader {

    private static final Logger logger = LoggerFactory.getLogger(AgentLoader.class);

    private static volatile boolean alreadyLoaded = false;

    public synchronized static void loadAgent() throws RuntimeException {

        if (alreadyLoaded) {
            return;
        }

        logger.info("dynamically loading javaagent");
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        String jarFilePath = getJarPath();
        if (jarFilePath == null) {
            throw new RuntimeException("Cannot find either the compilation target folder nor the EvoSuite jar in classpath: " + System.getProperty("java.class.path"));
        } else {
            logger.info("Using JavaAgent in " + jarFilePath);
        }

        /*
         * We need to use reflection on a new instantiated ClassLoader because
         * we can make no assumption whatsoever on the class loader of AgentLoader
         *
         * TODO: it is likely that here, instead of null, we should access to an environment variable
         * to identify where tools.jar is.
         * This is because maybe there can be problems if tests generated on local machine are then
         * published on a remote Continuous Integration server
         */
        ClassLoader toolLoader = new ToolsJarLocator(null).getLoaderForToolsJar();

        logger.info("Classpath: " + System.getProperty("java.class.path"));

        try {
            logger.info("Going to attach agent to process " + pid);

            attachAgent(pid, jarFilePath, toolLoader);

        } catch (Exception e) {
            Throwable cause = e.getCause();
            String causeDescription = cause == null ? "" : " , cause " + cause.getClass() + " " + cause.getMessage();
            logger.error("Exception " + e.getClass() + ": " + e.getMessage() + causeDescription, e);
            try {
                Thread.sleep(5000);
                String msg = "Trying again to attach agent:" + jarFilePath + "\n";
                msg += "VM: " + nameOfRunningVM + "\n";
                msg += "PID: " + pid + "\n";
                logger.error(msg);

                attachAgent(pid, jarFilePath, toolLoader);

            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }

        alreadyLoaded = true;
    }

    private static void attachAgent(String pid, String jarFilePath,
                                    ClassLoader toolLoader) throws Exception {

        Class<?> string = toolLoader.loadClass("java.lang.String");

        Class<?> provider = toolLoader.loadClass("com.sun.tools.attach.spi.AttachProvider");
        Method getProviders = provider.getDeclaredMethod("providers");
        List<?> list = (List<?>) getProviders.invoke(null);
        if (list == null || list.isEmpty()) {
            String msg = "AttachProvider.providers() failed to return any provider. Tool classloader: " + toolLoader;
            throw new RuntimeException(msg);
        }
        if (list.stream().anyMatch(Objects::isNull)) {
            throw new RuntimeException("AttachProvider.providers() returned null values");
        }

        ByteBuddyAgent.attach(new File(jarFilePath), pid);
    }

    private static boolean isEvoSuiteMainJar(String path) throws IllegalArgumentException {

        if (path.endsWith("classes")) {
			/*
				we need to treat this specially:
				eg, Jenkins/Maven on Linux on a module with only tests ended up
				with not creating "target/classes" (it does on Mac though) but still putting
				it on the classpath
			 */
            return false;
        }

        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("Non-existing file " + path);
        }

        String name = file.getName();

        if (name.toLowerCase().contains("evosuite") && name.endsWith(".jar")) {
            try (JarFile jar = new JarFile(file)) {
                Manifest manifest = jar.getManifest();
                if (manifest == null) {
                    return false;
                }

                Attributes attributes = manifest.getMainAttributes();
                String agentClass = attributes.getValue("Agent-Class");
                String agent = InstrumentingAgent.class.getName(); // this is hardcoded in the pom.xml file
                if (agentClass != null && agentClass.trim().equalsIgnoreCase(agent)) {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }

        return false;
    }


    private static String getJarPath() {
        String classPath = System.getProperty("java.class.path");
        String jarFilePath = searchInAClassPath(classPath);


        if (jarFilePath == null) {
            jarFilePath = searchInCurrentClassLoaderIfUrlOne();
        }

        if (jarFilePath == null) {
            jarFilePath = searchInCurrentClassLoaderIfItProvidesClasspathAPI();
        }

        if (jarFilePath == null) {
            /*
             * this could happen in Eclipse or during test execution in Maven, and so search in compilation 'target' folder
             */
            jarFilePath = searchInFolder("target");
        }

        if (jarFilePath == null) {
            /*
             * this could happen in Eclipse or during test execution in Maven, and so search in compilation 'target' folder
             */
            /*
             * FIXME: what is this???????? Definitively the above comment
             * is just a copy&amp;paste that makes no sense here...
             */
            jarFilePath = searchInFolder("lib");
        }

        if (jarFilePath == null) {
            /*
             * nothing seems to work, so try .m2 folder
             */
            //this can really mess up things
            //jarFilePath = searchInM2();
        }

        return jarFilePath;
    }

    private static String searchInAClassPath(String classPath) {
        String[] tokens = classPath.split(File.pathSeparator);

        for (String entry : tokens) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            String jar = findEvoSuiteMainJar(entry);
            if (jar != null) {
                return jar;
            }
        }
        return null;
    }

    private static String searchInCurrentClassLoaderIfItProvidesClasspathAPI() {

        /*
            this could happen for AntClassLoader.
            Note: we cannot use instanceof here, as we do not want to add further third-party dependencies
         */

        ClassLoader loader = AgentLoader.class.getClassLoader();
        while (loader != null) {

            try {
                Method m = loader.getClass().getMethod("getClasspath");
                String classPath = (String) m.invoke(loader);
                String jar = searchInAClassPath(classPath);
                if (jar != null) {
                    return jar;
                }
            } catch (Exception e) {
                //OK, this can happen, not really an error
            }

            loader = loader.getParent();
        }

        return null;
    }

    private static String searchInCurrentClassLoaderIfUrlOne() {

        Set<URI> uris = new HashSet<>();

        ClassLoader loader = AgentLoader.class.getClassLoader();
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                URLClassLoader urlLoader = (URLClassLoader) loader;
                for (URL url : urlLoader.getURLs()) {
                    try {
                        URI uri = url.toURI();
                        uris.add(uri);

                        File file = new File(uri);
                        String path = file.getAbsolutePath();
                        String jar = findEvoSuiteMainJar(path);
                        if (jar != null) {
                            return jar;
                        }
                    } catch (Exception e) {
                        logger.error("Error while parsing URL " + url);
                    }
                }
            }

            loader = loader.getParent();
        }

        String msg = "Failed to find EvoSuite jar in current classloader. URLs of classloader:";
        for (URI uri : uris) {
            msg += "\n" + uri.toString();
        }
        logger.warn(msg);

        return null;
    }

    private static String findEvoSuiteMainJar(String path) {
        if (isEvoSuiteMainJar(path)) {
            return path;
        }
        if (JarPathing.isPathingJar(path)) {
            for (String subpath : JarPathing.extractCPFromPathingJar(path).split(File.pathSeparator)) {
                if (isEvoSuiteMainJar(subpath)) {
                    return subpath;
                }
            }
        }
        return null;
    }

    private static String searchInFolder(String folder) {
        File target = new File(folder);
        if (!target.exists()) {
            logger.debug("No target folder " + target.getAbsolutePath());
            return null;
        }

        if (!target.isDirectory()) {
            logger.debug("'target' exists, but it is not a folder");
            return null;
        }

        for (File file : target.listFiles()) {
            String path = file.getAbsolutePath();
            String jar = findEvoSuiteMainJar(path);
            if (jar != null) {
                return jar;
            }
        }

        return null;
    }
}