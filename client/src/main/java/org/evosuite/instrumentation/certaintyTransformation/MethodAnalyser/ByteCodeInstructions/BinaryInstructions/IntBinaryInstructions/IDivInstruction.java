package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.IntBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class IDivInstruction extends IntBinaryInstruction {
    public IDivInstruction(String className, String methodName, int lineNUmber, String methodDescriptor,int instructionNumber) {
        super(INT_BINARY_INSTRUCTION.IDIV, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.IDIV);
    }
}
