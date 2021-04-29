package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.MixedUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.I2F;

public class I2FInstruction extends AtoB_UnaryInstruction {
    public I2FInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "I2F", instructionNumber, StackTypeSet.TWO_COMPLEMENT, StackTypeSet.FLOAT, I2F);
    }
}
