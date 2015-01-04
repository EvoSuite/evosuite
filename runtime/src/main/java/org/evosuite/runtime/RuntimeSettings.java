package org.evosuite.runtime;

import org.evosuite.runtime.sandbox.Sandbox;

/**
 * Created by arcuri on 6/10/14.
 */
public class RuntimeSettings {

    /*
     * By default, all these properties should be false.
     *
     * WARNING If this behavior is changed, or any variable name is changed,
     * we HAVE TO update how JUnit code is generated
     */

    /**
     * Shall the test cases use the mocking framework to remove non-determinism like
     * CPU clock?
     */
    public static boolean mockJVMNonDeterminism = false;

    /**
     * Should the use of System.in be mocked?
     */
    public static boolean mockSystemIn = false;

    /**
     * Shall the test cases use a virtual file system?
     */
    public static boolean useVFS = false;

    /**
     * Shall the test cases use a virtual network?
     */
    public static boolean useVNET = false;

    /**
     * Should the static state be reset after each test execution?
     */
    public static boolean resetStaticState = false;

    /**
     * How is the sandbox configured?
     */
    public static Sandbox.SandboxMode sandboxMode = Sandbox.SandboxMode.RECOMMENDED;

    /**
     * How many threads is each test allowed to start?
     * Note: such checks depend on RuntimeSettings#mockJVMNonDeterminism
     */
    public static int maxNumberOfThreads = 100;
    
    /**
     * Should tests be executed in a separate instrumenting class loader
     * or with the standard classloader and instrumentation via an agent?
     */
    public static boolean useSeparateClassLoader = false;
}
