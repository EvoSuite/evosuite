package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayLoadInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.BALOAD;

public class BArrayLoadInstruction extends ArrayLoadInstruction {
    public BArrayLoadInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "BALOAD", instructionNumber, StackTypeSet.TWO_COMPLEMENT, BALOAD);
    }
}
