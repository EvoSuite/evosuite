/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.sandbox;

import java.awt.AWTPermission;
import java.io.FilePermission;
import java.io.SerializablePermission;
import java.lang.management.ManagementPermission;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.AccessControlContext;
import java.security.AllPermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.security.UnresolvedPermission;
import java.sql.SQLPermission;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.LoggingPermission;

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

import org.evosuite.Properties;
import org.evosuite.Properties.SandboxMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * http://download.oracle.com/javase/6/docs/technotes/guides/security/
 * permissions.html
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
class MSecurityManager extends SecurityManager {

	private static Logger logger = LoggerFactory.getLogger(MSecurityManager.class);

	private final PermissionStatistics statistics = PermissionStatistics.getInstance();

	private final SecurityManager defaultManager;

	/**
	 * Is EvoSuite executing a test case?
	 */
	private volatile boolean executingTestCase;

	/**
	 * Default Java properties before we run the SUT
	 */
	private volatile java.util.Properties defaultProperties;

	/**
	 * If SUT changed some properties, we need to re-set the default values
	 */
	private volatile boolean needToRestoreProperties;

	/**
	 * Data structure containing all the (EvoSuite) threads that do not need to
	 * go through the same sandbox as the SUT threads
	 */
	private volatile Set<Thread> privilegedThreads;

	/**
	 * Check whether a privileged thread should use the sandbox as for SUT code
	 */
	private volatile boolean ignorePrivileged;

	/**
	 * Create a custom security manager for the SUT. The thread that create this
	 * instance is automatically added as "privileged"
	 */
	public MSecurityManager() {
		privilegedThreads = new CopyOnWriteArraySet<Thread>();
		privilegedThreads.add(Thread.currentThread());
		defaultManager = System.getSecurityManager();
		executingTestCase = false;
		defaultProperties = (java.util.Properties) System.getProperties().clone();
		ignorePrivileged = false;
	}

	/**
	 * Use this method if you are going to execute SUT code from a privileged
	 * thread (ie if you don't want to do it on a new thread)
	 * 
	 * @throws SecurityException
	 * @throws IllegalStateException
	 */
	public void goingToExecuteUnsafeCodeOnSameThread() throws SecurityException,
	        IllegalStateException {
		if (!privilegedThreads.contains(Thread.currentThread())) {
			throw new SecurityException(
			        "Only a privileged thread can execute unsafe code");
		}
		if (ignorePrivileged) {
			throw new IllegalStateException("The thread is already executing unsafe code");
		}
		ignorePrivileged = true;
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
		if (!ignorePrivileged) {
			throw new IllegalStateException("The thread was not executing unsafe code");
		}
		ignorePrivileged = false;
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
	public void makePriviligedAllCurrentThreads() {
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
			throw new IllegalStateException();
		}
		executingTestCase = true;
		needToRestoreProperties = false;
	}

	public void goingToEndTestCase() throws IllegalStateException {
		if (!executingTestCase) {
			throw new IllegalStateException();
		}
		/*
		 * The synchronization is used to avoid (if possible) a SUT thread to modify a property just immediately after we restore them. this could
		 * actually happen if this method is called while a SUT thread is executing a permission check
		 */
		synchronized (defaultProperties) {
			if (needToRestoreProperties) {
				System.setProperties((java.util.Properties) defaultProperties.clone());
				needToRestoreProperties = false;
			}
		}

		executingTestCase = false;
	}

	/**
	 * Add a thread to the list of privileged thread. This is useful if EvoSuite
	 * needs to spawn new threads that require permissions.
	 * 
	 * @param t
	 * @throws SecurityException
	 *             if the thread calling this method is not privileged itself
	 */
	public void addPrivilegedThread(Thread t) throws SecurityException {
		if (privilegedThreads.contains(Thread.currentThread())) {
			logger.debug("Adding privileged thread: " + t.getName());
			privilegedThreads.add(t);
		} else {
			throw new SecurityException(
			        "Unprivileged thread cannot add a privileged thread");
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
			if (executingTestCase) {
				/*
				 * report statistics only during test case execution, although still log them. The reason is to avoid EvoSuite threads which might not
				 * privileged to mess up with the statistics on the SUT
				 */
				statistics.permissionDenied(perm);
			}
			String stack = "\n";
			for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
				stack += e + "\n";
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
	 * @param perm
	 *            permission for which the security manager is asked
	 * @return false if access is forbidden, true otherwise
	 */
	private boolean allowPermission(Permission perm) {

		if (Properties.SANDBOX_MODE.equals(SandboxMode.OFF)) {
			return true;
		}

		if (Properties.SANDBOX_MODE.equals(SandboxMode.IO)) {
			PermissionStatistics.getInstance().countThreads(Thread.currentThread().getThreadGroup().activeCount());

			if (perm instanceof FilePermission) {
				return checkFilePermission((FilePermission) perm);
			}

			return true;
		}

		// first check if calling thread belongs to EvoSuite rather than the SUT
		if (!ignorePrivileged && privilegedThreads.contains(Thread.currentThread())) {
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

		if (!executingTestCase) {
			/*
			 * Here, the thread is not "privileged" (either from SUT or an un-registered by EvoSuite), and we are not executing a test case (if from
			 * SUT, that means the thread was not stopped properly). So, we deny any permission
			 */
			logger.debug("Unprivileged thread trying to execute potentially harmfull code outsie SUT code execution. Permission: "
			        + perm.toString());
			return false;
		}

		/*
		 * If we only check threads at the end of test case execution, we would miss
		 * all the threads that are started and ended within the execution.
		 * Checking every time the SM is called is a cheap way to get this method called
		 * by the SUT during test case execution without the need to do any bytecode
		 * instrumentation. 
		 */
		PermissionStatistics.getInstance().countThreads(Thread.currentThread().getThreadGroup().activeCount());

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

		// for (int elementCounter = 0; elementCounter < stackTraceElements.length; elementCounter++) {
		// FIXME: what is this?
		// if (e.getClassName().contains("MockingBridge") && Properties.MOCKS)
		// return true;
		// }

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
			logger.debug("Unrecognized permission type: " + canonicalName);
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

	/*
	 * Note: many of the String constants used below in the various methods come from sun.security.util.SecurityConstants but accessing them directly
	 * can issue some warnings, and might make EvoSuite more difficult to port and use on different OS,installations, or even Java versions
	 */

	protected boolean checkSocketPermission(SocketPermission perm) {
		/*
		 * Handling UDP/TCP connections will require special mocks. So, for now we just deny this permission
		 */
		return false;
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

		if (name.equals("monitor")) {
			return true;
		}

		/*
		 * "control" sounds bit risky
		 */
		return false;
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

		if (name.equals("getSSLSessionContext")) {
			return true;
		}

		/*
		 * setHostnameVerifier, setDefaultSSLContext
		 */
		return false;
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
		if (name.startsWith("putProviderProperty.")) {
			return true;
		}

		/*
		 * createAccessControlContext setPolicy createPolicy.{policy type} setProperty.{key} insertProvider.{provider name} removeProvider.{provider
		 * name} setSystemScope setIdentityPublicKey setIdentityInfo addIdentityCertificate removeIdentityCertificate
		 * clearProviderProperties.{provider name} putProviderProperty.{provider name} removeProviderProperty.{provider name} setSignerKeyPair
		 */
		return false;
	}

	protected boolean checkAWTPermission(AWTPermission perm) {
		/*
		 * For now, we run EvoSuite in headless mode (ie no support for display, mouse, keyboard, etc). Methods that will need those devices will
		 * throw a Headless exception. so, here, we can just grant permissions, as shouldn't really have any effect. When we ll start to test GUI
		 * (without headless), then we ll need to carefully check which permissions to grant (eg "createRobot" seems very dangerous)
		 */
		if ("true".equals(System.getProperty("java.awt.headless"))) {
			return true;
		} else {
			/*
			 * accessClipboard accessEventQueue accessSystemTray createRobot fullScreenExclusive listenToAllAWTEvents readDisplayPixels
			 * replaceKeyboardFocusManager setAppletStub setWindowAlwaysOnTop showWindowWithoutWarningBanner toolkitModality watchMousePointer
			 */
			return false;
		}
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
		        || name.equals("accessDeclaredMembers")) {
			return true;
		}

		// never allow it!!! far too dangerous, as it would break the sandbox
		if (name.equals("setSecurityManager") || name.equals("createSecurityManager")) {
			return false;
		}

		// AWT needs to be treated specially
		if ("true".equals(System.getProperty("java.awt.headless")) && isAWTThread()) {
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
		if (name.startsWith("exitVM") || name.equals("shutdownHooks")) {
			return false;
		}

		/*
		 * For the moment, we don't allow it. but we might once we address Networking
		 */
		if (name.equals("setFactory")) {
			return false;
		}

		/*
		 * As client use logging, and we don't read std/err output from it, then it should be safe to allow it
		 */
		if (name.equals("setIO")) {
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
			return false;
		}

		if (name.startsWith("loadLibrary.")) {

			/*
			 * There might quite a few risks if SUT uses native code developed by user. By default, we deny this permissions, but then we can allow
			 * the loading of some specific libraries. Ultimately, the user should be able to choose if some libraries can be loaded or not
			 */

			String library = name.substring("loadLibrary.".length(), name.length());

			if (library.equals("awt") || library.equals("fontmanager")
			        || library.equals("net") || library.equals("lcms")
			        || library.equals("j2pkcs11") || library.equals("nio")
			        || library.equals("laf") || library.endsWith("libmawt.so")
			        || library.equals("jpeg") || library.endsWith("liblwawt.dylib")
			        || library.equals("cmm")) {
				return true;
			}

			return false;
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
		 * This is permissions in NIO, which seems safe...
		 */
		if (name.equals("charsetProvider") || name.equals("selectorProvider")) {
			return true;
		}

		/*
		 * this is also useful for checking types in the String constants, and to be warned if they ll change in future JDKs
		 */
		logger.debug("SUT asked for a runtime permission that EvoSuite does not recognize: "
		        + name);

		return false;
	}

	protected boolean checkPropertyPermission(PropertyPermission perm) {
		/*
		 * we allow both writing and reading any properties. But, if SUT writes anything, then we need to re-store the values to their default. this
		 * is very important, otherwise: 1) test cases might have side effects on each other 2) SUT might change properties that are used by EvoSuite
		 */

		if (perm.getActions().contains("write") && !needToRestoreProperties) {
			synchronized (defaultProperties) {
				if (!executingTestCase) { // just to be sure
					return false;
				}
				needToRestoreProperties = true;
			}
		}

		return true;
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

		/*
		 * explanatory note by Daniel concerning integration of this security manager with VFS functionality (-Dvirtual_fs=true):
		 * 
		 * In the overwritten classes (File, FileInputStream, FileOutputStream as of now) I left out all security manager checks concerning read &
		 * write permission for virtual files (that are stored in the virtual file system). That means, if we have something like 'new
		 * FileOutputStream(virtualFile)', no security exception is thrown, even if this security manager is active and would forbid write access. For
		 * original files the security manager behaves as specified. I know that I thereby partially violate the original specification of
		 * File/FileInputStream/FileOutputStream when to throw a security exception. The (not so nice) alternative I can think of, would be to check
		 * if Properties.VIRTUAL_FS is true, to analyze the current stack trace in this very method here and to allow write/execute access if there is
		 * File/FileInputStream/FileOutputStream in the stack trace. If you prefer this method (or a better one?) to be implemented (and to have
		 * File/FIS/FOS throw security exceptions also for virtual files), please let me know.
		 * 
		 * Reminder: EvoSuiteIO.shouldBeOriginal labels all File instances as 'virtual' files, that are created by the class under test while the VFS
		 * is enabled. If such a file instance exists on the real file system, it is copied into memory and all subsequent file operations (provided
		 * by the classes File/FIS/FOS) are performed on this in-memory copy. Nevertheless, there is an option to only allow existing files to be read
		 * from within the sandbox read folder (-Drestricted_read=true)
		 */

		/*
		 * FIXME: "contains" is pretty unsecure, and we should check actual path hierarchy. Eg, SUT could create a file named
		 * $Properties.SANDBOX_FOLDER anywhere in the file system
		 */
		if (fp.getName().contains(Properties.SANDBOX_FOLDER)) {
			/*
			 * need to check "execute". in some cases, for browising directory we need "execute", but we don't want to execute files!!! (eg scripts)
			 */
			if (action.equals("write") || action.equals("delete")) {
				// return true; //TODO allow it once the I/O is fixed and properly tested
			}
		}

		return false;
	}
}
