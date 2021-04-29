package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class LAndInstruction extends LongBinaryInstruction {
    public LAndInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                           int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LAND, className, methodName, lineNumber, methodDescriptor,instructionNumber, Opcodes.LAND);
    }
}
