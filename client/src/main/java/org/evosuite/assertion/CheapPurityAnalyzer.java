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
package org.evosuite.assertion;

import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.InheritanceTree;
import org.evosuite.setup.TestCluster;
import org.evosuite.utils.JdkPureMethodsList;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class performs a very cheap purity analysis by under-approximating the set of
 * pure methods. It does not use any kind of escape-analysis. The purity analysis
 * is solely based on already collected bytecode instructions during class loading.
 * A method is <i>cheap-pure</i> if and only if:
 * <ul>
 * 	<li>The method is listed in the JdkPureMethodList</li>
 * 	<li>There is no declared overriding method that is not <i>cheap-pure</i></li>
 *  <li>All invoked classes are loaded in the inheritance tree</li>
 * 	<li>Has no PUTSTATIC nor PUTFIELD instructions</li>
 * 	<li>All static invocations (INVOKESTATIC) are made to <i>cheap-pure</i> static methods</li>
 *  <li>All special invocations (INVOKESPECIAL) are also made to <i>cheap-pure</i> methods</li>
 *  <li>All interface invocations (INVOKEINTERFACE) are also made to <i>cheap-pure</i> methods</li>
 * </ul>
 *
 * @author galeotti
 */
public class CheapPurityAnalyzer {

    private static final Logger logger = LoggerFactory
            .getLogger(CheapPurityAnalyzer.class);

    private final HashSet<MethodEntry> updateFieldMethodList = new HashSet<>();
    private final HashMap<MethodEntry, Boolean> purityCache = new HashMap<>();
    private final HashSet<MethodEntry> methodEntries = new HashSet<>();

    /**
     * We return this value when we can't conclude if a given method is pure or not.
     */
    private static final boolean DEFAULT_PURITY_VALUE = false;

    private static final CheapPurityAnalyzer instance = new CheapPurityAnalyzer();

    public static CheapPurityAnalyzer getInstance() {
        return instance;
    }

    public List<String> getPureMethods(String className) {
        ArrayList<String> list = new ArrayList<>();
        for (MethodEntry m : methodEntries) {
            if (m.className.equals(className) && isPure(m) && !m.methodName.equals(ClassResetter.STATIC_RESET)) {
                list.add(m.methodName + m.descriptor);
            }
        }
        return list;
    }

    /**
     * Returns if the method is cheap-pure.
     *
     * @param className  The declaring class name
     * @param methodName The method name
     * @param descriptor The method descriptor
     * @return true if the method is cheap-pure, false otherwise
     */
    public boolean isPure(String className, String methodName, String descriptor) {
        MethodEntry entry = new MethodEntry(className, methodName, descriptor);
        return isPure(entry);
    }

    private boolean isPure(MethodEntry entry) {
        Stack<MethodEntry> emptyStack = new Stack<>();
        return isPure(entry, emptyStack);
    }

    private boolean isCached(MethodEntry entry) {
        return this.purityCache.containsKey(entry);
    }

    private boolean getCacheValue(MethodEntry entry) {
        return this.purityCache.get(entry);
    }

    private void addCacheValue(MethodEntry entry, boolean new_value) {
        if (isCached(entry)) {
            boolean old_value = this.purityCache.get(entry);
            if (old_value == false && new_value == true) {
                String fullyQuantifiedMethodName = entry.className + "."
                        + entry.methodName + entry.descriptor;

                logger.warn("Purity value in cache cannot evolve from NOT_PURE to PURE for method "
                        + fullyQuantifiedMethodName);
            }
        }
        this.purityCache.put(entry, new_value);
    }

    private boolean isPure0(MethodEntry entry, Stack<MethodEntry> callStack) {
        if (isRandomCall(entry)) {
            return false;
        }

        if (isArrayCall(entry)) {
            return true;
        }

        if (isJdkPureMethod(entry)) {
            return true;
        }

        if (!BytecodeInstrumentation.checkIfCanInstrument(entry.className)) {
            return false;
        }

        if (this.updateFieldMethodList.contains(entry)) {
            // If the method has an implementation that
            // modifies any field, we conclude the method is
            // NOT PURE
            return false;
        }

        if (staticCalls.containsKey(entry)) {
            Set<MethodEntry> calls = staticCalls.get(entry);
            if (checkAnyCallImpure(calls, entry, callStack)) {
                // If the method has an implementation that
                // invokes at least one *static method*  that is not
                // pure, we conclude the method is NOT PURE
                return false;
            }
        }

        if (specialCalls.containsKey(entry)) {
            Set<MethodEntry> calls = specialCalls.get(entry);
            if (checkAnyCallImpure(calls, entry, callStack)) {
                // If the method has an implementation that
                // has at least one *special call* that is not
                // pure, we conclude the method is NOT PURE
                return false;
            }
        }

        if (virtualCalls.containsKey(entry)) {
            Set<MethodEntry> calls = virtualCalls.get(entry);
            if (checkAnyCallImpure(calls, entry, callStack)) {
                // If the method has an implementation that
                // invokes at least one *virtual method* that is not
                // pure, we conclude the method is NOT PURE
                return false;
            }
        }

        if (interfaceCalls.containsKey(entry)) {
            Set<MethodEntry> calls = interfaceCalls.get(entry);
            if (checkAnyCallImpure(calls, entry, callStack)) {
                // If the method has an implementation that
                // has at least one *interface call* that is not
                // pure, we conclude the method is NOT PURE
                return false;
            }
        }

        // check overriding methods
        if (checkAnyOverridingMethodImpure(entry, callStack)) {
            // If there is any descendant of this class that
            // has a declaration for this method that is not pure,
            // we conclude the method is NOT PURE
            return false;
        }

        if (this.interfaceMethodEntries.contains(entry)) {
            // IF this is an interface method returns true
            // since we could not find any implementor of
            // this method interface that is impure
            return true;
        }

        if (this.methodsWithBodies.contains(entry)) {
            // This means a method body for Foo.m() declared in Foo
            // and there are no reasons to think Foo.m()
            // (namely no calls to impure methods, no field updates,
            // no descendant that has an impure implementation),
            // we conclude the method is *PURE*
            return true;
        } else {
            // This means there is no body for Foo.m() declared in Foo,
            // but there is no impure descendant of Foo.m() also.
            // Then, the closest implementation of m() in the
            // superclasses of Foo should be checked since this might
            // be called during runtime
            boolean purityValueClosestSuperclass = isPureSuperclass(entry, callStack);

            return purityValueClosestSuperclass;
        }

    }

    private boolean isPureSuperclass(MethodEntry entry, Stack<MethodEntry> callStack) {
        InheritanceTree inheritanceTree = TestCluster.getInheritanceTree();
        for (String superClassName : inheritanceTree
                .getOrderedSuperclasses(entry.className)) {
            if (superClassName.equals(entry.className))
                continue;
            MethodEntry superEntry = new MethodEntry(superClassName,
                    entry.methodName, entry.descriptor);
            if (!callStack.contains(superEntry) && methodsWithBodies.contains(superEntry)) {
                // We can conclude the purity of this method because
                // we found an implementation in a super class for it
                Stack<MethodEntry> newStack = new Stack<>();
                newStack.addAll(callStack);
                newStack.add(superEntry);
                boolean purityValueForSuperClass = isPure(superEntry, newStack);
                return purityValueForSuperClass;
            }
        }

        // We cannot conclusive decide if the
        // method is pure or not, so we fail
        // to default purity value
        return DEFAULT_PURITY_VALUE;
    }

    private boolean isRandomCall(MethodEntry entry) {
        if (entry.className.equals("java.util.Random"))
            return true;
        else if (entry.className.equals("java.security.SecureRandom"))
            return true;
        else if (entry.className.equals(org.evosuite.runtime.Random.class.getName()))
            return true;
        else return entry.className.equals("java.lang.Math")
                    && entry.methodName.equals("random");
    }

    /**
     * clone, equals, getClass, hashCode, toString seem pure
     * TODO: finalize, notify, notifyAll, wait?
     *
     * @param entry
     * @return
     */
    private boolean isArrayCall(MethodEntry entry) {
        return entry.className.startsWith("[");
    }

    private boolean isPure(MethodEntry entry, Stack<MethodEntry> callStack) {
        if (isCached(entry)) {
            return getCacheValue(entry);
        } else {
            boolean isPure = isPure0(entry, callStack);
            addCacheValue(entry, isPure);
            return isPure;
        }
    }

    private boolean checkAnyOverridingMethodImpure(MethodEntry entry,
                                                   Stack<MethodEntry> callStack) {
        InheritanceTree inheritanceTree = DependencyAnalysis
                .getInheritanceTree();

        String className = "" + entry.className;
//		while (className.contains("[L")) {
//			className = className.substring(2, className.length() - 1);
//		}

        if (!inheritanceTree.hasClass(className)) {
            logger.warn(className
                    + " was not found in the inheritance tree. Using DEFAULT value for cheap-purity analysis");
            return DEFAULT_PURITY_VALUE;
        }

        Set<String> subclasses = inheritanceTree.getSubclasses(className);
        for (String subclassName : subclasses) {
            if (!entry.className.equals(subclassName)) {

                MethodEntry subclassEntry = new MethodEntry(subclassName,
                        entry.methodName, entry.descriptor);

                if (!callStack.contains(subclassEntry)
                        && methodEntries.contains(subclassEntry)) {

                    Stack<MethodEntry> newStack = new Stack<>();
                    newStack.addAll(callStack);
                    newStack.add(subclassEntry);
                    if (!isPure(subclassEntry, newStack)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isJdkPureMethod(MethodEntry entry) {
        String paraz = entry.descriptor;
        Type[] parameters = org.objectweb.asm.Type.getArgumentTypes(paraz);
        String newParams = "";
        if (parameters.length != 0) {
            for (Type i : parameters) {
                newParams = newParams + "," + i.getClassName();
            }
            newParams = newParams.substring(1);
        }
        String qualifiedName = entry.className + "." + entry.methodName + "("
                + newParams + ")";

        return (JdkPureMethodsList.instance.checkPurity(qualifiedName));
    }

    private boolean checkAnyCallImpure(Set<MethodEntry> calls,
                                       MethodEntry entry, Stack<MethodEntry> callStack) {
        for (MethodEntry callMethodEntry : calls) {
            if (!callStack.contains(callMethodEntry)) {
                Stack<MethodEntry> copyOfStack = new Stack<>();
                copyOfStack.addAll(callStack);
                copyOfStack.add(entry);
                if (!isPure(callMethodEntry, copyOfStack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns if a Method is <code>cheap-pure</code>
     *
     * @param method
     * @return true if the method is cheap-pure, otherwise false.
     */
    public boolean isPure(java.lang.reflect.Method method) {
        // Using getName rather than getCanonicalName because that's what
        // the inheritancetree also uses
        String className = method.getDeclaringClass().getName();
        if (MockList.isAMockClass(className)) {
            className = method.getDeclaringClass().getSuperclass().getName();
        }

        String methodName = method.getName();
        String descriptor = Type.getMethodDescriptor(method);

        MethodEntry entry = new MethodEntry(className, methodName, descriptor);
        boolean isPureValue = isPure(entry);
        return isPureValue;
    }

    private static class MethodEntry {
        private final String className;
        private final String methodName;
        private final String descriptor;

        public MethodEntry(String className, String methodName,
                           String descriptor) {
            this.className = className;
            this.methodName = methodName;
            this.descriptor = descriptor;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + className.hashCode();
            result = prime * result + descriptor.hashCode();
            result = prime * result + methodName.hashCode();
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
            MethodEntry other = (MethodEntry) obj;
            return (className.equals(other.className) && methodName
                    .equals(other.methodName))
                    && descriptor.equals(other.descriptor);
        }

        @Override
        public String toString() {
            return "MethodEntry [className=" + className + ", methodName="
                    + methodName + ", descriptor=" + descriptor
                    + "]";
        }
    }

    public void addMethod(String className, String methodName,
                          String methodDescriptor) {
        MethodEntry entry = new MethodEntry(className, methodName,
                methodDescriptor);
        methodEntries.add(entry);
    }

    public void addUpdatesFieldMethod(String className, String methodName,
                                      String descriptor) {
        String classNameWithDots = className.replace('/', '.');
        MethodEntry entry = new MethodEntry(classNameWithDots, methodName,
                descriptor);
        updateFieldMethodList.add(entry);
    }

    private final HashMap<MethodEntry, Set<MethodEntry>> staticCalls = new HashMap<>();
    private final HashMap<MethodEntry, Set<MethodEntry>> virtualCalls = new HashMap<>();
    private final HashMap<MethodEntry, Set<MethodEntry>> specialCalls = new HashMap<>();
    private final HashMap<MethodEntry, Set<MethodEntry>> interfaceCalls = new HashMap<>();

    public void addStaticCall(String sourceClassName, String sourceMethodName,
                              String sourceDescriptor, String targetClassName,
                              String targetMethodName, String targetDescriptor) {

        addCall(staticCalls, sourceClassName, sourceMethodName,
                sourceDescriptor, targetClassName, targetMethodName,
                targetDescriptor);

    }

    public void addVirtualCall(String sourceClassName, String sourceMethodName,
                               String sourceDescriptor, String targetClassName,
                               String targetMethodName, String targetDescriptor) {

        addCall(virtualCalls, sourceClassName, sourceMethodName,
                sourceDescriptor, targetClassName, targetMethodName,
                targetDescriptor);

    }

    public void addInterfaceCall(String sourceClassName,
                                 String sourceMethodName, String sourceDescriptor,
                                 String targetClassName, String targetMethodName,
                                 String targetDescriptor) {

        addCall(interfaceCalls, sourceClassName, sourceMethodName,
                sourceDescriptor, targetClassName, targetMethodName,
                targetDescriptor);

    }

    private static void addCall(HashMap<MethodEntry, Set<MethodEntry>> calls,
                                String sourceClassName, String sourceMethodName,
                                String sourceDescriptor, String targetClassName,
                                String targetMethodName, String targetDescriptor) {

        MethodEntry sourceEntry = new MethodEntry(sourceClassName,
                sourceMethodName, sourceDescriptor);
        MethodEntry targetEntry = new MethodEntry(targetClassName,
                targetMethodName, targetDescriptor);
        if (!calls.containsKey(sourceEntry)) {
            calls.put(sourceEntry, new HashSet<>());
        }
        calls.get(sourceEntry).add(targetEntry);
    }

    public void addSpecialCall(String sourceClassName, String sourceMethodName,
                               String sourceDescriptor, String targetClassName,
                               String targetMethodName, String targetDescriptor) {

        addCall(specialCalls, sourceClassName, sourceMethodName,
                sourceDescriptor, targetClassName, targetMethodName,
                targetDescriptor);
    }

    private final HashSet<MethodEntry> interfaceMethodEntries = new HashSet<>();

    private final HashSet<MethodEntry> methodsWithBodies = new HashSet<>();

    public void addInterfaceMethod(String className, String methodName,
                                   String methodDescriptor) {
        MethodEntry entry = new MethodEntry(className, methodName,
                methodDescriptor);
        interfaceMethodEntries.add(entry);
    }

    public void addMethodWithBody(String className, String methodName,
                                  String methodDescriptor) {
        MethodEntry entry = new MethodEntry(className, methodName,
                methodDescriptor);
        methodsWithBodies.add(entry);
    }

}
