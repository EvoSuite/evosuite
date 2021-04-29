package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.MixedUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.I2B;

public class I2BInstruction extends AtoB_UnaryInstruction {

    public I2BInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "I2B", instructionNumber, StackTypeSet.TWO_COMPLEMENT, StackTypeSet.BYTE, I2B);
    }
}
