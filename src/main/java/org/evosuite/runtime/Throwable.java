package org.evosuite.runtime;

import java.io.PrintStream;
import java.io.PrintWriter;

public class Throwable {

	public static void printStackTrace(PrintStream p) {
		for(StackTraceElement elem : Thread.getStackTrace()) {
			p.append(elem.toString());
			p.append("\n");
		}
	}
	
	public static void printStackTrace(PrintWriter p) {
		for(StackTraceElement elem : Thread.getStackTrace()) {
			p.append(elem.toString());
			p.append("\n");
		}		
	}
	
	public static String toString(Throwable t) {
		return t.getClass().getCanonicalName();
	}

	public static String toString(Exception t) {
		return t.getClass().getCanonicalName();
	}

}
