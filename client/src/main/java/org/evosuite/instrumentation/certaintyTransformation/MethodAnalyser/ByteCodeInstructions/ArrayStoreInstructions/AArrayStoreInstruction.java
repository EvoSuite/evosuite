package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ArrayStoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.AASTORE;

public class AArrayStoreInstruction extends ArrayStoreInstructions {
    public AArrayStoreInstruction(String className, String methodName, int lineNumber,String methodDescriptor, int instructionNumber) {
        super(className, methodName, lineNumber, methodDescriptor,"AASTORE", instructionNumber, StackTypeSet.AO, AASTORE);
    }
}
