package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StoreInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import static org.objectweb.asm.Opcodes.FSTORE;

public class FStoreInstruction extends StoreInstruction {
    public FStoreInstruction(String className, String methodName, int line,String methodDescriptor,
                             int localVariableIndex,
                             int instructionNumber) {
        super(className, methodName, line, methodDescriptor,"FSTORE " + localVariableIndex, localVariableIndex, instructionNumber,
                StackTypeSet.FLOAT, FSTORE);
    }
}
