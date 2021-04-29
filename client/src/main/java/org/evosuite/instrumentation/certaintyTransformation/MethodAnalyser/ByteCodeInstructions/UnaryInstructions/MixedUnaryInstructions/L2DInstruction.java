package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.MixedUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.L2D;

public class L2DInstruction extends AtoB_UnaryInstruction {
    public L2DInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"L2D", instructionNumber, StackTypeSet.LONG, StackTypeSet.DOUBLE, L2D);
    }
}
