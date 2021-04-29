package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class LMulInstruction extends LongBinaryInstruction {
    public LMulInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LMUL, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.LMUL);
    }
}
