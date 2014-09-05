package org.evosuite.runtime;

import org.evosuite.runtime.agent.InstrumentingAgent;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import java.lang.reflect.Method;
import java.util.List;

/**
 * When running tests with Maven (eg, "mvn test")
 * we need to use this listener (configured from pom.xml).
 * Reason is to address the following issue:
 * "manual" tests executed before EvoSuite ones that
 * lead to load SUT classes while the Java Agent is not
 * active.
 * 
 * <p>
 * Note: re-instrumenting is not really an option,
 * as it has its limitations (eg don't change signature)
 * 
 * @author arcuri
 *
 */
public class InitializingListener extends RunListener{
	
	/**
	 * Name of the method that is used to initialize the SUT classes
	 */
	public static final String INITIALIZE_CLASSES_METHOD = "initializeClasses";
	
	@Override
    public void testRunStarted(Description description) throws Exception {
		
		/*
		 * WARN, Maven bug: Maven calls this method with a
		 * null input :(
		 * This bug as bean reported, but not fixed yet.
		 * 
		 * TODO: once fixed, no need to use tmp files
		 */
	
		if(description != null){
			//can't be in a logger, as used in Maven
			java.lang.System.out.println("WARN: Maven bug has now been fixed. Update EvoSuite");
			return;
		}
		
		List<String> list = classesToInit();
		
		InstrumentingAgent.initialize();
		
		for(String name : list){
			Method m = null;
			try{
				//reflection might load some SUT class
				InstrumentingAgent.activate();
				Class<?> test = InitializingListener.class.getClassLoader().loadClass(name);
				m = test.getDeclaredMethod(INITIALIZE_CLASSES_METHOD);
				m.setAccessible(true);
			} catch(NoSuchMethodException e){
				/*
				 * this is ok.
				 * Note: we could skip the test based on some pattern on the
				 * name, but not really so important in the end
				 */
			} catch(Exception e){
				java.lang.System.out.println("Exception while loading class "+name+": "+e.getMessage());
			}	finally{
				InstrumentingAgent.deactivate();
			}
			
			if(m == null){
				continue;
			}
			
			try{
				m.invoke(null);
			} catch(Exception e){
				java.lang.System.out.println("Exception while calling "+name+"."+INITIALIZE_CLASSES_METHOD+"(): "+e.getMessage());
			}
		}
	}	

	private List<String> classesToInit(){
		return null; //TODO
	}
}
