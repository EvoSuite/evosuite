package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.FloatUnaryInstructions;

import org.objectweb.asm.Opcodes;

public class FNegInstruction extends FloatUnaryInstruction {
    public FNegInstruction(String className, String methodName, int lineNumber,String methodDescriptor,
                           int instructionNumber) {
        super(FLOAT_UNARY_INSTRUCTION.FNEG, className, methodName, lineNumber,methodDescriptor, instructionNumber, Opcodes.FNEG);
    }
}
