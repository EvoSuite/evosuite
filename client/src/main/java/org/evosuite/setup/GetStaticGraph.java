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
package org.evosuite.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a graph with the INVOKESTATIC/GETSTATIC relation.
 * The main usage of this graph is to get the set of all the static fields that are
 * read in static methods that are reachable from the target class.
 * The method <code>getStaticFields</code> returns that information.
 *
 * @author galeotti
 */
public class GetStaticGraph {

    GetStaticGraph() {
    }

    private static final Logger logger = LoggerFactory
            .getLogger(GetStaticGraph.class);

    private final Set<StaticFieldReadEntry> staticFieldReads = new LinkedHashSet<>();
    private final Set<StaticMethodCallEntry> staticMethodCalls = new LinkedHashSet<>();

    /**
     * Returns if there is a static method call egde (INVOKESTATIC bytecode
     * instruction) from <owner,methodName> to <targetClass,targetField>.
     *
     * @param owner
     * @param methodName
     * @param targetClass
     * @param targetMethod
     * @return
     */
    public boolean hasStaticMethodCall(String owner, String methodName,
                                       String targetClass, String targetMethod) {
        StaticMethodCallEntry call = new StaticMethodCallEntry(owner,
                methodName, targetClass, targetMethod);
        return staticMethodCalls.contains(call);
    }

    /**
     * Add a static method call (bytecode instruction INVOKESTATIC) to the graph.
     *
     * @param owner
     * @param methodName
     * @param targetClass
     * @param targetMethod
     */
    public void addStaticMethodCall(String owner, String methodName,
                                    String targetClass, String targetMethod) {
        StaticMethodCallEntry call = new StaticMethodCallEntry(owner,
                methodName, targetClass, targetMethod);
        logger.info("Adding new static method call: " + call);
        staticMethodCalls.add(call);
    }

    /**
     * Returns if there is a static field read egde (GETSTATIC bytecode
     * instruction)from <owner,methodName> to <targetClass,targetField>.
     *
     * @param owner
     * @param methodName
     * @param targetClass
     * @param targetField
     * @return
     */
    public boolean hasStaticFieldRead(String owner, String methodName,
                                      String targetClass, String targetField) {
        StaticFieldReadEntry read = new StaticFieldReadEntry(owner, methodName,
                targetClass, targetField);
        return staticFieldReads.contains(read);
    }

    /**
     * Add a static field read (bytecode instruction GETSTATIC) to the graph
     *
     * @param owner
     * @param methodName
     * @param targetClass
     * @param targetField
     */
    public void addStaticFieldRead(String owner, String methodName,
                                   String targetClass, String targetField) {
        StaticFieldReadEntry read = new StaticFieldReadEntry(owner, methodName,
                targetClass, targetField);
        logger.info("Adding new static field read: " + read);
        staticFieldReads.add(read);

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((staticFieldReads == null) ? 0 : staticFieldReads.hashCode());
        result = prime
                * result
                + ((staticMethodCalls == null) ? 0 : staticMethodCalls
                .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GetStaticGraph other = (GetStaticGraph) obj;
        if (staticFieldReads == null) {
            if (other.staticFieldReads != null)
                return false;
        } else if (!staticFieldReads.equals(other.staticFieldReads))
            return false;
        if (staticMethodCalls == null) {
            return other.staticMethodCalls == null;
        } else return staticMethodCalls.equals(other.staticMethodCalls);
    }

    /**
     * Returns the set of class names (with dots) of those classes
     * such that there is a at least one edge and the class is the source of
     * the edge.
     *
     * @return
     */
    public Set<String> getSourceClasses() {
        Set<String> sourceClasses = new LinkedHashSet<>();
        for (StaticFieldReadEntry entry : staticFieldReads) {
            sourceClasses.add(entry.getSourceClass().replace('/', '.'));
        }
        for (StaticMethodCallEntry entry : staticMethodCalls) {
            sourceClasses.add(entry.getSourceClass().replace('/', '.'));
        }
        return sourceClasses;
    }

    /**
     * Returns the set of class names (with dots) of those classes
     * such that there is a at least one edge and the class is the target of
     * the edge.
     *
     * @return
     */
    public Set<String> getTargetClasses() {
        Set<String> targetClasses = new LinkedHashSet<>();
        for (StaticFieldReadEntry entry : staticFieldReads) {
            targetClasses.add(entry.getTargetClass().replace('/', '.'));
        }
        for (StaticMethodCallEntry entry : staticMethodCalls) {
            targetClasses.add(entry.getTargetClass().replace('/', '.'));
        }
        return targetClasses;
    }

    /**
     * Returns a classname->set(fieldname) with those static fields reached by
     * static methods (included <clinit>)
     *
     * @return
     */
    public Map<String, Set<String>> getStaticFields() {
        Map<String, Set<String>> staticFields = new LinkedHashMap<>();
        for (StaticFieldReadEntry read : this.staticFieldReads) {
            String className = read.getTargetClass().replace('/', '.');
            if (!staticFields.containsKey(className)) {
                staticFields.put(className, new LinkedHashSet<>());
            }
            staticFields.get(className).add(read.getTargetField());
        }
        return staticFields;
    }
}
