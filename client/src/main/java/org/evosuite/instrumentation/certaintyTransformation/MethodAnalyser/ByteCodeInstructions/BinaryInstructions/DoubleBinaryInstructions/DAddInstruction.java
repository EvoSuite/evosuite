package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.DoubleBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class DAddInstruction extends DoubleBinaryInstruction {
    public DAddInstruction(String className, String methodName, int lineNUmber, String methodDescriptor,
                           int instructionNumber) {
        super(DOUBLE_BINARY_INSTRUCTION.DADD, className, methodName, lineNUmber, methodDescriptor,instructionNumber, Opcodes.DADD);
    }
}
