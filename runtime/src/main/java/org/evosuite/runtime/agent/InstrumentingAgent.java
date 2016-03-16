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
package org.evosuite.runtime.agent;

import java.lang.instrument.Instrumentation;

import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.mock.MockFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the entry point for the JavaAgent.
 * This is responsible to instrument the code in
 * the generated JUnit test cases.
 * During EvoSuite search, EvoSuite does not need an agent,
 * as instrumentation can be done at classloader level.
 * 
 * <p>
 * Note: we need JavaAgent in JUnit as the classes could have already
 * been loaded/instrumented, eg as it happens when tools like
 * Emma, Cobertura, Javalanche, etc., are used.
 * 
 * @author arcuri
 *
 */
public class  InstrumentingAgent {

	private static final Logger logger = LoggerFactory.getLogger(InstrumentingAgent.class);

	private static volatile TransformerForTests transformer;

	private static Instrumentation instrumentation;
		
	
	static{
		try{
			transformer = new TransformerForTests();
		} catch(Exception e){
			logger.error("Failed to initialize TransformerForTests: "+e.getMessage(),e);
			transformer = null;
		}
	}
	
	/**
	 * This is called by JVM when agent starts
	 * @param args
	 * @param inst
	 * @throws Exception
	 */
	public static void premain(String args, Instrumentation inst) throws Exception {
		logger.info("Executing premain of JavaAgent");
		checkTransformerState();
		inst.addTransformer(transformer,true);
		instrumentation = inst;
	}

	/**
	 * This is called by JVM when agent starts
	 * @param args
	 * @param inst
	 * @throws Exception
	 */
	public static void agentmain(String args, Instrumentation inst) throws Exception {
		logger.info("Executing agentmain of JavaAgent");
		checkTransformerState();
		inst.addTransformer(transformer,true);
		instrumentation = inst;
	}

	private static void checkTransformerState() throws IllegalStateException{
		if(transformer == null){
			String msg = "TransformerForTests was not properly initialized";
			logger.error(msg);
			throw new IllegalStateException(msg);
		}
	}

	/**
	 * Force the dynamic loading of the agent
	 */
	public static void initialize() {
		MockFramework.disable(); //need an explicit "activate" call
		AgentLoader.loadAgent();
	}

	public static TransformerForTests getTransformer() throws IllegalStateException{
		checkTransformerState();
		return transformer;
	}

	public static Instrumentation getInstrumentation(){
		return instrumentation;
	}
	
	/**
	 * Once loaded, an agent will always read the byte[] 
	 * of the loaded classes. Here we tell it if those byte[]
	 * should be instrumented
	 */
	public static void activate(){
		checkTransformerState();
		MockFramework.enable();
		transformer.activate();
		LoopCounter.getInstance().setActive(true);
	}
	
	/**
	 * Stop instrumenting classes
	 */
	public static void deactivate(){
		checkTransformerState();
		MockFramework.disable();
		transformer.deactivate();
		LoopCounter.getInstance().setActive(false);
	}
	
	/**
	 *  Tells EvoSuite that we are going to re-instrument classes.
	 *  In these cases, we cannot change the class signatures
	 *  
	 * @param on
	 */
	public static void setRetransformingMode(boolean on){
		transformer.setRetransformingMode(on);
	}
}