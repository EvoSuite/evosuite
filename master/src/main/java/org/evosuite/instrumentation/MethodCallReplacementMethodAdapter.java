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
package org.evosuite.instrumentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.runtime.MockList;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * <p>
 * MethodCallReplacementMethodAdapter class.
 * </p>
 * 
 * @author fraser
 */
public class MethodCallReplacementMethodAdapter extends GeneratorAdapter {

	private class MethodCallReplacement {
		private final String className;
		private final String methodName;
		private final String desc;

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
		public MethodCallReplacement(String className, String methodName, String desc,
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
		}

		public boolean isTarget(String owner, String name, String desc) {
			return className.equals(owner) && methodName.equals(name)
			        && this.desc.equals(desc);
		}

		public void insertMethodCall(MethodCallReplacementMethodAdapter mv, int opcode) {
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
			                   replacementDesc);
		}

		public void insertConstructorCall(MethodCallReplacementMethodAdapter mv,
		        MethodCallReplacement replacement, boolean isSelf) {
			// if(!mv.needToWaitForSuperConstructor) {
			if (!isSelf) {
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
			                   replacementMethodName, replacementDesc);
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
		super(Opcodes.ASM4, mv, access, methodName, desc);
		this.className = className;
		this.superClassName = superClassName;
		if (methodName.equals("<init>")) {
			needToWaitForSuperConstructor = true;
		}
		if (Properties.REPLACE_CALLS) {

			//java.lang.*
			addJavaLangCalls();

			/*
			replacementCalls.add(new MethodCallReplacement("java/util/Date", "<init>",
				"()V", "org/evosuite/runtime/Date", "getDate", "()Ljava/util/Date;",
				true, true));
			*/

			//java.util.Calendar
			addCalendarCalls();

			// java.util.Random
			// Is now handled by MockRandom
			// addRandomCalls();

			// java.security.SecureRandom
			addSecureRandomCalls();
			
			addGUICalls();

			//java.util.UUID.randomUUID()
			replacementCalls.add(new MethodCallReplacement("java/util/UUID", "randomUUID",
					"()Ljava/util/UUID;", "org/evosuite/runtime/Random", "randomUUID", "()Ljava/util/UUID;", false, false));
			

		}

		for (Class<?> mock : MockList.getList()) {
			replaceAllConstructors(mock, mock.getSuperclass());
			replaceAllStaticMethods(mock, mock.getSuperclass());
			replaceAllInvokeSpecial(mock, mock.getSuperclass());
		}
	}

	private void addJavaLangCalls() {
		
		//java/lang/Runtime
		replacementCalls.add(new MethodCallReplacement("java/lang/Runtime", "freeMemory",
		        "()J", "org/evosuite/runtime/System", "freeMemory", "()J", true, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Runtime", "maxMemory",
		        "()J", "org/evosuite/runtime/System", "maxMemory", "()J", true, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Runtime", "totalMemory",
		        "()J", "org/evosuite/runtime/System", "totalMemory", "()J", true, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Runtime", "availableProcessors",
		        "()I", "org/evosuite/runtime/System", "availableProcessors", "()I", true, false));		
		
		//java/lang/System
		replacementCalls.add(new MethodCallReplacement("java/lang/System", "exit",
		        "(I)V", "org/evosuite/runtime/System", "exit", "(I)V", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/System",
		        "currentTimeMillis", "()J", "org/evosuite/runtime/System",
		        "currentTimeMillis", "()J", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/System",
		        "nanoTime", "()J", "org/evosuite/runtime/System", "nanoTime", "()J",
		        false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/System",
		        "identityHashCode", "(Ljava/lang/Object;)I",
		        "org/evosuite/runtime/System", "identityHashCode",
		        "(Ljava/lang/Object;)I", false, false));
		
		//java/lang/Object
		replacementCalls.add(new MethodCallReplacement("java/lang/Object",
		        "hashCode", "()I", "org/evosuite/runtime/System", "identityHashCode",
		        "(Ljava/lang/Object;)I", false, false));
		
		replacementCalls.add(new MethodCallReplacement("java/lang/Math", "random",
		        "()D", "org/evosuite/runtime/Random", "nextDouble", "()D", false,
		        false));

		//java/lang/Thread
		replacementCalls.add(new MethodCallReplacement("java/lang/Thread",
		        "getStackTrace", "()[Ljava/lang/StackTraceElement;",
		        "org/evosuite/runtime/Thread", "getStackTrace",
		        "()[Ljava/lang/StackTraceElement;", true, false));
		
		//java/lang/Throwable
		replacementCalls.add(new MethodCallReplacement("java/lang/Throwable",
		        "toString", "()Ljava/lang/String;", "org/evosuite/runtime/Throwable",
		        "toString", "(Ljava/lang/Throwable;)Ljava/lang/String;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Throwable",
				"getStackTrace", "()[Ljava/lang/StackTraceElement;",
				"org/evosuite/runtime/Thread", "getStackTrace",
				"()[Ljava/lang/StackTraceElement;", true, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Throwable",
				"printStackTrace", "(Ljava/io/PrintStream;)V",
				"org/evosuite/runtime/Throwable", "printStackTrace",
				"(Ljava/io/PrintStream;)V", true, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Throwable",
				"printStackTrace", "(Ljava/io/PrintWriter;)V",
				"org/evosuite/runtime/Throwable", "printStackTrace",
				"(Ljava/io/PrintWriter;)V", true, false));
		
		//java/lang/Exception
		replacementCalls.add(new MethodCallReplacement("java/lang/Exception",
		        "toString", "()Ljava/lang/String;", "org/evosuite/runtime/Throwable",
		        "toString", "(Ljava/lang/Exception;)Ljava/lang/String;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Exception",
		        "getStackTrace", "()[Ljava/lang/StackTraceElement;",
		        "org/evosuite/runtime/Thread", "getStackTrace",
		        "()[Ljava/lang/StackTraceElement;", true, false));

		//java/lang/Thread
		replacementCalls.add(new MethodCallReplacement("java/lang/Thread", "getName",
		        "()Ljava/lang/String;", "org/evosuite/runtime/Thread", "getName",
		        "(Ljava/lang/Thread;)Ljava/lang/String;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Thread", "getId",
		        "()J", "org/evosuite/runtime/Thread", "getId",
		        "(Ljava/lang/Thread;)J", false, false));

		//java/lang/Class
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getClasses", "()[Ljava/lang/Class;",
		        "org/evosuite/runtime/Reflection", "getClasses",
		        "(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getAnnotations", "()[Ljava/lang/annotation/Annotation;",
		        "org/evosuite/runtime/Reflection", "getAnnotations",
		        "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getFields", "()[Ljava/lang/reflect/Field;",
		        "org/evosuite/runtime/Reflection", "getFields",
		        "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getConstructors", "()[Ljava/lang/reflect/Constructor;",
		        "org/evosuite/runtime/Reflection", "getConstructors",
		        "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getMethods", "()[Ljava/lang/reflect/Method;",
		        "org/evosuite/runtime/Reflection", "getMethods",
		        "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getDeclaredClasses", "()[Ljava/lang/Class;",
		        "org/evosuite/runtime/Reflection", "getDeclaredClasses",
		        "(Ljava/lang/Class;)[Ljava/lang/Class;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getDeclaredAnnotations", "()[Ljava/lang/annotation/Annotation;",
		        "org/evosuite/runtime/Reflection", "getDeclaredAnnotations",
		        "(Ljava/lang/Class;)[Ljava/lang/annotation/Annotation;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getDeclaredFields", "()[Ljava/lang/reflect/Field;",
		        "org/evosuite/runtime/Reflection", "getDeclaredFields",
		        "(Ljava/lang/Class;)[Ljava/lang/reflect/Field;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getDeclaredConstructors", "()[Ljava/lang/reflect/Constructor;",
		        "org/evosuite/runtime/Reflection", "getDeclaredConstructors",
		        "(Ljava/lang/Class;)[Ljava/lang/reflect/Constructor;", false, false));
		replacementCalls.add(new MethodCallReplacement("java/lang/Class",
		        "getDeclaredMethods", "()[Ljava/lang/reflect/Method;",
		        "org/evosuite/runtime/Reflection", "getDeclaredMethods",
		        "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false, false));

		//java/lang/ClassLoader
		replacementCalls.add(new MethodCallReplacement("java/lang/ClassLoader",
		        "getResource", "(Ljava/lang/String;)Ljava/net/URL;", "org/evosuite/runtime/ResourceLoader",
		        "getResource", "(Ljava/lang/String;)Ljava/net/URL;", true, false));

	}
	
	private void addGUICalls() {
		replacementCalls.add(new MethodCallReplacement("javax/swing/JComponent",
		        "getPreferredSize", "()Ljava/awt/Dimension;",
		        "org/evosuite/runtime/gui/JComponent", "getPreferredSize",
		        "()Ljava/awt/Dimension;", true, false));		
	}

	private void addCalendarCalls() {
		replacementCalls.add(new MethodCallReplacement("java/util/Calendar",
		        "getInstance", "()Ljava/util/Calendar;",
		        "org/evosuite/runtime/Calendar", "getCalendar",
		        "()Ljava/util/Calendar;", false, false));

		replacementCalls.add(new MethodCallReplacement("java/util/Calendar",
		        "getInstance", "(Ljava/util/Locale;)Ljava/util/Calendar;",
		        "org/evosuite/runtime/Calendar", "getCalendar",
		        "(Ljava/util/Locale;)Ljava/util/Calendar;", false, false));

		replacementCalls.add(new MethodCallReplacement("java/util/Calendar",
		        "getInstance", "(Ljava/util/TimeZone;)Ljava/util/Calendar;",
		        "org/evosuite/runtime/Calendar", "getCalendar",
		        "(Ljava/util/TimeZone;)Ljava/util/Calendar;", false, false));

		replacementCalls.add(new MethodCallReplacement("java/util/Calendar",
		        "getInstance",
		        "(Ljava/util/TimeZone;Ljava/util/Locale;)Ljava/util/Calendar;",
		        "org/evosuite/runtime/Calendar", "getCalendar",
		        "(Ljava/util/TimeZone;Ljava/util/Locale;)Ljava/util/Calendar;",
		        false, false));
	}

	private void addRandomCalls() {
		replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextInt",
		        "()I", "org/evosuite/runtime/Random", "nextInt", "()I", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextInt",
		        "(I)I", "org/evosuite/runtime/Random", "nextInt", "(I)I", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
		        "nextDouble", "()D", "org/evosuite/runtime/Random", "nextDouble",
		        "()D", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
		        "nextFloat", "()F", "org/evosuite/runtime/Random", "nextFloat",
		        "()F", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
		        "nextLong", "()J", "org/evosuite/runtime/Random", "nextLong", "()J",
		        true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
		        "nextGaussian", "()D", "org/evosuite/runtime/Random", "nextGaussian",
		        "()D", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random",
				"nextBoolean", "()Z", "org/evosuite/runtime/Random", "nextBoolean",
				"()Z", true, false));
		replacementCalls.add(new MethodCallReplacement("java/util/Random", "nextBytes",
				"([B)V", "org/evosuite/runtime/Random", "nextBytes", "([B)V", true, false));
	}

	private void addSecureRandomCalls() {
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
		        "nextInt", "()I", "org/evosuite/runtime/Random", "nextInt", "()I",
		        true, false));

		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom", "nextInt",
				"(I)I", "org/evosuite/runtime/Random", "nextInt", "(I)I", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextDouble", "()D", "org/evosuite/runtime/Random", "nextDouble",
				"()D", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextFloat", "()F", "org/evosuite/runtime/Random", "nextFloat",
				"()F", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextLong", "()J", "org/evosuite/runtime/Random", "nextLong", "()J",
				true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextGaussian", "()D", "org/evosuite/runtime/Random", "nextGaussian",
				"()D", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom",
				"nextBoolean", "()Z", "org/evosuite/runtime/Random", "nextBoolean",
				"()Z", true, false));
		replacementCalls.add(new MethodCallReplacement("java/security/SecureRandom", "nextBytes",
				"([B)V", "org/evosuite/runtime/Random", "nextBytes", "([B)V", true, false));
	}

	private void replaceAllStaticMethods(Class<?> mockClass, Class<?> target)
	        throws IllegalArgumentException {

		for (Method m : target.getMethods()) {
			if (!Modifier.isStatic(m.getModifiers())) {
				continue;
			}

			String desc = Type.getMethodDescriptor(m);
			replacementCalls.add(new MethodCallReplacement(
			        target.getCanonicalName().replace('.', '/'), m.getName(), desc,
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
			        target.getCanonicalName().replace('.', '/'), "<init>", desc,
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
			        target.getCanonicalName().replace('.', '/'), method.getName(), desc,
			        mockClass.getCanonicalName().replace('.', '/'), method.getName(),
			        desc, false, false));
		}
	}

	/* (non-Javadoc)
	 * @see org.objectweb.asm.MethodVisitor#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	/** {@inheritDoc} */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {

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
			super.visitMethodInsn(opcode, owner, name, desc);
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
