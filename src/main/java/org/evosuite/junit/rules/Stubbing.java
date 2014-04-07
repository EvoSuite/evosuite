package org.evosuite.junit.rules;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DebugGraphics;

public class Stubbing extends BaseRule {

	private Map<String, String> propertiesToSet = new HashMap<String, String>();
	
	private Set<String> propertiesToClear = new LinkedHashSet<String>();
	
	private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties().clone();
	
	private PrintStream systemOut = null;
	
	private PrintStream systemErr = null;
	
	private PrintStream logStream = null;
	  
	public Stubbing() {
		org.evosuite.Properties.REPLACE_CALLS = true;
		initProperties();
	}
	
	public void initProperties() {
		
	}
	
	public void setProperty(String key, String value) {
		propertiesToSet.put(key, value);
	}
	
	public void clearProperty(String key) {
		propertiesToClear.add(key);
	}
	
	@Override
	protected void before() {
		org.evosuite.runtime.Runtime.getInstance().resetRuntime();
	    systemErr = System.err; 
	    systemOut = System.out; 
	    logStream = DebugGraphics.logStream(); 
		for(String key : propertiesToSet.keySet()) {
		    java.lang.System.setProperties((java.util.Properties) defaultProperties.clone()); 
		    java.lang.System.setProperty(key, propertiesToSet.get(key));
		}
		for(String key : propertiesToClear){
			java.lang.System.clearProperty(key);
		}

	}

	@Override
	protected void after() {
	    System.setErr(systemErr); 
	    System.setOut(systemOut); 
	    DebugGraphics.setLogStream(logStream);
	    java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
	}

}
