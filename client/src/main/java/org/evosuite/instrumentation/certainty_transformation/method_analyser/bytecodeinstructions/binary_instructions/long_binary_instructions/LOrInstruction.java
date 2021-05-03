package org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.binary_instructions.long_binary_instructions;

import org.objectweb.asm.Opcodes;

public class LOrInstruction extends LongBinaryInstruction {
    public LOrInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LOR, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.LOR);
    }
}
