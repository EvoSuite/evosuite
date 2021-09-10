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
package org.evosuite.symbolic.vm;

import org.evosuite.dse.AbstractVM;
import org.evosuite.symbolic.LambdaUtils;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.fp.RealConstant;
import org.evosuite.symbolic.expr.ref.ReferenceConstant;
import org.evosuite.symbolic.expr.ref.ReferenceExpression;
import org.evosuite.symbolic.expr.reftype.LambdaSyntheticType;
import org.evosuite.symbolic.expr.str.StringBinaryExpression;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.instrument.ConcolicInstrumentingClassLoader;
import org.evosuite.symbolic.instrument.ConcolicMethodAdapter;
import org.evosuite.symbolic.vm.heap.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;
import org.objectweb.asm.Type;

import java.lang.reflect.*;
import java.util.*;

/**
 * Explicit inter-procedural control transfer: InvokeXXX, Return, etc.
 * <p>
 * We ignore the CALLER_STACK_PARAM calls here, as we have maintained the
 * operand stack during the caller's execution, so we already know the operand
 * stack values and therefore the parameter values to be used for a given method
 * invocation.
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public final class CallVM extends AbstractVM {

    public static final String STRING_CONCATENATION_INDY_PARAMETER_IDENTIFIER = "\u0001";
    /**
     * Environment
     */
    private final SymbolicEnvironment env;

    /**
     * Instrumentation
     */
    private final ConcolicInstrumentingClassLoader classLoader;

    private int stackParamCount = 0;
    private final HashMap<Member, MemberInfo> memberInfos = new HashMap<>();

    /**
     * Constructor
     */
    public CallVM(SymbolicEnvironment env, ConcolicInstrumentingClassLoader classLoader) {
        this.env = env;
        this.classLoader = classLoader;
    }

    /**
     * Begin of a basic block that is the begin of an exception handler.
     * <p>
     * We could be in an entirely different invocation frame than the previous
     * instruction was in.
     * <p>
     * TODO: Account for different call sites in the same method. This may lead
     * to the need to discard frames although they are of the same function as
     * indicated by the parameters.
     */
    @Override
    public void HANDLER_BEGIN(int access, String className, String methName, String methDesc) {

        if (conf.CLINIT.equals(methName)) {

            discardFramesClassInitializer(className, methName);

        } else {

            // the method or constructor containing this handler
            Member function = null;
            if (conf.INIT.equals(methName))
                function = resolveConstructorOverloading(className, methDesc);
            else
                function = resolveMethodOverloading(className, methName, methDesc);

            /**
             * function could be equal to null if handler is in class
             * initializer
             */
            discardFrames(className, methName, function);
        }

        env.topFrame().operandStack.clearOperands();
        /**
         * This exception is added to the HANDLER_BEGIN because no other
         * instruction adds the corresponding exception. The handler will store
         * the exception to the locals table
         */
        ReferenceConstant exception_reference = new ReferenceConstant(Type.getType(Exception.class), -1);
        env.topFrame().operandStack.pushRef(exception_reference);
    }

    /**
     * Cache max values for this method, except for static initializers.
     */
    @Override
    public void METHOD_MAXS(String className, String methName, String methDesc, int maxStack, int maxLocals) {
        if (conf.CLINIT.equals(methName))
            return;

        Member member = null;
        if (conf.INIT.equals(methName))
            member = resolveConstructorOverloading(className, methDesc);
        else
            member = resolveMethodOverloading(className, methName, methDesc);

        if (member == null)
            return; // TODO: could not resolve method or constructor

        if (memberInfos.containsKey(member))
            return;

        memberInfos.put(member, new MemberInfo(maxStack, maxLocals));
    }

    /**
     * Pop operands off caller stack
     * <p>
     * Method methName is about to start execution.
     * <p>
     * At this point we have either already seen (observed InvokeXXX) or missed
     * this invocation of the methName method.
     * <p>
     * We miss any of the following: - invoke <clinit> (as there are no such
     * statements) - invoke <init> (as we do not add instrumentation code for
     * these)
     * <p>
     * User code cannot call the <clinit>() method directly. Instead, the JVM
     * invokes a class's initializer implicitly, upon the first use of the
     * class.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Concepts.doc.html
     * #32316
     * http://java.sun.com/docs/books/jvms/second_edition/html/Concepts.doc
     * .html#19075
     * http://java.sun.com/docs/books/jvms/second_edition/html/Overview
     * .doc.html#16262
     */
    @Override
    public void METHOD_BEGIN(int access, String className, String methName, String methDesc) {
        /* TODO: Use access param to determine needsThis */

        if (conf.CLINIT.equals(methName)) {
            CLINIT_BEGIN(className);
            return;
        }

        if (env.topFrame().weInvokedInstrumentedCode() == false
                || env.topFrame().weInvokedSyntheticLambdaCodeThatInvokesNonInstrCode()) {
            /** TODO: Stream API seems to need special treatment as the call stack is of the form:
             4 - lambda static method call (instrumented)
             3 - lambda's call (non-instrumented)
             2 - Stream API code (non-instrumented code)
             1 - stream API call (instrumented)
             0 - Code (Instruemnted)
             This way we loose track of symbolic elements in the first non-instrumented code section */
            // An uninstrumented caller has called instrumented code
            // This is problemtatic
        }

        prepareStackIfNeeded(className, methName, methDesc);

        /* Begin of a method or constructor */
        final Frame callerFrame = env.topFrame(); // guy who (transitively)
        // called us
        Frame frame;
        boolean calleeNeedsThis = false;
        if (conf.INIT.equals(methName)) {
            Constructor<?> constructor = resolveConstructorOverloading(className, methDesc);
            int maxLocals = conf.MAX_LOCALS_DEFAULT;
            MemberInfo memberInfo = memberInfos.get(constructor);
            if (memberInfo != null)
                maxLocals = memberInfo.maxLocals;
            frame = new ConstructorFrame(constructor, maxLocals);
            calleeNeedsThis = true;

            if (callerFrame.weInvokedInstrumentedCode() == false) {
                /**
                 * Since this is a constructor called from un-instrumented code,
                 * we need to "simulate" the missing NEW. This means 1) create a
                 * new object reference 2) populate the localstable with the new
                 * reference
                 */
                Class<?> clazz = classLoader.getClassForName(className);
                Type objectType = Type.getType(clazz);
                ReferenceConstant newObject = this.env.heap.buildNewReferenceConstant(objectType);
                frame.localsTable.setRefLocal(0, newObject);
            }
        } else {
            Method method = resolveMethodOverloading(className, methName, methDesc);
            int maxLocals = conf.MAX_LOCALS_DEFAULT;
            MemberInfo memberInfo = memberInfos.get(method);
            if (memberInfo != null)
                maxLocals = memberInfo.maxLocals;
            frame = new MethodFrame(method, maxLocals);
            calleeNeedsThis = !Modifier.isStatic(method.getModifiers());
        }

        /*
         * If our caller called uninstrumented code then we should not ruin his
         * operand stack! Instead, METHOD_BEGIN_PARAM will supply the concrete
         * parameter values and create corresponding symbolic constants.
         */
        if (callerFrame.weInvokedInstrumentedCode() == false) {

            env.pushFrame(frame);

            // deal with Class.newInstance?

            return;
        }

        /*
         * Our caller directly called us. We should take our parameters from his
         * stack.
         */
        Class<?>[] paramTypes = getArgumentClasses(methDesc);
        final Deque<Operand> params = new LinkedList<>();
        Iterator<Operand> it = env.topFrame().operandStack.iterator();

        for (int i = paramTypes.length - 1; i >= 0; i--) {
            // read parameters from caller operand srack
            Operand param = it.next();
            params.push(param);
        }

        int index = 0;
        for (Operand param : params) {
            frame.localsTable.setOperand(index + (calleeNeedsThis ? 1 : 0), param);
            if (param instanceof SingleWordOperand)
                index += 1;
            else if (param instanceof DoubleWordOperand)
                index += 2;
            else {
                throw new IllegalStateException("Unknown operand type " + param.getClass().getName());
            }
        }

        if (calleeNeedsThis) { // "this" instance
            Operand param = it.next();
            ReferenceOperand refOperand = (ReferenceOperand) param;
            frame.localsTable.setRefLocal(0, refOperand.getReference());
        }

        env.pushFrame(frame);
    }

    @Override
    public void METHOD_BEGIN_RECEIVER(Object value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            ReferenceExpression ref = env.heap.getReference(value);
            env.topFrame().localsTable.setRefLocal(0, ref);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, int value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            IntegerConstant literal_value = ExpressionFactory.buildNewIntegerConstant(value);
            env.topFrame().localsTable.setBv32Local(index, literal_value);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, boolean value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            METHOD_BEGIN_PARAM(nr, index, value ? 1 : 0);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, byte value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            METHOD_BEGIN_PARAM(nr, index, (int) value);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, char value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            METHOD_BEGIN_PARAM(nr, index, (int) value);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, short value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            METHOD_BEGIN_PARAM(nr, index, (int) value);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, long value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            IntegerConstant literal_value = ExpressionFactory.buildNewIntegerConstant(value);
            env.topFrame().localsTable.setBv64Local(index, literal_value);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, double value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            RealConstant literal_value = ExpressionFactory.buildNewRealConstant(value);
            env.topFrame().localsTable.setFp64Local(index, literal_value);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, float value) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            RealConstant literal_value = ExpressionFactory.buildNewRealConstant(value);
            env.topFrame().localsTable.setFp32Local(index, literal_value);
        }
    }

    @Override
    public void METHOD_BEGIN_PARAM(int nr, int index, Object conc_ref) {
        if (!env.callerFrame().weInvokedInstrumentedCode()) {
            ReferenceExpression symb_ref = env.heap.getReference(conc_ref);
            env.topFrame().localsTable.setRefLocal(index, symb_ref);
        }
    }

    /**
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#invokestatic
     */
    @Override
    public void INVOKESTATIC(String className, String methName, String methDesc) {
        stackParamCount = 0;
        env.topFrame().invokeNeedsThis = false;
        methodCall(className, methName, methDesc);
    }

    /**
     * We get this callback immediately after the user's invokedynamic instruction for string concatenation.
     * <p>
     * See: {@link ConcolicMethodAdapter#visitInvokeDynamicInsn}
     */
    @Override
    public void INVOKEDYNAMIC(String concatenationResult, String stringOwnerClass, String stringRecipe) {
        // Retrieve concatenated elements from recipe
        String[] pieces = stringRecipe.split(STRING_CONCATENATION_INDY_PARAMETER_IDENTIFIER, -1);

        List<StringConstant> symbolicLiterals = new ArrayList();
        Stack<Expression<?>> symbolicParameters = new Stack();

        // Build literals
        for (String piece : pieces) {
            symbolicLiterals.add(ExpressionFactory.buildNewStringConstant(piece));
        }

        // Pop arguments
        for (int i = 0; i < pieces.length - 1; i++) {
            Operand symbolicOperand = env.topFrame().operandStack.popOperand();
            Expression currentOperandExpression = OperandUtils.retrieveOperandExpression(symbolicOperand);

            // For Strings we take the expression stored in the heap
            if (currentOperandExpression instanceof ReferenceExpression
                    && currentOperandExpression.getConcreteValue().getClass().equals(String.class)) {
                currentOperandExpression = env.heap.getField(
                        Types.JAVA_LANG_STRING,
                        SymbolicHeap.$STRING_VALUE,
                        null,
                        (ReferenceExpression) currentOperandExpression,
                        (String) currentOperandExpression.getConcreteValue());
            }

            symbolicParameters.add(currentOperandExpression);
        }

        Expression<String> symbolicResult = symbolicLiterals.get(0);
        // We create a chain of appended elements
        for (int i = 1; i < symbolicLiterals.toArray().length; ++i) {
            Expression<?> currentParameter = symbolicParameters.pop();
            Expression<String> currentLiteral = symbolicLiterals.get(i);

            // Appends next parameter
            symbolicResult = buildNewAppendExpression(symbolicResult, currentParameter);

            // Appends next literal
            symbolicResult = new StringBinaryExpression(symbolicResult, Operator.APPEND_STRING, currentLiteral, symbolicResult.getConcreteValue() + currentLiteral.getConcreteValue());
        }


        ReferenceConstant resultReference = (ReferenceConstant) env.heap.getReference(concatenationResult);
        env.heap.putField(Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE, concatenationResult, resultReference, symbolicResult);

        env.topFrame().operandStack.pushRef(resultReference);
    }

    /**
     * Helper for creating Strings append expressions.
     *
     * @param symbolicResult
     * @param expression
     * @return
     */
    private StringBinaryExpression buildNewAppendExpression(Expression<String> symbolicResult, Expression<?> expression) {
        Class type = expression.getConcreteValue().getClass();

        if (type.equals(Integer.class) || type.equals(Long.class) || type.equals(Short.class) || type.equals(Byte.class)) {
            return new StringBinaryExpression(symbolicResult, Operator.APPEND_INTEGER, expression, symbolicResult.getConcreteValue() + expression.getConcreteValue());
        } else if (type.equals(Character.class)) {
            return new StringBinaryExpression(symbolicResult, Operator.APPEND_CHAR, expression, symbolicResult.getConcreteValue() + expression.getConcreteValue());
        } else if (type.equals(Boolean.class)) {
            return new StringBinaryExpression(symbolicResult, Operator.APPEND_BOOLEAN, expression, symbolicResult.getConcreteValue() + expression.getConcreteValue());
        } else if (type.equals(Float.class) || type.equals(Double.class)) {
            return new StringBinaryExpression(symbolicResult, Operator.APPEND_REAL, expression, symbolicResult.getConcreteValue() + expression.getConcreteValue());
        } else if (type.equals(String.class)) {
            return new StringBinaryExpression(symbolicResult, Operator.APPEND_STRING, expression, symbolicResult.getConcreteValue() + expression.getConcreteValue());
        } else {
            throw new IllegalArgumentException("Expression appended type not supported yet");
        }
    }

    /**
     * We get this callback immediately after the user's invokedynamic instruction.
     * <p>
     * See: {@link ConcolicMethodAdapter#visitInvokeDynamicInsn}
     */
    @Override
    public void INVOKEDYNAMIC(Object indyResult, String owner) {
        final Class<?> anonymousClass = indyResult.getClass();

        if (!LambdaUtils.isLambda(anonymousClass))
            throw new IllegalArgumentException("InvokeDynamic for things other than lambdas are not implemented yet!, class found: " + anonymousClass.getName());

        env.heap.buildNewLambdaConstant(anonymousClass, conf.isIgnored(owner));    // Add it as lambda owner
        Type anonymousClassType = Type.getType(anonymousClass);
        env.ensurePrepared(anonymousClass); // prepare symbolic fields

        // Create reference
        final ReferenceConstant symbolicRef = env.heap.buildNewReferenceConstant(anonymousClassType);
        env.heap.initializeReference(indyResult, symbolicRef);

        /**
         * emulate JVM's anonymous Lambda class instantiation: This
         * class seems to have the right kind of fields for all
         * scenarios (generated static lambda method or simple
         * reference to an existing static or instance method.
         * */
        final Field[] fields = anonymousClass.getDeclaredFields();
        for (int i = fields.length - 1; i >= 0; i--) {
            Operand symbolicOperand = env.topFrame().operandStack.popOperand();
            Expression<?> symbolicValue = OperandUtils.retrieveOperandExpression(symbolicOperand);
            env.heap.putField(anonymousClass.getName(), fields[i].getName(), anonymousClass, symbolicRef, symbolicValue);
        }

        env.topFrame().operandStack.pushRef(symbolicRef); // Symbolic instance produced by invokedynamic
    }

    /**
     * Used to invoke any
     * <ul>
     * <li>instance initialization method <init> = (constructor + field init)
     * </li>
     * <li>private method</li>
     * <li>method of a superclass of the current class</li>
     * </ul>
     * <p>
     * No dynamic dispatch (unlike InvokeVirtual)
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#invokespecial
     * http://java.sun.com/docs/books/jvms/second_edition
     * /html/Overview.doc.html#12174
     * http://java.sun.com/docs/books/jvms/second_edition
     * /html/Concepts.doc.html#33032
     */
    @Override
    public void INVOKESPECIAL(String className, String methName, String methDesc) {
        stackParamCount = 0;
        env.topFrame().invokeNeedsThis = true;

        if (conf.INIT.equals(methName)) {
            boolean instrumented = !conf.isIgnored(className);
            env.topFrame().invokeInstrumentedCode(instrumented);
            env.topFrame().invokeLambdaSyntheticCodeThatInvokesNonInstrCode(false);
        } else {
            methodCall(className, methName, methDesc);
        }
    }

    @Override
    public void INVOKESPECIAL(Object conc_receiver, String className, String methName, String methDesc) {
        INVOKESPECIAL(className, methName, methDesc);
//		String concreteClassName = conc_receiver.getClass().getName();
//		if (concreteClassName != null) {
//			INVOKESPECIAL(concreteClassName, methName, methDesc);
//		} else {
//			INVOKESPECIAL(className, methName, methDesc);
//		}
    }

    /**
     * We get this callback right before the user code makes the corresponding
     * virtual call to method className.methName(methDesc). See:
     * {@link ConcolicMethodAdapter#visitMethodInsn}
     *
     * <p>
     * The current instrumentation system only calls this version of
     * INVOKEVIRTUAL for methods that take two or fewer parameters.
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#invokevirtual
     */
    @Override
    public void INVOKEVIRTUAL(Object conc_receiver, String className, String methName, String methDesc) {
        stackParamCount = 0;

        env.topFrame().invokeNeedsThis = true;

        Iterator<Operand> it = env.topFrame().operandStack.iterator();
        Type[] argTypes = Type.getArgumentTypes(methDesc);
        for (int i = 0; i < argTypes.length; i++) {
            it.next();
        }
        ReferenceOperand ref_operand = (ReferenceOperand) it.next();
        ReferenceExpression symb_receiver = ref_operand.getReference();
        env.heap.initializeReference(conc_receiver, symb_receiver);

        if (nullReferenceViolation(conc_receiver, symb_receiver))
            return;

        String concreteClassName = conc_receiver.getClass().getName();
        Method virtualMethod = methodCall(concreteClassName, methName, methDesc);
        chooseReceiverType(className, conc_receiver, methDesc, virtualMethod);
    }

    /**
     * We get this callback right before the user code makes the corresponding
     * call to interface method className.methName(methDesc). See:
     * {@link ConcolicMethodAdapter#visitMethodInsn}
     *
     * <p>
     * http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.
     * doc6.html#invokeinterface
     */
    @Override
    public void INVOKEINTERFACE(Object concreteReceiver, String className, String methName, String methDesc) {
        stackParamCount = 0;
        env.topFrame().invokeNeedsThis = true;

        if (nullReferenceViolation(concreteReceiver, null))
            return;

        // Ilebrero: Lambdas doesn't seem to be instrumentable.
        // The code itself is in the respective owner class.
        if (LambdaUtils.isLambda(concreteReceiver.getClass())) {

            // Check if we call non-instrumented code
            Class anonymousClass = concreteReceiver.getClass();
            LambdaSyntheticType lambdaReferenceType = (LambdaSyntheticType) env.heap.getReferenceType(anonymousClass);

            // If this lambda hasn't been seen before, we assume it's not instrumented
            env.topFrame().invokeInstrumentedCode(!lambdaReferenceType.callsNonInstrumentedCode());
            env.topFrame().invokeLambdaSyntheticCodeThatInvokesNonInstrCode(lambdaReferenceType.callsNonInstrumentedCode());

            // Nothing to do.
            if (lambdaReferenceType.callsNonInstrumentedCode()) return;

            // TODO(ilebrero): If this lambda is related to a method reference, we need to replace the lambda's symbolic
            //                 receiver with the method reference's related instance as this is just a redirection,
            //                 is this possible? Currently when trying to get a symbolic field, as the symbolic receiver
            //                 is from the lambda, no previous symbolic elements of tat object instance are being used.

            /**
             * for closures: In case we jump to a closure, we need to add the bounded closure elements to the stack
             * 				 as this is implicitly done by the JVM. Notice that at this point the descriptor won't
             * 				 tell us about this element (i.e (Ljava/lang/Object;)Ljava/lang/Object;) but the actual
             * 				 closure method will have extra elemenets on its descriptor (i.e.
             * 				 (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;))
             * - pop original operands
             * - pop receiver
             * - push captured (as receiver fields) operands
             * - push original operands
             */
            if (anonymousClass.getDeclaredFields().length > 0) {

                // Pop current symbolic operands
                final int interfaceParamsAmount = getArgumentClasses(methDesc).length;
                final Operand[] symbolicArguments = new Operand[interfaceParamsAmount];
                for (int i = interfaceParamsAmount - 1; i >= 0; i--) {
                    symbolicArguments[i] = env.topFrame().operandStack.popOperand();
                }

                // Pop receiver
                final ReferenceExpression receiverSymbolic = env.topFrame().operandStack.popRef();

                // Push closure bounded fields expressions
                final Field[] fields = anonymousClass.getDeclaredFields();
                final Expression[] fieldExpressions = new Expression[fields.length];

                for (Field field : fields) {
                    int fieldLoc = Integer.parseInt(field.getName().substring(4)) - 1;
                    fieldExpressions[fieldLoc] = env.heap.getField(anonymousClass.getName(), field.getName(), anonymousClass, receiverSymbolic);
                }

                // Push receiver
                env.topFrame().operandStack.pushRef(receiverSymbolic);

                // Push new operands
                for (Expression expression : fieldExpressions) {
                    Operand operand = OperandUtils.expressionToOperand(expression);
                    env.topFrame().operandStack.pushOperand(operand);
                }

                // Push original operands
                for (int i = 0; i < interfaceParamsAmount; i++) {
                    env.topFrame().operandStack.pushOperand(symbolicArguments[i]);
                }

            }

            return;
        }

        String concreteClassName = concreteReceiver.getClass().getName();
        Method method = methodCall(concreteClassName, methName, methDesc);
        chooseReceiverType(className, concreteReceiver, methDesc, method);
    }

    /**
     * Dispose our frame, we have no value to return.
     */
    @Override
    public void RETURN() {
        popFrameAndDisposeCallerParams();
    }

    /**
     * Dispose our frame and transfer the return value back.
     */
    @Override
    public void IRETURN() {
        Frame returningFrame = popFrameAndDisposeCallerParams();

        if (env.topFrame().weInvokedInstrumentedCode()) {
            Operand ret_val = returningFrame.operandStack.popOperand();
            env.topFrame().operandStack.pushOperand(ret_val);
        }
    }

    @Override
    public void LRETURN() {
        IRETURN();
    }

    @Override
    public void FRETURN() {
        IRETURN();
    }

    @Override
    public void DRETURN() {
        IRETURN();
    }

    @Override
    public void ARETURN() {
        IRETURN();
    }

    /**
     * No actual return value.
     * <p>
     * If we invoked uninstrumented code, then throw away the parameters passed
     * to that uninstrumented code.
     */
    @Override
    public void CALL_RESULT(String owner, String name, String desc) {
        if (callResultIsPushed())
            // RETURN already did it
            return;

        /**
         * Since control flow is returning from un-instrumented code, we
         * must get rid of the method arguments since the callee did not
         * consume the method arguments.
         */
        env.topFrame().disposeMethInvokeArgs(desc);
    }

    /**
     * Our chance to capture the value returned by a native or un-instrumented
     * method.
     */
    @Override
    public void CALL_RESULT(boolean res, String owner, String name, String desc) {
        CALL_RESULT(owner, name, desc);

        if (callResultIsPushed()) { // RETURN already did
            // it
            return;
        } else {
            /**
             * We are returning from uninstrumented code. This is the only way
             * of storing the uninstrumented return value to the symbolic state.
             */
            int i = res ? 1 : 0;
            IntegerConstant value = ExpressionFactory.buildNewIntegerConstant(i);
            env.topFrame().operandStack.pushBv32(value);
        }
    }

    /**
     * int, short, byte all map to a BitVec32
     * <p>
     * TODO: Will this work for char?
     */
    @Override
    public void CALL_RESULT(int res, String owner, String name, String desc) {
        CALL_RESULT(owner, name, desc);

        if (callResultIsPushed()) {// RETURN already did
            // it
            return;
        } else {
            /**
             * We are returning from uninstrumented code. This is the only way
             * of storing the uninstrumented return value to the symbolic state.
             */
            IntegerConstant value = ExpressionFactory.buildNewIntegerConstant(res);
            env.topFrame().operandStack.pushBv32(value);
        }
    }

    @Override
    public void CALL_RESULT(Object res, String owner, String name, String desc) {
        CALL_RESULT(owner, name, desc);

        if (callResultIsPushed())
            // RETURN already did it
            return;
        else {
            /**
             * We are returning from uninstrumented code. This is the only way
             * of storing the method return value to the symbolic state.
             */
            ReferenceExpression symb_ref = env.heap.getReference(res);
            env.topFrame().operandStack.pushRef(symb_ref);
        }
    }

    @Override
    public void CALL_RESULT(long res, String owner, String name, String desc) {
        CALL_RESULT(owner, name, desc);

        if (callResultIsPushed()) {
            // RETURN already did it
            return;
        } else {
            /**
             * We are returning from uninstrumented code. This is the only way
             * of storing the uninstrumented return value to the symbolic state.
             */
            IntegerConstant value = ExpressionFactory.buildNewIntegerConstant(res);
            env.topFrame().operandStack.pushBv64(value);
        }
    }

    @Override
    public void CALL_RESULT(double res, String owner, String name, String desc) {
        CALL_RESULT(owner, name, desc);

        if (callResultIsPushed()) {
            // RETURN already did it
            return;
        } else {
            /**
             * We are returning from uninstrumented code. This is the only way
             * of storing the uninstrumented return value to the symbolic state.
             */
            RealConstant value = ExpressionFactory.buildNewRealConstant(res);
            env.topFrame().operandStack.pushFp64(value);
        }
    }

    @Override
    public void CALL_RESULT(float res, String owner, String name, String desc) {
        CALL_RESULT(owner, name, desc);

        if (callResultIsPushed()) {// RETURN already did
            // it
            return;
        } else {
            /**
             * We are returning from uninstrumented code. This is the only way
             * of storing the uninstrumented return value to the symbolic state.
             */
            RealConstant value = ExpressionFactory.buildNewRealConstant(res);
            env.topFrame().operandStack.pushFp32(value);
        }

    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, int value) {
        stackParamCount++;
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, boolean value) {
        stackParamCount++;
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, short value) {
        stackParamCount++;
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, byte value) {
        stackParamCount++;
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, char value) {
        stackParamCount++;
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, long value) {
        stackParamCount++;
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, float value) {
        stackParamCount++;
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, double value) {
        stackParamCount++;
    }

    @Override
    public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object conc_ref) {
        stackParamCount++;

        int operand_index = stackParamCount - 1;
        Operand op = getOperand(operand_index);
        ReferenceOperand ref_op = (ReferenceOperand) op;
        ReferenceExpression symb_ref = ref_op.getReference();

        env.heap.initializeReference(conc_ref, symb_ref);
    }

    private Operand getOperand(int index) {
        Operand op;
        Iterator<Operand> it = env.topFrame().operandStack.iterator();
        for (int i = 0; i < index + 1; i++) {
            op = it.next();
            if (i == index) {
                return op;
            }
        }
        return null;
    }

    /**
     * Start executing a static (class) initializer -- <clinit>()
     */
    private void CLINIT_BEGIN(String className) {
        /*
         * <clinit>() method can read textually earlier fields
         */
//		env.ensurePrepared(className);
        Frame frame = new StaticInitializerFrame(className);
        env.pushFrame(frame); // <clinit>() has no parameters
    }

    /**
     * @param function the method we are looking for in the frame stack
     * @return constructor matches with the current frame, after discarding some
     * frames when necessary to match
     */
    private boolean discardFrames(String className, String methName, Member function) {
        if (function == null)
            throw new IllegalArgumentException("function should be non null");

        if (env.topFrame() instanceof FakeBottomFrame)
            return false;

        Frame topFrame = env.topFrame();
        if (topFrame instanceof StaticInitializerFrame) {
            StaticInitializerFrame clinitFrame = (StaticInitializerFrame) topFrame;
            if (methName.equals(conf.INIT) && clinitFrame.getClassName().equals(className)) {
                return true;
            }
        }

        if (function != null && function.equals(topFrame.getMember()))
            return true;

        env.popFrame();
        return discardFrames(className, methName, function);
    }

    private boolean discardFramesClassInitializer(String className, String methName) {
        if (!conf.CLINIT.equals(methName))
            throw new IllegalArgumentException("methName should be <clinit>");

        if (env.topFrame() instanceof FakeBottomFrame)
            return false;

        Frame topFrame = env.topFrame();
        if (topFrame instanceof StaticInitializerFrame) {
            StaticInitializerFrame clinitFrame = (StaticInitializerFrame) topFrame;
            if (methName.equals(conf.CLINIT) && clinitFrame.getClassName().equals(className)) {
                return true;
            }
        }

        env.popFrame();
        return discardFramesClassInitializer(className, methName);
    }

    private void prepareStackIfNeeded(String className, String methName, String methDesc) {

        Method method = null;
        if (env.isEmpty()) {
            Class<?> claz = classLoader.getClassForName(className);

            Method[] declMeths = claz.getDeclaredMethods();
            for (Method declMeth : declMeths) {
                if (!Modifier.isPublic(declMeth.getModifiers()))
                    continue;
                if (declMeth.getName().equals(methName))
                    method = declMeth;
            }

            if (method != null) {
                env.prepareStack(method);
            }
        }

        if (env.isEmpty()) {
            throw new IllegalStateException();
        }

    }

    /**
     * Asm method descriptor --> Method parameters as Java Reflection classes.
     * <p>
     * Does not include the receiver for
     */
    private Class<?>[] getArgumentClasses(String methDesc) {
        Class<?>[] classes;

        Type[] asmTypes = Type.getArgumentTypes(methDesc);
        classes = new Class<?>[asmTypes.length];
        for (int i = 0; i < classes.length; i++)
            classes[i] = classLoader.getClassForType(asmTypes[i]);

        return classes;
    }

    private static Method findMethodFromClass(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, argTypes);
        } catch (NoSuchMethodException ignored) {
        }
        return method;
    }

    /**
     * Resolves (static) method overloading.
     * <p>
     * Ensures that owner class is prepared.
     * <p>
     * FIXME: user code calling java.util.Deque.isEmpty() crashes this method
     *
     * @return method named name, declared by owner or one of its super-classes,
     * which has the parameters encoded in methDesc.
     */
    private Method resolveMethodOverloading(String owner, String name, String methDesc) {
        if (owner.equals("com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl")) {
            int y = 0;
        }
        Method method = null;
        final Deque<Class<?>> interfaces = new LinkedList<>();

        Class<?> claz = env.ensurePrepared(owner);
        /* Resolve method overloading -- need method parameter types */
        Class<?>[] argTypes = getArgumentClasses(methDesc);
        while ((method == null) && (claz != null)) {
            interfaces.addAll(Arrays.asList(claz.getInterfaces()));

            method = findMethodFromClass(claz, name, argTypes);

            if (method == null)
                claz = claz.getSuperclass();

            if (claz == null && !interfaces.isEmpty())
                claz = interfaces.pop();
        }

        if (method == null)
            throw new IllegalArgumentException("Failed to resolve " + owner + "." + name);

        return method;
    }

    private Constructor<?> resolveConstructorOverloading(String owner, String desc) {
        Constructor<?> constructor = null;

        Class<?> claz = env.ensurePrepared(owner);

        /* Resolve overloading -- need parameter types */
        Class<?>[] argTypes = getArgumentClasses(desc);

        try {
            constructor = claz.getDeclaredConstructor(argTypes);
        } catch (NoSuchMethodException nsme) {
            throw new IllegalArgumentException("Failed to resolve constructor of " + owner);
        }

        return constructor;
    }

    /**
     * @return method is instrumented. It is neither native nor declared by an
     * ignored JDK class, etc.
     */
    private boolean isIgnored(Method method) {
        if (Modifier.isNative(method.getModifiers()))
            return false;

        /* virtual method */
        /** NOTE (ilebrero): are there other cases like this? */
        /** TODO (ilebrero): Create a special case for local and anonymous classes goal tracking for DSE. So far,
         * 					 evosuite is skipping tracking those, even though in DSE they are symbolized and tests are
         * 					 created (In fact, they are being dropped by TestSuiteMinizer for not finding goals that
         * 					 they cover). */
        if (method.getDeclaringClass().isAnonymousClass() || method.getDeclaringClass().isLocalClass()) {
            // anonymous or local class
            String name = method.getDeclaringClass().getName();
            int indexOf = name.indexOf("$");
            String fullyQualifiedTopLevelClassName = name.substring(0, indexOf);
            return !conf.isIgnored(fullyQualifiedTopLevelClassName);
        } else {
            String declClass = method.getDeclaringClass().getCanonicalName();
            return !conf.isIgnored(declClass);

        }
    }

    /**
     * Method call
     * <ul>
     * <li>not a constructor <init></li>
     * <li>not a class initializer <clinit></li>
     * </ul>
     *
     * @return static method descriptor
     */
    private Method methodCall(String className, String methName, String methDesc) {
        final Method method = resolveMethodOverloading(className, methName, methDesc);
        /* private method may be native */
        boolean instrumented = isIgnored(method);
        env.topFrame().invokeInstrumentedCode(instrumented);
        env.topFrame().invokeLambdaSyntheticCodeThatInvokesNonInstrCode(false);
        return method;
    }

    private boolean nullReferenceViolation(Object conc_receiver, ReferenceExpression symb_receiver) {
        return conc_receiver == null;
    }

    /**
     * Add dynamic type of receiver to path condition.
     */
    private void chooseReceiverType(String className, Object receiver, String methDesc, Method staticMethod) {

        if (nullReferenceViolation(receiver, null)) {
            throw new IllegalArgumentException("we are post null-deref check");
        }

        /*
         * Only encode the receiver type in a constraint if dynamic dispatach
         * can happen: not(isFinal(static receiver type))
         */
        final Class<?> staticReceiver = env.ensurePrepared(className);
        if (Modifier.isFinal(staticReceiver.getModifiers()))
            return;

        /*
         * Heuristic: Do not encode the receiver type if a method is
         * "final native", e.g., Object.getClass().
         *
         * not( isNative(static method descriptor) && isFinal(static method
         * descriptor))
         */
        final int methodModifiers = staticMethod.getModifiers();
        if (Modifier.isNative(methodModifiers) && Modifier.isFinal(methodModifiers))
            return;

    }

    private Frame popFrameAndDisposeCallerParams() {
        Frame frame = env.popFrame();

        if (!env.isEmpty() && env.topFrame().weInvokedInstrumentedCode())
            env.topFrame().disposeMethInvokeArgs(frame);

        return frame;
    }

    /**
     * Nested class: Container for maximum size of operand stack and maximum
     * number of local variables.
     */
    private final static class MemberInfo {
        @SuppressWarnings("unused")
        final int maxStack, maxLocals;

        MemberInfo(int maxStack, int maxLocals) {
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
        }
    }

    private boolean callResultIsPushed() {
        return env.topFrame().weInvokedInstrumentedCode()
                && !env.topFrame().weInvokedSyntheticLambdaCodeThatInvokesNonInstrCode();
    }
}