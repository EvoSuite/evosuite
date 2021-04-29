package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.IntBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class IOrInstruction extends IntBinaryInstruction {
    public IOrInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(INT_BINARY_INSTRUCTION.IOR, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.IOR);
    }
}
