/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.setup.callgraph;

import org.evosuite.Properties;
import org.evosuite.classpath.ResourceList;
import org.evosuite.setup.Call;
import org.evosuite.setup.CallContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * CallGraph implementation. Based on the previous implementation in the
 * CallTree class. This class is a wrapper of the Graph class. I didn't use the
 * jgrapht graph classes on purpose because I had problems with the DFS
 * algorithms implemented for them. On the bright hand, this implementation
 * should be more efficient.
 *
 * @author mattia
 */

public class CallGraph implements Iterable<CallGraphEntry> {

    /**
     * The CallGraphImpl class is a wrap of the Graph class. Internally the
     * graph is represented reversed, i.e. if a method m1 points to a method m2,
     * the graph connects the methods with an edge from m2 to m1. The methods in
     * this class however mask this representation.
     */
    private final ReverseCallGraph graph = new ReverseCallGraph();

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory
            .getLogger(CallGraph.class);

    private final String className;

    private final Set<CallGraphEntry> cutNodes = Collections.synchronizedSet(new LinkedHashSet<>());

    private final Set<String> callGraphClasses = Collections.synchronizedSet(new LinkedHashSet<>());

    private final Set<String> toTestClasses = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Set<String> toTestMethods = Collections.synchronizedSet(new LinkedHashSet<>());

    private final Set<String> notToTestClasses = Collections.synchronizedSet(new LinkedHashSet<>());


    private final Set<CallContext> publicMethods = Collections.synchronizedSet(new LinkedHashSet<>());

    public CallGraph(String className) {
        this.className = className;
    }

    public ReverseCallGraph getGraph() {
        return graph;
    }

    public void removeClasses(Collection<CallGraphEntry> vertexes) {
        for (CallGraphEntry vertex : vertexes) {
            graph.removeVertex(vertex);
        }
    }

    public void removeClass(CallGraphEntry vertex) {
        graph.removeVertex(vertex);
    }

    /**
     * add public methods
     *
     * @param className
     * @param methodName
     */
    public void addPublicMethod(String className, String methodName) {
        publicMethods.add(new CallContext(ResourceList
                .getClassNameFromResourcePath(className), methodName));
    }

    /**
     * add call to the call graph
     *
     * @param sourceClass
     * @param sourceMethod
     * @param targetClass
     * @param targetMethod
     */

    public boolean addCall(String sourceClass, String sourceMethod,
                           String targetClass, String targetMethod) {
        CallGraphEntry from = new CallGraphEntry(targetClass, targetMethod);
        CallGraphEntry to = new CallGraphEntry(sourceClass, sourceMethod);

//		logger.info("Adding new call from: " + to + " -> " + from);

        if (sourceClass.equals(className))
            cutNodes.add(to);

        if (!graph.containsEdge(from, to)) {
            graph.addEdge(from, to);
            callGraphClasses.add(targetClass.replaceAll("/", "."));
            return true;
        }
        return false;
    }

    /**
     * @return true if the method is in the callgraph.
     */
    public boolean hasMethod(String classname, String methodName) {
        return graph.containsVertex(new CallGraphEntry(classname, methodName));
    }

    /**
     * @return true if the call is in the callgraph.
     */
    public boolean hasCall(String owner, String methodName, String targetClass,
                           String targetMethod) {

        CallGraphEntry from = new CallGraphEntry(targetClass, targetMethod);
        CallGraphEntry to = new CallGraphEntry(owner, methodName);

        return graph.getEdges().containsKey(to)
                && graph.getEdges().get(to).contains(from);
    }

    /**
     * @return calls exiting from the method, empty set if the call is not in
     * the graph.
     */
    public Set<CallGraphEntry> getCallsFrom(String owner, String methodName) {
        CallGraphEntry call = new CallGraphEntry(owner, methodName);
        return getCallsFromMethod(call);
    }

    /**
     * @return calls exiting from the method, empty set if the call is not in
     * the graph
     */
    public Set<CallGraphEntry> getCallsFromMethod(CallGraphEntry call) {
        if (graph.getEdges().containsKey(call))
            return graph.getEdges().get(call);
        else {
            return new HashSet<>();
        }
    }

    /**
     * computes and returns the call contexts of the specific method
     *
     * @param className
     * @param methodName
     * @return
     */
    public Set<CallContext> getMethodEntryPoint(String className, String methodName) {
        Set<CallContext> contexts = new HashSet<>();
        List<Call> cont = new ArrayList<>();
        cont.add(new Call(className, methodName));
        CallContext context = new CallContext(cont);
        if (publicMethods.contains(context)) {
            contexts.add(context);
        } else {
            contexts.add(new CallContext());
        }
        return contexts;
    }

    /**
     * computes and returns the call contexts that starts from the target class
     * and end in the specific method
     *
     * @param className
     * @param methodName
     * @return
     */
    public Set<CallContext> getAllContextsFromTargetClass(String className, String methodName) {
        CallGraphEntry root = new CallGraphEntry(className, methodName);
        Set<List<CallGraphEntry>> paths = PathFinder.getPahts(graph, root);
        Set<CallContext> contexts = convertIntoCallContext(paths);
        if (!Properties.EXCLUDE_IBRANCHES_CUT)
            addPublicClassMethod(className, methodName, contexts);
        return contexts;
    }

    private void addPublicClassMethod(String className, String methodName, Set<CallContext> contexts) {
        List<Call> calls = new ArrayList<>();
        Call call = new Call(className, methodName);
        calls.add(call);
        CallContext context = new CallContext(calls);
        if (publicMethods.contains(context) && className.equals(this.className))
            contexts.add(context);
    }

    private Set<CallContext> convertIntoCallContext(
            Set<List<CallGraphEntry>> paths) {
        Set<CallContext> contexts = new HashSet<>();

        // return only context that starts from the class under test
        for (List<CallGraphEntry> list : paths) {
            boolean insert = false;
            List<Call> cont = new ArrayList<>();

            for (int i = list.size() - 1; i >= 0; i--) {
                if (!insert && list.get(i).getClassName().equals(className)) {
                    insert = true;
                }
                if (insert)
                    cont.add(new Call(list.get(i).getClassName(), list.get(i)
                            .getMethodName()));
            }
            contexts.add(new CallContext(cont));
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
     * @return classes reachable from the class under test
     */
    public Set<String> getClassesUnderTest() {
        if (toTestClasses.isEmpty())
            computeInterestingClasses(graph);
        return toTestClasses;
    }

    /**
     * Determine if className can be reached from the class under test
     *
     * @param className
     * @return
     */
    public boolean isCalledClass(String className) {
        if (toTestClasses.isEmpty())
            computeInterestingClasses(graph);
        return toTestClasses.contains(className);
    }

    public boolean isCalledClassOld(String className) {
        if (toTestClasses.contains(className)) return true;
        if (notToTestClasses.contains(className)) return false;

        for (CallGraphEntry e : graph.getEdges().keySet()) {
            if (e.getClassName().equals(className)) {
                if (checkClassInPaths(this.className, graph, e))
                    return true;
            }
        }
        return false;
    }

    private boolean computeInterestingClasses(Graph<CallGraphEntry> g) {
        Set<CallGraphEntry> startingVertices = new HashSet<>();
        for (CallGraphEntry e : graph.getVertexSet()) {
            if (e.getClassName().equals(className)) {
                startingVertices.add(e);
            }
        }
        Set<String> classes = new HashSet<>();
        Set<String> methodclasses = new HashSet<>();
        for (CallGraphEntry startingVertex : startingVertices) {
            PathFinderDFSIterator<CallGraphEntry> dfs = new PathFinderDFSIterator<>(
                    g, startingVertex, true);
            while (dfs.hasNext()) {
                CallGraphEntry e = dfs.next();
                classes.add(e.getClassName());
                methodclasses.add(e.getClassName() + e.getMethodName());
            }
        }
        toTestMethods.addAll(methodclasses);
        toTestClasses.addAll(classes);
        return true;
    }

    private boolean checkClassInPaths(String targetClass, Graph<CallGraphEntry> g, CallGraphEntry startingVertex) {
        if (!g.containsVertex(startingVertex)) {
            return false;
        }
        Set<String> classes = new HashSet<>();
        PathFinderDFSIterator<CallGraphEntry> dfs = new PathFinderDFSIterator<>(g, startingVertex);
        while (dfs.hasNext()) {
            CallGraphEntry e = dfs.next();
            classes.add(e.getClassName());
            if (e.getClassName().equals(targetClass)) {
                toTestClasses.addAll(classes);
                return true;
            }
        }
        notToTestClasses.addAll(classes);
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
        if (toTestMethods.isEmpty())
            computeInterestingClasses(graph);
        return toTestMethods.contains(className + methodName);
    }

    public boolean isCalledMethodOld(String className, String methodName) {


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
        return graph.getVertexSet().iterator();
    }

    /**
     * @return a copy of the current vertexset
     */
    public Set<CallGraphEntry> getViewOfCurrentMethods() {
        return new LinkedHashSet<>(graph.getVertexSet());
    }

    /**
     * @return set of class names.
     */
    public Set<String> getClasses() {
        return callGraphClasses;
    }

}
