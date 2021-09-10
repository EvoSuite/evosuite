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
package org.evosuite.runtime.instrumentation;

import org.evosuite.PackageInfo;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.*;
import org.evosuite.runtime.mock.java.lang.MockThrowable;
import org.evosuite.runtime.util.ReflectionUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gordon
 */
public class MethodCallReplacementCache {

    private static final Logger logger = LoggerFactory.getLogger(MethodCallReplacementCache.class);

    private static MethodCallReplacementCache instance = null;

    /**
     * method replacements, which are called with Opcodes.INVOKESTATIC
     */
    private final Map<String, Map<String, MethodCallReplacement>> replacementCalls = new HashMap<>();

    /**
     * method replacements, which are called with Opcodes.INVOKEVIRTUAL
     */
    // private final Set<MethodCallReplacement> virtualReplacementCalls = new
    // HashSet<MethodCallReplacement>();

    /**
     * method replacements, which are called with Opcodes.INVOKESPECIAL
     */
    private final Map<String, Map<String, MethodCallReplacement>> specialReplacementCalls = new HashMap<>();

    private MethodCallReplacementCache() {

        if (RuntimeSettings.mockJVMNonDeterminism) {

            // java.lang.*
            addJavaLangCalls();

            // javax.swing.JComponent.getPreferredSize()
            addReplacementCall(new MethodCallReplacement("javax/swing/JComponent", "getPreferredSize",
                    "()Ljava/awt/Dimension;", Opcodes.INVOKEVIRTUAL,
                    PackageInfo.getNameWithSlash(org.evosuite.runtime.mock.javax.swing.MockJComponent.class),
                    "getPreferredSize", "()Ljava/awt/Dimension;", true, false));

            addExtraceExceptionReplacements();

        }

        handleMockList();

    }

    public static MethodCallReplacementCache getInstance() {
        if (instance == null) {
            instance = new MethodCallReplacementCache();
        }
        return instance;
    }

    public static void resetSingleton() {
        instance = null;
    }

    private void addReplacementCall(MethodCallReplacement replacement) {
        if (!replacementCalls.containsKey(replacement.getClassName())) {
            replacementCalls.put(replacement.getClassName(), new HashMap<>());
        }
        replacementCalls.get(replacement.getClassName()).put(replacement.getMethodNameWithDesc(), replacement);
    }

    private void addSpecialReplacementCall(MethodCallReplacement replacement) {
        if (!specialReplacementCalls.containsKey(replacement.getClassName())) {
            specialReplacementCalls.put(replacement.getClassName(), new HashMap<>());
        }
        specialReplacementCalls.get(replacement.getClassName()).put(replacement.getMethodNameWithDesc(), replacement);
    }

    // private void addVirtualReplacementCall(MethodCallReplacement replacement)
    // {
    // virtualReplacementCalls.add(replacement);
    // }

    public boolean hasReplacementCall(String className, String methodNameWithDesc) {
        if (!replacementCalls.containsKey(className))
            return false;

        return replacementCalls.get(className).containsKey(methodNameWithDesc);
    }

    public MethodCallReplacement getReplacementCall(String className, String methodNameWithDesc) {
        return replacementCalls.get(className).get(methodNameWithDesc);
    }

    public boolean hasSpecialReplacementCall(String className, String methodNameWithDesc) {
        if (!specialReplacementCalls.containsKey(className))
            return false;

        return specialReplacementCalls.get(className).containsKey(methodNameWithDesc);
    }

    public MethodCallReplacement getSpecialReplacementCall(String className, String methodNameWithDesc) {
        return specialReplacementCalls.get(className).get(methodNameWithDesc);
    }

    // public Iterator<MethodCallReplacement> getVirtualReplacementCalls() {
    // return virtualReplacementCalls.iterator();
    // }

    /**
     * Ideally, all mocking should be handled by either OverrideMock or
     * StaticReplacementMock. However, even in such cases, we would not be able
     * to get mocks from classes that are not instrumented (eg, other Java API
     * classes we do not mock). In theory, we should mock all of them. The
     * problem raises for "Exceptions", as they are used everywhere, and would
     * require to mock the full Java API (which is not going to happen...).
     * Solution is, beside using OverrideMock for them, to also a further static
     * replacement (implemented in this method).
     * <p/>
     * <p/>
     * Note: why not just using static replacement instead of OverrideMock?
     * Because static replacement will not work if an exception instance is used
     * in a non-instrumented class, whereas OverrideMock would. Still, it could
     * be tedious to prepare OverrideMock for every single type of exception, so
     * the static replacement here could be a temporary workaround
     */
    private void addExtraceExceptionReplacements() {

        List<Class<? extends Throwable>> classes = Arrays.asList(IOException.class,
                // following java.lang
                Throwable.class, ArithmeticException.class, ArrayIndexOutOfBoundsException.class,
                ArrayStoreException.class, ClassCastException.class, ClassNotFoundException.class,
                CloneNotSupportedException.class, EnumConstantNotPresentException.class, Exception.class,
                IllegalAccessException.class, IllegalArgumentException.class, IllegalMonitorStateException.class,
                IllegalStateException.class, IllegalThreadStateException.class, IndexOutOfBoundsException.class,
                InstantiationException.class, InterruptedException.class, NegativeArraySizeException.class,
                NoSuchFieldException.class, NoSuchMethodException.class, NullPointerException.class,
                NumberFormatException.class, ReflectiveOperationException.class, RuntimeException.class,
                SecurityException.class, StringIndexOutOfBoundsException.class, TypeNotPresentException.class,
                UnsupportedOperationException.class, AbstractMethodError.class, AssertionError.class,
                BootstrapMethodError.class, ClassCircularityError.class, ClassFormatError.class, Error.class,
                ExceptionInInitializerError.class, IllegalAccessError.class, IncompatibleClassChangeError.class,
                InstantiationError.class, InternalError.class, LinkageError.class, NoClassDefFoundError.class,
                NoSuchFieldError.class, NoSuchMethodError.class, OutOfMemoryError.class, StackOverflowError.class,
                ThreadDeath.class, UnknownError.class, UnsatisfiedLinkError.class, UnsupportedClassVersionError.class,
                VerifyError.class, VirtualMachineError.class);

        for (Class<?> k : classes) {

            String jvmOriginal = k.getName().replace('.', '/');
            String jvmMock = MockThrowable.class.getName().replace('.', '/');

            addReplacementCall(new MethodCallReplacement(jvmOriginal, "getStackTrace",
                    "()[Ljava/lang/StackTraceElement;", Opcodes.INVOKEVIRTUAL, jvmMock, "replacement_getStackTrace",
                    "(Ljava/lang/Throwable;)[Ljava/lang/StackTraceElement;", false, false));

            addReplacementCall(new MethodCallReplacement(jvmOriginal, "printStackTrace", "(Ljava/io/PrintStream;)V",
                    Opcodes.INVOKEVIRTUAL, jvmMock, "replacement_printStackTrace",
                    "(Ljava/lang/Throwable;Ljava/io/PrintStream;)V", false, false));

            addReplacementCall(new MethodCallReplacement(jvmOriginal, "printStackTrace", "(Ljava/io/PrintWriter;)V",
                    Opcodes.INVOKEVIRTUAL, jvmMock, "replacement_printStackTrace",
                    "(Ljava/lang/Throwable;Ljava/io/PrintWriter;)V", false, false));
        }

    }

    private void handleMockList() {
        for (Class<? extends EvoSuiteMock> mock : MockList.getList()) {

            if (OverrideMock.class.isAssignableFrom(mock)) {
                replaceAllConstructors(mock, mock.getSuperclass());
                replaceAllStaticMethods(mock, mock.getSuperclass());
                replaceAllInvokeSpecial(mock, mock.getSuperclass());
                handleStaticReplacementMethods(mock);
            } else if (StaticReplacementMock.class.isAssignableFrom(mock)) {

                String mockedName;
                try {
                    mockedName = ((StaticReplacementMock) mock.newInstance()).getMockedClassName();
                } catch (InstantiationException | IllegalAccessException e1) {
                    logger.error("Cannot instantiate mock " + mock.getCanonicalName());
                    continue;
                }
                Class<?> mocked;
                try {
                    mocked = StaticReplacementMock.class.getClassLoader().loadClass(mockedName);
                } catch (ClassNotFoundException e) {
                    // should never happen
                    logger.error(
                            "Mock class " + mock.getCanonicalName() + " has non-existent mocked target " + mockedName);
                    continue;
                }

                replaceAllStaticMethods(mock, mocked);
                replaceAllInstanceMethodsWithStatic(mock, mocked);
                replaceAllConstructorsWithStaticCalls(mock, mocked);
            }
        }
    }

    private void handleStaticReplacementMethods(Class<? extends EvoSuiteMock> mockClass) {

        for (Method m : mockClass.getMethods()) {

            StaticReplacementMethod srm = m.getAnnotation(StaticReplacementMethod.class);
            if (srm == null) {
                continue;
            }

            if (!Modifier.isStatic(m.getModifiers())) {
                throw new RuntimeException("EvoSuite Bug: improper annotations in class " + mockClass.getName());
            }

            String target;
            if (OverrideMock.class.isAssignableFrom(mockClass)) {
                target = mockClass.getSuperclass().getCanonicalName();
            } else {
                throw new RuntimeException("EvoSuite Bug: StaticReplacementMethod can only be used in OverrideMock");
            }

            String desc = Type.getMethodDescriptor(m);
            addSpecialReplacementCall(
                    new MethodCallReplacement(target.replace('.', '/'), m.getName(), desc, Opcodes.INVOKESPECIAL,
                            mockClass.getCanonicalName().replace('.', '/'), m.getName(), desc, false, false));

        }
    }

    private void addJavaLangCalls() {

        // java/lang/System
        addReplacementCall(new MethodCallReplacement("java/lang/System", "exit", "(I)V", Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.runtime.System.class), "exit", "(I)V", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/System", "setSecurityManager", "(Ljava/lang/SecurityManager;)V", Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.runtime.System.class), "setSecurityManager", "(Ljava/lang/SecurityManager;)V", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/System", "currentTimeMillis", "()J",
                Opcodes.INVOKESTATIC, PackageInfo.getNameWithSlash(org.evosuite.runtime.System.class),
                "currentTimeMillis", "()J", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/System", "nanoTime", "()J", Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.runtime.System.class), "nanoTime", "()J", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/System", "identityHashCode", "(Ljava/lang/Object;)I",
                Opcodes.INVOKESTATIC, "org/evosuite/runtime/System", "identityHashCode", "(Ljava/lang/Object;)I", false,
                false));

        // java/lang/Object
        addReplacementCall(new MethodCallReplacement("java/lang/Object", "hashCode", "()I", Opcodes.INVOKEVIRTUAL,
                PackageInfo.getNameWithSlash(org.evosuite.runtime.System.class), "identityHashCode",
                "(Ljava/lang/Object;)I", false, false));

        addReplacementCall(new MethodCallReplacement("java/lang/Object", "toString", "()Ljava/lang/String;",
                Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.System.class), "toString",
                "(Ljava/lang/Object;)Ljava/lang/String;", false, false));

        addReplacementCall(new MethodCallReplacement("java/lang/Math", "random", "()D", Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(org.evosuite.runtime.Random.class), "nextDouble", "()D", false, false));

        // java/lang/Class
        addReplacementCall(new MethodCallReplacement("java/lang/Class", "getClasses", "()[Ljava/lang/Class;",
                Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class),
                "getClasses", "(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
        addReplacementCall(
                new MethodCallReplacement("java/lang/Class", "getAnnotations", "()[Ljava/lang/annotation/Annotation;",
                        Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class),
                        "getAnnotations", "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/Class", "getFields", "()[Ljava/lang/reflect/Field;",
                Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class), "getFields",
                "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", false, false));
        addReplacementCall(
                new MethodCallReplacement("java/lang/Class", "getConstructors", "()[Ljava/lang/reflect/Constructor;",
                        Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class),
                        "getConstructors", "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/Class", "getMethods", "()[Ljava/lang/reflect/Method;",
                Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class),
                "getMethods", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/Class", "getDeclaredClasses", "()[Ljava/lang/Class;",
                Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class),
                "getDeclaredClasses", "(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/Class", "getDeclaredAnnotations",
                "()[Ljava/lang/annotation/Annotation;", Opcodes.INVOKEVIRTUAL,
                PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class), "getDeclaredAnnotations",
                "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", false, false));
        addReplacementCall(
                new MethodCallReplacement("java/lang/Class", "getDeclaredFields", "()[Ljava/lang/reflect/Field;",
                        Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class),
                        "getDeclaredFields", "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/Class", "getDeclaredConstructors",
                "()[Ljava/lang/reflect/Constructor;", Opcodes.INVOKEVIRTUAL,
                PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class), "getDeclaredConstructors",
                "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", false, false));
        addReplacementCall(
                new MethodCallReplacement("java/lang/Class", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;",
                        Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class),
                        "getDeclaredMethods", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/Class", "getInterfaces", "()[Ljava/lang/Class;",
                Opcodes.INVOKEVIRTUAL, PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class),
                "getInterfaces", "(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
        addReplacementCall(new MethodCallReplacement("java/lang/Class", "getModifiers", "()I", Opcodes.INVOKEVIRTUAL,
                PackageInfo.getNameWithSlash(org.evosuite.runtime.Reflection.class), "getModifiers",
                "(Ljava/lang/Class;)I", false, false));

        /*
         *
         * FIXME: it is unclear why getResource was mocked away by always
         * returning null. At least at a first look, it seems quite wrong, and
         * it breaks quite a few static initializers (eg in Liferay). If it led
         * to some unstable tests, then we could just mock it properly
         *
         * //java/lang/ClassLoader addReplacementCall(new
         * MethodCallReplacement("java/lang/ClassLoader", "getResource",
         * "(Ljava/lang/String;)Ljava/net/URL;", Opcodes.INVOKEVIRTUAL,
         * "org/evosuite/runtime/ResourceLoader", "getResource",
         * "(Ljava/lang/String;)Ljava/net/URL;", true, false));
         */
    }

    private void replaceAllInstanceMethodsWithStatic(Class<?> mockClass, Class<?> target) {

        /*
         * replace "fooInstance.bar(x)" with "MockFooClass.bar(fooInstance,x)"
         */

        for (Method m : ReflectionUtils.getMethods(target)) {
            if (Modifier.isStatic(m.getModifiers())) {
                continue;
            }

            /*
             * TODO: should check Object methods, and see if they were mocked.
             * If not, we could just skip them. If other methods are not mocked,
             * should throw an exception?
             */
            Class<?>[] parameters = new Class<?>[m.getParameterCount() + 1];
            parameters[0] = target;
            int numParam = 1;
            for (Class<?> paramClass : m.getParameterTypes()) {
                parameters[numParam++] = paramClass;
            }

            try {
                mockClass.getMethod(m.getName(), parameters);
            } catch (NoSuchMethodException e) {
                // logger.debug("Skipping method " + m.getName());
                continue;
            }

            String desc = Type.getMethodDescriptor(m);
            Type[] argumentTypes = Type.getArgumentTypes(m);
            Type[] mockedArgumentTypes = new Type[argumentTypes.length + 1];
            mockedArgumentTypes[0] = Type.getType(target);
            for (int i = 0; i < argumentTypes.length; i++)
                mockedArgumentTypes[i + 1] = argumentTypes[i];
            String mockedDesc = Type.getMethodDescriptor(Type.getReturnType(m), mockedArgumentTypes);
            addReplacementCall(new MethodCallReplacement(target.getCanonicalName().replace('.', '/'), m.getName(), desc,
                    Opcodes.INVOKEVIRTUAL, mockClass.getCanonicalName().replace('.', '/'), m.getName(), mockedDesc,
                    false, false));
        }
    }

    private void replaceAllStaticMethods(Class<?> mockClass, Class<?> target) throws IllegalArgumentException {

        for (Method m : target.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) {
                continue;
            }

            String desc = Type.getMethodDescriptor(m);
            addReplacementCall(new MethodCallReplacement(target.getCanonicalName().replace('.', '/'), m.getName(), desc,
                    Opcodes.INVOKESTATIC, mockClass.getCanonicalName().replace('.', '/'), m.getName(), desc, false,
                    false));
        }
    }

    /**
     * Replace all the constructors of {@code target} with a constructor (with
     * same input parameters) of mock subclass {@code mockClass}.
     *
     * @param mockClass
     * @param target
     * @throws IllegalArgumentException
     */
    private void replaceAllConstructors(Class<?> mockClass, Class<?> target) throws IllegalArgumentException {

        if (!target.isAssignableFrom(mockClass)) {
            throw new IllegalArgumentException("Constructor replacement can be done only for subclasses. Class "
                    + mockClass + " is not an instance of " + target);
        }

        for (Constructor<?> constructor : ReflectionUtils.getDeclaredConstructors(mockClass)) {
            String desc = Type.getConstructorDescriptor(constructor);
            addSpecialReplacementCall(new MethodCallReplacement(target.getCanonicalName().replace('.', '/'), "<init>",
                    desc, Opcodes.INVOKESPECIAL, mockClass.getCanonicalName().replace('.', '/'), "<init>", desc, false,
                    false));
        }
    }

    /**
     * Replace all the constructors of {@code target} with a static call (with
     * same input parameters) of static mock class {@code mockClass}.
     *
     * @param mockClass
     * @param target
     * @throws IllegalArgumentException
     */
    private void replaceAllConstructorsWithStaticCalls(Class<?> mockClass, Class<?> target)
            throws IllegalArgumentException {

        for (Constructor<?> constructor : target.getConstructors()) {
            String desc = Type.getConstructorDescriptor(constructor);
            String replacementDesc = desc.substring(0, desc.length() - 1) + Type.getDescriptor(target);
            addReplacementCall(new MethodCallReplacement(target.getCanonicalName().replace('.', '/'), "<init>", desc,
                    Opcodes.INVOKESPECIAL, mockClass.getCanonicalName().replace('.', '/'), target.getSimpleName(),
                    replacementDesc, true, true));
        }
    }

    /**
     * Replace all the methods of {@code target} with a method (with same input
     * parameters) of mock subclass {@code mockClass}.
     *
     * @param mockClass
     * @param target
     * @throws IllegalArgumentException
     */
    private void replaceAllInvokeSpecial(Class<?> mockClass, Class<?> target) throws IllegalArgumentException {

        if (!target.isAssignableFrom(mockClass)) {
            throw new IllegalArgumentException("Method replacement can be done only for subclasses. Class " + mockClass
                    + " is not an instance of " + target);
        }

        // logger.debug("Static Mock: " + mockClass.getCanonicalName() + " for " + target.getCanonicalName());
        for (Method method : mockClass.getMethods()) {
            String desc = Type.getMethodDescriptor(method);
            addSpecialReplacementCall(new MethodCallReplacement(target.getCanonicalName().replace('.', '/'),
                    method.getName(), desc, Opcodes.INVOKESPECIAL, mockClass.getCanonicalName().replace('.', '/'),
                    method.getName(), desc, false, false));
        }
    }

}
