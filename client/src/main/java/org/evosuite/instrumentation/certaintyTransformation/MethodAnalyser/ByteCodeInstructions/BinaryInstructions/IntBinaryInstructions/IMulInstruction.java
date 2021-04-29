package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.IntBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class IMulInstruction extends IntBinaryInstruction {
    public IMulInstruction(String className, String methodName, int lineNUmber, String methodDescriptor,int instructionNumber) {
        super(INT_BINARY_INSTRUCTION.IMUL, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.IMUL);
    }
}
