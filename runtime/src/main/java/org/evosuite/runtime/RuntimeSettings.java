package org.evosuite.runtime;

import org.evosuite.runtime.sandbox.Sandbox;

/**
 * Created by arcuri on 6/10/14.
 */
public class RuntimeSettings {

    /*
     * Bt default, all these properties should be false.
     * If this is changed, we also need to change how JUnit code is generated
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
     * How is the sandbox configured?
     */
    public static Sandbox.SandboxMode sandboxMode = Sandbox.SandboxMode.RECOMMENDED;
}
