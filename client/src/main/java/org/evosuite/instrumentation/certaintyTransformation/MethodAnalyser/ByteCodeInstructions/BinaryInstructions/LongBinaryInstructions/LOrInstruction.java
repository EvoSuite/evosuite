package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class LOrInstruction extends LongBinaryInstruction {
    public LOrInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LOR, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.LOR);
    }
}
