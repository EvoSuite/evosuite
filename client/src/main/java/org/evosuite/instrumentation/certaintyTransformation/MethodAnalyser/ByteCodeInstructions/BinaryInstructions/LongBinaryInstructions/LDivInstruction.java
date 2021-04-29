package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.BinaryInstructions.LongBinaryInstructions;

import org.objectweb.asm.Opcodes;

public class LDivInstruction extends LongBinaryInstruction {
    public LDivInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                           int instructionNumber) {
        super(LONG_BINARY_INSTRUCTION.LDIV, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.LDIV);
    }
}
