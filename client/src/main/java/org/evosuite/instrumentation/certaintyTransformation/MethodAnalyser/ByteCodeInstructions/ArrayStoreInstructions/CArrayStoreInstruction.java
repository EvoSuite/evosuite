package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.CASTORE;

public class CArrayStoreInstruction extends ArrayStoreInstructions {
    public CArrayStoreInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "CASTORE", instructionNumber, StackTypeSet.CHAR, CASTORE);
    }
}
