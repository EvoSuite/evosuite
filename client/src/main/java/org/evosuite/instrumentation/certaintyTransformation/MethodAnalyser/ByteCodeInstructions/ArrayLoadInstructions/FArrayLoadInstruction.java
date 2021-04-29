package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.FALOAD;

public class FArrayLoadInstruction extends ArrayLoadInstruction {
    public FArrayLoadInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor, "FALOAD", instructionNumber, StackTypeSet.FLOAT, FALOAD);
    }
}
