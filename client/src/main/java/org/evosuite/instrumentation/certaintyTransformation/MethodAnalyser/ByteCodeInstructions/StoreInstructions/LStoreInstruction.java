package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.LSTORE;

public class LStoreInstruction extends StoreInstruction {
    public LStoreInstruction(String className, String methodName, int line, String methodDescriptor,int localVariableIndex,
                             int instructionNumber) {
        super(className, methodName, line, methodDescriptor,"LSTORE " + localVariableIndex, localVariableIndex, instructionNumber,
                StackTypeSet.LONG, LSTORE);
    }
}
