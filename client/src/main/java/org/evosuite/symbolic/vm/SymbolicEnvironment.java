/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.vm;

import org.evosuite.dse.MainConfig;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.instrument.ConcolicInstrumentingClassLoader;
import org.evosuite.testcase.execution.EvosuiteError;
import org.objectweb.asm.Type;

/**
 * 
 * @author galeotti
 * 
 */
public final class SymbolicEnvironment {

	/**
	 * Storage for symbolic information in the memory heap
	 */
	public final SymbolicHeap heap = new SymbolicHeap();

	/**
	 * Stack of function/method/constructor invocation frames
	 */
	private final Deque<Frame> stackFrame = new LinkedList<Frame>();

	/**
	 * Classes whose static fields have been set to the default zero value or a
	 * dummy value.
	 */
	private final Set<Class<?>> preparedClasses = new HashSet<Class<?>>();

	private final ConcolicInstrumentingClassLoader classLoader;

	public SymbolicEnvironment(ConcolicInstrumentingClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public Frame topFrame() {
		return stackFrame.peek();
	}

	public void pushFrame(Frame frame) {
		stackFrame.push(frame);
	}

	public Frame callerFrame() {
		Frame top = stackFrame.pop();
		Frame res = stackFrame.peek();
		stackFrame.push(top);
		return res;
	}

	public Frame popFrame() {
		return stackFrame.pop();
	}

	public Class<?> ensurePrepared(String className) {
		Type ownerType = Type.getType(className);
		if (ownerType.getSort() == Type.ARRAY) {
			Type elemType = ownerType.getElementType();
			if (isValueType(elemType))
				return primitiveClassType(elemType);
			else {
				// ensurePrepared component class
				className = elemType.getClassName();
				Class<?> claz = classLoader.getClassForName(className);
				ensurePrepared(claz);
				
				// returns claz[] instead of claz
				Class<?> arrayClaz = Array.newInstance(claz, 0).getClass();
				return arrayClaz;
			}
		} else {
			Class<?> claz = classLoader.getClassForName(className);
			ensurePrepared(claz);
			return claz;
		}
	}

	private Class<?> primitiveClassType(Type t) {
		if (t.equals(Type.BOOLEAN_TYPE))
			return boolean[].class;
		if (t.equals(Type.CHAR_TYPE))
			return char[].class;
		if (t.equals(Type.SHORT_TYPE))
			return short[].class;
		if (t.equals(Type.BYTE_TYPE))
			return byte[].class;
		if (t.equals(Type.INT_TYPE))
			return int[].class;
		if (t.equals(Type.LONG_TYPE))
			return long[].class;
		if (t.equals(Type.FLOAT_TYPE))
			return float[].class;
		if (t.equals(Type.DOUBLE_TYPE))
			return double[].class;

		throw new EvosuiteError(t.toString()
				+ " is not a primitive value class!");

	}

	private boolean isValueType(Type t) {
		return t.equals(Type.BOOLEAN_TYPE) || t.equals(Type.CHAR_TYPE)
				|| t.equals(Type.SHORT_TYPE) || t.equals(Type.BYTE_TYPE)
				|| t.equals(Type.INT_TYPE) || t.equals(Type.LONG_TYPE)
				|| t.equals(Type.FLOAT_TYPE) || t.equals(Type.DOUBLE_TYPE);
	}

	public void ensurePrepared(Class<?> claz) {
		if (preparedClasses.contains(claz))
			return; // done, we have prepared this class earlier

		Class<?> superClass = claz.getSuperclass();
		if (superClass != null)
			ensurePrepared(superClass); // prepare super class first

		String className = claz.getCanonicalName();
		if (className == null) {
			// no canonical name
		}
		/*
		 * Field[] fields = claz.getDeclaredFields();
		 * 
		 * final boolean isIgnored = MainConfig.get().isIgnored(className);
		 * 
		 * for (Field field : fields) {
		 * 
		 * final int fieldModifiers = field.getModifiers(); if (isIgnored &&
		 * Modifier.isPrivate(fieldModifiers)) continue; // skip private field
		 * of ignored class.
		 * 
		 * }
		 */
		preparedClasses.add(claz);

	}

	/**
	 * Prepare stack of function invocation frames.
	 * 
	 * Clear function invocation stack, push a frame that pretends to call the
	 * method under test. We push variables for our method onto the
	 * pseudo-callers stack, so our method can pop them from there.
	 */
	public void prepareStack(Method mainMethod) {
		stackFrame.clear();
		// bottom of the stack trace
		this.pushFrame(new FakeBottomFrame());

		// frame for argument purposes
		final FakeMainCallerFrame fakeMainCallerFrame = new FakeMainCallerFrame(
				mainMethod, MainConfig.get().MAX_LOCALS_DEFAULT); // fake caller
																	// of method
																	// under
																	// test

		if (mainMethod != null) {
			boolean isInstrumented = isInstrumented(mainMethod);
			fakeMainCallerFrame.invokeInstrumentedCode(isInstrumented);
			String[] emptyStringArray = new String[] {};
			ReferenceExpression emptyStringRef = heap.getReference(emptyStringArray);
			fakeMainCallerFrame.operandStack.pushRef(emptyStringRef);
		}
		this.pushFrame(fakeMainCallerFrame);
	}

	/**
	 * @return method is instrumented. It is neither native nor declared by an
	 *         ignored JDK class, etc.
	 */
	private static boolean isInstrumented(Method method) {
		if (Modifier.isNative(method.getModifiers()))
			return false;

		String declClass = method.getDeclaringClass().getCanonicalName();
		return !MainConfig.get().isIgnored(declClass);
	}

	public boolean isEmpty() {
		return stackFrame.isEmpty();
	}

}
