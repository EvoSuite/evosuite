package org.evosuite.setup;

import org.evosuite.Properties;
import org.evosuite.runtime.*;
import org.evosuite.runtime.System;
import org.evosuite.runtime.testdata.FileSystemHandling;
import org.evosuite.runtime.util.SystemInUtil;
import org.evosuite.runtime.vfs.VirtualFileSystem;
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
    private volatile boolean hasAddedNetwork;

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


    private void handleNetwork(TestCase test){
        //TODO
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
