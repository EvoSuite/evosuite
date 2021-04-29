package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.IntUnaryInstructions;

import org.objectweb.asm.Opcodes;

public class INegInstruction extends IntUnaryInstruction {
    public INegInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                           int instructionNumber) {
        super(INT_UNARY_INSTRUCTION.INEG, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.INEG);
    }
}
