/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.evosuite.runtime.agent.InstrumentingAgent;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

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
	
	/**
	 * File name of list of scaffolding files to use for initialization.
	 * Note: this is needed only till Maven bug gets fixed
	 */
	public static final String SCAFFOLDING_LIST_FILE_STRING = ".scaffolding_list.tmp";
	
	@Override
    public void testRunStarted(Description description) throws Exception {
		
		java.lang.System.out.println("Executing "+InitializingListener.class.getName());
		
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

        /*
            TODO: this is not 100% correct, but anyway this is done only when running tests with "mvn test"
            by the final users, not really in the experiments.
            So, activating everything should be fine
         */
        RuntimeSettings.activateAllMocking();
        RuntimeSettings.mockSystemIn = true;
        RuntimeSettings.resetStaticState = true;

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
				
		List<String> list = new ArrayList<>();
		
		File scaffolding = new File(InitializingListener.SCAFFOLDING_LIST_FILE_STRING);
		if(! scaffolding.exists()){
			java.lang.System.out.println("WARN: scaffolding file not found. If this modules has tests, recall to use 'evosuite:prepare'");
			return list;
		}

		Scanner in = null;
		try {
			in = new Scanner(scaffolding);
			while(in.hasNext()){
				list.add(in.next().trim());
			}
		} catch (Exception e) {
			java.lang.System.out.println("ERROR while reading scaffolding list file: "+e.getMessage());
		} finally {
			if(in != null){
				in.close();
			}
		}
		
		return list; 
	}
}
