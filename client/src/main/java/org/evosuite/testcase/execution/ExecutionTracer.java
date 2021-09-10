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
package org.evosuite.testcase.execution;

import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.dataflow.Definition;
import org.evosuite.coverage.dataflow.Use;
import org.evosuite.instrumentation.testability.BooleanHelper;
import org.evosuite.seeding.ConstantPoolManager;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * This class collects information about chosen branches/paths at runtime
 *
 * @author Gordon Fraser
 */
public class ExecutionTracer {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionTracer.class);

    private static ExecutionTracer instance = null;

    /**
     * We need to disable the execution tracer sometimes, e.g. when calling
     * equals in the branch distance function
     */
    private boolean disabled = true;

    /**
     * Flag that is used to kill threads that are stuck in endless loops
     */
    private boolean killSwitch = false;

    private int num_statements = 0;

    private ExecutionTrace trace;


    private static boolean checkCallerThread = true;

    /**
     * If a thread of a test case survives for some reason (e.g. long call to
     * external library), then we don't want its data in the current trace
     */
    private static volatile Thread currentThread = null;

    /**
     * <p>
     * setThread
     * </p>
     *
     * @param thread a {@link java.lang.Thread} object.
     */
    public static void setThread(Thread thread) {
        currentThread = thread;
    }

    /**
     * <p>
     * disable
     * </p>
     */
    public static void disable() {
        ExecutionTracer tracer = ExecutionTracer.getExecutionTracer();
        tracer.disabled = true;
    }

    /**
     * <p>
     * enable
     * </p>
     */
    public static void enable() {
        ExecutionTracer tracer = ExecutionTracer.getExecutionTracer();
        tracer.disabled = false;
    }

    /**
     * <p>
     * isEnabled
     * </p>
     *
     * @return a boolean.
     */
    public static boolean isEnabled() {
        ExecutionTracer tracer = ExecutionTracer.getExecutionTracer();
        return !tracer.disabled;
    }

    /**
     * <p>
     * Setter for the field <code>killSwitch</code>.
     * </p>
     *
     * @param value a boolean.
     */
    public static void setKillSwitch(boolean value) {
        ExecutionTracer tracer = ExecutionTracer.getExecutionTracer();
        tracer.killSwitch = value;
    }

    /**
     * <p>
     * Setter for the field <code>checkCallerThread</code>.
     * </p>
     *
     * @param checkCallerThread a boolean.
     */
    public static void setCheckCallerThread(boolean checkCallerThread) {
        ExecutionTracer.checkCallerThread = checkCallerThread;
    }

    /**
     * <p>
     * enable context instrumentation
     * </p>
     */
    public static void enableContext() {
        logger.info("enable context and trace instrumentation");
        ExecutionTraceImpl.enableContext();
    }

    /**
     * <p>
     * disable context instrumentation
     * </p>
     */
    public static void disableContext() {
        logger.info("disable context and trace instrumentation");
        ExecutionTraceImpl.disableContext();
    }

    /**
     * <p>
     * disableTraceCalls
     * </p>
     */
    public static void disableTraceCalls() {
        ExecutionTraceImpl.disableTraceCalls();
    }

    /**
     * <p>
     * enableTraceCalls
     * </p>
     */
    public static void enableTraceCalls() {
        ExecutionTraceImpl.enableTraceCalls();
    }

    public static boolean isTraceCallsEnabled() {
        return ExecutionTraceImpl.isTraceCallsEnabled();
    }

    /**
     * <p>
     * getExecutionTracer
     * </p>
     *
     * @return a {@link org.evosuite.testcase.execution.ExecutionTracer} object.
     */
    public static ExecutionTracer getExecutionTracer() {
        if (instance == null) {
            instance = new ExecutionTracer();
        }
        return instance;
    }

    /**
     * Reset for new execution
     */
    public void clear() {
        trace = new ExecutionTraceProxy();
        BooleanHelper.clearStack();
        num_statements = 0;
    }

    /**
     * Obviously more than one thread is executing during the creation of
     * concurrent TestCases. #TODO steenbuck we should test if
     * Thread.currentThread() is in the set of currently executing threads
     *
     * @return
     */
    public static boolean isThreadNeqCurrentThread() {
        if (!checkCallerThread) {
            return false;
        }
        if (currentThread == null) {
            logger.error("CurrentThread has not been set!");
            Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
            for (Thread t : map.keySet()) {
                String msg = "Thread: " + t + "\n";
                for (StackTraceElement e : map.get(t)) {
                    msg += " -> " + e + "\n";
                }
                logger.error(msg);
            }
            currentThread = Thread.currentThread();
        }
        return Thread.currentThread() != currentThread;
    }

    /**
     * Return trace of current execution
     *
     * @return a {@link org.evosuite.testcase.execution.ExecutionTrace} object.
     */
    public ExecutionTrace getTrace() {
        trace.finishCalls();
        return trace;

        // ExecutionTrace copy = trace.clone();
        // // copy.finishCalls();
        // return copy;
    }

    /**
     * Return the last explicitly thrown exception
     *
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getLastException() {
        return trace.getExplicitException();
    }

    /**
     * Called by instrumented code whenever a new method is called
     *
     * @param classname  a {@link java.lang.String} object.
     * @param methodname a {@link java.lang.String} object.
     * @param caller     a {@link java.lang.Object} object.
     * @throws org.evosuite.testcase.execution.TestCaseExecutor$TimeoutExceeded if any.
     */
    public static void enteredMethod(String classname, String methodname, Object caller)
            throws TestCaseExecutor.TimeoutExceeded {
        ExecutionTracer tracer = getExecutionTracer();

        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        //logger.trace("Entering method " + classname + "." + methodname);
        tracer.trace.enteredMethod(classname, methodname, caller);
    }

    /**
     * Called by instrumented code whenever a return values is produced
     *
     * @param value      a int.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     */
    public static void returnValue(int value, String className, String methodName) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        //logger.trace("Return value: " + value);
        tracer.trace.returnValue(className, methodName, value);
    }

    /**
     * Called by instrumented code whenever a return values is produced
     *
     * @param value      a {@link java.lang.Object} object.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     */
    public static void returnValue(Object value, String className, String methodName) {
        if (isThreadNeqCurrentThread())
            return;

        if (!ExecutionTracer.isEnabled())
            return;

        if (value == null) {
            returnValue(0, className, methodName);
            return;
        }
        StringBuilder tmp = null;
        try {
            // setLineCoverageDeactivated(true);
            // logger.warn("Disabling tracer: returnValue");
            ExecutionTracer.disable();
            tmp = new StringBuilder(value.toString());
        } catch (Throwable t) {
            return;
        } finally {
            ExecutionTracer.enable();
        }
        int index = 0;
        int position = 0;
        boolean found = false;
        boolean deleteAddresses = true;
        char c = ' ';
        // quite fast method to detect memory addresses in Strings.
        while ((position = tmp.indexOf("@", index)) > 0) {
            for (index = position + 1; index < position + 17 && index < tmp.length(); index++) {
                c = tmp.charAt(index);
                if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
                        || (c >= 'A' && c <= 'F')) {
                    found = true;
                } else {
                    break;
                }
            }
            if (deleteAddresses && found) {
                tmp.delete(position + 1, index);
            }
        }

        returnValue(tmp.toString().hashCode(), className, methodName);
    }

    /**
     * Called by instrumented code whenever a method is left
     *
     * @param classname  a {@link java.lang.String} object.
     * @param methodname a {@link java.lang.String} object.
     */
    public static void leftMethod(String classname, String methodname) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        tracer.trace.exitMethod(classname, methodname);
        // logger.trace("Left method " + classname + "." + methodname);
    }

    /**
     * Called by the instrumented code each time a new source line is executed
     */
    public static void checkTimeout() {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (tracer.killSwitch) {
            // logger.info("Raising TimeoutException as kill switch is active - passedLine");
            if (!isInStaticInit())
                throw new TestCaseExecutor.TimeoutExceeded();
        }
    }

    private static boolean isInStaticInit() {
        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
            if (elem.getMethodName().equals("<clinit>"))
                return true;
        }
        return false;
    }

    /**
     * Called by the instrumented code each time a new source line is executed
     *
     * @param line       a int.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     */
    public static void passedLine(String className, String methodName, int line) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        tracer.trace.linePassed(className, methodName, line);
    }

    /**
     * Called by the instrumented code each time a new branch is taken
     *
     * @param val         a int.
     * @param opcode      a int.
     * @param branch      a int.
     * @param bytecode_id a int.
     */
    public static void passedBranch(int val, int opcode, int branch, int bytecode_id) {

        ExecutionTracer tracer = getExecutionTracer();
        // logger.info("passedBranch val="+val+", opcode="+opcode+", branch="+branch+", bytecode_id="+bytecode_id);
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        ConstantPoolManager.getInstance().addDynamicConstant(val);

        // logger.trace("Called passedBranch1 with opcode "+AbstractVisitor.OPCODES[opcode]+" and val "+val+" in branch "+branch);
        double distance_true = 0.0;
        double distance_false = 0.0;
        switch (opcode) {
            case Opcodes.IFEQ:
                distance_true = Math.abs((double) val); // The greater abs is, the
                // further away from 0
                distance_false = distance_true == 0 ? 1.0 : 0.0; // Anything but 0
                // is good
                break;
            case Opcodes.IFNE:
                distance_false = Math.abs((double) val); // The greater abs is, the
                // further away from 0
                distance_true = distance_false == 0 ? 1.0 : 0.0; // Anything but 0
                // leads to NE
                break;
            case Opcodes.IFLT:
                distance_true = val >= 0 ? val + 1.0 : 0.0; // The greater, the
                // further away from < 0
                distance_false = val < 0 ? 0.0 - val + 1.0 : 0.0; // The smaller,
                // the further
                // away from < 0
                break;
            case Opcodes.IFGT:
                distance_true = val <= 0 ? 0.0 - val + 1.0 : 0.0;
                distance_false = val > 0 ? val + 1.0 : 0.0;
                break;
            case Opcodes.IFGE:
                distance_true = val < 0 ? 0.0 - val + 1.0 : 0.0;
                distance_false = val >= 0 ? val + 1.0 : 0.0;
                break;
            case Opcodes.IFLE:
                distance_true = val > 0 ? val + 1.0 : 0.0; // The greater, the
                // further away from < 0
                distance_false = val <= 0 ? 0.0 - val + 1.0 : 0.0; // The smaller,
                // the further
                // away from < 0
                break;
            default:
                logger.error("Unknown opcode: " + opcode);

        }
        // logger.trace("1 Branch distance true : " + distance_true);
        // logger.trace("1 Branch distance false: " + distance_false);

        // Add current branch to control trace
        tracer.trace.branchPassed(branch, bytecode_id, distance_true, distance_false);
    }

    public static void passedPutStatic(String classNameWithDots, String fieldName) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        tracer.trace.putStaticPassed(classNameWithDots, fieldName);
    }


    /**
     * This method is added in the transformed bytecode
     *
     * @param className
     */
    public static void exitClassInit(String className) {

        final String classNameWithDots = className.replace('/', '.');

        ExecutionTracer tracer = getExecutionTracer();
//		if (tracer.disabled)
//			return;
//
//		if (isThreadNeqCurrentThread())
//			return;
//
//		checkTimeout();

        tracer.trace.classInitialized(classNameWithDots);

    }

    /**
     * @param classNameWithDots
     * @param fieldName
     */
    public static void passedGetStatic(String classNameWithDots, String fieldName) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        tracer.trace.getStaticPassed(classNameWithDots, fieldName);
    }


    /**
     * Called by the instrumented code each time a new branch is taken
     *
     * @param val1        a int.
     * @param val2        a int.
     * @param opcode      a int.
     * @param branch      a int.
     * @param bytecode_id a int.
     */
    public static void passedBranch(int val1, int val2, int opcode, int branch,
                                    int bytecode_id) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        ConstantPoolManager.getInstance().addDynamicConstant(val1);
        ConstantPoolManager.getInstance().addDynamicConstant(val2);

		/* logger.trace("Called passedBranch2 with opcode "
		        + AbstractVisitor.OPCODES[opcode] + ", val1=" + val1 + ", val2=" + val2
		        + " in branch " + branch); */
        double distance_true = 0;
        double distance_false = 0;
        switch (opcode) {
            // Problem is that the JVM is a stack machine
            // x < 5 gets compiled to a val2 > val1,
            // because operators are on the stack in reverse order
            case Opcodes.IF_ICMPEQ:
                // The greater the difference, the further away
                distance_true = Math.abs((double) val1 - (double) val2);
                // Anything but 0 is good
                distance_false = distance_true == 0 ? 1.0 : 0.0;
                break;
            case Opcodes.IF_ICMPNE:
                // The greater abs is, the further away from 0
                distance_false = Math.abs((double) val1 - (double) val2);
                // Anything but 0 leads to NE
                distance_true = distance_false == 0 ? 1.0 : 0.0;
                break;
            case Opcodes.IF_ICMPLT:
                // val1 >= val2?
                distance_true = val1 >= val2 ? (double) val1 - (double) val2 + 1.0 : 0.0;
                distance_false = val1 < val2 ? (double) val2 - (double) val1 + 1.0 : 0.0;
                break;
            case Opcodes.IF_ICMPGE:
                // val1 < val2?
                distance_true = val1 < val2 ? (double) val2 - (double) val1 + 1.0 : 0.0;
                distance_false = val1 >= val2 ? (double) val1 - (double) val2 + 1.0 : 0.0;
                break;
            case Opcodes.IF_ICMPGT:
                // val1 <= val2?
                distance_true = val1 <= val2 ? (double) val2 - (double) val1 + 1.0 : 0.0;
                distance_false = val1 > val2 ? (double) val1 - (double) val2 + 1.0 : 0.0;
                break;
            case Opcodes.IF_ICMPLE:
                // val1 > val2?
                distance_true = val1 > val2 ? (double) val1 - (double) val2 + 1.0 : 0.0;
                distance_false = val1 <= val2 ? (double) val2 - (double) val1 + 1.0 : 0.0;
                break;
            default:
                logger.error("Unknown opcode: " + opcode);
        }
        // logger.trace("2 Branch distance true: " + distance_true);
        // logger.trace("2 Branch distance false: " + distance_false);

        // Add current branch to control trace
        tracer.trace.branchPassed(branch, bytecode_id, distance_true, distance_false);
        // tracer.trace.branchPassed(branch, distance_true, distance_false);

    }

    /**
     * Called by the instrumented code each time a new branch is taken
     *
     * @param val1        a {@link java.lang.Object} object.
     * @param val2        a {@link java.lang.Object} object.
     * @param opcode      a int.
     * @param branch      a int.
     * @param bytecode_id a int.
     */
    public static void passedBranch(Object val1, Object val2, int opcode, int branch,
                                    int bytecode_id) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        double distance_true = 0;
        double distance_false = 0;
        // logger.warn("Disabling tracer: passedBranch with 2 Objects");

        switch (opcode) {
            case Opcodes.IF_ACMPEQ:
                distance_true = val1 == val2 ? 0.0 : 1.0;
                break;
            case Opcodes.IF_ACMPNE:
                distance_true = val1 != val2 ? 0.0 : 1.0;
                break;
        }

        distance_false = distance_true == 0 ? 1.0 : 0.0;

        // Add current branch to control trace
        tracer.trace.branchPassed(branch, bytecode_id, distance_true, distance_false);
    }

    /**
     * Called by the instrumented code each time a new branch is taken
     *
     * @param val         a {@link java.lang.Object} object.
     * @param opcode      a int.
     * @param branch      a int.
     * @param bytecode_id a int.
     */
    public static void passedBranch(Object val, int opcode, int branch, int bytecode_id) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        double distance_true = 0;
        double distance_false = 0;
        switch (opcode) {
            case Opcodes.IFNULL:
                distance_true = val == null ? 0.0 : 1.0;
                break;
            case Opcodes.IFNONNULL:
                distance_true = val == null ? 1.0 : 0.0;
                break;
            default:
                logger.error("Warning: encountered opcode " + opcode);
        }
        distance_false = distance_true == 0 ? 1.0 : 0.0;
        // enable();

        // logger.trace("Branch distance true: " + distance_true);
        // logger.trace("Branch distance false: " + distance_false);

        // Add current branch to control trace
        tracer.trace.branchPassed(branch, bytecode_id, distance_true, distance_false);
    }

    /**
     * Called by instrumented code each time a variable gets written to (a
     * Definition)
     *
     * @param caller a {@link java.lang.Object} object.
     * @param defID  a int.
     */
    public static void passedDefinition(Object object, Object caller, int defID) {
        if (isThreadNeqCurrentThread())
            return;

        ExecutionTracer tracer = getExecutionTracer();
        if (!tracer.disabled)
            tracer.trace.definitionPassed(object, caller, defID);
    }

    /**
     * Called by instrumented code each time a variable is read from (a Use)
     *
     * @param caller a {@link java.lang.Object} object.
     * @param useID  a int.
     */
    public static void passedUse(Object object, Object caller, int useID) {

        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        tracer.trace.usePassed(object, caller, useID);
    }

    /**
     * Called by instrumented code each time a field method call is passed
     * <p>
     * Since it was not clear whether the field method call constitutes a
     * definition or a use when the instrumentation was initially added this
     * method will redirect the call accordingly
     *
     * @param caller
     * @param defuseId
     */
    public static void passedFieldMethodCall(Object callee, Object caller, int defuseId) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        if (DefUsePool.isKnownAsDefinition(defuseId)) {
            Definition passedDef = DefUsePool.getDefinitionByDefUseId(defuseId);
            passedDefinition(callee, caller, passedDef.getDefId());
        } else if (DefUsePool.isKnownAsUse(defuseId)) {
            Use passedUse = DefUsePool.getUseByDefUseId(defuseId);
            passedUse(callee, caller, passedUse.getUseId());
        } else
            throw new EvosuiteError(
                    "instrumentation called passedFieldMethodCall with invalid defuseId: "
                            + defuseId + ", known IDs: " + DefUsePool.getDefUseCounter());
    }

    /**
     * <p>
     * passedMutation
     * </p>
     *
     * @param distance   a double.
     * @param mutationId a int.
     */
    public static void passedMutation(double distance, int mutationId) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        tracer.trace.mutationPassed(mutationId, distance);
    }

    /**
     * <p>
     * exceptionThrown
     * </p>
     *
     * @param exception  a {@link java.lang.Object} object.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     */
    public static void exceptionThrown(Object exception, String className,
                                       String methodName) {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        tracer.trace.setExplicitException((Throwable) exception);

    }

    /**
     * <p>
     * statementExecuted
     * </p>
     */
    public static void statementExecuted() {
        ExecutionTracer tracer = getExecutionTracer();
        if (tracer.disabled)
            return;

        if (isThreadNeqCurrentThread())
            return;

        checkTimeout();

        tracer.num_statements++;
    }

    /**
     * <p>
     * getNumStatementsExecuted
     * </p>
     *
     * @return a int.
     */
    public int getNumStatementsExecuted() {
        return num_statements;
    }

    private ExecutionTracer() {
        trace = new ExecutionTraceProxy();
    }

}
