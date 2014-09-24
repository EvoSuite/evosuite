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
package org.evosuite.runtime.instrumentation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.EvoSuiteMock;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.MockList;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.mock.java.lang.MockThrowable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>
 * MethodCallReplacementMethodAdapter class.
 * </p>
 * 
 * @author fraser
 */
public class MethodCallReplacementMethodAdapter extends GeneratorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(MethodCallReplacementMethodAdapter.class);

	private class MethodCallReplacement {
		private final String className;
		private final String methodName;
		private final String desc;
		private final int origOpcode;

		private final String replacementClassName;
		private final String replacementMethodName;
		private final String replacementDesc;

		private final boolean popCallee;
		private final boolean popUninitialisedReference;

		/**
		 * 
		 * @param className
		 * @param methodName
		 * @param desc
		 * @param replacementClassName
		 * @param replacementMethodName
		 * @param replacementDesc
		 * @param pop
		 *            if {@code true}, then get rid of the receiver object from
		 *            the stack. This is needed when a non-static method is
		 *            replaced by a static one, unless you make the callee of
		 *            the original method a parameter of the static replacement
		 *            method
		 * @param pop2
		 *            is needed if you replace the super-constructor call
		 */
		public MethodCallReplacement(String className, String methodName, String desc, int opcode,
				String replacementClassName, String replacementMethodName,
				String replacementDesc, boolean pop, boolean pop2) {
			this.className = className;
			this.methodName = methodName;
			this.desc = desc;
			this.replacementClassName = replacementClassName;
			this.replacementMethodName = replacementMethodName;
			this.replacementDesc = replacementDesc;
			this.popCallee = pop;
			this.popUninitialisedReference = pop2;
			this.origOpcode = opcode;
		}

		public boolean isTarget(String owner, String name, String desc) {
			return className.equals(owner) && methodName.equals(name)
					&& this.desc.equals(desc);
		}

		public void insertMethodCall(MethodCallReplacementMethodAdapter mv, int opcode) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, MockFramework.class.getCanonicalName().replace('.', '/'), "isEnabled", "()Z", false);
			Label origCallLabel = new Label();
			Label afterOrigCallLabel = new Label();

			Label annotationStartTag = new AnnotatedLabel(true, true);
			annotationStartTag.info = Boolean.TRUE;			
			mv.visitLabel(annotationStartTag);
			mv.visitJumpInsn(Opcodes.IFEQ, origCallLabel);
			Label annotationEndTag = new AnnotatedLabel(true, false);
			annotationEndTag.info = Boolean.FALSE;			
			mv.visitLabel(annotationEndTag);

			if (popCallee) {
				Type[] args = Type.getArgumentTypes(desc);
				Map<Integer, Integer> to = new HashMap<Integer, Integer>();
				for (int i = args.length - 1; i >= 0; i--) {
					int loc = newLocal(args[i]);
					storeLocal(loc);
					to.put(i, loc);
				}

				pop();//callee
				if (popUninitialisedReference)
					pop();

				for (int i = 0; i < args.length; i++) {
					loadLocal(to.get(i));
				}
			}
			mv.visitMethodInsn(opcode, replacementClassName, replacementMethodName,
					replacementDesc, false);
			mv.visitJumpInsn(Opcodes.GOTO, afterOrigCallLabel);
			mv.visitLabel(origCallLabel);
			mv.mv.visitMethodInsn(origOpcode, className, methodName, desc, false); // TODO: What is itf here?
			mv.visitLabel(afterOrigCallLabel);
		}

		public void insertConstructorCall(MethodCallReplacementMethodAdapter mv,
				MethodCallReplacement replacement, boolean isSelf) {
			Label origCallLabel = new Label();
			Label afterOrigCallLabel = new Label();
			
			if (!isSelf) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, MockFramework.class.getCanonicalName().replace('.', '/'), "isEnabled", "()Z", false);
				Label annotationStartTag = new AnnotatedLabel(true, true);
				annotationStartTag.info = Boolean.TRUE;			
				mv.visitLabel(annotationStartTag);
				mv.visitJumpInsn(Opcodes.IFEQ, origCallLabel);
				Label annotationEndTag = new AnnotatedLabel(true, false);
				annotationEndTag.info = Boolean.FALSE;			
				mv.visitLabel(annotationEndTag);

				Type[] args = Type.getArgumentTypes(desc);
				Map<Integer, Integer> to = new HashMap<Integer, Integer>();
				for (int i = args.length - 1; i >= 0; i--) {
					int loc = newLocal(args[i]);
					storeLocal(loc);
					to.put(i, loc);
				}

				pop2();//uninitialized reference (which is duplicated)
				newInstance(Type.getType(replacement.replacementClassName));
				dup();

				for (int i = 0; i < args.length; i++) {
					loadLocal(to.get(i));
				}
			}
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, replacementClassName,
					replacementMethodName, replacementDesc, false);
			if (!isSelf) {
				mv.visitJumpInsn(Opcodes.GOTO, afterOrigCallLabel);
				mv.visitLabel(origCallLabel);
				mv.mv.visitMethodInsn(Opcodes.INVOKESPECIAL, className, methodName, desc, false);
				mv.visitLabel(afterOrigCallLabel);
			}
		}
	}

	/**
	 * method replacements, which are called with Opcodes.INVOKESTATIC
	 */
	private final Set<MethodCallReplacement> replacementCalls = new HashSet<MethodCallReplacement>();

	/**
	 * method replacements, which are called with Opcodes.INVOKEVIRTUAL
	 */
	private final Set<MethodCallReplacement> virtualReplacementCalls = new HashSet<MethodCallReplacement>();

	/**
	 * method replacements, which are called with Opcodes.INVOKESPECIAL
	 */
	private final Set<MethodCallReplacement> specialReplacementCalls = new HashSet<MethodCallReplacement>();

	private final String className;

	private final String superClassName;

	private boolean needToWaitForSuperConstructor = false;

	/**
	 * <p>
	 * Constructor for MethodCallReplacementMethodAdapter.
	 * </p>
	 * 
	 * @param mv
	 *            a {@link org.objectweb.asm.MethodVisitor} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 * @param access
	 *            a int.
	 * @param desc
	 *            a {@link java.lang.String} object.
	 */
	public MethodCallReplacementMethodAdapter(MethodVisitor mv, String className,
			String superClassName, String methodName, int access, String desc) {
		super(Opcodes.ASM5, mv, access, methodName, desc);
		this.className = className;
		this.superClassName = superClassName;
		if (methodName.equals("<init>")) {
			needToWaitForSuperConstructor = true;
		}
		if (RuntimeSettings.mockJVMNonDeterminism) {

			//java.lang.*
			addJavaLangCalls();


			//java.util.Calendar
			addCalendarCalls();

			// java.security.SecureRandom
			addSecureRandomCalls();

			addGUICalls();

			addExtraceExceptionReplacements();
			
			//java.util.UUID.randomUUID()
			replacementCalls.add(new MethodCallReplacement("java/util/UUID", "randomUUID",
					"()Ljava/util/UUID;", Opcodes.INVOKESTATIC, "org/evosuite/runtime/Random", "randomUUID", "()Ljava/util/UUID;", false, false));


		}

		handleMockList();
	}

	/**
	 * Ideally, all mocking should be handled by either OverrideMock or StaticReplacementMock.
	 * However, even in such cases, we would not be able to get mocks from classes that are
	 * not instrumented (eg, other Java API classes we do not mock).
	 * In theory, we should mock all of them.
	 * The problem raises for "Exceptions", as they are used everywhere, and would require
	 * to mock the full Java API (which is not going to happen...).
	 * Solution is, beside using OverrideMock for them, to also a further static replacement (implemented
	 * in this method).
	 * 
	 * <p>
	 * Note: why not just using static replacement instead of OverrideMock? Because static replacement
	 * will not work if an exception instance is used in a non-instrumented class, whereas OverrideMock would.
	 * Still, it could be tedious to prepare OverrideMock for every single type of exception, so the static
	 * replacement here could be a temporary workaround
	 * 
	 */
	private void addExtraceExceptionReplacements(){

		List<Class<? extends Throwable>> classes = Arrays.asList(
				IOException.class,
				//following java.lang
				Throwable.class,
				ArithmeticException.class,
				ArrayIndexOutOfBoundsException.class,
				ArrayStoreException.class,
				ClassCastException.class,
				ClassNotFoundException.class,
				CloneNotSupportedException.class,
				EnumConstantNotPresentException.class,
				Exception.class,
				IllegalAccessException.class,
				IllegalArgumentException.class,
				IllegalMonitorStateException.class,
				IllegalStateException.class,
				IllegalThreadStateException.class,
				IndexOutOfBoundsException.class,
				InstantiationException.class,
				InterruptedException.class,
				NegativeArraySizeException.class,
				NoSuchFieldException.class,
				NoSuchMethodException.class,
				NullPointerException.class,
				NumberFormatException.class,
				ReflectiveOperationException.class,
				RuntimeException.class,
				SecurityException.class,
				StringIndexOutOfBoundsException.class,
				TypeNotPresentException.class,
				UnsupportedOperationException.class,
				AbstractMethodError.class,
				AssertionError.class,
				BootstrapMethodError.class,
				ClassCircularityError.class,
				ClassFormatError.class,
				Error.class,
				ExceptionInInitializerError.class,
				IllegalAccessError.class,
				IncompatibleClassChangeError.class,
				InstantiationError.class,
				InternalError.class,
				LinkageError.class,
				NoClassDefFoundError.class,
				NoSuchFieldError.class,
				NoSuchMethodError.class,
				OutOfMemoryError.class,
				StackOverflowError.class,
				ThreadDeath.class,
				UnknownError.class,
				UnsatisfiedLinkError.class,
				UnsupportedClassVersionError.class,
				VerifyError.class,
				VirtualMachineError.class
				);

		for(Class<?> k : classes){
			
			String jvmOriginal = k.getName().replace('.', '/');
			String jvmMock = MockThrowable.class.getName().replace('.', '/'); 
			
			replacementCalls.add(new MethodCallReplacement(
					jvmOriginal,	 "getStackTrace", "()[Ljava/lang/StackTraceElement;", Opcodes.INVOKEVIRTUAL,
					jvmMock, "replacement_getStackTrace","(Ljava/lang/Throwable;)[Ljava/lang/StackTraceElement;",
					false, false));
			
			replacementCalls.add(new MethodCallReplacement(
					jvmOriginal,"printStackTrace", "(Ljava/io/PrintStream;)V", Opcodes.INVOKEVIRTUAL,
					jvmMock, "printStackTrace", "(Ljava/lang/Throwable;Ljava/io/PrintStream;)V", 
					false, false));
			
			replacementCalls.add(new MethodCallReplacement(
					jvmOriginal,"printStackTrace", "(Ljava/io/PrintWriter;)V", Opcodes.INVOKEVIRTUAL,
					jvmMock, "replacement_printStackTrace","(Ljava/lang/Throwable;Ljava/io/PrintWriter;)V",
					false, false));
		}
		
		
	}

	private void handleMockList() {
		for (Class<? extends EvoSuiteMock> mock : MockList.getList()) {

			if(OverrideMock.class.isAssignableFrom(mock)){
				replaceAllConstructors(mock, mock.getSuperclass());
				replaceAllStaticMethods(mock, mock.getSuperclass());
				replaceAllInvokeSpecial(mock, mock.getSuperclass());

			} else if(StaticReplacementMock.class.isAssignableFrom(mock)){

				String mockedName;
				try {
					mockedName = ((StaticReplacementMock)mock.newInstance()).getMockedClassName();
				} catch (InstantiationException | IllegalAccessException e1) {
					logger.error("Cannot instantiate mock "+mock.getCanonicalName());
					continue;
				}
				Class<?> mocked;
				try {
					mocked = StaticReplacementMock.class.getClassLoader().loadClass(mockedName);
				} catch (ClassNotFoundException e) {
					//should never happen
					logger.error("Mock class "+mock.getCanonicalName()+" has non-existent mocked target "+mockedName);
					continue;
				}

				replaceAllStaticMethods(mock, mocked);
				replaceAllInstanceMethodsWithStatic(mock,mocked);
			}
		}
	}

	private void addJavaLangCalls() {

		//java/lang/System
		replacementCalls.add(new MethodCallReplacement("java/lang/System", "exit",
				"(I)V",  Opcodes.INVOKESTATIC, "org/evosuite/runtime/System", "exit", "(I)V", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/System",
				"currentTimeMillis", "()J",  Opcodes.INVOKESTATIC, "org/evosuite/runtime/System",
				"currentTimeMillis", "()J", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/System",
				"nanoTime", "()J",  Opcodes.INVOKESTATIC, "org/evosuite/runtime/System", "nanoTime", "()J",
				false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/System",
				"identityHashCode", "(Ljava/lang/Object;)I",  Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/System", "identityHashCode",
				"(Ljava/lang/Object;)I", false, false));

		//java/lang/Object
		replacementCalls.add(new MethodCallReplacement("java/lang/Object",
				"hashCode", "()I", Opcodes.INVOKEVIRTUAL, "org/evosuite/runtime/System", "identityHashCode",
				"(Ljava/lang/Object;)I", false, false));

		replacementCalls.add(new MethodCallReplacement("java/lang/Math", "random",
				"()D", Opcodes.INVOKESTATIC, "org/evosuite/runtime/Random", "nextDouble", "()D", false,
				false));

		//java/lang/Thread
		replacementCalls.add(new MethodCallReplacement("java/lang/Thread",
				"getStackTrace", "()[Ljava/lang/StackTraceElement;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Thread", "getStackTrace",
				"()[Ljava/lang/StackTraceElement;", true, false));

		//java/lang/Thread
		replacementCalls.add(new MethodCallReplacement("java/lang/Thread", "getName",
				"()Ljava/lang/String;", Opcodes.INVOKEVIRTUAL, "org/evosuite/runtime/Thread", "getName",
				"(Ljava/lang/Thread;)Ljava/lang/String;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Thread", "getId",
				"()J", Opcodes.INVOKEVIRTUAL, "org/evosuite/runtime/Thread", "getId",
				"(Ljava/lang/Thread;)J", false, false));

		//java/lang/Class
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getClasses", "()[Ljava/lang/Class;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getClasses",
				"(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getAnnotations", "()[Ljava/lang/annotation/Annotation;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getAnnotations",
				"(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getFields", "()[Ljava/lang/reflect/Field;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getFields",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getConstructors", "()[Ljava/lang/reflect/Constructor;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getConstructors",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getMethods", "()[Ljava/lang/reflect/Method;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getMethods",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getDeclaredClasses", "()[Ljava/lang/Class;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredClasses",
				"(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredAnnotations",
				"(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getDeclaredFields", "()[Ljava/lang/reflect/Field;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredFields",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredConstructors",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
				"getDeclaredMethods", "()[Ljava/lang/reflect/Method;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/Reflection", "getDeclaredMethods",
				"(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false, false));

		//java/lang/ClassLoader
		replacementCalls.add(new MethodCallReplacement("java/lang/ClassLoader",
				"getResource", "(Ljava/lang/String;)Ljava/net/URL;",  Opcodes.INVOKEVIRTUAL, "org/evosuite/runtime/ResourceLoader",
				"getResource", "(Ljava/lang/String;)Ljava/net/URL;", true, false));

	}

	private void addGUICalls() {
		replacementCalls.add(new MethodCallReplacement("javax/swing/JComponent",
				"getPreferredSize", "()Ljava/awt/Dimension;", Opcodes.INVOKEVIRTUAL,
				"org/evosuite/runtime/gui/JComponent", "getPreferredSize",
				"()Ljava/awt/Dimension;", true, false));		
	}

	private void addCalendarCalls() {
		replacementCalls.add(new MethodCallReplacement("java/util/Calendar",
				"getInstance", "()Ljava/util/Calendar;", Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/Calendar", "getCalendar",
				"()Ljava/util/Calendar;", false, false));

		replacementCalls.add(new MethodCallReplacement("java/util/Calendar",
				"getInstance", "(Ljava/util/Locale;)Ljava/util/Calendar;", Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/Calendar", "getCalendar",
				"(Ljava/util/Locale;)Ljava/util/Calendar;", false, false));

		replacementCalls.add(new MethodCallReplacement("java/util/Calendar",
				"getInstance", "(Ljava/util/TimeZone;)Ljava/util/Calendar;", Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/Calendar", "getCalendar",
				"(Ljava/util/TimeZone;)Ljava/util/Calendar;", false, false));

		replacementCalls.add(new MethodCallReplacement("java/util/Calendar",
				"getInstance",
				"(Ljava/util/TimeZone;Ljava/util/Locale;)Ljava/util/Calendar;", Opcodes.INVOKESTATIC,
				"org/evosuite/runtime/Calendar", "getCalendar",
				"(Ljava/util/TimeZone;Ljava/util/Locale;)Ljava/util/Calendar;",
				false, false));
	}

	private void addRandomCalls() {
		replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextInt",
				"()I", Opcodes.INVOKEVIRTUAL, "org/evosuite/runtime/Random", "nextInt", "()I", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextInt",
				"(I)I", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextInt", "(I)I", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
				"nextDouble", "()D", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextDouble",
				"()D", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
				"nextFloat", "()F", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextFloat",
				"()F", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
				"nextLong", "()J", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextLong", "()J",
				true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
				"nextGaussian", "()D", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextGaussian",
				"()D", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
				"nextBoolean", "()Z", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextBoolean",
				"()Z", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextBytes",
				"([B)V", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextBytes", "([B)V", true, false));
	}

	private void addSecureRandomCalls() {
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextInt", "()I", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextInt", "()I",
				true, false));

		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom", "nextInt",
				"(I)I", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextInt", "(I)I", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextDouble", "()D", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextDouble",
				"()D", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextFloat", "()F", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextFloat",
				"()F", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextLong", "()J", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextLong", "()J",
				true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextGaussian", "()D", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextGaussian",
				"()D", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextBoolean", "()Z", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextBoolean",
				"()Z", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom", "nextBytes",
				"([B)V", Opcodes.INVOKEVIRTUAL,  "org/evosuite/runtime/Random", "nextBytes", "([B)V", true, false));
	}


	private void replaceAllInstanceMethodsWithStatic(Class<?> mockClass, Class<?> target){

		/*
		 *  replace "fooInstance.bar(x)"  with "MockFooClass.bar(fooInstance,x)"
		 */

		for (Method m : target.getMethods()) {
			if (Modifier.isStatic(m.getModifiers())) {
				continue;
			}

			String desc = Type.getMethodDescriptor(m);
			Type[] argumentTypes = Type.getArgumentTypes(m);
			Type[] mockedArgumentTypes = new Type[argumentTypes.length + 1];
			mockedArgumentTypes[0] = Type.getType(target);
			for(int i = 0; i < argumentTypes.length; i++)
				mockedArgumentTypes[i+1] = argumentTypes[i];
			String mockedDesc = Type.getMethodDescriptor(Type.getReturnType(m), mockedArgumentTypes);
			replacementCalls.add(new MethodCallReplacement(
					target.getCanonicalName().replace('.', '/'), m.getName(), desc, Opcodes.INVOKEVIRTUAL, 
					mockClass.getCanonicalName().replace('.', '/'), m.getName(), mockedDesc,
					false, false));
		}
	}	

	private void replaceAllStaticMethods(Class<?> mockClass, Class<?> target)
			throws IllegalArgumentException {

		for (Method m : target.getMethods()) {
			if (!Modifier.isStatic(m.getModifiers())) {
				continue;
			}

			String desc = Type.getMethodDescriptor(m);
			replacementCalls.add(new MethodCallReplacement(
					target.getCanonicalName().replace('.', '/'), m.getName(), desc, Opcodes.INVOKESTATIC, 
					mockClass.getCanonicalName().replace('.', '/'), m.getName(), desc,
					false, false));
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
	private void replaceAllConstructors(Class<?> mockClass, Class<?> target)
			throws IllegalArgumentException {

		if (!target.isAssignableFrom(mockClass)) {
			throw new IllegalArgumentException(
					"Constructor replacement can be done only for subclasses. Class "
							+ mockClass + " is not an instance of " + target);
		}

		for (Constructor<?> constructor : mockClass.getConstructors()) {
			String desc = Type.getConstructorDescriptor(constructor);
			specialReplacementCalls.add(new MethodCallReplacement(
					target.getCanonicalName().replace('.', '/'), "<init>", desc, Opcodes.INVOKESPECIAL, 
					mockClass.getCanonicalName().replace('.', '/'), "<init>", desc,
					false, false));
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
	private void replaceAllInvokeSpecial(Class<?> mockClass, Class<?> target)
			throws IllegalArgumentException {

		if (!target.isAssignableFrom(mockClass)) {
			throw new IllegalArgumentException(
					"Method replacement can be done only for subclasses. Class "
							+ mockClass + " is not an instance of " + target);
		}

		for (Method method : mockClass.getMethods()) {
			String desc = Type.getMethodDescriptor(method);
			specialReplacementCalls.add(new MethodCallReplacement(
					target.getCanonicalName().replace('.', '/'), method.getName(), desc, Opcodes.INVOKESPECIAL, 
					mockClass.getCanonicalName().replace('.', '/'), method.getName(),
					desc, false, false));
		}
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {

		boolean isReplaced = false;
		// static replacement methods
		for (MethodCallReplacement replacement : replacementCalls) {
			if (replacement.isTarget(owner, name, desc)) {
				isReplaced = true;
				replacement.insertMethodCall(this, Opcodes.INVOKESTATIC);
				break;
			}
		}

		// for constructors
		if (!isReplaced) {
			for (MethodCallReplacement replacement : specialReplacementCalls) {
				if (replacement.isTarget(owner, name, desc)
						&& opcode == Opcodes.INVOKESPECIAL) {
					isReplaced = true;
					boolean isSelf = false;
					if (needToWaitForSuperConstructor) {
						String originalClassNameWithDots = owner.replace('/', '.');
						if (originalClassNameWithDots.equals(superClassName)) {
							isSelf = true;
						}
					}
					if (replacement.methodName.equals("<init>"))
						replacement.insertConstructorCall(this, replacement, isSelf);
					else
						replacement.insertMethodCall(this, Opcodes.INVOKESPECIAL);
					break;
				}
			}
		}

		// non-static replacement methods
		if (!isReplaced) {
			for (MethodCallReplacement replacement : virtualReplacementCalls) {
				if (replacement.isTarget(owner, name, desc)) {
					isReplaced = true;
					replacement.insertMethodCall(this, Opcodes.INVOKEVIRTUAL);
					break;
				}
			}
		}

		if (!isReplaced) {
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}
		if (needToWaitForSuperConstructor) {
			if (opcode == Opcodes.INVOKESPECIAL) {
				String originalClassNameWithDots = owner.replace('/', '.');
				if (originalClassNameWithDots.equals(superClassName)) {
					needToWaitForSuperConstructor = false;
				}
			}
		}

	}
}
