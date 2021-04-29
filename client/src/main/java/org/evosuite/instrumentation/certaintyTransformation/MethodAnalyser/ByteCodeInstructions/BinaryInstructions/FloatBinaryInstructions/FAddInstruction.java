package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.FloatBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class FAddInstruction extends FloatBinaryInstruction {
    public FAddInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(FLOAT_BINARY_INSTRUCTION.FADD, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.FADD);
    }
}
