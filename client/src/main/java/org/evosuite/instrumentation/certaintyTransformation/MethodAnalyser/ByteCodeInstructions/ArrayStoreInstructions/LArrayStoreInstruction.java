package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.LASTORE;

public class LArrayStoreInstruction extends ArrayStoreInstructions {
    public LArrayStoreInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber,methodDescriptor, "LASTORE", instructionNumber, StackTypeSet.LONG, LASTORE);
    }
}
