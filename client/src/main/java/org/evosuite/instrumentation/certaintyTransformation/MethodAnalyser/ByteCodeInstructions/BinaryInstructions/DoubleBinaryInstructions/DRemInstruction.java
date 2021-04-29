package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.DoubleBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class DRemInstruction extends DoubleBinaryInstruction {
    public DRemInstruction(String className, String methodName, int lineNUmber,String methodDescriptor, int instructionNumber) {
        super(DOUBLE_BINARY_INSTRUCTION.DREM, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.DREM);
    }
}
