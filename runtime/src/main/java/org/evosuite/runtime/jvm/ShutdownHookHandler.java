/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.jvm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import org.evosuite.runtime.sandbox.Sandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This singleton class is used to store/run the the shutdown thread hooks that
 * have been registered during test execution 
 * 
 * @author arcuri
 *
 */
public class ShutdownHookHandler {

	private static final Logger logger = LoggerFactory.getLogger(ShutdownHookHandler.class);
	
	private static ShutdownHookHandler instance = new ShutdownHookHandler();
	
	/**
	 * A reference to the actual map in the JVM that holds the shutdown
	 * threads. This is initialized by reflection
	 */
	private IdentityHashMap<Thread, Thread> hooksReference;
	
	/**
	 * Map of existing hooks before test case execution.
	 * This is needed to identify the new ones that will be added
	 */
	private IdentityHashMap<Thread, Thread> existingHooks;
	
	/**
	 * Singleton constructor
	 * @throws Exception 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	@SuppressWarnings("unchecked")
	private ShutdownHookHandler() {
		
		try{
			Field field = Class.forName("java.lang.ApplicationShutdownHooks").getDeclaredField("hooks");
			field.setAccessible(true);
			hooksReference = (IdentityHashMap<Thread, Thread>)field.get(null);
			
		} catch(Exception e){
			/*
			 * This should never happen, unless new JVM do change the API we are
			 * accessing by reflection
			 */
			String msg = "Failed to initialize shutdown hook handling";
			logger.error(msg);
		}
	}
	
	public static ShutdownHookHandler getInstance(){
		return instance;
	}
	
	/**
	 * Important to check what hooks are currently registered
	 */
	public void initHandler(){
		if(hooksReference == null){
			return; //
		}
		
		if(existingHooks != null){
			List<Thread> list = removeNewHooks();
			if(list!=null && !list.isEmpty()) {
				//only log if there were SUT hooks not executed
				logger.warn("Previous hooks were not executed. Going to remove them");
			}
		}
		
		existingHooks = new IdentityHashMap<>();
		existingHooks.putAll(hooksReference);
	}
	
	/**
	 * Get all hooks added since the call to initHandler
	 * but before executeAddedHooks
	 * 
	 * @return
	 */
	public List<Thread> getAddedHooks(){
		if(hooksReference == null || existingHooks == null){
			return null; 
		}
		
		List<Thread> list = new ArrayList<>();
		for(Thread t : hooksReference.values()){
			if(! existingHooks.containsKey(t)){
				list.add(t);
			}
		}
		
		return list;
	}
	
	/**
	 * This is mainly needed for test/debugging
	 * @return
	 */
	public int getNumberOfAllExistingHooks(){
		if(hooksReference==null){
			return -1;
		}
		return hooksReference.size();
	}
	
	/**
	 * If the JVM is halted, then no shutdown hook should be executed.
	 * This usually happen when Runtime.getRuntime().halt() is called. 
	 */
	public void processWasHalted(){
		removeNewHooks();
	}


		
	/**
	 * Run {@link ShutdownHookHandler#executeAddedHooks()} in a try/catch
	 * 
	 * @return a negative value if there was any exception
	 */
	public int safeExecuteAddedHooks(){
		
		//the shutdown hook threads should still be checked against the sandbox
		boolean safe = Sandbox.isSafeToExecuteSUTCode();
		int n = -1;

		assert !Sandbox.isSecurityManagerInitialized() || Sandbox.isOnAndExecutingSUTCode() :
				"Executing hooks outside of a test case, but with sandbox on";

		try{
			if(!safe){
				Sandbox.goingToExecuteUnsafeCodeOnSameThread();
			}
			n =  executeAddedHooks();
		} catch(Throwable t){
			logger.debug("Shutdown hooks threw exception: "+t);
		} finally{
			if(!safe){
				Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
			}
		}
		return n;
	}
	
	/**
	 * Execute all added shutdown hooks on this thread.
	 * They are removed from the JVM.
	 * 
	 * @return the number of executed shutdown threads
	 */
	public int executeAddedHooks(){
		List<Thread> list = removeNewHooks();
		if(list==null || list.isEmpty()){
			return 0;
		}
		
		/* now execute on this thread.
		 * 
		 * note: they are not executed in parallel due to simplify testing/debugging
		 * of the generated test cases
		 * 
		 * if they throw an exception, or if they hang, that will be handled by the
		 * generated tests
		 */
		
		for(Thread t : list){
			t.run();
		}
		
		return list.size();
	}

	private List<Thread> removeNewHooks() {
		List<Thread> list = getAddedHooks();
		existingHooks = null;

		if(list==null || list.isEmpty()){
			return null;
		}
		
		//first remove them from JVM hooks
		for(Thread t : list){
			hooksReference.remove(t);
		}

		return list;
	}
}
