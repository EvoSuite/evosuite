package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class LAddInstruction extends LongBinaryInstruction {
    public LAddInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LADD, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.LADD);
    }
}
