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
package org.evosuite.testcase.fm;

import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericMethod;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by Andrea Arcuri on 27/07/15.
 */
public class MethodDescriptor implements Comparable<MethodDescriptor>, Serializable {

    private static final long serialVersionUID = -6747363265640233704L;

    protected static final Logger logger = LoggerFactory.getLogger(MethodDescriptor.class);

    private final String methodName;
    private final String inputParameterMatchers;
    private final String className;
    /**
     * How often the method was called
     */
    private int counter;

    private GenericMethod method;

    private String id; //derived field


    /**
     * @param method     the one that is going to be mocked
     * @param retvalType type of the class the mocked method belongs to. The type might be parameterized (ie generics)
     */
    public MethodDescriptor(Method method, GenericClass<?> retvalType) {
        Inputs.checkNull(method, retvalType);
        this.method = new GenericMethod(method, retvalType);
        methodName = method.getName();
        className = method.getDeclaringClass().getName();
        inputParameterMatchers = initMatchers(this.method, retvalType);
    }

    private MethodDescriptor(GenericMethod m, String methodName, String className, String inputParameterMatchers) {
        this.method = m;
        this.methodName = methodName;
        this.className = className;
        this.inputParameterMatchers = inputParameterMatchers;
    }

    private String initMatchers(GenericMethod method, GenericClass<?> retvalType) {

        String matchers = "";
        Type[] types = method.getParameterTypes();
        List<GenericClass<?>> parameterClasses = method.getParameterClasses();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                matchers += " , ";
            }

            GenericClass<?> genericParameter = parameterClasses.get(i);
            Type type = genericParameter.getRawClass();
            if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
                matchers += "anyInt()";
            } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
                matchers += "anyLong()";
            } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
                matchers += "anyBoolean()";
            } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
                matchers += "anyDouble()";
            } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
                matchers += "anyFloat()";
            } else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
                matchers += "anyShort()";
            } else if (type.equals(Character.TYPE) || type.equals(Character.class)) {
                matchers += "anyChar()";
            } else if (type.equals(String.class)) {
                matchers += "anyString()";
            } else if (type.equals(List.class)) {
                matchers += "anyList()";
            } else if (type.equals(Set.class)) {
                matchers += "anySet()";
            } else if (type.equals(Map.class)) {
                matchers += "anyMap()";
            } else if (type.equals(Collection.class)) {
                matchers += "anyCollection()";
            } else if (type.equals(Iterable.class)) {
                matchers += "anyIterable()";
            } else {
                if (type.getTypeName().equals(Object.class.getName())) {
                    /*
                        Ideally here we should use retvalType to understand if the target class
                        is using generics and if this method parameters would need to be handled
                        accordingly. However, doing it does not seem so trivial...
                        so a current workaround is that, when a method takes an Object as input (which is
                        that would happen in case of Generics T), we use the undetermined "any()"
                     */
                    matchers += "any()";
                } else {
                    if (type instanceof Class) {
                        matchers += "any(" + ((Class) type).getCanonicalName() + ".class)";
                    } else {
                        //what to do here? is it even possible?
                        matchers += "nullable(" + genericParameter.getRawClass().getCanonicalName() + ".class)";
                        // matchers += "any(" + type.getTypeName() + ".class)";
                    }
                }
            }
        }

        return matchers;
    }


    public void changeClassLoader(ClassLoader loader) {
        method.changeClassLoader(loader);
    }

    /**
     * For example, do not mock methods with no return value
     *
     * @return
     */
    public boolean shouldBeMocked() {

        int modifiers = method.getMethod().getModifiers();

        if (method.getReturnType().equals(Void.TYPE) ||
                method.getName().equals("equals") ||
                method.getName().equals("hashCode") ||
                Modifier.isPrivate(modifiers)) {

            return false;
        }

        if (Properties.hasTargetClassBeenLoaded()) {
            //null can happen in some unit tests

            if (!Modifier.isPublic(modifiers)) {
                assert !Modifier.isPrivate(modifiers); //previous checks

                String sutName = Properties.TARGET_CLASS;

                int lastIndexMethod = className.lastIndexOf('.');
                int lastIndexSUT = sutName.lastIndexOf('.');

                boolean samePackage;
                if (lastIndexMethod != lastIndexSUT) {
                    samePackage = false;
                } else if (lastIndexMethod < 0) {
                    samePackage = true; //default package
                } else {
                    samePackage = className.substring(0, lastIndexMethod).equals(sutName.substring(0, lastIndexSUT));
                }

                return samePackage;
            }
        } else {
            logger.warn("The target class should be loaded before invoking this method");
        }

        return true;
    }

    public MethodDescriptor getCopy() {
        MethodDescriptor copy = new MethodDescriptor(method, methodName, className, inputParameterMatchers);
        copy.counter = this.counter;
        return copy;
    }

    public int getNumberOfInputParameters() {
        return method.getNumParameters();
    }

    public Object executeMatcher(int i) throws IllegalArgumentException {
        if (i < 0 || i >= getNumberOfInputParameters()) {
            throw new IllegalArgumentException("Invalid index: " + i);
        }

        Type[] types = method.getParameterTypes();
        List<GenericClass<?>> parameterClasses = method.getParameterClasses();
        GenericClass<?> parameterClass = parameterClasses.get(i);
        Class<?> type = parameterClass.getRawClass();

        try {
            if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
                return Mockito.anyInt();
            } else if (type.equals(Long.TYPE) || type.equals(Long.class)) {
                return Mockito.anyLong();
            } else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
                return Mockito.anyBoolean();
            } else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
                return Mockito.anyDouble();
            } else if (type.equals(Float.TYPE) || type.equals(Float.class)) {
                return Mockito.anyFloat();
            } else if (type.equals(Short.TYPE) || type.equals(Short.class)) {
                return Mockito.anyShort();
            } else if (type.equals(Character.TYPE) || type.equals(Character.class)) {
                return Mockito.anyChar();
            } else if (type.equals(String.class)) {
                return Mockito.anyString();
            } else if (type.equals(List.class)) {
                return Mockito.anyList();
            } else if (type.equals(Set.class)) {
                return Mockito.anySet();
            } else if (type.equals(Map.class)) {
                return Mockito.anyMap();
            } else if (type.equals(Collection.class)) {
                return Mockito.anyCollection();
            } else if (type.equals(Iterable.class)) {
                return Mockito.anyIterable();
            } else {
                return Mockito.nullable(type);
            }
        } catch (Exception e) {
            logger.error("Failed to executed Mockito matcher n{} of type {} in {}.{}: {}", i, type, className, methodName, e.getMessage());
            throw new EvosuiteError(e);
        }
    }

    @Deprecated // better (more precise results) to use the other constructor
    public MethodDescriptor(String className, String methodName, String inputParameterMatchers) throws IllegalArgumentException {
        Inputs.checkNull(methodName, inputParameterMatchers);
        this.className = className;
        this.methodName = methodName;
        this.inputParameterMatchers = inputParameterMatchers;
        counter = 0;
    }

    public GenericMethod getGenericMethodFor(GenericClass<?> clazz) throws ConstructionFailedException {
        return method.getGenericInstantiation(clazz);
    }

    public GenericClass<?> getReturnClass() {
        return method.getGeneratedClass();
    }

    public Method getMethod() {
        assert method != null;
        return method.getMethod();
    }

    public GenericMethod getGenericMethod() {
        return method;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getInputParameterMatchers() {
        return inputParameterMatchers;
    }

    public String getID() {
        if (id == null) {
            id = className + "." + getMethodName() + "#" + getInputParameterMatchers();
        }
        return id;
    }

    public int getCounter() {
        return counter;
    }

    public void increaseCounter() {
        counter++;
    }


    @Override
    public int compareTo(MethodDescriptor o) {
        int com = this.className.compareTo(o.className);
        if (com != 0) {
            return com;
        }
        com = this.methodName.compareTo(o.methodName);
        if (com != 0) {
            return com;
        }
        com = this.inputParameterMatchers.compareTo(o.inputParameterMatchers);
        if (com != 0) {
            return com;
        }
        return this.counter - o.counter;
    }
}
