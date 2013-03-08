/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.setup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class CallTree implements Iterable<CallTreeEntry> {

	private static final Logger logger = LoggerFactory.getLogger(CallTree.class);
	
	private final String className;

	private final Set<CallTreeEntry> calls = new LinkedHashSet<CallTreeEntry>();

	private final Set<CallTreeEntry> rootCalls = new LinkedHashSet<CallTreeEntry>();

	private final Map<CallTreeEntry, Set<CallTreeEntry>> callMap = new LinkedHashMap<CallTreeEntry, Set<CallTreeEntry>>();

	private final Set<String> callTreeClasses = new LinkedHashSet<String>();

	public CallTree(String className) {
		this.className = className;
	}

	public void addCall(String owner, String methodName, String targetClass,
	        String targetMethod) {
		CallTreeEntry call = new CallTreeEntry(owner, methodName, targetClass,
		        targetMethod);
		logger.info("Adding new call: "+call.toString());
		calls.add(call);
		if (owner.equals(className))
			rootCalls.add(call);
		if (!callMap.containsKey(call))
			callMap.put(call, new LinkedHashSet<CallTreeEntry>());
		// callMap.get(call).add(call);
		callTreeClasses.add(targetClass.replaceAll("/", "."));
	}

	public Set<String> getClasses() {
		return callTreeClasses;
	}

	public boolean hasCall(String owner, String methodName, String targetClass,
	        String targetMethod) {
		CallTreeEntry call = new CallTreeEntry(owner, methodName, targetClass,
		        targetMethod);
		return calls.contains(call);
	}

	public void addCalls(Set<CallTreeEntry> calls) {
		this.calls.addAll(calls);
		for (CallTreeEntry call : calls) {
			if (call.getSourceClass().equals(className))
				rootCalls.add(call);
		}
	}

	public Set<CallTreeEntry> getCallsFrom(String owner, String methodName) {
		Set<CallTreeEntry> callSet = new LinkedHashSet<CallTreeEntry>();
		for (CallTreeEntry call : rootCalls) {
			if (call.getSourceMethod().equals(methodName)) {
				callSet.addAll(getCallsFrom(call));
			}
		}
		return callSet;
	}

	public Set<CallTreeEntry> getCallsFrom(CallTreeEntry call) {
		Set<CallTreeEntry> callSet = new LinkedHashSet<CallTreeEntry>();
		for (CallTreeEntry otherCall : rootCalls) {

		}
		return callSet;
	}

	public Set<CallContext> getAllContexts(String className, String methodName) {
		Set<CallContext> contexts = new HashSet<CallContext>();
		// TODO: implement
		// TODO: A rootCall only exists for methods which call other methods
		//       we also need methods that don't call other methods!
		for(CallTreeEntry entry : rootCalls) {
			logger.info("Root call: "+entry.toString());
		}
		return contexts;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Determine if className can be called through the target class
	 * 
	 * @param className
	 * @return
	 */
	public boolean isCalledClass(String className) {
		for (CallTreeEntry call : calls) {
			if (call.getTargetClass().equals(className))
				return true;
		}
		return false;
	}

	/**
	 * Determine if methodName of className can be called through the target
	 * class
	 * 
	 * @param className
	 * @param methodName
	 * @return
	 */
	public boolean isCalledMethod(String className, String methodName) {
		for (CallTreeEntry call : calls) {
			if (call.getTargetClass().equals(className))
				if (call.getTargetMethod().equals(methodName))
					return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<CallTreeEntry> iterator() {
		return calls.iterator();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		LoggingUtils.getEvoLogger().info("Calls: " + calls.size());
		StringBuilder builder = new StringBuilder();

		for (CallTreeEntry call : calls) {
			builder.append(call.toString());
			builder.append("\n");
		}

		return builder.toString();
	}
}
