package org.evosuite.agent;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class  InstrumentingAgent {

	private static final Logger logger = LoggerFactory.getLogger(InstrumentingAgent.class);

	private static TransformerForTests transformer = new TransformerForTests();

	/**
	 * This is called by JVM when agent starts
	 * @param args
	 * @param inst
	 * @throws Exception
	 */
	public static void premain(String args, Instrumentation inst) throws Exception {
		inst.addTransformer(transformer);
	}

	/**
	 * This is called by JVM when agent starts
	 * @param args
	 * @param inst
	 * @throws Exception
	 */
	public static void agentmain(String args, Instrumentation inst) throws Exception {
		inst.addTransformer(transformer);
	}

	/**
	 * Force the dynamic loading of the agent
	 */
	public static void initialize() {
		AgentLoader.loadAgent();
	}

	public static TransformerForTests getTransformer() {
		return transformer;
	}

	/**
	 * Once loaded, an agent will always read the byte[] 
	 * of the loaded classes. Here we tell it if those byte[]
	 * should be instrumented
	 */
	public static void activate(){
		transformer.activate();
	}
	
	/**
	 * Stop instrumenting classes
	 */
	public static void deactivate(){
		transformer.deacitvate();
	}
}