package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.BASTORE;

public class BArrayStoreInstruction extends ArrayStoreInstructions {
    public BArrayStoreInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "BASTORE", instructionNumber, StackTypeSet.TWO_COMPLEMENT, BASTORE);
    }
}
