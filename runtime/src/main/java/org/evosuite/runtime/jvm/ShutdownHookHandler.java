package org.evosuite.runtime.jvm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

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
	 * Importat to check what hooks are currently registered
	 */
	public void initHandler(){
		if(hooksReference == null){
			return; //
		}
		
		if(existingHooks != null){
			logger.warn("Previous hooks were not executed. Going to remove them");
			removeNewHooks();
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
		 * if they throw an exception, or if they hand, that will be handled by the
		 * generated tests
		 */
		
		for(Thread t : list){
			t.run();
		}
		
		return list.size();
	}

	private List<Thread> removeNewHooks() {
		List<Thread> list = getAddedHooks();
		if(list==null || list.isEmpty()){
			return null;
		}
		
		//first remove them from JVM hooks
		for(Thread t : list){
			hooksReference.remove(t);
		}

		existingHooks = null;
		return list;
	}
}
