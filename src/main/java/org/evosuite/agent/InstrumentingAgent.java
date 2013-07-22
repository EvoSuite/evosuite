package org.evosuite.agent;

import java.lang.instrument.Instrumentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class  InstrumentingAgent {

	private static final Logger logger = LoggerFactory.getLogger(InstrumentingAgent.class);

	private static TransformerForTests transformer = new TransformerForTests();

	public static void premain(String args, Instrumentation inst) throws Exception {
		inst.addTransformer(transformer);
	}

	public static void agentmain(String args, Instrumentation inst) throws Exception {
		inst.addTransformer(transformer);
	}

	public static void initialize() {
		AgentLoader.loadAgent();
	}

	public static TransformerForTests getTransformer() {
		return transformer;
	}

	public static void activate(){
		transformer.activate();
	}
	
	public static void deactivate(){
		transformer.deacitvate();
	}
}