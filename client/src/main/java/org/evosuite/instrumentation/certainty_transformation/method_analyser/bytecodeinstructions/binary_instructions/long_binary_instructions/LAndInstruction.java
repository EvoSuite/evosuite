package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.long_binary_instructions;

import org.objectweb.asm.Opcodes;

public class LAndInstruction extends LongBinaryInstruction {
    public LAndInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                           int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LAND, className, methodName, lineNumber, methodDescriptor,instructionNumber, Opcodes.LAND);
    }
}
