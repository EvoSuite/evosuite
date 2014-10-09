package org.evosuite.setup.callgraph;

import java.util.Set;

/**
 * 
 * @author mattia
 *
 */
public interface CallGraph {

	public boolean isCalledClass(String className);

	public Set<String> getClasses();

	public void addPublicMethod(String className, String methodName);

	public boolean hasCall(String owner, String methodName, String targetClass,
			String targetMethod);

	public void addCall(String owner, String methodName, String targetClass,
			String targetMethod);

}
