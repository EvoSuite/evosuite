package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class LSubInstruction extends LongBinaryInstruction {
    public LSubInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LSUB, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.LSUB);
    }
}
