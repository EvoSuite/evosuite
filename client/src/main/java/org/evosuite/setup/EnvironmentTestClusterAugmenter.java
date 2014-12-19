package org.evosuite.setup;

import org.evosuite.Properties;
import org.evosuite.runtime.*;
import org.evosuite.runtime.System;
import org.evosuite.runtime.testdata.*;
import org.evosuite.runtime.util.SystemInUtil;
import org.evosuite.runtime.vfs.VirtualFileSystem;
import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.evosuite.testcase.TestCase;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 *
 * This class is responsible to augment {@link org.evosuite.setup.TestCluster}
 * with search operators based on the environment the SUT interacts with
 *
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

    public EnvironmentTestClusterAugmenter(TestCluster cluster) {
        this.cluster = cluster;
    }

    /**
     *
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

        //important, as test might have been changed since last update (eg mutation)
        test.getAccessedEnvironment().clear();

        if (Properties.REPLACE_CALLS) {
            handleReplaceCalls();
        }

        if (Properties.VIRTUAL_FS) {
            handleVirtualFS(test);
        }

        if(Properties.REPLACE_SYSTEM_IN){
            handleSystemIn();
        }

        if(Properties.VIRTUAL_NET){
            handleNetwork(test);
        }
    }

    private void handleNetwork(TestCase test){
        /*
            there are several things that are mocked in the network.
            based on what the SUT used, we might only need a subset of
            methods used to manipulate the mocked network
         */

        // TODO might need more stuff once we handle assertion generation

        test.getAccessedEnvironment().addLocalListeningPorts(VirtualNetwork.getInstance().getViewOfLocalListeningPorts());
        test.getAccessedEnvironment().addRemoteURLs(VirtualNetwork.getInstance().getViewOfRemoteAccessedFiles());
        test.getAccessedEnvironment().addRemoteContactedPorts(VirtualNetwork.getInstance().getViewOfRemoteContactedPorts());

        if(!hasAddedRemoteURLs && test.getAccessedEnvironment().getViewOfRemoteURLs().size() > 0){
            hasAddedRemoteURLs = true;
            try {
                TestCluster.getInstance().addTestCall(new GenericMethod(
                        NetworkHandling.class.getMethod("createRemoteTextFile", new Class<?>[]{EvoSuiteURL.class,String.class}),
                        new GenericClass(NetworkHandling.class)
                ));
            } catch (Exception e){
                logger.error("Error while handling hasAddedRemoteURLs: "+e.getMessage(),e);
            }
        }

        boolean openedTCP = false;
        boolean openedUDP = false;

        for(EndPointInfo info : test.getAccessedEnvironment().getViewOfLocalListeningPorts()){
            if(info.getType().equals(VirtualNetwork.ConnectionType.TCP)){
                openedTCP = true;
            } else if(info.getType().equals(VirtualNetwork.ConnectionType.UDP)){
                openedUDP = true;
            }
            if(openedTCP && openedUDP){
                break;
            }
        }

        if(!hasAddedUdpSupport && openedUDP){
            hasAddedUdpSupport = true;
            try {
                TestCluster.getInstance().addTestCall(new GenericMethod(
                        NetworkHandling.class.getMethod("sendUdpPacket", new Class<?>[]{EvoSuiteLocalAddress.class, EvoSuiteRemoteAddress.class, byte[].class}),
                        new GenericClass(NetworkHandling.class)
                ));
                TestCluster.getInstance().addTestCall(new GenericMethod(
                        NetworkHandling.class.getMethod("sendUdpPacket", new Class<?>[]{EvoSuiteLocalAddress.class, byte[].class}),
                        new GenericClass(NetworkHandling.class)
                ));
            } catch (Exception e){
                logger.error("Error while handling hasAddedUdpSupport: "+e.getMessage(),e);
            }
        }

        if(!hasAddedTcpListeningSupport && openedTCP){
            hasAddedTcpListeningSupport = true;

            try {
                TestCluster.getInstance().addTestCall(new GenericMethod(
                        NetworkHandling.class.getMethod("sendDataOnTcp", new Class<?>[]{EvoSuiteLocalAddress.class, byte[].class}),
                        new GenericClass(NetworkHandling.class)
                ));
                TestCluster.getInstance().addTestCall(new GenericMethod(
                        NetworkHandling.class.getMethod("sendMessageOnTcp", new Class<?>[]{EvoSuiteLocalAddress.class, String.class}),
                        new GenericClass(NetworkHandling.class)
                ));
            } catch (Exception e){
                logger.error("Error while handling hasAddedTcpListeningSupport: "+e.getMessage(),e);
            }
        }

        if(!hasAddedTcpRemoteSupport && test.getAccessedEnvironment().getViewOfRemoteContactedPorts().size() > 0){
            hasAddedTcpRemoteSupport = true;

            try {
                TestCluster.getInstance().addTestCall(new GenericMethod(
                        NetworkHandling.class.getMethod("openRemoteTcpServer", new Class<?>[]{EvoSuiteRemoteAddress.class}),
                        new GenericClass(NetworkHandling.class)
                ));
            } catch (Exception e){
                logger.error("Error while handling hasAddedTcpRemoteSupport: "+e.getMessage(),e);
            }
        }
    }

    /**
     * If System.in was used, add methods to handle/simulate it
     */
    private void handleSystemIn(){
        if(!hasAddedSystemIn && SystemInUtil.getInstance().hasBeenUsed()){
            hasAddedSystemIn = true;

            try {
                TestCluster.getInstance().addTestCall(new GenericMethod(
                        SystemInUtil.class.getMethod("addInputLine",new Class<?>[] { String.class }),
                        new GenericClass(SystemInUtil.class)));
            } catch (SecurityException e) {
                logger.error("Error while handling Random: "+e.getMessage(),e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling Random: "+e.getMessage(),e);
            }
        }
    }




    private void handleVirtualFS(TestCase test) {
        test.getAccessedEnvironment().addLocalFiles(VirtualFileSystem.getInstance().getAccessedFiles());

        if (!hasAddedFiles && VirtualFileSystem.getInstance().getAccessedFiles().size() > 0) {
            logger.info("Adding EvoSuiteFile calls to cluster");

            hasAddedFiles = true;

            try {
				/*
				 * all methods in FileSystemHandling will be used in the search
				 */
                for(Method m : FileSystemHandling.class.getMethods()){
                    cluster.addTestCall(new GenericMethod(m,
                            new GenericClass(FileSystemHandling.class)));
                }
            } catch (Exception e) {
                logger.error("Error while handling virtual file system: "+e.getMessage(),e);
            }
        }
    }

    private void handleReplaceCalls() {

        if (!hasAddedRandom && Random.wasAccessed()) {
            hasAddedRandom = true;
            try {
                cluster.addTestCall(new GenericMethod(
                        Random.class.getMethod("setNextRandom",
                                new Class<?>[]{int.class}),
                        new GenericClass(
                                Random.class)));
            } catch (SecurityException e) {
                logger.error("Error while handling Random: "+e.getMessage(),e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling Random: "+e.getMessage(),e);
            }
        }

        if (!hasAddedSystem && org.evosuite.runtime.System.wasTimeAccessed()) {
            hasAddedSystem = true;
            try {
                cluster.addTestCall(new GenericMethod(
                        System.class.getMethod("setCurrentTimeMillis",
                                new Class<?>[]{long.class}),
                        new GenericClass(
                                System.class)));
            } catch (SecurityException e) {
                logger.error("Error while handling System: "+e.getMessage(),e);
            } catch (NoSuchMethodException e) {
                logger.error("Error while handling System: "+e.getMessage(),e);
            }
        }
    }


}
