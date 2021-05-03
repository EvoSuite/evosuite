package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.unary_instructions.double_unary_instructions;

import org.objectweb.asm.Opcodes;

public class DNegInstruction extends DoubleUnaryInstruction {
    public DNegInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(DOUBLE_UNARY_INSTRUCTION.DNEG, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.DNEG);
    }
}
