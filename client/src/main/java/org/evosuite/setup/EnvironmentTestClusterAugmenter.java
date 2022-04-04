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
package org.evosuite.setup;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.runtime.Random;
import org.evosuite.runtime.System;
import org.evosuite.runtime.annotation.EvoSuiteAssertionOnly;
import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.annotation.EvoSuiteExclude;
import org.evosuite.runtime.annotation.EvoSuiteInclude;
import org.evosuite.runtime.testdata.*;
import org.evosuite.runtime.util.JOptionPaneInputs;
import org.evosuite.runtime.util.JOptionPaneInputs.GUIAction;
import org.evosuite.runtime.util.SystemInUtil;
import org.evosuite.runtime.vfs.VirtualFileSystem;
import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is responsible to augment {@link org.evosuite.setup.TestCluster}
 * with search operators based on the environment the SUT interacts with
 * <p>
 * Created by arcuri on 6/10/14.
 */
public class EnvironmentTestClusterAugmenter {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentTestClusterAugmenter.class);

    private volatile boolean hasAddedRandom;
    private volatile boolean hasAddedSystem;
    private volatile boolean hasAddedFiles;
    private volatile boolean hasAddedSystemIn;

    private volatile boolean hasAddedRemoteURLs;
    private volatile boolean hasAddedUdpSupport;
    private volatile boolean hasAddedTcpListeningSupport;
    private volatile boolean hasAddedTcpRemoteSupport;

    private final TestCluster cluster;
    private TestClusterGenerator testClusterGenerator;

    /**
     * Keep track of all EvoSuite classes that have been already fully handled
     * (via recursion)
     */
    private final Set<String> handledClasses;

    public EnvironmentTestClusterAugmenter(TestCluster cluster) {
        this.cluster = cluster;
        testClusterGenerator = TestGenerationContext.getInstance().getTestClusterGenerator();
        // testClusterGenerator = new TestClusterGenerator(cluster.getInheritanceTree());
        this.handledClasses = new LinkedHashSet<>();
    }

    /**
     * <p>
     * If access to certain classes was observed at runtime, this method adds
     * test calls to the test cluster which may lead to covering more branches.
     * For example, if file access was observed, statements will be introduced
     * that perform mutations on the accessed files like content modification.
     *
     * <p>
     * (Idea by Gordon, JavaDoc written by Daniel)
     *
     * @see org.evosuite.runtime.Random
     * @see org.evosuite.runtime.System
     */
    public void handleRuntimeAccesses(TestCase test) {

        if (testClusterGenerator == null) {
            testClusterGenerator = TestGenerationContext.getInstance().getTestClusterGenerator();
            // Initialisation might not be ready yet
            if (testClusterGenerator == null)
                return;
        }

        // important, as test might have been changed since last update (eg
        // mutation)
        test.getAccessedEnvironment().clear();

        if (Properties.REPLACE_CALLS) {
            handleReplaceCalls();
        }

        if (Properties.VIRTUAL_FS) {
            handleVirtualFS(test);
        }

        if (Properties.REPLACE_SYSTEM_IN) {
            handleSystemIn();
        }

        if (Properties.REPLACE_GUI) {
            handleGUI();
        }

        if (Properties.VIRTUAL_NET) {
            handleNetwork(test);
        }
    }

    private boolean hasAddedJOptionPaneInputsForStrings = false;

    private boolean hasAddedJOptionPaneYesNoCancelSelection = false;

    private boolean hasAddedJOptionPaneYesNoSelection = false;

    private boolean hasAddedJOptionPaneOkCancelSelection = false;

    private boolean hasAddedJOptionPaneOptionSelection = false;

    private void handleGUI() {

        if (!hasAddedJOptionPaneInputsForStrings && JOptionPaneInputs.getInstance().hasDialog(GUIAction.STRING_INPUT)) {
            hasAddedJOptionPaneInputsForStrings = true;

            try {
                final Class<?> clazz = JOptionPaneInputs.class;
                final String ENQUEUE_INPUT_STRING = "enqueueInputString";
                final Method method_to_call = clazz.getMethod(ENQUEUE_INPUT_STRING, String.class);
                final GenericClass<?> genericClass = GenericClassFactory.get(clazz);
                final GenericMethod genericMethod = new GenericMethod(method_to_call, genericClass);

                // adds JOptionPaneInputs.enqueueString() to the palette of
                // methods that can be used
                TestCluster.getInstance().addEnvironmentTestCall(genericMethod);

            } catch (SecurityException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            }
        }

        if (!hasAddedJOptionPaneYesNoCancelSelection
                && JOptionPaneInputs.getInstance().hasDialog(GUIAction.YES_NO_CANCEL_SELECTION)) {
            hasAddedJOptionPaneYesNoCancelSelection = true;

            try {
                final Class<?> clazz = JOptionPaneInputs.class;
                final String ENQUEUE_YES_NO_CANCEL_SELECTION = "enqueueYesNoCancelSelection";
                final Method method_to_call = clazz.getMethod(ENQUEUE_YES_NO_CANCEL_SELECTION,
                        int.class);
                final GenericClass<?> genericClass = GenericClassFactory.get(clazz);
                final GenericMethod genericMethod = new GenericMethod(method_to_call, genericClass);

                // adds JOptionPaneInputs.enqueueString() to the palette of
                // methods that can be used
                TestCluster.getInstance().addEnvironmentTestCall(genericMethod);

            } catch (SecurityException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            }
        }

        if (!hasAddedJOptionPaneYesNoSelection
                && JOptionPaneInputs.getInstance().hasDialog(GUIAction.YES_NO_SELECTION)) {
            hasAddedJOptionPaneYesNoSelection = true;

            try {
                final Class<?> clazz = JOptionPaneInputs.class;
                final String ENQUEUE_YES_NO_SELECTION = "enqueueYesNoSelection";
                final Method method_to_call = clazz.getMethod(ENQUEUE_YES_NO_SELECTION, int.class);
                final GenericClass<?> genericClass = GenericClassFactory.get(clazz);
                final GenericMethod genericMethod = new GenericMethod(method_to_call, genericClass);

                // adds JOptionPaneInputs.enqueueString() to the palette of
                // methods that can be used
                TestCluster.getInstance().addEnvironmentTestCall(genericMethod);

            } catch (SecurityException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            }
        }

        if (!hasAddedJOptionPaneOkCancelSelection
                && JOptionPaneInputs.getInstance().hasDialog(GUIAction.OK_CANCEL_SELECTION)) {
            hasAddedJOptionPaneOkCancelSelection = true;

            try {
                final Class<?> clazz = JOptionPaneInputs.class;
                final String ENQUEUE_OK_CANCEL_SELECTION = "enqueueOkCancelSelection";
                final Method method_to_call = clazz.getMethod(ENQUEUE_OK_CANCEL_SELECTION,
                        int.class);
                final GenericClass<?> genericClass = GenericClassFactory.get(clazz);
                final GenericMethod genericMethod = new GenericMethod(method_to_call, genericClass);

                // adds JOptionPaneInputs.enqueueString() to the palette of
                // methods that can be used
                TestCluster.getInstance().addEnvironmentTestCall(genericMethod);

            } catch (SecurityException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            }
        }

        if (!hasAddedJOptionPaneOptionSelection
                && JOptionPaneInputs.getInstance().hasDialog(GUIAction.OPTION_SELECTION)) {
            hasAddedJOptionPaneOptionSelection = true;

            try {
                final Class<?> clazz = JOptionPaneInputs.class;
                final String ENQUEUE_OPTION_SELECTION = "enqueueOptionSelection";
                final Method method_to_call = clazz.getMethod(ENQUEUE_OPTION_SELECTION, int.class);
                final GenericClass<?> genericClass = GenericClassFactory.get(clazz);
                final GenericMethod genericMethod = new GenericMethod(method_to_call, genericClass);

                // adds JOptionPaneInputs.enqueueString() to the palette of
                // methods that can be used
                TestCluster.getInstance().addEnvironmentTestCall(genericMethod);

            } catch (SecurityException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            }
        }

    }

    /**
     * Add the given klass to the test cluster. Also recursively add (as
     * modifiers) all the other EvoSuite classes for which the given class is a
     * generator
     *
     * @param klass
     */
    private boolean addEnvironmentClassToCluster(Class<?> klass) {
        if (handledClasses.contains(klass.getCanonicalName()) || !TestClusterUtils.isEvoSuiteClass(klass)) {
            return false; // already handled, or not valid
        }
        handledClasses.add(klass.getCanonicalName());

        boolean excludeClass = klass.getAnnotation(EvoSuiteClassExclude.class) != null;

        // only consider public constructors/methods

        for (Constructor<?> c : klass.getConstructors()) {
            // first check if it should be skipped
            if (shouldSkip(excludeClass, c)) {
                continue;
            }

            GenericAccessibleObject<?> gc = new GenericConstructor(c, klass);
            TestCluster.getInstance().addEnvironmentTestCall(gc);
            GenericClass<?> genclass = GenericClassFactory.get(klass);
            TestCluster.getInstance().invalidateGeneratorCache(genclass);
            TestCluster.getInstance().addGenerator(genclass, gc);

            testClusterGenerator.addNewDependencies(Arrays.asList(c.getParameterTypes()));
        }

        for (Method m : klass.getMethods()) {
            if (shouldSkip(excludeClass, m)) {
                continue;
            }

            GenericAccessibleObject<?> gm = new GenericMethod(m, klass);
            TestCluster.getInstance().addEnvironmentTestCall(gm);

            testClusterGenerator.addNewDependencies(Arrays.asList(m.getParameterTypes()));

            Class<?> returnType = m.getReturnType();
            if (!returnType.equals(Void.TYPE)) {
                GenericClass<?> genclass = GenericClassFactory.get(returnType);
                TestCluster.getInstance().invalidateGeneratorCache(genclass);
                TestCluster.getInstance().addGenerator(genclass, gm);
                addEnvironmentDependency(returnType);
            }
        }

        return true;
    }

    private void addEnvironmentDependency(Class<?> klass) {
        if (handledClasses.contains(klass.getCanonicalName()) || !TestClusterUtils.isEvoSuiteClass(klass)) {
            return; // already handled, or not valid
        }

        handledClasses.add(klass.getCanonicalName());
        boolean excludeClass = klass.getAnnotation(EvoSuiteClassExclude.class) != null;
        // do not consider constructors

        for (Method m : klass.getMethods()) {
            if (shouldSkip(excludeClass, m)) {
                continue;
            }

            GenericAccessibleObject<?> gm = new GenericMethod(m, klass);
            GenericClass<?> gc = GenericClassFactory.get(klass);
            TestCluster.getInstance().addModifier(gc, gm);

            testClusterGenerator.addNewDependencies(Arrays.asList(m.getParameterTypes()));

            Class<?> returnType = m.getReturnType();

            if (!returnType.equals(Void.TYPE)) {
                GenericClass<?> genclass = GenericClassFactory.get(returnType);
                TestCluster.getInstance().invalidateGeneratorCache(genclass);
                TestCluster.getInstance().addGenerator(genclass, gm);
                addEnvironmentDependency(returnType);
            }
        }
    }

    private boolean isObjectMethod(AccessibleObject ao) {
        if (!(ao instanceof Method)) {
            return false;
        }

        /*
         * Note: this check is not 100% precise (one could have new completely
         * unrelated method with same name from Object but different signature).
         * However, as we only apply it on EvoSuite methods, should be fine
         */

        Method m = (Method) ao;
        String name = m.getName();
        switch (name) {
            case "clone":
            case "equals":
            case "finalize":
            case "getClass":
            case "hashCode":
            case "notify":
            case "notifyAll":
            case "toString":
            case "wait":
                return true;
            default:
                return false;
        }
    }

    private boolean shouldSkip(boolean excludeClass, AccessibleObject c) {

        if (isObjectMethod(c)) {
            return true;
        }

        if (excludeClass) {
            boolean include = c.getAnnotation(EvoSuiteInclude.class) != null;
            return !include;
        } else {
            boolean exclude = c.getAnnotation(EvoSuiteExclude.class) != null
                    || c.getAnnotation(EvoSuiteAssertionOnly.class) != null;
            return exclude;
        }
    }

    private void handleNetwork(TestCase test) {
        /*
         * there are several things that are mocked in the network. based on
         * what the SUT used, we might only need a subset of methods used to
         * manipulate the mocked network
         */

        // TODO might need more stuff once we handle assertion generation

        test.getAccessedEnvironment()
                .addLocalListeningPorts(VirtualNetwork.getInstance().getViewOfLocalListeningPorts());
        test.getAccessedEnvironment().addRemoteURLs(VirtualNetwork.getInstance().getViewOfRemoteAccessedFiles());
        test.getAccessedEnvironment()
                .addRemoteContactedPorts(VirtualNetwork.getInstance().getViewOfRemoteContactedPorts());

        if (!hasAddedRemoteURLs && test.getAccessedEnvironment().getViewOfRemoteURLs().size() > 0) {
            hasAddedRemoteURLs = true;
            try {
                TestCluster.getInstance()
                        .addEnvironmentTestCall(new GenericMethod(
                                NetworkHandling.class.getMethod("createRemoteTextFile",
                                        EvoSuiteURL.class, String.class),
                                GenericClassFactory.get(NetworkHandling.class)));
            } catch (Exception e) {
                logger.error("Error while handling hasAddedRemoteURLs: " + e.getMessage(), e);
            }
        }

        boolean openedTCP = false;
        boolean openedUDP = false;

        for (EndPointInfo info : test.getAccessedEnvironment().getViewOfLocalListeningPorts()) {
            if (info.getType().equals(VirtualNetwork.ConnectionType.TCP)) {
                openedTCP = true;
            } else if (info.getType().equals(VirtualNetwork.ConnectionType.UDP)) {
                openedUDP = true;
            }
            if (openedTCP && openedUDP) {
                break;
            }
        }

        if (!hasAddedUdpSupport && openedUDP) {
            hasAddedUdpSupport = true;
            try {
                TestCluster.getInstance()
                        .addEnvironmentTestCall(
                                new GenericMethod(
                                        NetworkHandling.class.getMethod("sendUdpPacket",
                                                EvoSuiteLocalAddress.class,
                                                EvoSuiteRemoteAddress.class, byte[].class),
                                        GenericClassFactory.get(NetworkHandling.class)));
                TestCluster.getInstance()
                        .addEnvironmentTestCall(new GenericMethod(
                                NetworkHandling.class.getMethod("sendUdpPacket",
                                        EvoSuiteLocalAddress.class, byte[].class),
                                GenericClassFactory.get(NetworkHandling.class)));
            } catch (Exception e) {
                logger.error("Error while handling hasAddedUdpSupport: " + e.getMessage(), e);
            }
        }

        if (!hasAddedTcpListeningSupport && openedTCP) {
            hasAddedTcpListeningSupport = true;

            try {
                TestCluster.getInstance()
                        .addEnvironmentTestCall(new GenericMethod(
                                NetworkHandling.class.getMethod("sendDataOnTcp",
                                        EvoSuiteLocalAddress.class, byte[].class),
                                GenericClassFactory.get(NetworkHandling.class)));
                TestCluster.getInstance()
                        .addEnvironmentTestCall(new GenericMethod(
                                NetworkHandling.class.getMethod("sendMessageOnTcp",
                                        EvoSuiteLocalAddress.class, String.class),
                                GenericClassFactory.get(NetworkHandling.class)));
            } catch (Exception e) {
                logger.error("Error while handling hasAddedTcpListeningSupport: " + e.getMessage(), e);
            }
        }

        if (!hasAddedTcpRemoteSupport && test.getAccessedEnvironment().getViewOfRemoteContactedPorts().size() > 0) {
            hasAddedTcpRemoteSupport = true;

            try {
                TestCluster.getInstance()
                        .addEnvironmentTestCall(new GenericMethod(
                                NetworkHandling.class.getMethod("openRemoteTcpServer",
                                        EvoSuiteRemoteAddress.class),
                                GenericClassFactory.get(NetworkHandling.class)));
            } catch (Exception e) {
                logger.error("Error while handling hasAddedTcpRemoteSupport: " + e.getMessage(), e);
            }
        }
    }

    /**
     * If System.in was used, add methods to handle/simulate it
     */
    private void handleSystemIn() {
        if (!hasAddedSystemIn && SystemInUtil.getInstance().hasBeenUsed()) {
            hasAddedSystemIn = true;

            try {
                TestCluster.getInstance()
                        .addEnvironmentTestCall(new GenericMethod(
                                SystemInUtil.class.getMethod("addInputLine", String.class),
                                GenericClassFactory.get(SystemInUtil.class)));
            } catch (SecurityException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            }
        }
    }

    private void handleVirtualFS(TestCase test) {
        test.getAccessedEnvironment().addLocalFiles(VirtualFileSystem.getInstance().getAccessedFiles());

        if (!hasAddedFiles && VirtualFileSystem.getInstance().getAccessedFiles().size() > 0) {
            logger.info("Adding EvoSuiteFile calls to cluster");

            hasAddedFiles = true;

            addEnvironmentClassToCluster(FileSystemHandling.class);
        }
    }

    private void handleReplaceCalls() {

        if (!hasAddedRandom && Random.wasAccessed()) {
            hasAddedRandom = true;
            try {
                cluster.addTestCall(
                        new GenericMethod(Random.class.getMethod("setNextRandom", int.class),
                                GenericClassFactory.get(Random.class)));
            } catch (SecurityException | NoSuchMethodException e) {
                logger.error("Error while handling Random: " + e.getMessage(), e);
            }
        }

        if (!hasAddedSystem && org.evosuite.runtime.System.wasTimeAccessed()) {
            hasAddedSystem = true;
            try {
                cluster.addTestCall(
                        new GenericMethod(System.class.getMethod("setCurrentTimeMillis", long.class),
                                GenericClassFactory.get(System.class)));
            } catch (SecurityException e) {
                logger.error("Error while handling System: " + e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling System: " + e.getMessage(), e);
            }
        }
    }

}
