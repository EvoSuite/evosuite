package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.SASTORE;

public class SArrayStoreInstruction extends ArrayStoreInstructions {
    public SArrayStoreInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "SASTORE", instructionNumber, StackTypeSet.SHORT, SASTORE);
    }
}
