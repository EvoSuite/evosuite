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

import org.objectweb.asm.Type;

import java.lang.reflect.Member;

/**
 * Invocation frame.
 *
 * @author csallner@uta.edu (Christoph Csallner)
 */
public abstract class Frame {

    /**
     * The last method we invoked is instrumented. This means we invoked some
     * code that is neither native nor defined by an uninstrumented JDK class.
     */
    private boolean weInvokedInstrumentedCode = true;

    /**
     * The last method we invoked was the code of a jvm-generated
     * magic lambda class.
     */
    private boolean weInvokedSynteticLambdaCodeThatInvokesNonInstrCode = false;

    /**
     * @return the last method invoked is instrumented, because it is neither
     * native nor defined by an uninstrumented JDK class, etc.
     */
    boolean weInvokedInstrumentedCode() {
        return weInvokedInstrumentedCode;
    }

    /**
     * The next call is to an instrumented method. The called method is neither
     * native, nor defined by an uninstrumented JDK class, etc.
     * <p>
     * <p>
     * Usage: This method has to be called just before any
     * method/constructor/clinit call.
     */
    public void invokeInstrumentedCode(boolean b) {
        weInvokedInstrumentedCode = b;
    }

    /**
     * The next call is to a lambda's synthetic method which calls non-instrumented code.
     */
    boolean weInvokedSyntheticLambdaCodeThatInvokesNonInstrCode() {
        return weInvokedSynteticLambdaCodeThatInvokesNonInstrCode;
    }

    public void invokeLambdaSyntheticCodeThatInvokesNonInstrCode(boolean b) {
        weInvokedSynteticLambdaCodeThatInvokesNonInstrCode = b;
    }

    /**
     * The last method invoked by this frame needs a "this" receiver reference,
     * i.e., is an instance method or constructor.
     */
    boolean invokeNeedsThis;

    /**
     * Operand stack
     */
    public final OperandStack operandStack = new OperandStack();

    /**
     * List of local variables
     */
    final LocalsTable localsTable;

    /**
     * Constructor
     */
    protected Frame(int maxLocals) {
        localsTable = new LocalsTable(maxLocals);
    }

    public abstract Member getMember();

    /**
     * Without the "this" receiver instance parameter
     */
    public abstract int getNrFormalParameters();

    public abstract int getNrFormalParametersTotal();

    /**
     * Dispose operands we passed to called method.
     * <p>
     * //@param nrFormalParameters without "this" receiver instance parameter
     *
     * @param nrFormalParameters with "this" receiver instance parameter
     */
    private void disposeOperands(int nrFormalParameters) {
        for (int i = 0; i < nrFormalParameters; i++)
            operandStack.popOperand();
    }

    /**
     * We invoked some other method or constructor. Now we want to discard the
     * parameters we used for this call from our operand stack.
     */
    void disposeMethInvokeArgs(String methDesc) {
        disposeOperands(Type.getArgumentTypes(methDesc).length);
        if (invokeNeedsThis)
            operandStack.popOperand();
    }

    /**
     * Dispose the parameters we needed to invoke frame (of instrumented
     * method).
     */
    void disposeMethInvokeArgs(Frame frame) {
        disposeOperands(frame.getNrFormalParametersTotal());
    }

    @Override
    public String toString() {
        return getMember().getName() + "--" + operandStack; //$NON-NLS-1$
    }
}
