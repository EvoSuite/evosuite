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
package org.evosuite.dse;

import java.util.LinkedList;
import java.util.List;


/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/**
 * Entry-point
 * 
 * The instrumentation inserted into user code is hard-coded to call static
 * methods of this class. Here we just multiplex these incoming calls to a list
 * of registered listeners.
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class VM {

	/**
	 * Single VM instance
	 */
	private static VM vm = new VM();

	/**
	 * Is this a recursive callback?
	 * 
	 * <pre>
	 * VM.meth()   // true
	 * user.meth()
	 * VM.meth()   // false
	 * </pre>
	 */
	private static boolean ignoreCallback = false;

	public static void disableCallBacks() {
		ignoreCallback = true;
	}

	public static void enableCallBacks() {
		ignoreCallback = false;
	}

	protected int nrCallbacksPath = 0;
	protected int nrCallbacksMethodExploration = 0;

	/**
	 * To be called before executing a new path through a method.
	 */
	protected void zeroPathCallbacks() {
		vm.nrCallbacksPath = 0;
	}

	/**
	 * To be called before exploring a new method.
	 */
	protected void zeroAllCallbacks() {
		zeroPathCallbacks();
		vm.nrCallbacksMethodExploration = 0;
	}

	protected void countCallback() {
		nrCallbacksPath += 1;
		nrCallbacksMethodExploration += 1;
	}

	public boolean isStopped() {
		return stopped;
	}

	/*
	 * Instance fields
	 */

	/*
	 * For each monitored VM ByteCode instruction, we call each listener.
	 */
	// protected IVM[] listeners = new IVM[] { new InsnLogger() };
	protected IVM[] listeners = new IVM[0];

	protected List<IVM> prependListeners = new LinkedList<IVM>();
	protected List<IVM> appendListeners = new LinkedList<IVM>();

	/**
	 * Registers paramListeners and any listernes previously queued via
	 * prependListeners and appendListeners. The listeners are registered to be
	 * called in the following order: (prependListeners, paramListeners,
	 * appendListeners).
	 */
	public void setListeners(List<IVM> paramListeners) {
		List<IVM> list = new LinkedList<IVM>();
		list.addAll(prependListeners);
		list.addAll(paramListeners);
		list.addAll(appendListeners);
		this.listeners = list.toArray(new IVM[list.size()]);
	}

	/**
	 * This method should be called before {@link #setListeners}. This method
	 * queues listener ivm to be added to the list of listeners by setListeners.
	 */
	public void prependListener(IVM ivm) {
		prependListeners.add(ivm);
	}

	/**
	 * This method should be called before {@link #setListeners}. This method
	 * queues listener ivm to be added to the list of listeners by setListeners.
	 */
	public void appendListener(IVM ivm) {
		appendListeners.add(ivm);
	}

	/**
	 * Dsc calls this method just before invoking the current entry method.
	 */
	public void prepareConcolicExecution() {
		stopped = false;
		ignoreCallback = false;
		zeroPathCallbacks();
	}

	public static void NEW(String typeName) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.NEW(typeName);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	private boolean stopped = false;

	protected static void handleException(Throwable t) {
		/**
		 * Listeners are not supposed to throw exceptions to the VM except the
		 * StopVMException.
		 */
		if (t instanceof StopVMException) {
			// No more callbacks are done since the list is erased
			// TODO catch StopVMException in Listeners. Enforce no listener
			// exception reaches the VM.
			vm.listeners = new IVM[0];
			vm.stopped = true;
		} else if (t instanceof OutOfMemoryError) {
			// do not wrap memory exceptions
			throw (OutOfMemoryError) t;
		} else if (t instanceof StackOverflowError) {
			// do not wrap memory exceptions
			throw (StackOverflowError) t;
		} else {
			ignoreCallback = true;
			throw new VMError(
					"An error occurred while executing the DSE instrumentation.",
					t);
		}
	}

	/*
	 * Internal callbacks -- callbacks generated by VM internally -- not
	 * directly from instrumented user program
	 */

	// public static void internal__BRANCH_ADDED_TO_PC(Constraint conjunct) {
	// vm.countCallback();
	// for (IVM listener: vm.listeners)
	// listener.BRANCH_ADDED_TO_PC(conjunct);
	// }

	/*
	 * External callbacks -- comes directly from instrumented user program
	 */

	public static void CALLER_STACK_PARAM(int value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALLER_STACK_PARAM(boolean value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALLER_STACK_PARAM(byte value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALLER_STACK_PARAM(char value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALLER_STACK_PARAM(short value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALLER_STACK_PARAM(long value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALLER_STACK_PARAM(float value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALLER_STACK_PARAM(double value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALLER_STACK_PARAM(Object value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALLER_STACK_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * Line number in the Java source code.
	 */
	public static void SRC_LINE_NUMBER(int lineNr) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.SRC_LINE_NUMBER(lineNr);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * New method frame, before first instruction
	 */
	public static void METHOD_BEGIN(int access, String className,
			String methName, String methDesc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN(access, className, methName, methDesc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * Max values of a method
	 */
	public static void METHOD_MAXS(String className, String methName,
			String methDesc, int maxStack, int maxLocals) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_MAXS(className, methName, methDesc, maxStack,
						maxLocals);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * Pass concrete values that the caller passed into a method call, before
	 * first instruction of called method is executed.
	 */
	public static void METHOD_BEGIN_PARAM(int value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_PARAM(boolean value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_PARAM(byte value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_PARAM(char value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_PARAM(short value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_PARAM(long value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_PARAM(float value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_PARAM(double value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_PARAM(Object value, int nr,
			int calleeLocalsIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_PARAM(nr, calleeLocalsIndex, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void METHOD_BEGIN_RECEIVER(Object value) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.METHOD_BEGIN_RECEIVER(value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALL_RESULT(String owner, String name, String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALL_RESULT(owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALL_RESULT(boolean res, String owner, String name,
			String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALL_RESULT(res, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALL_RESULT(int res, String owner, String name,
			String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALL_RESULT(res, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALL_RESULT(long res, String owner, String name,
			String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALL_RESULT(res, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALL_RESULT(double res, String owner, String name,
			String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALL_RESULT(res, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALL_RESULT(float res, String owner, String name,
			String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALL_RESULT(res, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALL_RESULT(Object res, String owner, String name,
			String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALL_RESULT(res, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * New basic block that is not the start of an exception handler
	 */
	public static void BB_BEGIN() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.BB_BEGIN();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * New basic block that is the start of an exception handler
	 */
	public static void HANDLER_BEGIN(int access, String className,
			String methName, String methDesc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.HANDLER_BEGIN(access, className, methName, methDesc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/*
	 * Interpreter for the 200-odd JVM bytecode instructions
	 */

	public static void NOP() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.NOP();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ACONST_NULL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ACONST_NULL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iconst_i
	 */
	public static void ICONST_M1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ICONST_M1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iconst_i
	 */
	public static void ICONST_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ICONST_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iconst_i
	 */
	public static void ICONST_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ICONST_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iconst_i
	 */
	public static void ICONST_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ICONST_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iconst_i
	 */
	public static void ICONST_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ICONST_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iconst_i
	 */
	public static void ICONST_4() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ICONST_4();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iconst_i
	 */
	public static void ICONST_5() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ICONST_5();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LCONST_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LCONST_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LCONST_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LCONST_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FCONST_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FCONST_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FCONST_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FCONST_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FCONST_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FCONST_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DCONST_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DCONST_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DCONST_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DCONST_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void BIPUSH(int value) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.BIPUSH(value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void SIPUSH(int value) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.SIPUSH(value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#ldc
	 */
	public static void LDC(String x) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LDC(x);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#ldc
	 */
	public static void LDC(Class<?> x) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LDC(x);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#ldc
	 */
	public static void LDC(int x) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LDC(x);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#ldc
	 */
	public static void LDC(float x) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LDC(x);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/** Handled by LDC */
	public static void LDC_W() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LDC_W();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#ldc2_w
	 */
	public static void LDC2_W(long x) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LDC2_W(x);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#ldc2_w
	 */
	public static void LDC2_W(double x) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LDC2_W(x);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iload
	 */
	public static void ILOAD(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ILOAD(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LLOAD(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LLOAD(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FLOAD(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FLOAD(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DLOAD(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DLOAD(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ALOAD(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ALOAD(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iload_n
	 */
	public static void ILOAD_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ILOAD_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ILOAD_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ILOAD_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ILOAD_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ILOAD_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ILOAD_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ILOAD_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#lload_n
	 */
	public static void LLOAD_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LLOAD_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LLOAD_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LLOAD_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LLOAD_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LLOAD_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LLOAD_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LLOAD_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc4. html#fload_n
	 */
	public static void FLOAD_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FLOAD_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FLOAD_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FLOAD_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FLOAD_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FLOAD_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FLOAD_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FLOAD_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3. html#dload_n
	 */
	public static void DLOAD_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DLOAD_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DLOAD_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DLOAD_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DLOAD_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DLOAD_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DLOAD_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DLOAD_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc
	 * . html#aload_n
	 */
	public static void ALOAD_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ALOAD_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ALOAD_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ALOAD_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ALOAD_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ALOAD_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ALOAD_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ALOAD_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IALOAD(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IALOAD(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LALOAD(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LALOAD(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FALOAD(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FALOAD(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DALOAD(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DALOAD(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void AALOAD(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.AALOAD(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void BALOAD(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.BALOAD(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CALOAD(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CALOAD(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void SALOAD(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.SALOAD(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#istore
	 */
	public static void ISTORE(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ISTORE(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LSTORE(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LSTORE(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FSTORE(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FSTORE(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DSTORE(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DSTORE(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ASTORE(int i) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ASTORE(i);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#istore_n
	 */
	public static void ISTORE_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ISTORE_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ISTORE_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ISTORE_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ISTORE_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ISTORE_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ISTORE_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ISTORE_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc8. html#lstore_n
	 */
	public static void LSTORE_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LSTORE_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LSTORE_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LSTORE_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LSTORE_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LSTORE_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LSTORE_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LSTORE_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc4. html#fstore_n
	 */
	public static void FSTORE_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FSTORE_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FSTORE_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FSTORE_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FSTORE_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FSTORE_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FSTORE_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FSTORE_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc3. html#dstore_n
	 */
	public static void DSTORE_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DSTORE_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DSTORE_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DSTORE_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DSTORE_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DSTORE_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DSTORE_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DSTORE_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc
	 * . html#aSTORE_n
	 */
	public static void ASTORE_0() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ASTORE_0();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ASTORE_1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ASTORE_1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ASTORE_2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ASTORE_2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ASTORE_3() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ASTORE_3();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IASTORE(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IASTORE(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LASTORE(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LASTORE(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FASTORE(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FASTORE(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DASTORE(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DASTORE(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void AASTORE(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.AASTORE(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void BASTORE(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.BASTORE(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CASTORE(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CASTORE(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void SASTORE(Object receiver, int index) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.SASTORE(receiver, index);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void POP() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.POP();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void POP2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.POP2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DUP() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DUP();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DUP_X1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DUP_X1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DUP_X2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DUP_X2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DUP2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DUP2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DUP2_X1() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DUP2_X1();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DUP2_X2() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DUP2_X2();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void SWAP() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.SWAP();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#iadd
	 */
	public static void IADD() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IADD();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LADD() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LADD();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FADD() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FADD();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DADD() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DADD();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ISUB() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ISUB();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LSUB() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LSUB();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FSUB() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FSUB();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DSUB() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DSUB();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	/**
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
	 * doc6. html#imul
	 */
	public static void IMUL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IMUL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LMUL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LMUL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FMUL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FMUL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DMUL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DMUL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IDIV(int rhs) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IDIV(rhs);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LDIV(long rhs) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LDIV(rhs);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FDIV(float rhs) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FDIV(rhs);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DDIV(double rhs) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DDIV(rhs);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IREM(int rhs) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IREM(rhs);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LREM(long rhs) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LREM(rhs);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FREM(float rhs) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FREM(rhs);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DREM(double rhs) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DREM(rhs);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void INEG() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.INEG();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LNEG() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LNEG();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FNEG() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FNEG();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DNEG() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DNEG();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ISHL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ISHL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LSHL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LSHL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ISHR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ISHR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LSHR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LSHR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IUSHR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IUSHR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LUSHR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LUSHR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IAND() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IAND();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LAND() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LAND();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IOR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IOR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LOR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LOR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IXOR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IXOR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LXOR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LXOR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IINC(int i, int value) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IINC(i, value);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void I2L() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.I2L();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void I2F() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.I2F();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void I2D() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.I2D();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void L2I() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.L2I();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void L2F() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.L2F();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void L2D() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.L2D();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void F2I() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.F2I();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void F2L() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.F2L();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void F2D() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.F2D();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void D2I() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.D2I();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void D2L() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.D2L();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void D2F() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.D2F();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void I2B() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.I2B();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void I2C() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.I2C();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void I2S() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.I2S();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LCMP() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LCMP();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FCMPL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FCMPL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FCMPG() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FCMPG();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DCMPL() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DCMPL();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DCMPG() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DCMPG();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IFEQ(int param, String className, String methName,
			int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IFEQ(className, methName, branchIndex, param);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IFNE(int param, String className, String methName,
			int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IFNE(className, methName, branchIndex, param);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IFLT(int param, String className, String methName,
			int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IFLT(className, methName, branchIndex, param);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IFGE(int param, String className, String methName,
			int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IFGE(className, methName, branchIndex, param);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IFGT(int param, String className, String methName,
			int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IFGT(className, methName, branchIndex, param);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IFLE(int param, String className, String methName,
			int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IFLE(className, methName, branchIndex, param);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IF_ICMPEQ(int left, int right, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IF_ICMPEQ(className, methName, branchIndex, left,
						right);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IF_ICMPNE(int left, int right, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IF_ICMPNE(className, methName, branchIndex, left,
						right);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IF_ICMPLT(int left, int right, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IF_ICMPLT(className, methName, branchIndex, left,
						right);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IF_ICMPGE(int left, int right, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IF_ICMPGE(className, methName, branchIndex, left,
						right);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IF_ICMPGT(int left, int right, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IF_ICMPGT(className, methName, branchIndex, left,
						right);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IF_ICMPLE(int left, int right, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IF_ICMPLE(className, methName, branchIndex, left,
						right);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IF_ACMPEQ(Object left, Object right, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IF_ACMPEQ(className, methName, branchIndex, left,
						right);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IF_ACMPNE(Object left, Object right, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IF_ACMPNE(className, methName, branchIndex, left,
						right);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void GOTO() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.GOTO();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void JSR() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.JSR();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void RET() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.RET();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void TABLESWITCH(int target, int min, int max,
			String className, String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.TABLESWITCH(className, methName, branchIndex, target,
						min, max);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LOOKUPSWITCH(int target, int[] goals, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LOOKUPSWITCH(className, methName, branchIndex, target,
						goals);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IRETURN() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IRETURN();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void LRETURN() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.LRETURN();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void FRETURN() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.FRETURN();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void DRETURN() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.DRETURN();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ARETURN() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ARETURN();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void RETURN() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.RETURN();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void GETSTATIC(String owner, String name, String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.GETSTATIC(owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void PUTSTATIC(String owner, String name, String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.PUTSTATIC(owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void GETFIELD(Object receiver, String owner, String name,
			String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.GETFIELD(receiver, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void PUTFIELD(Object receiver, String owner, String name,
			String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.PUTFIELD(receiver, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void INVOKESTATIC(String owner, String name, String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.INVOKESTATIC(owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void INVOKESPECIAL(String owner, String name, String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.INVOKESPECIAL(owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void INVOKEVIRTUAL(Object receiver, String owner,
			String name, String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.INVOKEVIRTUAL(receiver, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void INVOKESPECIAL(Object receiver, String owner,
			String name, String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.INVOKESPECIAL(receiver, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void INVOKEINTERFACE(Object receiver, String owner,
			String name, String desc) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.INVOKEINTERFACE(receiver, owner, name, desc);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void UNUSED() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.UNUSED();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	protected static Class<?> getArrayComponentType(int componentTypeInt) {
		switch (componentTypeInt) {
		case 4:
			return boolean.class;
		case 5:
			return char.class;
		case 6:
			return float.class;
		case 7:
			return double.class;
		case 8:
			return byte.class;
		case 9:
			return short.class;
		case 10:
			return int.class;
		case 11:
			return long.class;
		default:
			throw new IllegalArgumentException(componentTypeInt
					+ " is not a legal newarray component type.");
		}
	}

	public static void NEWARRAY(int length, int componentTypeInt) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.NEWARRAY(length,
						getArrayComponentType(componentTypeInt));
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ANEWARRAY(int length, String componentTypeName) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ANEWARRAY(length, componentTypeName);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ARRAYLENGTH(Object reference) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ARRAYLENGTH(reference);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void ATHROW(Object throwable) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.ATHROW((Throwable) throwable);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void CHECKCAST(Object reference, String typeName) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.CHECKCAST(reference, typeName);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void INSTANCEOF(Object reference, String typeName) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.INSTANCEOF(reference, typeName);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void MONITORENTER() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.MONITORENTER();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void MONITOREXIT() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.MONITOREXIT();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void WIDE() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.WIDE();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void MULTIANEWARRAY(String arrayTypeDesc, int nrDimensions) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.MULTIANEWARRAY(arrayTypeDesc, nrDimensions);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IFNULL(Object param, String className, String methName,
			int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IFNULL(className, methName, branchIndex, param);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void IFNONNULL(Object param, String className,
			String methName, int branchIndex) {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.IFNONNULL(className, methName, branchIndex, param);
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void GOTO_W() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.GOTO_W();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static void JSR_W() {
		if (ignoreCallback)
			return;
		ignoreCallback = true;
		vm.countCallback();
		try {
			for (IVM listener : vm.listeners)
				listener.JSR_W();
		} catch (Throwable t) {
			handleException(t);
		}
		ignoreCallback = false;
	}

	public static VM getInstance() {
		return vm;
	}
	
	public static void clearInstance() {
		vm = new VM();
	}
	
}
