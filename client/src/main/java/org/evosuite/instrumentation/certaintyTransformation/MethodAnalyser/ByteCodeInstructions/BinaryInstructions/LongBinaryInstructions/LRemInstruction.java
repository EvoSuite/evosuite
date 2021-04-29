package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class LRemInstruction extends LongBinaryInstruction {
    public LRemInstruction(String className, String methodName, int lineNumber,
                           String methodDescriptor, int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LREM, className, methodName, lineNumber, methodDescriptor, instructionNumber, Opcodes.LREM);
    }
}
