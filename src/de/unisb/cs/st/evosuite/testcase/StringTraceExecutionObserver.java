package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class StringTraceExecutionObserver extends ExecutionObserver {

	private static Logger logger = Logger.getLogger(StringTraceExecutionObserver.class);
	
	Map<Integer,String> trace = new HashMap<Integer,String>();
	
	@Override
	public void output(int position, String output) {
		//logger.info("Received output: "+output);
		//trace.put(position, output.trim());
		// TODO: can't use this for oracles
	}
	
	public void statement(int position, Scope scope, VariableReference retval) {
		Object object = scope.get(retval);
		
		//System.out.println("TG: Adding value "+object.toString());
		// Only add string if this is not Object.toString()
		try {
			if(object == null) {
				//logger.info("Received return value null");
				trace.put(position, "null");
			}
			else {
				Set<String> unusable = new HashSet<String>();
				unusable.add("java.lang.Object");
				//unusable.add("java.util.AbstractCollection");
				//unusable.add("org.jaxen.pattern.UnionPattern");
				//unusable.add("org.jaxen.pattern.LocationPathPattern");
				String declared_class = object.getClass().getMethod("toString").getDeclaringClass().getName();

//				if(!object.getClass().getMethod("toString").getDeclaringClass().equals(java.lang.Object.class)) {
				if(!unusable.contains(declared_class)) {
					String value = object.toString();
					if(value == null) {
						//logger.info("Received return value that converts to null string");
						trace.put(position, "null");
					}
					else {
						if(!value.matches("@[abcdef\\d]+")) {
							value = value.replaceAll("@[abcdef\\d]+", "");
							//logger.info(object.getClass().getMethod("toString").getDeclaringClass()+" says: "+value);
							trace.put(position, value);
						}
					}
				}
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		catch(Throwable e) {
			logger.debug("Failed to add object of class " +object.getClass()+" to string trace: "+e.getMessage());
		}
	}

	public StringOutputTrace getTrace() {
		return new StringOutputTrace(trace);
	}

	@Override
	public void clear() {
		trace.clear();
	}
	
}
