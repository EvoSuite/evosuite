package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.MixedUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.L2I;

public class L2IInstruction extends AtoB_UnaryInstruction {
    public L2IInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "L2I", instructionNumber, StackTypeSet.LONG, StackTypeSet.INT, L2I);
    }
}
