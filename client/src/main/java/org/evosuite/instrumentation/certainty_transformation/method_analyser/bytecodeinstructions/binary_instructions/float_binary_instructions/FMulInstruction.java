package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.float_binary_instructions;

import org.objectweb.asm.Opcodes;

public class FMulInstruction extends FloatBinaryInstruction {
    public FMulInstruction(String className, String methodName, int lineNUmber,String methodDescriptor,
                           int instructionNumber) {
        super(FLOAT_BINARY_INSTRUCTION.FMUL, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.FMUL);
    }
}
