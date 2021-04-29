package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.IntBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class IRemInstruction extends IntBinaryInstruction {
    public IRemInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(INT_BINARY_INSTRUCTION.IREM, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.IREM);
    }
}
