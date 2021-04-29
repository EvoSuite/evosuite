package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.LongUnaryInstructions;

import org.objectweb.asm.Opcodes;

public class LNegInstruction extends LongUnaryInstruction {
    public LNegInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(LONG_UNARY_INSTRUCTION.LNEG, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.LNEG);
    }
}
