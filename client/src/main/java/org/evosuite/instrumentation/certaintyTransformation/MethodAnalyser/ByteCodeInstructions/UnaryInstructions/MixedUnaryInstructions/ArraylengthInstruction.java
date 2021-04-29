package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.MixedUnaryInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.UnaryInstructions.AtoB_UnaryInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.ARRAYLENGTH;

public class ArraylengthInstruction extends AtoB_UnaryInstruction {
    public ArraylengthInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {

        super(className, methodName, lineNumber,methodDescriptor, "ARRAYLENGTH", instructionNumber, StackTypeSet.ARRAY,
                StackTypeSet.INT, ARRAYLENGTH);
    }
}
