package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.DoubleBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class DMulInstruction extends DoubleBinaryInstruction {
    public DMulInstruction(String className, String methodName, int lineNUmber
            ,String methodDescriptor,int instructionNumber) {
        super(DOUBLE_BINARY_INSTRUCTION.DMUL, className, methodName, lineNUmber,methodDescriptor, instructionNumber, Opcodes.DMUL);
    }
}
