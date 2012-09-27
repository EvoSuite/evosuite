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
import java.io.File;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.AccessControlContext;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.LinkedList;
import java.util.List;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.LoggingPermission;
import java.security.AllPermission;

import org.evosuite.Properties;
import org.evosuite.testcase.TestRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.core.util.ThreadSafePropertyEditor;

/**
 * <p>Mocked Security Manager, which forbids any access to I/O, network, etc.</p>
 * 
 * <p>Note: this class needs to be thread safe, as it will be accessed by the SUT</p>
 * 
 * <p>Regarding the different permissions, and the associated risks in allowing them, see:
 * 
 * http://download.oracle.com/javase/6/docs/technotes/guides/security/permissions.html
 * </p>
 * 
 * <p>This class grants permissions based on thread references, and not on the "context".
 * As such it is actually more restrictive, and granting some kinds of risky permissions should so be fine</p>
 * 
 */
class MSecurityManager extends SecurityManager {

	private static Logger logger = LoggerFactory.getLogger(MSecurityManager.class);

	private final PermissionStatistics statistics = PermissionStatistics.getInstance();

	private SecurityManager defaultManager;

	/**
	 *  Is EvoSuite executing a test case? 
	 */
	private volatile boolean executingTestCase;
	
	
	/**
	 *  Default Java properties before we run the SUT
	 */
	private volatile java.util.Properties defaultProperties;
	
	/**
	 * If SUT changed some properites, we need to re-set the default values
	 */
	private volatile boolean needToRestoreProperties;
	
	/**
	 *  Data structure containing all the (EvoSuite) threads that do not need to go through
	 *  the same sandbox as the SUT threads
	 */
	private Set<Thread> privilegedThreads; 
	
	/**
	 *  Create a custom security manager for the SUT.
	 *  The thread that create this instance is automatically added as "privileged"
	 */
	public MSecurityManager(){
		privilegedThreads = new CopyOnWriteArraySet<Thread>();
		privilegedThreads.add(Thread.currentThread());
		defaultManager = System.getSecurityManager();
		executingTestCase = false;
		defaultProperties = (java.util.Properties) System.getProperties().clone();
	}
	
	/**
	 * When we start EvoSuite, quite a few other threads could start as well (e.g., "Reference Handler", "Finalizer" and "Signal Dispatcher").
	 * This is a convenience method to grant permissions to all threads before starting to execute test cases
	 * 
	 * WARNING: to use only before any SUT code has been executed. Afterwards, it would not be safe (cannot really
	 * guarantee that all SUT have been terminated) 
	 */
	public void makePriviligedAllCurrentThreads(){
		ThreadGroup root = Thread.currentThread().getThreadGroup();
		while(root.getParent() != null){
			root  = root.getParent();
		}
		
		/*
		 * As discussed in the API, this code is not 100% robust
		 */
		Thread[] threads = new Thread[root.activeCount() + 10];
		root.enumerate(threads);
		for(Thread t : threads){
			if(t != null){
				addPrivilegedThread(t);
			}
		}
	}
	
	/**
	 * Use this manager as security manager
	 * 
	 * @throws IllegalStateException
	 */
	public void apply() throws IllegalStateException{
		try{
			System.setSecurityManager(this);
		} catch(SecurityException e){
			//this should never happen in EvoSuite, ie this object should be created just once
			logger.error("Cannot instantiate mock security manager",e);
			throw new IllegalStateException(e); 
		}
	}
	
	/**
	 *  Note: an un-privileged thread would throw a security exception
	 */
	public void restoreDefaultManager() throws SecurityException{
		System.setSecurityManager(defaultManager);
	}
	
	public void goingToExecuteTestCase() throws IllegalStateException{
		if(executingTestCase){
			throw new IllegalStateException();
		}
		executingTestCase = true;
		needToRestoreProperties = false;
	}
	
	public void goingToEndTestCase() throws IllegalStateException{
		if(!executingTestCase){
			throw new IllegalStateException();
		}
		/*
		 * The synchronization is used to avoid (if possible) a SUT thread to modify
		 * a property just immediately after we restore them. this could actually happen if
		 * this method is called while a SUT thread is executing a permission check  
		 */
		synchronized(defaultProperties){
			if(needToRestoreProperties){
				System.setProperties((java.util.Properties)defaultProperties.clone()); 
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
	 * @throws SecurityException if the thread calling this method is not privileged itself
	 */
	public void addPrivilegedThread(Thread t) throws SecurityException{
		if(privilegedThreads.contains(Thread.currentThread())){
			logger.debug("Adding privileged thread: "+t.getName()); 
			privilegedThreads.add(t);
		} else {
			throw new SecurityException("Unprivileged thread cannot add a privileged thread");
		}
	}
	
	
	//------------------------------------------------------------------------------------------
	
	/*
	 * the following two methods are the only ones we need to override from SecurityManager, as all 
	 * the other call those 2. However, if the SecurityManager code will change in future,
	 * we might need to double-check this class
	 */
	
	/**
	 * {@inheritDoc}}
	 */
	@Override
	 public void checkPermission(Permission perm, Object context) throws SecurityException, NullPointerException{
		/*
		 * Note: this code is copy and paste from "super", with only one difference
		 */
		 if (context instanceof AccessControlContext) {
			 checkPermission(perm); // this is the difference, i.e. we ignore context //TODO maybe check if privileged, and if so, actually use the context?
			} else {
			    throw new SecurityException();
			}
	 }
	
	
	/**
	 * {@inheritDoc}
	 * 
	 * Overridden method for checking permissions for any operation.
	 */
	@Override
	public void checkPermission(Permission perm) throws SecurityException{
		// check access  
		if (!allowPermission(perm)) {
			if(executingTestCase){
				/*
				 * report statistics only during test case execution, although still log them.
				 * The reason is to avoid EvoSuite threads which might not privileged to mess
				 * up with the statistics on the SUT 
				 */
				statistics.permissionDenied(perm);
			}
			String stack = "\n";
			for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
				stack += e + "\n";
			}
			logger.info("Security manager blocks permission " + perm + stack);
			throw new SecurityException("Security manager blocks " + perm + stack);
		} else {
			if(executingTestCase){
				statistics.permissionAllowed(perm);
			}
		}

		return;
	}

	//------------------------------------------------------------------------------------------
	
	
	
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
		
		//first check if calling thread belongs to EvoSuite rather than the SUT
		if(privilegedThreads.contains(Thread.currentThread())){
			if(defaultManager==null){
				return true;  // no security manager, so allow it
			} else {
				try{
					defaultManager.checkPermission(perm); // if not allowed, it will throw exception
				} catch(SecurityException e){
					return false;
				}
				return true;
			}
		}
		
		if(!executingTestCase){
			/*
			 * Here, the thread is not "privileged" (either from SUT or an un-registered by EvoSuite), and we
			 * are not executing a test case (if from SUT, that means the thread was not stopped properly).
			 * So, we deny any permission
			 */
			logger.info("Unprivileged thread trying to execute potentially harmfull code outsie SUT code execution. Permission: "+perm.toString());
			return false;
		}
		
		/*
		 * FIXME: check what it is intended for
		 */
		PermissionStatistics.getInstance().countThreads(Thread.currentThread().getThreadGroup().activeCount());
		
		if(perm instanceof AllPermission){
			return checkAllPermission((AllPermission)perm);
		}
		
		if(perm instanceof SecurityPermission){
			return checkSecurityPermission((SecurityPermission) perm);
		}
		
		if(perm instanceof FilePermission){
			return checkFilePermission((FilePermission)perm);
		}
		
		if(perm instanceof LoggingPermission){
			return checkLoggingPermission((LoggingPermission)perm);
		}
		
		if(perm instanceof ReflectPermission){
			return checkReflectPermission((ReflectPermission)perm);
		}
		
		if(perm instanceof PropertyPermission){
			return checkPropertyPermission((PropertyPermission)perm);
		}

		if(perm instanceof RuntimePermission){
			return checkRuntimePermission((RuntimePermission)perm);
		}
		
		if(perm instanceof AWTPermission){
			return checkAWTPermission((AWTPermission)perm);
		}
		
		//TODO: there are several other permission types that we still need to handle 
		
		
		
		//for (int elementCounter = 0; elementCounter < stackTraceElements.length; elementCounter++) {					
			// FIXME: what is this?			 
			//if (e.getClassName().contains("MockingBridge") && Properties.MOCKS)
			//	return true;
		//}

		//TODO put it back
		//logger.error("Unrecognized permission type: "+perm.getClass().getCanonicalName());
		
		return false;
	}
	
	
	/*
	 * Note: many of the String constants used below in the various methods come from
	 * sun.security.util.SecurityConstants
	 * but accessing them directly can issue some warnings, and might make EvoSuite more difficult to port
	 * and use on different OS,installations, or even Java versions
	 */

	
	protected boolean  checkAllPermission(AllPermission perm){
		/*
		 * should never grant all the permissions
		 */
		return false;
	}
	
	
	protected boolean checkSecurityPermission(SecurityPermission perm){
		/*
		 * TODO: quite a few are actually safe
		 */
		return false;
	}
	
	
	protected boolean checkAWTPermission(AWTPermission perm){
		//TODO need to investigate in more details
		if ("true".equals(System.getProperty("java.awt.headless"))){
			return true;
		} else {
			return false;
		}
	}

	
	protected boolean checkRuntimePermission(RuntimePermission perm){
		
		final String name = perm.getName().trim(); 
		
		/*
		 *  At the moment this is the only way to allow classes under test define and load
		 *  other classes, but the way it is done seriously damages security of the program.
		 *  However, as we check permissions based on thread references, it might be safe.
		 *  See comments on allowing reflection. 
		 */
		if (name.equals("getClassLoader")
				|| name.equals("createClassLoader")
				|| name.startsWith("accessClassInPackage")
				|| name.startsWith("defineClassInPackage")
				|| name.equals("setContextClassLoader")
				|| name.equals("accessDeclaredMembers")){
			return true;
		}

		//never allow it!!! far too dangerous, as it would break the sandbox
		if(name.equals("setSecurityManager") || name.equals("createSecurityManager")){
			return false;
		}
		
		//AWT needs to be treated specially
		if ("true".equals(System.getProperty("java.awt.headless"))
		        && isAWTThread()) {
			if (name.equals("shutdownHooks"))
				return true;
			if (name.equals("modifyThreadGroup"))
				return true;
			if (name.equals("modifyThread"))
				return true;
		}

		
		/*
		 * Note: this actually should never be called, as the instrumenting class loader
		 * should replace System.exit 
		 */
		if(name.startsWith("exitVM") || name.equals("shutdownHooks")){
			return false;
		}


		/*
		 * For the moment, we don't allow it. but we might once we address Networking
		 */
		if(name.equals("setFactory")){
			return false;
		}

		/*
		 * As client use logging, and we don't read std/err output from it, then 
		 * it should be safe to allow it 
		 */
		if(name.equals("setIO")){
			return true;
		}
		

		/*
		 * far too risky for the moment, as they might mess up with EvoSuite threads.
		 * But they could be allowed in the future, if we manage to identify the SUT's threads,
		 * and allow such permissions only on those. At any rate, because such  permissions are useful for
		 * multi-thread code, it is not major priority now, because anyway we don't really support that kind
		 * of software yet
		 */
		if(name.equals("modifyThread") ||
				name.equals("stopThread") ||
				name.equals("modifyThreadGroup")){
			return false;
		}
		
		/*
		 * this looks safe... but not 100% sure to be honest
		 */
		if (name.equals("setDefaultUncaughtExceptionHandler")){
			return true;
		}
		
		/*
		 * those are perfectly fine
		 */
		if(name.startsWith("getenv.") || name.equals("getProtectionDomain") || name.equals("readFileDescriptor")){
			return true;
		}
		
		/*
		 * not fully understand this one... so let's block it for now
		 */
		if(name.equals("writeFileDescriptor")){
			return false;
		}

		if (name.startsWith("loadLibrary.")){
			
			/*
			 * There might quite a few risks if SUT uses native code developed by user.
			 * By default, we deny this permissions, but then we can allow the loading of some
			 * specific libraries. 
			 * Ultimately, the user should be able to choose if some libraries can be loaded or not
			 */
			
			String library = name.substring("loadLibrary.".length(), name.length());
			
			if(library.equals("awt") ||
					library.equals("fontmanager") ||
					library.equals("laf")){
				return true;
			}
			
			return false;
		}

		/*
		 * Definitely not! furthermore, testing machine might not have a printer anyway...
		 * if SUT needs a printer, I guess we should somehow manage to mock it.
		 * But low priority, as I don't think many SUTs do print...
		 */
		if (name.equals("queuePrintJob")){
			return false;
		}

		
		/*
		 * Why not? only possible issue is if here, in this security manager, we use stack info to check whether or not
		 * allow permissions
		 */
		if (name.equals("getStackTrace")){
			return true;
		}
		
		/*
		 * writing in persistent backing store of the file system does not sound much safe...
		 */
		if (name.equals("preferences")){
			return false;
		}
		
	
		/*
		 * This is permissions in NIO, which seems safe...
		 */
		if (name.equals("charsetProvider") || name.equals("selectorProvider")){
			return true;
		}
		
		/*
		 * this is also useful for checking types in the String constants, and to be warned if they ll change in
		 * future JDKs 
		 */
		logger.error("SUT asked for a runtime permission that EvoSuite does not recognize: "+name);	
		
		return false; 
	}
	
	protected boolean checkPropertyPermission(PropertyPermission perm){
		/*
		 * we allow both writing and reading any properties.
		 * But, if SUT writes anything, then we need to re-store the values to their default.
		 * this is very important, otherwise:
		 * 1) test cases might have side effects on each other
		 * 2) SUT might change properties that are used by EvoSuite
		 */

		if(perm.getActions().contains("write") && !needToRestoreProperties){
			synchronized(defaultProperties){
				if(!executingTestCase){ //just to be sure
					return false;
				}
				needToRestoreProperties = true;
			}
		}

		return true;
	}

	protected boolean checkReflectPermission(ReflectPermission perm){
		/*
		 * might be some possible side effects but, again, such side-effects
		 * would be confined inside the client JVM.
		 * 
		 * One issue though: the SUT can access to this security manager, and through
		 * reflection mess up its internal state, which might lead to allow other
		 * security permissions...
		 */
		return true;
	}
	
	protected boolean checkLoggingPermission(LoggingPermission perm){
		/*
		 * we allow it because worst thing  it can happen is getting more/less 
		 * log events from client. we might lose some debugging, but really not a big deal,
		 * because in any case log levels are set in master (ie client events do not change them)
		 */
		return true;
	}
	
	protected boolean checkFilePermission(FilePermission fp){
		String action = fp.getActions();
		if(action==null){
			//might not ever happen, but just in case we log it and return false
			logger.warn("File permission with empty action");
			return false;
		}
		
		/*
		 * Reading can be considered "fine", even outside sandbox. 
		 * Only issue I can think of is on Windows, if a process is trying to delete a file a EvoSuite client is reading, then
		 * that deletion would be forbidden, as client has lock. 
		 */
		if(action.equals("read")){
			return true;
		}
		
		/*
		 * FIXME: "contains" is pretty unsecure, and we should check actual path hierarchy. Eg, SUT could create
		 * a file named $Properties.SANDBOX_FOLDER anywhere in the file system
		 */
		if (fp.getName().contains(Properties.SANDBOX_FOLDER)){ 
			/*
			 * need to check "execute". in some cases, for browising directory we need "execute", but we don't
			 * want to execute files!!! (eg scripts) 
			 */
			if(action.equals("write") || action.equals("delete")){ 
				//return true; //TODO allow it once the I/O is fixed and properly tested
			}
		}
		
		return false;
	}
}
