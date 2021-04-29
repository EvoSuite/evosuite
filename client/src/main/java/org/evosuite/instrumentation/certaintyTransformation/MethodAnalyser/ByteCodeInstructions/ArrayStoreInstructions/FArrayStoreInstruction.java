package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions;


import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.FASTORE;

public class FArrayStoreInstruction extends ArrayStoreInstructions {
    public FArrayStoreInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "FASTORE", instructionNumber, StackTypeSet.FLOAT, FASTORE);
    }
}
