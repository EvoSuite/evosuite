package org.evosuite.setup.callgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.classpath.ResourceList;
import org.evosuite.setup.CallContext;
import org.evosuite.setup.CallTree;
import org.evosuite.setup.CallTreeEntry;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author mattia
 *
 */

public class CallGraphImpl implements CallGraph, Iterable<CallGraphEntry> {

	private ReverseCallGraph graph = new ReverseCallGraph();

	private static final Logger logger = LoggerFactory
			.getLogger(CallTree.class);

	private final String className;

	private final Set<CallGraphEntry> calls = new LinkedHashSet<CallGraphEntry>();

	// private final Map<String, Set<CallTreeEntry>> incomingCallsMap = new
	// LinkedHashMap<String, Set<CallTreeEntry>>();
	// private final Map<String, Set<CallTreeEntry>> outgoingCallsMap = new
	// LinkedHashMap<String, Set<CallTreeEntry>>();

	private final Set<CallGraphEntry> cutNodes = new LinkedHashSet<CallGraphEntry>();

	private final Set<String> callGraphClasses = new LinkedHashSet<String>();

	private final Set<CallContext> publicMethods = new LinkedHashSet<CallContext>();

	public CallGraphImpl(String className) {
		this.className = className;
	}

	public void addPublicMethod(String className, String methodName) {
		publicMethods.add(new CallContext(ResourceList
				.getClassNameFromResourcePath(className), methodName));
	}

	public void addCall(String owner, String methodName, String targetClass,
			String targetMethod) {

		CallGraphEntry from = new CallGraphEntry(targetClass, targetMethod);
		CallGraphEntry to = new CallGraphEntry(owner, methodName);

		logger.info("Adding new call from: " + from + " -> " + to);

		if (owner.equals(className))
			cutNodes.add(to);

		graph.addEdge(from, to);

		callGraphClasses.add(targetClass.replaceAll("/", "."));
	}

	public boolean hasCall(String owner, String methodName, String targetClass,
			String targetMethod) {

		CallGraphEntry from = new CallGraphEntry(targetClass, targetMethod);
		CallGraphEntry to = new CallGraphEntry(owner, methodName);

		return graph.getEdges().containsKey(to)
				&& graph.getEdges().get(to).contains(from);
	}

	public Set<CallGraphEntry> getCallsFrom(String owner, String methodName) {

		return graph.getEdges().get(new CallGraphEntry(owner, methodName));
	}

	public Set<CallGraphEntry> getCallsFrom(CallGraphEntry call) {

		return graph.getEdges().get(call);
	}

	public Set<CallContext> getPublicContext(String className, String methodName) {
		Set<CallContext> contexts = new HashSet<CallContext>();

		// Check if this method can be called directly
		for (CallContext context : publicMethods) {
			if (context.getRootClassName().equals(className)) {
				if (context.getRootMethodName().equals(methodName)) {
					contexts.add(context);
					break;
				}
			}
		}

		return contexts;
	}

	// TODO: Right now, this only works up to depth 2. Need to add recursion to
	// generalize this.
	public Set<CallContext> getAllContexts(String className, String methodName) {

		CallGraphEntry root = new CallGraphEntry(className, methodName);

		Set<List<CallGraphEntry>> paths = PathFinder.getPahts(graph, root);

		return convertIntoCallContext(paths);

	}

	private Set<CallContext> convertIntoCallContext(
			Set<List<CallGraphEntry>> paths) {
		Set<CallContext> contexts = new HashSet<>();

		for (List<CallGraphEntry> list : paths) {
			CallContext c = new CallContext();
			for (int i = list.size() - 1; i >= 0; i--) {
				c.addCalledMethod(list.get(i).getClassName(), list.get(i)
						.getMethodName());
			}
			contexts.add(c);
		}
		return contexts;
	}

//	private Set<CallContext> getDirectCallingContext(String className,
//			String methodName) {
//		CallContext context = new CallContext(className, methodName);
//		Set<CallContext> contexts = new LinkedHashSet<CallContext>();
//
//		// Check if the method can also be called indirectly
//		for (CallTreeEntry entry : calls) {
//			if (entry.getTargetClass().equals(className)
//					&& entry.getTargetMethod().equals(methodName)) {
//				CallContext superContext = context.getSuperContext(
//						entry.getSourceClass(), entry.getSourceMethod());
//				contexts.add(superContext);
//			}
//		}
//		return contexts;
//	} 



	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Determine if className can be called through the target class
	 * 
	 * TODO optimize it with a dedicated DFS that stop the search as soon as it find the node
	 * @param className
	 * @return
	 */
	public boolean isCalledClass(String className) {
		for (CallGraphEntry e : graph.getEdges().keySet()) {
			if (e.getClassName().equals(className)) {
				for (List<CallGraphEntry> c : PathFinder.getPahts(graph, e)) {
					for (CallGraphEntry entry : c) {
						if (entry.getClassName().equals(this.className))
							return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Determine if methodName of className can be called through the target
	 * class
	 * TODO optimize it with a dedicated DFS that stop the search as soon as it find the node
	 * @param className
	 * @param methodName
	 * @return
	 */
	public boolean isCalledMethod(String className, String methodName) {
		CallGraphEntry tmp = new CallGraphEntry(className, methodName);
		for (CallGraphEntry e : graph.getEdges().keySet()) {
			if (e.equals(tmp)) {
				for (List<CallGraphEntry> c : PathFinder.getPahts(graph, e)) {
					for (CallGraphEntry entry : c) {
						if (entry.getClassName().equals(this.className))
							return true;
					}
				}
			}
		}
		return false;
	} 

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<CallGraphEntry> iterator() {
		return calls.iterator();
	}

	public Set<CallGraphEntry> getViewOfCurrentCalls() {
		return new LinkedHashSet<CallGraphEntry>(calls);
	}


	@Override
	public Set<String> getClasses() {
 		return callGraphClasses;
	}

}
