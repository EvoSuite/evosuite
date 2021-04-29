package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.IntBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class IAndInstruction extends IntBinaryInstruction {

    public IAndInstruction(String className, String methodName, int lineNUmber, String methodDescriptor,int instructionNumber) {
        super(INT_BINARY_INSTRUCTION.IAND, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.IAND);
    }
}
