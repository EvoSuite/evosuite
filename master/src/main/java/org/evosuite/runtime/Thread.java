package org.evosuite.runtime;

import java.util.HashMap;
import java.util.Map;

public class Thread {

	public static StackTraceElement[] getStackTrace() {
		StackTraceElement[] stack = new StackTraceElement[3];
		stack[0] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
		stack[1] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
		stack[2] = new StackTraceElement("<evosuite>", "<evosuite>", "<evosuite>", -1);
		return stack;
	}
	
	private static Map<Integer, Long> threadMap = new HashMap<Integer, Long>();
	
	public static String getName(java.lang.Thread t) {
		return "Thread-"+getId(t);
	}
	
	public static long getId(java.lang.Thread t) {
		int identity = java.lang.System.identityHashCode(t);
		if(!threadMap.containsKey(identity)) {
			threadMap.put(identity, Long.valueOf(threadMap.size()));
		}
		
		return threadMap.get(identity);
	}
	
	public static void reset() {
		threadMap.clear();
	}
}
