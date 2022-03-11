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
package org.evosuite.runtime.sandbox;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.vfs.VirtualFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanPermission;
import javax.management.MBeanServerPermission;
import javax.management.MBeanTrustPermission;
import javax.management.remote.SubjectDelegationPermission;
import javax.net.ssl.SSLPermission;
import javax.security.auth.AuthPermission;
import javax.security.auth.PrivateCredentialPermission;
import javax.security.auth.kerberos.DelegationPermission;
import javax.security.auth.kerberos.ServicePermission;
import javax.sound.sampled.AudioPermission;
import javax.xml.ws.WebServicePermission;
import java.awt.*;
import java.io.File;
import java.io.FilePermission;
import java.io.SerializablePermission;
import java.lang.management.ManagementPermission;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.net.UnknownHostException;
import java.security.*;
import java.sql.SQLPermission;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.FileHandler;
import java.util.logging.LoggingPermission;

/**
 * <p>
 * Mocked Security Manager, which forbids any access to I/O, network, etc.
 * </p>
 * <p>
 * Note: this class needs to be thread safe, as it will be accessed by the SUT
 * </p>
 * <p>
 * Regarding the different permissions, and the associated risks in allowing
 * them, see:
 * http://download.oracle.com/javase/6/docs/technotes/guides/security/permissions.html
 * </p>
 * <p>
 * This class grants permissions based on thread references, and not on the
 * "context". As such it is actually more restrictive, and granting some kinds
 * of risky permissions should so be fine
 * </p>
 * <p>
 * FIXME: this class should be refactored in a way that each permission should
 * be defined in a property/configuration file. The user (through the Eclipse
 * plug-in) should be allowed to choose which permissions to allow. Current
 * settings in this class could be considered as the "default" settings.
 * </p>
 */
public class MSecurityManager extends SecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(MSecurityManager.class);


    /*
     *  these need to be "static final" as they should be determined
     *  only once before the security manager is actually on
     */

    private static final String USER_DIR = System.getProperty("user.home");

    private static final String JAVA_VERSION = System.getProperty("java.version");

    private static final String AWT_HEADLESS = System.getProperty("java.awt.headless");

    private static final String LOCALHOST_NAME;

    static {
        String tmp = null;
        try {
            tmp = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        LOCALHOST_NAME = tmp;
    }

    /**
     * Needed for the VFS
     */
    private static final File tmpFile;

    /**
     * Pattern name used by the mock of {@code java.uitl.logging.FileHandler}
     */
    public static final String FILE_HANDLER_NAME_PATTERN = ".tmp_file_needed_by_mock_of_FileHandler";

    /**
     * Set of files that will need to be deleted with a "deleteOnExit".
     * Note: we need to mark for deletion _after_ test execution, otherwise
     * we can end up in a infinite recursion.
     */
    private final Set<File> filesToDelete;

    static {
        File tmp = null;
        try {
            tmp = File.createTempFile("EvosuiteTmpFile", ".tmp");
            tmp.deleteOnExit();
        } catch (Exception e) {
            logger.error("Error while trying to create tmp file: " + e.getMessage());
        }
        tmpFile = tmp;

        /*
         * We need to force the loading of RuntimeSettings here,
         * otherwise we end up in a infinite loop when its jar
         * is accessed during the security checks
         */
        boolean forceLoading = RuntimeSettings.mockJVMNonDeterminism;
    }

    private final PermissionStatistics statistics = PermissionStatistics.getInstance();

    private final SecurityManager defaultManager;

    /**
     * Is EvoSuite executing a test case?
     */
    private volatile boolean executingTestCase;


    /**
     * Data structure containing all the (EvoSuite) threads that do not need to
     * go through the same sandbox as the SUT threads
     */
    private final Set<Thread> privilegedThreads;

    /**
     * Check whether a privileged thread should use the sandbox as for SUT code
     */
    private volatile Thread privilegedThreadToIgnore;

    /**
     * Name of all the methods in the MasterNodeRemote interface.
     * This is used to allow RMI communications even on non-privileged threads,
     * but only if coming from EvoSuite (and not from SUT)
     */
    private static Set<String> masterNodeRemoteMethodNames;

    private static boolean runningClientOnThread = false;

    /**
     * It can happen that EvoSuite encounters permissions it does not recognize.
     * This could be due to a bug in EvoSuite, or a custom permission of the SUT.
     * When an unrecognized permission is encountered, we might want to log it.
     * However, logging each single access might flood the logs
     */
    private final Set<Permission> unrecognizedPermissions;

    /**
     * Create a custom security manager for the SUT. The thread that create this
     * instance is automatically added as "privileged"
     */
    public MSecurityManager() {
        privilegedThreads = new CopyOnWriteArraySet<>();
        privilegedThreads.add(Thread.currentThread());
        defaultManager = System.getSecurityManager();
        executingTestCase = false;
        privilegedThreadToIgnore = null;
        unrecognizedPermissions = new CopyOnWriteArraySet<>();

        filesToDelete = new CopyOnWriteArraySet<>();
    }

    /**
     * We need to use reflection to avoid the runtime module to have a dependency
     * on MasterNodeRemote
     *
     * @param remoteNode
     */
    public static void setupMasterNodeRemoteHandling(Class<?> remoteNode) {
        Method[] methods = remoteNode.getMethods();
        Set<String> names = new HashSet<>();
        for (Method m : methods) {
            names.add(m.getName());
        }
        masterNodeRemoteMethodNames = Collections.unmodifiableSet(names);
    }

    public Set<Thread> getPrivilegedThreads() {
        Set<Thread> set = new LinkedHashSet<>(privilegedThreads);
        return set;
    }

    public static void setRunningClientOnThread(boolean runningClientOnThread) {
        MSecurityManager.runningClientOnThread = runningClientOnThread;
    }

    /**
     * This security manager creates one file when its class is loaded.
     * This file will be used for example by the virtual file system.
     * The file has to be created here, because creating new files
     * is prohibited by the security manager
     *
     * @return
     */
    public static File getRealTmpFile() {
        return tmpFile;
    }

    /**
     * Use this method if you are going to execute SUT code from a privileged
     * thread (ie if you don't want to do it on a new thread)
     *
     * @throws SecurityException
     * @throws IllegalStateException
     */
    public void goingToExecuteUnsafeCodeOnSameThread() throws SecurityException, IllegalStateException {
        if (!privilegedThreads.contains(Thread.currentThread())) {
            throw new SecurityException("Current thread is not privileged");
        }
        if (privilegedThreadToIgnore != null) {
            throw new IllegalStateException("The thread is already executing unsafe code");
        }
        privilegedThreadToIgnore = Thread.currentThread();
    }

    /**
     * Check if running SUT code on current thread would be done
     * inside the sandbox
     *
     * @return
     */
    public boolean isSafeToExecuteSUTCode() {
        Thread current = Thread.currentThread();
        if (!privilegedThreads.contains(current)) {
            //the thread is not privileged, so run inside the box
            return true;
        } else {
            // this can happen if the thread is privileged, but already running SUT code
            return privilegedThreadToIgnore == current;
        }
    }

    /**
     * Call after goingToExecuteUnsafeCodeOnSameThread when done with unsafe
     * code
     *
     * @throws SecurityException
     * @throws IllegalStateException
     */
    public void doneWithExecutingUnsafeCodeOnSameThread() throws SecurityException,
            IllegalStateException {
        if (!privilegedThreads.contains(Thread.currentThread())) {
            throw new SecurityException(
                    "Only a privileged thread can return from unsafe code execution");
        }
        if (privilegedThreadToIgnore == null) {
            throw new IllegalStateException("The thread was not executing unsafe code");
        }
        privilegedThreadToIgnore = null;
    }

    /**
     * <p>
     * When we start EvoSuite, quite a few other threads could start as well
     * (e.g., "Reference Handler", "Finalizer" and "Signal Dispatcher"). This is
     * a convenience method to grant permissions to all threads before starting
     * to execute test cases
     * </p>
     * <p>
     * WARNING: to use only before any SUT code has been executed. Afterwards,
     * it would not be safe (cannot really guarantee that all SUT have been
     * terminated)
     * </p>
     */
    public void makePrivilegedAllCurrentThreads() {
        ThreadGroup root = Thread.currentThread().getThreadGroup();
        while (root.getParent() != null) {
            root = root.getParent();
        }

        /*
         * As discussed in the API, this code is not 100% robust
         */
        Thread[] threads = new Thread[root.activeCount() + 10];
        root.enumerate(threads);
        for (Thread t : threads) {
            if (t != null) {
                addPrivilegedThread(t);
            }
        }
    }

    /**
     * Use this manager as security manager
     *
     * @throws IllegalStateException
     */
    public void apply() throws IllegalStateException {
        try {
            System.setSecurityManager(this);
        } catch (SecurityException e) {
            // this should never happen in EvoSuite, ie this object should be created just once
            logger.error("Cannot instantiate mock security manager", e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Note: an un-privileged thread would throw a security exception
     */
    public void restoreDefaultManager() throws SecurityException {
        System.setSecurityManager(defaultManager);
    }

    public void goingToExecuteTestCase() throws IllegalStateException {
        if (executingTestCase) {
            throw new IllegalStateException("Trying to set up the sandbox while executing a test case");
        }

        executingTestCase = true;
    }

    public boolean isExecutingTestCase() {
        return executingTestCase;
    }

    public void goingToEndTestCase() throws IllegalStateException {
        if (!executingTestCase) {
            throw new IllegalStateException("Trying to disable sandbox when not test case was run");
        }

        /*
         * it is important to call this method here as soon as the test case
         * has finished executing, because properties could be used by
         * EvoSuite as well
         */
        org.evosuite.runtime.System.restoreProperties();

        for (File file : filesToDelete) {
            file.deleteOnExit();
        }

        executingTestCase = false;
    }

    /**
     * Add a thread to the list of privileged thread. This is useful if EvoSuite
     * needs to spawn new threads that require permissions.
     *
     * @param t
     * @throws SecurityException if the thread calling this method is not privileged itself
     */
    public synchronized void addPrivilegedThread(Thread t) throws SecurityException {
        if (privilegedThreads.contains(Thread.currentThread())) {
            logger.debug("Adding privileged thread: \"" + t.getName() + "\"");
            privilegedThreads.add(t);
        } else {
            String current = Thread.currentThread().getName();
            String msg = "Unprivileged thread \"" + current + "\" cannot add a privileged thread: failed to add \"" + t.getName() + "\"";
            msg += "\nCurrent privileged threads are: ";
            for (Thread p : privilegedThreads) {
                msg += "\n\"" + p.getName() + "\"";
            }
            throw new SecurityException(msg);
        }
    }

    // ------------------------------------------------------------------------------------------

    /*
     * the following two methods are the only ones we need to override from SecurityManager, as all the other call those 2. However, if the
     * SecurityManager code will change in future JDK releases, we might need to double-check this class
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPermission(Permission perm, Object context)
            throws SecurityException, NullPointerException {

        /*
         * Note: this code is copy and paste from "super", with only one difference
         */
        if (context instanceof AccessControlContext) {
            checkPermission(perm); // this is the difference, i.e. we ignore context //TODO maybe check if privileged, and if so, actually use the
            // context?
        } else {
            throw new SecurityException();
        }
    }

    /**
     * {@inheritDoc} Overridden method for checking permissions for any
     * operation.
     */
    @Override
    public void checkPermission(Permission perm) throws SecurityException {
        // check access
        if (!allowPermission(perm)) {
            String stack = "\n";
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                stack += e + "\n";
            }
            if (executingTestCase) {
                /*
                 * report statistics only during test case execution, although still log them. The reason is to avoid EvoSuite threads which might not
                 * privileged to mess up with the statistics on the SUT
                 */
                statistics.permissionDenied(perm);
            }
            logger.debug("Security manager blocks permission " + perm + stack);

            throw new SecurityException("Security manager blocks " + perm + stack);
        } else {
            if (executingTestCase) {
                statistics.permissionAllowed(perm);
            }
        }

        return;
    }

    // ------------------------------------------------------------------------------------------

    private boolean isAWTThread() {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (e.getClassName().startsWith("java.awt"))
                return true;

            if (e.getClassName().startsWith("javax.swing"))
                return true;

            // Also treat the logmanager like AWT stuff, it is just as weird
            if (e.getClassName().startsWith("java.util.logging.LogManager"))
                return true;
        }
        return false;
    }

    /**
     * Method for checking if requested access, specified by the given
     * permission, is permitted.
     *
     * @param perm permission for which the security manager is asked
     * @return false if access is forbidden, true otherwise
     */
    private boolean allowPermission(Permission perm) {

        if (RuntimeSettings.sandboxMode.equals(Sandbox.SandboxMode.OFF)) {
            /*
             * allow everything
             */
            return true;
        }

        /*
         * We should always allow to check the stack trace,
         * as we use it for debugging (ie when logging)
         */
        if (perm instanceof RuntimePermission &&
                "getStackTrace".equals(perm.getName().trim())) {
            return true;
        }

        // Required in Java 11. Otherwise MSecurityManager.testCanLoadSwingStuff() fails du to the denied permission.
        if (perm instanceof RuntimePermission &&
                "loggerFinder".equals(perm.getName().trim())) {
            return true;
        }

        if (checkIfEvoSuiteRMI(perm) || checkIfRMIDuringTests(perm)) {
            return true;
        }

        // first check if calling thread belongs to EvoSuite rather than the SUT
        if (privilegedThreads.contains(Thread.currentThread())) {

            //it is an EvoSuite thread but, in special occasions, we might want to ignore its privileged status

            if (privilegedThreadToIgnore == null || !Thread.currentThread().equals(privilegedThreadToIgnore)) {

                if (defaultManager == null) {
                    return true; // no security manager, so allow it
                } else {
                    try {
                        defaultManager.checkPermission(perm); // if not allowed, it will throw exception
                    } catch (SecurityException e) {
                        return false;
                    }
                    return true;
                }
            }
        }


        if (RuntimeSettings.sandboxMode.equals(Sandbox.SandboxMode.IO)) {
            // TODO: This makes JVM8 on MacOS crash
            // PermissionStatistics.getInstance().countThreads(Thread.currentThread().getThreadGroup().activeCount());

            if (perm instanceof FilePermission) {
                return checkFilePermission((FilePermission) perm);
            }

            return true;
        }
		 
		/*
		 * Note: we had to remove this check, as some EvoSuite-RMI threads would be blocked by it 
		 * 
		if (!executingTestCase) {

			 // Here, the thread is not "privileged" (either from SUT or an un-registered by EvoSuite), and we are not executing a test case (if from
			 // SUT, that means the thread was not stopped properly). So, we deny any permission

			logger.debug("Unprivileged thread trying to execute potentially harmfull code outsie SUT code execution. Permission: "
			        + perm.toString());
			return false;
		}
		 */

        /*
         * If we only check threads at the end of test case execution, we would miss
         * all the threads that are started and ended within the execution.
         * Checking every time the SM is called is a cheap way to get this method called
         * by the SUT during test case execution without the need to do any bytecode
         * instrumentation.
         */
        // TODO: This makes JVM8 on MacOS crash
        //PermissionStatistics.getInstance().countThreads(Thread.currentThread().getThreadGroup().activeCount());

        if (perm instanceof FilePermission) {
            return checkFilePermission((FilePermission) perm);
        }

        /*
         * following are not checked if sandbox is in IO mode, in which only FilePermissions are checked
         */

        if (perm instanceof AllPermission) {
            return checkAllPermission((AllPermission) perm);
        }

        if (perm instanceof SecurityPermission) {
            return checkSecurityPermission((SecurityPermission) perm);
        }

        if (perm instanceof LoggingPermission) {
            return checkLoggingPermission((LoggingPermission) perm);
        }

        if (perm instanceof ReflectPermission) {
            return checkReflectPermission((ReflectPermission) perm);
        }

        if (perm instanceof PropertyPermission) {
            return checkPropertyPermission((PropertyPermission) perm);
        }

        if (perm instanceof RuntimePermission) {
            return checkRuntimePermission((RuntimePermission) perm);
        }

        if (perm instanceof AWTPermission) {
            return checkAWTPermission((AWTPermission) perm);
        }

        if (perm instanceof UnresolvedPermission) {
            return checkUnresolvedPermission((UnresolvedPermission) perm);
        }

        if (perm instanceof SerializablePermission) {
            return checkSerializablePermission((SerializablePermission) perm);
        }

        if (perm instanceof AudioPermission) {
            return checkAudioPermission((AudioPermission) perm);
        }

        if (perm instanceof DelegationPermission) {
            return checkDelegationPermission((DelegationPermission) perm);
        }

        if (perm instanceof ServicePermission) {
            return checkServicePermission((ServicePermission) perm);
        }

        if (perm instanceof SQLPermission) {
            return checkSQLPermission((SQLPermission) perm);
        }

        if (perm instanceof SSLPermission) {
            return checkSSLPermission((SSLPermission) perm);
        }

        if (perm instanceof PrivateCredentialPermission) {
            return checkPrivateCredentialPermission((PrivateCredentialPermission) perm);
        }

        if (perm instanceof WebServicePermission) {
            return checkWebServicePermission((WebServicePermission) perm);
        }

        if (perm instanceof SubjectDelegationPermission) {
            return checkSubjectDelegationPermission((SubjectDelegationPermission) perm);
        }

        if (perm instanceof ManagementPermission) {
            return checkManagementPermission((ManagementPermission) perm);
        }

        if (perm instanceof MBeanPermission) {
            return checkMBeanPermission((MBeanPermission) perm);
        }

        if (perm instanceof MBeanServerPermission) {
            return checkMBeanServerPermission((MBeanServerPermission) perm);
        }

        if (perm instanceof MBeanTrustPermission) {
            return checkMBeanTrustPermission((MBeanTrustPermission) perm);
        }

        if (perm instanceof NetPermission) {
            return checkNetPermission((NetPermission) perm);
        }

        if (perm instanceof AuthPermission) {
            return checkAuthPermission((AuthPermission) perm);
        }

        if (perm instanceof SocketPermission) {
            return checkSocketPermission((SocketPermission) perm);
        }

        /*
         * as far as JDK 6 is concern, those should be all possible permissions. But just in case, if there is a permission we don't know, we just
         * deny it
         */

        String canonicalName = perm.getClass().getCanonicalName();
        /*
         * API permissions seems all in java.* and javax.*
         * Although this check is not 100% bullet proof, it is a risk
         * we have to take, ie allowing SUT permissions
         */
        if (canonicalName.startsWith("java")) {
            if (!unrecognizedPermissions.contains(perm)) {
                unrecognizedPermissions.add(perm);
                logger.debug("Unrecognized permission type: " + canonicalName);
            }
            return false;
        } else {
            /*
             * We need to allow SUT permissions, as anyway they cannot make any harm that
             * we are not already handling
             */
            logger.debug("Allowing permission defined by the SUT: " + canonicalName);
            return true;
        }
    }

    /**
     * <p>This is tricky. Client publishes RMI objects that the Master
     * will try to access. The RMI objects will wait on a TCP socket.
     * When we export the RMI object, the running threads will be
     * marked as privileged. Problem here is that RMI code can spawn
     * new threads, which would be very difficult to identify and
     * make privileged.</p>
     *
     * <p> The solution here is to analyze the stack trace, and
     * allow only what the EvoSuite client actually requests.
     * It is not bullet-proof, but should be fine for now.
     * </p>
     *
     * @param perm
     * @return
     */
    private boolean checkIfEvoSuiteRMI(Permission perm) {

		/*
			FIXME: this does not check if it is the SUT that calls RMI.

			This would be a reason more to actually mock RMI in VNET
		 */

        if (!Thread.currentThread().getName().startsWith("RMI ") && !Thread.currentThread().getName().equals("Statistics sender in client process")) {
            return false;
        }

        final String pattern = "sun.rmi.";
        boolean foundRMI = false;

        //first check if there is any reference to RMI in the stack trace
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.toString().startsWith(pattern)) {
                foundRMI = true;
                break;
            }
        }

        if (!foundRMI) {
            //found no reference to RMI
            return false;
        }

        boolean foundMasterNode = false;

        traceLoop:
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            for (String masterNodeMethod : masterNodeRemoteMethodNames) {
                if (element.toString().contains(masterNodeMethod)) {
                    foundMasterNode = true;
                    break traceLoop;
                }
            }
        }

        if (!foundMasterNode) {
            //found no reference to RMI
            return false;
        }

        if (perm instanceof FilePermission && !perm.getActions().equals("read")) {
            //we do this just as a safety mechanism...
            logger.error("EvoSuite RMI is trying to interact with files: " + perm);
            return false;
        }

        return true;
    }

    public boolean checkIfRMIDuringTests(Permission perm) {

        /*
         * if we are running test cases to debug EvoSuite, we always want to allow RMI.
         * this is particularly true as we do have RMI in the Master as well, which usually
         * would run without a sandbox
         */
        return runningClientOnThread && Thread.currentThread().getName().startsWith("RMI TCP");
    }

    /*
     * Note: many of the String constants used below in the various methods come from sun.security.util.SecurityConstants but accessing them directly
     * can issue some warnings, and might make EvoSuite more difficult to port and use on different OS,installations, or even Java versions
     */

    protected boolean checkSocketPermission(SocketPermission perm) {
        /*
         * Handling UDP/TCP connections are handled by VNET mocks
         */

        String action = perm.getActions();
        String name = perm.getName();

        /*
                this kind of special: we do allow resolve of local host, although we do mock InetAddress.
                This is due to all kind of indirect calls in Swing that we do not fully mock, eg like
                sun.font.FcFontConfiguration.getFcInfoFile
                this is triggered from
                JComponent.getFontMetrics
                which is triggered by the very common
                JComponent.getPreferredSize

                Furthermore there are some issues with statistics handling if this is not enabled
             */
        return action.contains("resolve") && (name.equals(LOCALHOST_NAME) || name.contains(InetAddress.getLoopbackAddress().toString()));
    }

    protected boolean checkAuthPermission(AuthPermission perm) {
        /*
         * some of the permissions might be granted, but need to study them in details. but because it is pretty rare, for now we can just forbid it
         */
        return false;
    }

    protected boolean checkNetPermission(NetPermission perm) {
        /*
         * "specifyStreamHandler" seems the only tricky one. But because a URL cannot be used to write to file-system (although it can be used for
         * remote resources), it should be fine
         */
        return true;
    }

    // -----------------------------------------------------------------------------
    /*
     * EvoSuite does not use any bean, so allowing the SUT to create/use beans should be fine. However, there is possible issue of beans created by a
     * test case carrying over the following executions. In theory, this should be handled when we re-set static variables. If not, then here we can
     * do as following: if any of the bean permissions is called at least once, delete all beans after the test case is executed.
     */
    protected boolean checkMBeanPermission(MBeanPermission perm) {
        return true;
    }

    protected boolean checkMBeanServerPermission(MBeanServerPermission perm) {
        return true;
    }

    protected boolean checkMBeanTrustPermission(MBeanTrustPermission perm) {
        return true;
    }

    // -----------------------------------------------------------------------------

    protected boolean checkManagementPermission(ManagementPermission perm) {
        String name = perm.getName();

        return name.equals("monitor");

        /*
         * "control" sounds bit risky
         */
    }

    protected boolean checkSubjectDelegationPermission(SubjectDelegationPermission perm) {
        /*
         * seems fine
         */
        return true;
    }

    protected boolean checkWebServicePermission(WebServicePermission perm) {
        /*
         * "publishing a web service endpoint" should be fine, but unsure whether it has effects or not on opening UDP/TCP ports. Need more
         * investigations before allowing it
         */
        return false;
    }

    protected boolean checkPrivateCredentialPermission(PrivateCredentialPermission perm) {
        /*
         * it is only used to "read"
         */
        return true;
    }

    protected boolean checkSSLPermission(SSLPermission perm) {
        String name = perm.getName();

        return name.equals("getSSLSessionContext");

        /*
         * setHostnameVerifier, setDefaultSSLContext
         */
    }

    protected boolean checkSQLPermission(SQLPermission perm) {
        /*
         * SQL (and database in general) will require specialized techniques in EvoSuite. For now, we just forbid it
         */
        return false;
    }

    protected boolean checkServicePermission(ServicePermission perm) {
        /*
         * Seems used for some authentication protocols. If service is outside SUT, anyway it will be blocked in other ways (eg Sockets). If in SUT,
         * then it should be fine to allow it
         */
        return true;
    }

    protected boolean checkDelegationPermission(DelegationPermission perm) {
        /*
         * I don't really fully understand it, but in any case it seems pretty rare permission. For now we just forbid it
         */
        return false;
    }

    protected boolean checkAudioPermission(AudioPermission perm) {
        /*
         * If SUT plays some music, then I do not see any major side effect. In worst case, tester can just switch off the speakers during testing.
         */
        return true;
    }

    protected boolean checkSerializablePermission(SerializablePermission perm) {
        /*
         * seems safe
         */
        return true;
    }

    protected boolean checkUnresolvedPermission(UnresolvedPermission perm) {
        /*
         * From documentation:
         * -------------------------------------------------------------------------------------------------------------------------- The
         * java.security.UnresolvedPermission class is used to hold Permissions that were "unresolved" when the Policy was initialized. An unresolved
         * permission is one whose actual Permission class does not yet exist at the time the Policy is initialized (see below). The policy for a Java
         * runtime (specifying which permissions are available for code from various principals) is represented by a Policy object. Whenever a Policy
         * is initialized or refreshed, Permission objects of appropriate classes are created for all permissions allowed by the Policy. Many
         * permission class types referenced by the policy configuration are ones that exist locally (i.e., ones that can be found on CLASSPATH).
         * Objects for such permissions can be instantiated during Policy initialization. For example, it is always possible to instantiate a
         * java.io.FilePermission, since the FilePermission class is found on the CLASSPATH. Other permission classes may not yet exist during Policy
         * initialization. For example, a referenced permission class may be in a JAR file that will later be loaded. For each such class, an
         * UnresolvedPermission is instantiated. Thus, an UnresolvedPermission is essentially a "placeholder" containing information about the
         * permission. Later, when code calls AccessController.checkPermission on a permission of a type that was previously unresolved, but whose
         * class has since been loaded, previously-unresolved permissions of that type are "resolved". That is, for each such UnresolvedPermission, a
         * new object of the appropriate class type is instantiated, based on the information in the UnresolvedPermission. This new object replaces
         * the UnresolvedPermission, which is removed.
         * -------------------------------------------------------------------------------------------------------------------------- In theory it
         * shouldn't really happen, unless some customized permission classes are used in the SUT. It also poses a problem: we might run a test case
         * that throws this security exception but, if we run it again, it might not throw it anymore. Just to be sure, for now we deny this
         * permission
         */
        return false;
    }

    protected boolean checkAllPermission(AllPermission perm) {
        /*
         * should never grant all the permissions
         */
        return false;
    }

    protected boolean checkSecurityPermission(SecurityPermission perm) {

        String name = perm.getName();

        if (name.equals("getDomainCombiner") || name.equals("getPolicy")
                || name.equals("printIdentity") || name.equals("getSignerPrivateKey")
                || name.startsWith("getProperty.")) {
            return true;
        }

        /*
         * this seems needed when analyzing classpath, but not fully sure of its consequences
         */
        return name.startsWith("putProviderProperty.");

        /*
         * createAccessControlContext setPolicy createPolicy.{policy type} setProperty.{key} insertProvider.{provider name} removeProvider.{provider
         * name} setSystemScope setIdentityPublicKey setIdentityInfo addIdentityCertificate removeIdentityCertificate
         * clearProviderProperties.{provider name} putProviderProperty.{provider name} removeProviderProperty.{provider name} setSignerKeyPair
         */
    }

    protected boolean checkAWTPermission(AWTPermission perm) {
        /*
         * For now, we run EvoSuite in headless mode (ie no support for display, mouse, keyboard, etc). Methods that will need those devices will
         * throw a Headless exception. so, here, we can just grant permissions, as shouldn't really have any effect. When we ll start to test GUI
         * (without headless), then we ll need to carefully check which permissions to grant (eg "createRobot" seems very dangerous)
         */
        /*
         * accessClipboard accessEventQueue accessSystemTray createRobot fullScreenExclusive listenToAllAWTEvents readDisplayPixels
         * replaceKeyboardFocusManager setAppletStub setWindowAlwaysOnTop showWindowWithoutWarningBanner toolkitModality watchMousePointer
         */
        return "true".equals(AWT_HEADLESS);
    }

    protected boolean checkRuntimePermission(RuntimePermission perm) {

        final String name = perm.getName().trim();

        /*
         * At the moment this is the only way to allow classes under test define and load other classes, but the way it is done seriously damages
         * security of the program. However, as we check permissions based on thread references, it might be safe. See comments on allowing
         * reflection.
         */
        if (name.equals("getClassLoader") || name.equals("createClassLoader")
                || name.startsWith("accessClassInPackage")
                || name.startsWith("defineClassInPackage")
                || name.equals("setContextClassLoader")
                || name.equals("enableContextClassLoaderOverride")
                || name.equals("accessDeclaredMembers")
                || name.equals("accessSystemModules")) {
            return true;
        }

        // never allow it!!! far too dangerous, as it would break the sandbox
        if (name.equals("setSecurityManager")) {
            return false;
        }

        if (name.equals("createSecurityManager")) {
            return true; //just creating should not be a problem
        }

        // AWT needs to be treated specially
        //FIXME handling of awt read permission
        if ("true".equals(AWT_HEADLESS) && isAWTThread()) {
            if (name.equals("shutdownHooks"))
                return true;
            if (name.equals("modifyThreadGroup"))
                return true;
            if (name.equals("modifyThread"))
                return true;
        }

        /*
         * Note: this actually should never be called, as the instrumenting class loader should replace System.exit
         */
        if (name.startsWith("exitVM")) {
            return false;
        }

        if (name.equals("shutdownHooks")) {
            return RuntimeSettings.mockJVMNonDeterminism; // the hooks will be handled by mocking framework
        }

        /*
         * This is tricky.
         * This permission is called only by static methods in Socket/URL classes,
         * and the factory can be set only once.
         * So, if EvoSuite already set a factory, then the SUT cannot change it.
         * On the other hand, if the SUT set a factory, it will have potential
         * impact on EvoSuite code (eg, if EvoSuite will use new Socket/URLs after
         * test case execution). But such impact would only be inside the JVM,
         * so not a major problem.
         * In summary, as it seems pretty common, we do allow it for the time being.
         */
        if (name.equals("setFactory")) {
            return true;
        }

        /*
         * As client use logging, and we don't read std/err output from it, then it should be safe to allow it
         */
        if (name.equals("setIO")) {
            return true;
        }

        /*
         * Required to allow use of locale sensitive services in java.text and java.util
         */
        if (name.equals("localeServiceProvider")) {
            return true;
        }


        /*
         * we need it for reflection
         */
        if (name.equals("reflectionFactoryAccess")) {
            return true;
        }

        /*
         * it might be considered risky, as it can stop the EvoSuite threads. Worst case, we ll get no data from client, which is better than just
         * skipping testing the SUT by throwing a security exception
         */
        if (name.equals("modifyThread") || name.equals("stopThread")
                || name.equals("modifyThreadGroup")) {
            return true;
        }

        /*
         * this looks safe... but not 100% sure to be honest
         */
        if (name.equals("setDefaultUncaughtExceptionHandler")) {
            return true;
        }

        /*
         * those are perfectly fine
         */
        if (name.startsWith("getenv.") || name.equals("getProtectionDomain")
                || name.equals("readFileDescriptor")) {
            return true;
        }

        /*
         * not fully understand this one... so let's block it for now
         */
        if (name.equals("writeFileDescriptor")) {
            return false; //FIXME
        }

        if (name.startsWith("loadLibrary.")) {

            /*
             * There might quite a few risks if SUT uses native code developed by user. By default, we deny this permissions, but then we can allow
             * the loading of some specific libraries. Ultimately, the user should be able to choose if some libraries can be loaded or not
             */

            String library = name.substring("loadLibrary.".length());

            return library.equals("awt") || library.equals("fontmanager")
                    || library.equals("net") || library.equals("lcms")
                    || library.equals("j2pkcs11") || library.equals("nio")
                    || library.equals("laf") || library.endsWith("libmawt.so")
                    || library.equals("jpeg") || library.endsWith("liblwawt.dylib")
                    || library.equals("cmm") || library.equals("t2k")
                    || library.equals("jawt") || library.equals("sunec")
                    || library.equals("management") || library.equals("kcms")
                    || library.equals("dcpr") || library.equals("mlib_image")
                    || library.startsWith("jaybird") || library.equals("instrument")
                    || library.startsWith("osxui") || library.contains("libawt_lwawt")
                    || library.contains("libawt_headless") || library.contains("libawt_xawt")
                    || library.contains("javalcms");
        }

        /*
         * Definitely not! furthermore, testing machine might not have a printer anyway... if SUT needs a printer, I guess we should somehow manage to
         * mock it. But low priority, as I don't think many SUTs do print...
         */
        if (name.equals("queuePrintJob")) {
            return false;
        }

        /*
         * Why not? only possible issue is if here, in this security manager, we use stack info to check whether or not allow permissions
         */
        if (name.equals("getStackTrace")) {
            return true;
        }

        /*
         * writing in persistent backing store of the file system does not sound much safe...
         */
        if (name.equals("preferences")) {
            return false;
        }

        /*
         * These are permissions in NIO, which seems safe...
         */
        if (name.equals("charsetProvider") || name.equals("selectorProvider")) {
            return true;
        }


        //Java 7 permissions:
        if (name.equals("getFileSystemAttributes") || name.equals("fileSystemProvider")) {
            return true;
        }


        /*
         * this is also useful for checking types in the String constants, and to be warned if they ll change in future JDKs
         */
        if (!unrecognizedPermissions.contains(perm)) {
            unrecognizedPermissions.add(perm);
            logger.warn("SUT asked for a runtime permission that EvoSuite does not recognize: " + name);
        }

        return false;
    }

    protected boolean checkPropertyPermission(PropertyPermission perm) {

        /*
         * TODO: this will need to be removed once REPLACE_CALLS
         * will be on by default
         */
        if (perm.getName().equals("sun.font.fontmanager")) {
            return true;
        }

        if (perm.getActions().contains("write") && !executingTestCase) {
            return !org.evosuite.runtime.System.isSystemProperty(perm.getName());
        }

        return org.evosuite.runtime.System.handlePropertyPermission(perm);
    }

    protected boolean checkReflectPermission(ReflectPermission perm) {
        /*
         * might be some possible side effects but, again, such side-effects would be confined inside the client JVM. One issue though: the SUT can
         * access to this security manager, and through reflection mess up its internal state, which might lead to allow other security permissions...
         */
        return true;
    }

    protected boolean checkLoggingPermission(LoggingPermission perm) {
        /*
         * we allow it because worst thing it can happen is getting more/less log events from client. we might lose some debugging, but really not a
         * big deal, because in any case log levels are set in master (ie client events do not change them)
         */
        return true;
    }

    /**
     * Check if this is an access of a file needed for FileHandler, or its parent directory check
     *
     * @param fp
     * @return
     */
    private boolean isFileHandlerCall(FilePermission fp) {
        if (fp.getName().contains(FILE_HANDLER_NAME_PATTERN))
            return true;
        if (fp.getActions().equals("write")) {
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                if (e.getClassName().equals(FileHandler.class.getName()) && e.getMethodName().equals("isParentWritable")) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean checkFilePermission(FilePermission fp) {
        String action = fp.getActions();
        if (action == null) {
            // might not ever happen, but just in case we log it and return false
            logger.debug("File permission with empty action");
            return false;
        }

        /*
         * Reading can be considered "fine", even outside sandbox. Only issue I can think of is on Windows, if a process is trying to delete a file a
         * EvoSuite client is reading, then that deletion would be forbidden, as client has lock.
         */
        if (action.equals("read")) {
            return true;
        }

        if (RuntimeSettings.useVFS) {

            //we need at least one real file with all permissions, otherwise the VFS will not work
            boolean isTmpFile = fp.getName().equals(VirtualFileSystem.getInstance().getRealTmpFile().getPath());
            boolean isFileHandlerFile = isFileHandlerCall(fp);

            if (isFileHandlerFile) {
                /*
                 * Note: story here is complicated, see MockFileHandler class.
                 * As we do not know which files will be generated in its superclass FileHandler,
                 * here we just make sure to delete any tmp file after the JVM terminates.
                 *
                 * However the code below seems _really_ expensive to run, so we should just skip it
                 */
                //File tmpFile = new File(fp.getName());
                //tmpFile.deleteOnExit(); //Note: we cannot do it here, otherwise we end up in a infinite recursion...
                //filesToDelete.add(tmpFile);
                /*
                 * Note: As we do not delete the files, they will remain on the FS. FileHandler will then create
                 * a new file the next time, but once it runs out of IDs, it will check if the parent directory
                 * is writable, which would cause a security exception. We therefore either have to delete the file
                 * or check the stack trace to see if we are in FileHandler.isParentWritable
                 *
                 */
            }
            if (isTmpFile || isFileHandlerFile) {
                return true;
            }
        }

        String fontDir = USER_DIR + File.separator + ".java" + File.separator +
                "fonts" + File.separator + JAVA_VERSION;

        if (action.equals("write")) {
            if (fp.getName().startsWith(fontDir)) {
                /*
                 * NOTE: this is a very tricky situation.
                 * Issue arises in GUI classes like javax.swing.JComponent,
                 * which leads in loading font files.
                 * Now, based on the JVM, this could lead to execute code
                 * in sun.font.FcFontConfiguration which leads to writing files.
                 * Problem is that, normally, such code is executed in a
                 * AccessController.doPrivileged block (see sun.font.FontManagerFactory).
                 * However, our security manager is not able to properly handle
                 * those cases, so we just allow writing permissions on such
                 * target folder.
                 *
                 * Some points to consider:
                 * - it is unlikely that a SUT will write on such folder by chance,
                 *   but a malicious attacker could exploit such security hole.
                 * - if we end up with more cases like this, maybe we should
                 *   refactor/extend this class to handle AccessController.doPrivileged blocks.
                 *
                 */
                return true;
            } else if (fp.getName().contains("jacoco")) {
                /*
                 * This is not 100% secure, but Jacoco support
                 * is important
                 */
                for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                    if (e.getClassName().startsWith("org.jacoco.")) {
                        return true;
                    }
                }
            } else if (fp.getName().contains("gzoltar") || fp.getName().equals(System.getProperty("user.dir"))) {
                // By default, GZoltar writes the gzoltar.ser file that holds the coverage
                // of each test case to the user.dir defined in the scaffolding test class.
                // As user.dir might not exist, EvoSuite must grant access write access to
                // GZoltar.
                // Note: The following is not 100% secure, but GZoltar support is important.
                for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                    if (e.getClassName().startsWith("com.gzoltar.")) {
                        return true;
                    }
                }
            } else if (fp.getName().contains("clover")) {
                /*
                 * To make sure this is really clover trying to write a report
                 * we also check that this is invoked by clover
                 */
                for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                    if (e.getClassName().startsWith("com.atlassian.clover.")) {
                        return true;
                    }
                }
            }
        } else if (action.equals("delete")) {
            if (fp.getName().contains("clover.db.liverec")) {
                /*
                 * To make sure this is really clover trying to write a report
                 * we also check that this is invoked by clover
                 */
                for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                    if (e.getClassName().startsWith("com.atlassian.clover.")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
