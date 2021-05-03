package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.int_unary_instructions;

import org.objectweb.asm.Opcodes;

public class INegInstruction extends IntUnaryInstruction {
    public INegInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                           int instructionNumber) {
        super(INT_UNARY_INSTRUCTION.INEG, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.INEG);
    }
}
